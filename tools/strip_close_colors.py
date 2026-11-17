from __future__ import annotations

import concurrent.futures
import importlib
import math
import os
import subprocess
import sys
from collections import defaultdict
from pathlib import Path
from typing import Iterable, Literal

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ITEM_TEXTURE_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "babel" / "textures" / "item"
DRY_RUN = "--dry-run" in sys.argv
THRESHOLD = 0.45
MIN_PARALLEL_FILE_COUNT = 32
MAX_WORKERS = 8


def ensure_dependencies() -> None:
    try:
        importlib.import_module("PIL")
    except ModuleNotFoundError:
        print("Missing dependencies: Pillow")
        print("Installing via pip: Pillow")
        subprocess.check_call([
            sys.executable, "-m", "pip", "install", "Pillow",
            "-i", "https://pypi.tuna.tsinghua.edu.cn/simple"
        ])
        print("Installation complete.")


def iter_png_paths(texture_dir: Path) -> Iterable[Path]:
    yield from sorted(texture_dir.rglob("*.png"))


def srgb_to_linear(channel: int) -> float:
    value = channel / 255.0
    return value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4


def rgb_to_oklab(color: tuple[int, int, int]) -> tuple[float, float, float]:
    red, green, blue = (srgb_to_linear(channel) for channel in color)
    long_ = 0.4122214708 * red + 0.5363325363 * green + 0.0514459929 * blue
    medium = 0.2119034982 * red + 0.6806995451 * green + 0.1073969566 * blue
    short = 0.0883024619 * red + 0.2817188376 * green + 0.6299787005 * blue
    long_cbrt = long_ ** (1.0 / 3.0)
    medium_cbrt = medium ** (1.0 / 3.0)
    short_cbrt = short ** (1.0 / 3.0)
    return (
        0.2104542553 * long_cbrt + 0.7936177850 * medium_cbrt - 0.0040720468 * short_cbrt,
        1.9779984951 * long_cbrt - 2.4285922050 * medium_cbrt + 0.4505937099 * short_cbrt,
        0.0259040371 * long_cbrt + 0.7827717662 * medium_cbrt - 0.8086757660 * short_cbrt,
    )


def collect_colors(path: Path) -> dict[tuple[int, int, int], int]:
    from PIL import Image

    with Image.open(path) as image:
        rgba = image.convert("RGBA")
        raw = rgba.tobytes()
        counts: dict[tuple[int, int, int], int] = {}
        for index in range(0, len(raw), 4):
            alpha = raw[index + 3]
            if alpha == 0:
                continue
            color = (raw[index], raw[index + 1], raw[index + 2])
            counts[color] = counts.get(color, 0) + 1
    return counts


def build_replacement_map(
    counts: dict[tuple[int, int, int], int]
) -> tuple[dict[tuple[int, int, int], tuple[int, int, int]], int]:
    colors = list(counts)
    labs = {color: rgb_to_oklab(color) for color in colors}
    parent = {color: color for color in colors}

    def find(color: tuple[int, int, int]) -> tuple[int, int, int]:
        root = color
        while parent[root] != root:
            root = parent[root]
        while parent[color] != color:
            parent[color], color = root, parent[color]
        return root

    def union(left: tuple[int, int, int], right: tuple[int, int, int]) -> None:
        left_root, right_root = find(left), find(right)
        if left_root != right_root:
            parent[right_root] = left_root

    for index in range(len(colors)):
        for other in range(index + 1, len(colors)):
            if math.dist(labs[colors[index]], labs[colors[other]]) * 100.0 < THRESHOLD:
                union(colors[index], colors[other])

    groups: dict[tuple[int, int, int], list[tuple[int, int, int]]] = defaultdict(list)
    for color in colors:
        groups[find(color)].append(color)

    mapping: dict[tuple[int, int, int], tuple[int, int, int]] = {}
    skipped = 0
    for group in groups.values():
        if len(group) > 1:
            # 以像素数最多的色号为代表，保证主体色块不会被噪点色覆盖
            representative = max(group, key=lambda color: counts[color])
            for color in group:
                if color == representative:
                    continue
                if math.dist(labs[color], labs[representative]) * 100.0 < THRESHOLD:
                    mapping[color] = representative
                else:
                    # 链式相邻导致与代表色距离超标时不动，避免累积误差超出无损范围
                    skipped += 1
    return mapping, skipped


def strip_png(
    path: Path, dry_run: bool
) -> tuple[Literal["stripped", "unchanged", "error"], Path, int, int, int]:
    from PIL import Image

    try:
        counts = collect_colors(path)
        mapping, _ = build_replacement_map(counts)
        if not mapping:
            return "unchanged", path, 0, 0, 0

        if dry_run:
            remapped = sum(counts[color] for color in mapping)
            return "stripped", path, len(mapping), len(counts), remapped

        with Image.open(path) as image:
            rgba = image.convert("RGBA")
            size = rgba.size
            raw = bytearray(rgba.tobytes())

        remapped = 0
        for index in range(0, len(raw), 4):
            if raw[index + 3] == 0:
                continue
            replacement = mapping.get((raw[index], raw[index + 1], raw[index + 2]))
            if replacement is not None:
                raw[index], raw[index + 1], raw[index + 2] = replacement
                remapped += 1

        Image.frombytes("RGBA", size, bytes(raw)).save(path, format="PNG")
        return "stripped", path, len(mapping), len(counts), remapped
    except (OSError, ValueError):
        return "error", path, 0, 0, 0


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
    ensure_dependencies()

    if not ITEM_TEXTURE_DIR.exists():
        print(f"Texture directory not found: {ITEM_TEXTURE_DIR}")
        return 1

    png_paths = list(iter_png_paths(ITEM_TEXTURE_DIR))
    scanned = len(png_paths)
    stripped: list[Path] = []
    skipped_errors: list[Path] = []
    merged_colors = 0
    remapped_pixels = 0
    worker_count = get_worker_count(scanned)

    if worker_count == 1:
        results = (strip_png(png_path, DRY_RUN) for png_path in png_paths)
        for status, path, color_count, total_colors, remapped in results:
            if status == "stripped":
                stripped.append(path)
                merged_colors += color_count
                remapped_pixels += remapped
                print(f"Merged: {path.relative_to(PROJECT_ROOT)} ({total_colors} colors, merged {color_count}, {remapped}px)")
            elif status == "error":
                skipped_errors.append(path)
    else:
        # 避开 GIL，利用多核进行 CPU 密集的色差计算
        with concurrent.futures.ProcessPoolExecutor(max_workers=worker_count) as executor:
            for status, path, color_count, total_colors, remapped in executor.map(
                strip_png, png_paths, [DRY_RUN] * len(png_paths)
            ):
                if status == "stripped":
                    stripped.append(path)
                    merged_colors += color_count
                    remapped_pixels += remapped
                    print(f"Merged: {path.relative_to(PROJECT_ROOT)} ({total_colors} colors, merged {color_count}, {remapped}px)")
                elif status == "error":
                    skipped_errors.append(path)

    print(f"Scanned: {scanned}")
    print(f"Workers used: {worker_count}")
    print(f"Dry run: {DRY_RUN}")
    print(f"Threshold: {THRESHOLD:.2f} (OKLab deltaE x100)")
    print(f"Processed: {len(stripped)}")
    print(f"Merged colors: {merged_colors}")
    print(f"Remapped pixels: {remapped_pixels}")
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