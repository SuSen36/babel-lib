from __future__ import annotations

import concurrent.futures
import importlib
import os
import subprocess
import sys
from pathlib import Path
from typing import Iterable, Literal

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ASSETS_ROOT = PROJECT_ROOT / "src" / "main" / "resources" / "assets"
MIN_PARALLEL_FILE_COUNT = 32
MAX_WORKERS = 8


def ensure_dependencies():
    required = []
    try:
        importlib.import_module("PIL")
    except ModuleNotFoundError:
        required.append("Pillow")

    try:
        importlib.import_module("oxipng")
    except ModuleNotFoundError:
        # pyoxipng 在 Python 3.12+ 缺少预编译 wheel，易导致源码编译卡死，改用兼容的 oxipng-pybind
        required.append("oxipng-pybind")

    if required:
        print(f"Missing dependencies: {', '.join(required)}")
        print(f"Installing via pip: {' '.join(required)}")
        try:
            subprocess.check_call([
                sys.executable, "-m", "pip", "install", *required,
                "-i", "https://pypi.tuna.tsinghua.edu.cn/simple"
            ])
            print("Installation complete.")
        except subprocess.CalledProcessError:
            print("Failed to install oxipng-pybind, falling back to basic Pillow mode.")
            # 即使安装失败也不返回 False，允许脚本降级运行
    return True


def iter_png_paths(assets_root: Path) -> Iterable[Path]:
    for modid_dir in assets_root.iterdir():
        textures_dir = modid_dir / "textures"
        if textures_dir.is_dir():
            yield from textures_dir.rglob("*.png")


def clean_and_replace(path: Path) -> None:
    from PIL import Image, ImageChops

    with Image.open(path) as img:
        img.load()
        if img.mode != "RGBA":
            img = img.convert("RGBA")

        r, g, b, a = img.split()
        mask = a.point(lambda p: 255 if p > 0 else 0)
        r = ImageChops.multiply(r, mask)
        g = ImageChops.multiply(g, mask)
        b = ImageChops.multiply(b, mask)
        clean = Image.merge("RGBA", (r, g, b, a))

        # 清除元数据，防止冗余块导致体积膨胀
        clean.info.clear()
        # 禁用 optimize=True，避免 Pillow 的全局搜索破坏原有最优编码导致体积变大
        clean.save(path, format="PNG")

    # 尝试导入 oxipng 进行深度无损压缩，不可用时静默跳过
    try:
        import oxipng
        # level 4 在 oxipng 的压缩率与耗时之间相对平衡
        oxipng.optimize(path, path, level=4)
    except (ImportError, Exception):
        pass


def print_relative_paths(title: str, paths: list[Path]) -> None:
    print(f"{title}: {len(paths)}")
    for path in paths:
        print(path.relative_to(PROJECT_ROOT))


def process_png_path(path: Path) -> tuple[Literal["processed", "error"], Path]:
    try:
        clean_and_replace(path)
    except (OSError, ValueError):
        return "error", path
    return "processed", path


def get_worker_count(file_count: int) -> int:
    if file_count < MIN_PARALLEL_FILE_COUNT:
        return 1
    cpu_count = os.cpu_count() or 1
    return min(MAX_WORKERS, file_count, max(2, cpu_count))


def main() -> int:
    ensure_dependencies()

    if not ASSETS_ROOT.exists():
        print(f"Assets directory not found: {ASSETS_ROOT}")
        return 1

    png_paths = sorted(iter_png_paths(ASSETS_ROOT))
    scanned = len(png_paths)
    processed = 0
    skipped_errors: list[Path] = []
    worker_count = get_worker_count(scanned)

    if worker_count == 1:
        results = (process_png_path(png_path) for png_path in png_paths)
        for status, png_path in results:
            if status == "processed":
                processed += 1
            else:
                skipped_errors.append(png_path)
    else:
        # 避开 GIL，利用多核进行 CPU 密集的图像压缩
        with concurrent.futures.ProcessPoolExecutor(max_workers=worker_count) as executor:
            for status, png_path in executor.map(process_png_path, png_paths):
                if status == "processed":
                    processed += 1
                else:
                    skipped_errors.append(png_path)

    print(f"Scanned: {scanned}")
    print(f"Workers used: {worker_count}")
    print(f"Processed: {processed}")
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