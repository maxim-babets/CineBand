from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    db_host: str = "localhost"
    db_user: str = "root"
    db_password: str = ""
    db_name: str = "system_rekomendacji"


settings = Settings()
