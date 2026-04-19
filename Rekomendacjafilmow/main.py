"""
Legacy single-process demo (MySQL + pandas + KNN only).

The hybrid stack lives under:
  ../recommendation-api   — Spring Boot REST (MySQL + proxy to ML)
  ../recommendation-ml    — FastAPI: KNN, NMF matrix factorization, hybrid

Apply schema/data with Movie.txt, start the ML service, then the API.
"""

def main() -> None:
    print(
        "This script is retired. Use:\n"
        "  1) recommendation-ml:  python -m uvicorn app.main:app --reload --port 8000\n"
        "  2) recommendation-api: mvn spring-boot:run\n"
        "  3) GET http://localhost:8080/api/recommendations/1?method=hybrid"
    )


if __name__ == "__main__":
    main()
