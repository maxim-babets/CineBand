import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { fetchMovies, fetchMyRecommendations, fetchSurpriseDuo, postRecommendationFeedback } from '../api'
import type { Film, Method, MovieDetail, RecommendResponse } from '../types'

export function Home() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const [films, setFilms] = useState<Film[]>([])
  const [filmsError, setFilmsError] = useState<string | null>(null)

  const [method, setMethod] = useState<Method>('hybrid')
  const [limit, setLimit] = useState(8)
  const kNeighbors = 5

  const [recResult, setRecResult] = useState<RecommendResponse | null>(null)
  const [recError, setRecError] = useState<string | null>(null)
  const [recLoading, setRecLoading] = useState(false)

  const [duo, setDuo] = useState<MovieDetail[] | null>(null)
  const [surpriseLoading, setSurpriseLoading] = useState(false)
  const [surpriseError, setSurpriseError] = useState<string | null>(null)
  const [duoFbBusy, setDuoFbBusy] = useState<number | null>(null)
  const [duoFbMsg, setDuoFbMsg] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchMovies()
      .then((data) => {
        if (!cancelled) {
          setFilms(data)
          setFilmsError(null)
        }
      })
      .catch((e: Error) => {
        if (!cancelled) setFilmsError(e.message)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const loadRecommendations = useCallback(() => {
    if (!token) return
    setRecLoading(true)
    setRecError(null)
    setRecResult(null)
    fetchMyRecommendations(token, method, limit, kNeighbors)
      .then(setRecResult)
      .catch((e: Error) => setRecError(e.message))
      .finally(() => setRecLoading(false))
  }, [token, method, limit, kNeighbors])

  const loadSurpriseDuo = useCallback(() => {
    setSurpriseLoading(true)
    setSurpriseError(null)
    setDuo(null)
    fetchSurpriseDuo()
      .then((pair) => {
        const two = Array.isArray(pair) ? pair.slice(0, 2) : []
        if (two.length >= 2) {
          setDuo(two)
          setDuoFbMsg(null)
        } else {
          setSurpriseError('Need two films in the catalog.')
        }
      })
      .catch((e: Error) => setSurpriseError(e.message))
      .finally(() => setSurpriseLoading(false))
  }, [])

  const pickDuo = (m: MovieDetail) => {
    navigate(`/movie/${m.id}?ref=surprise`)
  }

  const sendDuoFeedback = async (m: MovieDetail, sentiment: 'LIKE' | 'DISLIKE') => {
    if (!token) return
    setDuoFbBusy(m.id)
    setDuoFbMsg(null)
    try {
      await postRecommendationFeedback(token, m.id, sentiment, 'SURPRISE')
      setDuoFbMsg(
        sentiment === 'LIKE'
          ? `Saved: you liked “${m.title}”. Tap the card for details and where to watch.`
          : `Saved: “${m.title}” won’t be pushed as hard.`
      )
    } catch (e) {
      setDuoFbMsg(e instanceof Error ? e.message : 'Could not save feedback')
    } finally {
      setDuoFbBusy(null)
    }
  }

  return (
    <div className="feed">
      <article className="card card--hero">
        <p className="eyebrow">Can’t decide?</p>
        <h2 className="card-title">Surprise duel</h2>
        {!duo && (
          <button type="button" className="btn btn--gold" onClick={loadSurpriseDuo} disabled={surpriseLoading}>
            {surpriseLoading ? 'Drawing…' : 'Get two picks'}
          </button>
        )}
        {surpriseError && <p className="error">{surpriseError}</p>}
        {duo && (
          <>
            <div className="duo-grid">
              {duo.map((m) => (
                <div key={m.id} className="duo-card duo-card--stack">
                  <button type="button" className="duo-card-hit" onClick={() => pickDuo(m)}>
                    <div className="duo-banner-wrap">
                      {m.banner_url ? (
                        <img src={m.banner_url} alt="" className="duo-banner" loading="lazy" />
                      ) : (
                        <div className="duo-banner duo-banner--ph" />
                      )}
                    </div>
                    <span className="duo-title">{m.title}</span>
                    <span className="duo-meta">
                      {m.releaseYear ?? '—'} · {m.genre ?? '—'}
                    </span>
                    <span className="duo-open-hint muted small">Tap for details and where to watch</span>
                  </button>
                  {token ? (
                    <div className="duo-feedback">
                      <button
                        type="button"
                        className="btn btn--gold btn--duo-fb"
                        disabled={duoFbBusy === m.id}
                        onClick={() => void sendDuoFeedback(m, 'LIKE')}
                      >
                        {duoFbBusy === m.id ? '…' : 'Like'}
                      </button>
                      <button
                        type="button"
                        className="btn btn--muted btn--duo-fb"
                        disabled={duoFbBusy === m.id}
                        onClick={() => void sendDuoFeedback(m, 'DISLIKE')}
                      >
                        Dislike
                      </button>
                    </div>
                  ) : (
                    <p className="duo-signin-hint muted small">
                      <Link to="/login">Sign in</Link> to like or dislike (feeds the recommender).
                    </p>
                  )}
                </div>
              ))}
            </div>
            {duoFbMsg && <p className="duo-fb-msg small">{duoFbMsg}</p>}
          </>
        )}
        {duo && (
          <button type="button" className="btn btn--ghost" onClick={loadSurpriseDuo}>
            Shuffle pair
          </button>
        )}
      </article>

      <article className="card">
        <div className="card-head">
          <h2 className="card-title card-title--inline">For you</h2>
          <span className="hint">Personalized ML — members only</span>
        </div>
        {!token && (
          <p className="muted">
            <Link to="/login">Sign in</Link> or <Link to="/register">create an account</Link> to unlock picks tuned
            to your taste.
          </p>
        )}
        {token && user && (
          <>
            <p className="signed-meta">
              Signed in as <strong>@{user.nick || user.id}</strong>
            </p>
            <div className="controls">
              <label>
                Blend
                <select value={method} onChange={(e) => setMethod(e.target.value as Method)}>
                  <option value="hybrid">Hybrid</option>
                  <option value="knn">KNN</option>
                  <option value="mf">Matrix factorization</option>
                </select>
              </label>
              <label>
                Count
                <input
                  type="number"
                  min={1}
                  max={20}
                  value={limit}
                  onChange={(e) => setLimit(Number(e.target.value) || 8)}
                />
              </label>
            </div>
            <button type="button" className="btn btn--outline" onClick={loadRecommendations} disabled={recLoading}>
              {recLoading ? 'Loading…' : 'Refresh picks'}
            </button>
            {recError && <p className="error">{recError}</p>}
            {recResult && (
              <ul className="timeline">
                {recResult.recommendations.map((r) => (
                  <li key={r.movieId} className="timeline-item">
                    <Link to={`/movie/${r.movieId}?ref=ml`} className="timeline-link">
                      <span className="timeline-title">{r.title}</span>
                      <span className="timeline-meta">{r.score.toFixed(3)} match</span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
            {!recResult && !recError && !recLoading && (
              <p className="muted small">
                Tap refresh to load suggestions. New accounts may be empty until there are ratings or feedback.
              </p>
            )}
          </>
        )}
      </article>

      <aside className="side">
        <h3 className="side-title">Browse</h3>
        {filmsError && <p className="error small">{filmsError}</p>}
        <ul className="browse">
          {films.map((f) => (
            <li key={f.id}>
              <Link to={`/movie/${f.id}`} className="browse-link browse-link--row">
                {f.banner_url ? (
                  <img src={f.banner_url} alt="" className="browse-thumb" loading="lazy" />
                ) : (
                  <div className="browse-thumb browse-thumb--ph" />
                )}
                <span className="browse-text">
                  <span className="browse-title">{f.title}</span>
                  <span className="browse-meta">
                    {f.releaseYear ?? '—'} · {f.genre ?? '—'}
                  </span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </aside>
    </div>
  )
}
