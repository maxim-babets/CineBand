"""Smoke tests — run: pytest from recommendation-ml/"""


def test_fastapi_app_metadata():
    from app.main import app

    assert app.title.startswith("CineBand")


def test_recommender_logger_does_not_crash_on_message():
    import logging

    from app import recommender

    logging.basicConfig(level=logging.INFO)
    # Logger is configured; load_ratings would need DB — we only assert module imports
    assert recommender.logger.name == "app.recommender"
