from __future__ import annotations

import concurrent.futures
import os
import sys
from pathlib import Path
from typing import Iterable

PROJECT_ROOT = Path(__file__).resolve().parents[1]
TARGET_SUFFIXES = (".java", ".json", ".toml", ".cfg", ".properties", ".mcmeta")
SKIP_DIR_NAMES = {"build", "run", "run-data", ".gradle", ".git"}
MIN_PARALLEL_FILE_COUNT = 32
MAX_WORKERS = 8
DRY_RUN = "--dry-run" in sys.argv[1:]


def iter_target_paths(root: Path) -> Iterable[Path]:
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix not in TARGET_SUFFIXES:
            continue
        skipped = any(part in SKIP_DIR_NAMES for part in path.relative_to(root).parts[:-1])
        if not skipped:
            yield path


def read_text(path: Path) -> str:
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return handle.read()


def write_text(path: Path, text: str) -> None:
    with path.open("w", encoding="utf-8", newline="") as handle:
        handle.write(text)


def strip_trailing_newline(path: Path) -> tuple[str, Path]:
    try:
        text = read_text(path)
        stripped = text.rstrip("\r\n")
        if stripped == text:
            return "unchanged", path
        if not DRY_RUN:
            write_text(path, stripped)
    except OSError:
        return "error", path
    return "changed", path


def print_relative_paths(title: str, paths: list[Path]) -> None:
    print(f"{title}: {len(paths)}")
    for path in paths:
        print(path.relative_to(PROJECT_ROOT))


def get_worker_count(file_count: int) -> int:
    if file_count < MIN_PARALLEL_FILE_COUNT:
        return 1
    cpu_count = os.cpu_count() or 1
    return min(MAX_WORKERS, file_count, max(2, cpu_count))


def main() -> int:
    if not PROJECT_ROOT.exists():
        print(f"Project root not found: {PROJECT_ROOT}")
        return 1

    target_paths = sorted(iter_target_paths(PROJECT_ROOT))
    scanned = len(target_paths)
    changed: list[Path] = []
    skipped_errors: list[Path] = []
    worker_count = get_worker_count(scanned)

    if worker_count == 1:
        results = (strip_trailing_newline(path) for path in target_paths)
    else:
        with concurrent.futures.ThreadPoolExecutor(max_workers=worker_count) as executor:
            results = executor.map(strip_trailing_newline, target_paths)

    for status, path in results:
        if status == "changed":
            changed.append(path)
        elif status == "error":
            skipped_errors.append(path)

    print(f"Scanned: {scanned}")
    print(f"Workers used: {worker_count}")
    print(f"Dry run: {DRY_RUN}")
    print_relative_paths("Trimmed", changed)
    print_relative_paths("Skipped read errors", skipped_errors)
    return 0


if __name__ == "__main__":
    exit_code = main()
    if len(sys.argv) == 1 and sys.stdin is not None and sys.stdin.isatty():
        try:
            input("Press Enter to exit...")
        except EOFError:
            pass
    raise SystemExit(exit_code)