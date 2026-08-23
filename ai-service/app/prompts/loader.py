"""Prompt loader — loads versioned prompts from markdown files with frontmatter."""
import re
from dataclasses import dataclass
from pathlib import Path
from string import Template

PROMPTS_DIR = Path(__file__).parent


@dataclass
class Prompt:
    version: str
    model: str
    temperature: float
    response_format: str | None
    description: str
    template: Template

    def format(self, **kwargs) -> str:
        return self.template.safe_substitute(kwargs)


_prompt_cache: dict[str, Prompt] = {}


def _parse_prompt_file(path: Path) -> Prompt:
    content = path.read_text(encoding="utf-8")

    # Parse frontmatter (--- ... ---)
    frontmatter_match = re.match(r"^---\n(.*?)\n---", content, re.DOTALL)
    if not frontmatter_match:
        raise ValueError(f"Invalid prompt file {path}: missing frontmatter")

    frontmatter = frontmatter_match.group(1)
    template_content = content[frontmatter_match.end():].lstrip()

    # Parse frontmatter key-value pairs
    metadata = {}
    for line in frontmatter.split("\n"):
        if ":" in line:
            key, value = line.split(":", 1)
            metadata[key.strip()] = value.strip().strip('"')

    return Prompt(
        version=metadata.get("version", "1.0"),
        model=metadata.get("model", ""),
        temperature=float(metadata.get("temperature", "0.7")),
        response_format=metadata.get("response_format") or None,
        description=metadata.get("description", ""),
        template=Template(template_content),
    )


def load_prompt(name: str) -> Prompt:
    """Load a prompt by filename (without extension)."""
    if name not in _prompt_cache:
        path = PROMPTS_DIR / f"{name}.md"
        if not path.exists():
            raise FileNotFoundError(f"Prompt not found: {path}")
        _prompt_cache[name] = _parse_prompt_file(path)
    return _prompt_cache[name]


def get_prompt_version(name: str) -> str:
    return load_prompt(name).version


def get_prompt_model(name: str) -> str:
    return load_prompt(name).model


def get_prompt_temperature(name: str) -> float:
    return load_prompt(name).temperature


def get_prompt_response_format(name: str) -> str | None:
    return load_prompt(name).response_format