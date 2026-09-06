from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Configuration from environment variables. Never hardcode values here."""

    model_config = SettingsConfigDict(env_file=".env", env_prefix="ML_", extra="ignore")

    service_name: str = "cloudforge-ml-service"
    version: str = "0.1.0"
    host: str = "127.0.0.1"
    port: int = 8000
    log_level: str = "info"


settings = Settings()
