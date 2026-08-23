from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    database_url: str = "postgresql+asyncpg://questhub:questhub@localhost:5432/questhub"
    monolith_base_url: str = "http://localhost:8080"

    # OpenRouter
    openrouter_api_key: str = ""
    openrouter_base_url: str = "https://openrouter.ai/api/v1"
    ai_model: str = "meta-llama/llama-3.1-8b-instruct:free"

    # Elasticsearch
    elasticsearch_url: str = "http://localhost:9200"
    elasticsearch_index: str = "quests"

    # Rate limits
    grade_limit_per_day: int = 20
    coach_session_limit_per_day: int = 5
    coach_message_limit_per_day: int = 60
    recommend_limit_per_hour: int = 10
    generate_limit_per_day: int = 3


settings = Settings()
