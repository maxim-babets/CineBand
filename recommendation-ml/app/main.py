import mysql.connector
from typing import Literal

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from app.recommender import build_recommendations

app = FastAPI(title="CineBand recommendation ML", version="1.0.0")


class RecommendBody(BaseModel):
    user_id: int = Field(..., ge=1)
    method: Literal["knn", "mf", "hybrid"] = "hybrid"
    limit: int = Field(10, ge=1, le=100)
    k_neighbors: int = Field(5, ge=1, le=50)


class RecommendResponse(BaseModel):
    user_id: int
    method: str
    recommendations: list[dict]


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/recommend", response_model=RecommendResponse)
def recommend(body: RecommendBody):
    try:
        items, _ = build_recommendations(
            body.user_id,
            body.method,
            body.limit,
            body.k_neighbors,
        )
    except mysql.connector.Error as e:
        raise HTTPException(status_code=503, detail=f"Database error: {e}") from e
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    return RecommendResponse(
        user_id=body.user_id,
        method=body.method,
        recommendations=items,
    )
