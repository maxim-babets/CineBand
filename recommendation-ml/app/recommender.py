from __future__ import annotations

import logging
import mysql.connector
import numpy as np

logger = logging.getLogger(__name__)
import pandas as pd
from sklearn.decomposition import NMF
from sklearn.neighbors import NearestNeighbors

from app.settings import settings


def _connection():
    return mysql.connector.connect(
        host=settings.db_host,
        user=settings.db_user,
        password=settings.db_password,
        database=settings.db_name,
    )


def load_ratings_and_films() -> tuple[pd.DataFrame, pd.DataFrame]:
    conn = _connection()
    try:
        cur = conn.cursor()
        cur.execute("SELECT id_filmu, tytul, gatunek, rok_wydania FROM Filmy")
        film_rows = cur.fetchall()
        cur.execute("SELECT id_uzytkownika, id_filmu, ocena FROM Oceny")
        rating_rows = cur.fetchall()
    finally:
        conn.close()

    films = pd.DataFrame(
        film_rows, columns=["movie_id", "title", "genre", "release_year"]
    )
    ratings = pd.DataFrame(
        rating_rows, columns=["user_id", "movie_id", "rating"]
    )
    logger.info(
        "Loaded ML inputs: %d films, %d ratings (includes Oceny updated by CineBand learning)",
        len(films),
        len(ratings),
    )
    return films, ratings


def _build_user_movie_matrix(ratings: pd.DataFrame) -> pd.DataFrame:
    return ratings.pivot(index="user_id", columns="movie_id", values="rating")


def recommend_knn(
    user_id: int,
    R: pd.DataFrame,
    k_neighbors: int,
    limit: int,
) -> dict[int, float]:
    if user_id not in R.index:
        return {}
    X = R.fillna(0.0).values
    uid_to_row = {u: i for i, u in enumerate(R.index)}
    u_row = uid_to_row[user_id]
    n_neighbors = min(k_neighbors + 1, len(R.index))
    knn = NearestNeighbors(metric="cosine", algorithm="brute")
    knn.fit(X)
    dists, idxs = knn.kneighbors([X[u_row]], n_neighbors=n_neighbors)
    pairs: list[tuple[int, float]] = []
    for i in range(len(idxs[0])):
        ni = int(idxs[0][i])
        if ni == u_row:
            continue
        pairs.append((ni, float(dists[0][i])))
        if len(pairs) >= k_neighbors:
            break

    scores: dict[int, float] = {}
    for movie_id in R.columns:
        if pd.notna(R.loc[user_id, movie_id]):
            continue
        num, den = 0.0, 0.0
        for n_row, dist in pairs:
            sim = max(0.0, 1.0 - dist)
            r = R.iloc[n_row][movie_id]
            if pd.notna(r) and sim > 0:
                num += sim * float(r)
                den += sim
        if den > 0:
            scores[int(movie_id)] = num / den
    return dict(sorted(scores.items(), key=lambda x: -x[1])[:limit])


def recommend_mf(
    user_id: int,
    R: pd.DataFrame,
    ratings: pd.DataFrame,
    limit: int,
) -> dict[int, float]:
    if user_id not in R.index:
        return {}
    global_mean = float(ratings["rating"].mean())
    R_imp = R.fillna(global_mean).values
    n_comp = min(10, max(1, min(R_imp.shape) - 1))
    nmf = NMF(
        n_components=n_comp,
        init="nndsvd",
        random_state=42,
        max_iter=1000,
    )
    W = nmf.fit_transform(R_imp)
    H = nmf.components_
    pred = W @ H

    uid_to_row = {u: i for i, u in enumerate(R.index)}
    u_row = uid_to_row[user_id]
    scores: dict[int, float] = {}
    for j, movie_id in enumerate(R.columns):
        if pd.notna(R.iloc[u_row, j]):
            continue
        scores[int(movie_id)] = float(pred[u_row, j])
    return dict(sorted(scores.items(), key=lambda x: -x[1])[:limit])


def _min_max_norm(values: dict[int, float]) -> dict[int, float]:
    if not values:
        return {}
    xs = list(values.values())
    lo, hi = min(xs), max(xs)
    if hi - lo < 1e-9:
        return {k: 1.0 for k in values}
    return {k: (v - lo) / (hi - lo) for k, v in values.items()}


def recommend_hybrid(
    user_id: int,
    R: pd.DataFrame,
    ratings: pd.DataFrame,
    k_neighbors: int,
    limit: int,
) -> dict[int, float]:
    knn_all = recommend_knn(user_id, R, k_neighbors, limit=len(R.columns))
    mf_all = recommend_mf(user_id, R, ratings, limit=len(R.columns))
    knn_n = _min_max_norm(knn_all)
    mf_n = _min_max_norm(mf_all)
    keys = set(knn_n) | set(mf_n)
    blended: dict[int, float] = {}
    for k in keys:
        a = knn_n.get(k)
        b = mf_n.get(k)
        if a is not None and b is not None:
            blended[k] = 0.5 * (a + b)
        elif a is not None:
            blended[k] = a
        else:
            blended[k] = b
    return dict(sorted(blended.items(), key=lambda x: -x[1])[:limit])


def build_recommendations(
    user_id: int,
    method: str,
    limit: int,
    k_neighbors: int,
) -> tuple[list[dict], pd.DataFrame]:
    films, ratings = load_ratings_and_films()
    R = _build_user_movie_matrix(ratings)

    if method == "knn":
        raw = recommend_knn(user_id, R, k_neighbors, limit)
    elif method == "mf":
        raw = recommend_mf(user_id, R, ratings, limit)
    elif method == "hybrid":
        raw = recommend_hybrid(user_id, R, ratings, k_neighbors, limit)
    else:
        raise ValueError(f"Unknown method: {method}")

    title_by_id = films.set_index("movie_id")["title"].to_dict()
    out: list[dict] = []
    for mid, score in raw.items():
        out.append(
            {
                "movieId": mid,
                "title": title_by_id.get(mid, ""),
                "score": round(score, 4),
            }
        )
    return out, films
