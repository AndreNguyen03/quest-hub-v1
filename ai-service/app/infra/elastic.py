"""Elasticsearch client for quest full-text search."""
from elasticsearch import AsyncElasticsearch

from app.config import settings

es = AsyncElasticsearch(settings.elasticsearch_url)
QUEST_INDEX = settings.elasticsearch_index
