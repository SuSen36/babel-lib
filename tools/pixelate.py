# -*- coding: utf-8 -*-
"""参考图 -> 低分辨率像素画底稿。

确定性图像处理，不含任何模型推理：
1. 大于 256 的图先按 k 倍降采样（全不透明用 BOX，含透明用最近邻，避免透明色渗入）
2. 去背景：有透明区域直接用 alpha；否则取四边逐通道中位数当背景色，从边缘洪泛去除同色背景
3. 裁主体外接框，居中补成正方形
4. 按面积加权平均缩到 SIDE x SIDE，覆盖率不足 50% 的格子保持透明，颜色在 OKLab 空间只对覆盖部分求均值
5. 四邻不完整的边缘格 RGB x 0.55，模拟物品贴图的深色描边
"""

from __future__ import annotations

import argparse
import importlib
import math
import subprocess
import sys
from pathlib import Path

# 第三方库延迟到 ensure_dependencies() 注入，避免模块导入时依赖第三方包
np = None
Image = None

SIDE = 16
MAX_SIDE = 256
TOLERANCE = 40.0
BORDER_RATIO = 0.85
ALPHA_THRESHOLD = 128
TRANSPARENT_FRACTION = 0.02
OBJECT_MIN = 0.01
OBJECT_MAX = 0.97
COVER_THRESHOLD = 0.5
RIM_SCALE = 0.55
VOTE_BIN = 24
# 均值已在 OKLab 空间进行，不再脱饱和，回拉仅用于抑制格内噪声
DOMINANT_MIX = 0.2


def ensure_dependencies() -> None:
    """检查并在缺失时自动安装 Pillow 与 numpy，随后注入模块级的 np / Image。"""
    global np, Image
    required = []
    try:
        importlib.import_module("PIL")
    except ModuleNotFoundError:
        required.append("Pillow")
    try:
        importlib.import_module("numpy")
    except ModuleNotFoundError:
        required.append("numpy")
    if required:
        print(f"Missing dependencies: {', '.join(required)}")
        print(f"Installing via pip: {' '.join(required)}")
        # 清华源在部分网络环境下 pip 取不到索引，失败后回退默认 PyPI
        indexes = [
            "https://pypi.tuna.tsinghua.edu.cn/simple",
            "https://pypi.org/simple",
        ]
        for i, index in enumerate(indexes):
            try:
                subprocess.check_call([
                    sys.executable, "-m", "pip", "install", *required,
                    "-i", index,
                ])
                break
            except subprocess.CalledProcessError:
                if i == len(indexes) - 1:
                    raise
                print(f"Install from {index} failed, retrying with PyPI.", file=sys.stderr)
        print("Installation complete.")
    import numpy as _np
    from PIL import Image as _Image
    np = _np
    Image = _Image


def srgb_to_oklab(rgb: np.ndarray) -> np.ndarray:
    c = rgb / 255.0
    linear = np.where(c <= 0.04045, c / 12.92, ((c + 0.055) / 1.055) ** 2.4)
    r = linear[..., 0]
    g = linear[..., 1]
    b = linear[..., 2]
    l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b
    m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b
    s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b
    l_ = np.cbrt(l)
    m_ = np.cbrt(m)
    s_ = np.cbrt(s)
    return np.stack([
        0.2104514153 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
        1.9779983836 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
        0.0259040424 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_,
    ], axis=-1)


def oklab_to_srgb(lab: np.ndarray) -> np.ndarray:
    bigL = lab[..., 0]
    a = lab[..., 1]
    b = lab[..., 2]
    l_ = bigL + 0.3963377774 * a + 0.2158037573 * b
    m_ = bigL - 0.1055613458 * a - 0.0638541728 * b
    s_ = bigL - 0.0894841775 * a - 1.2914855480 * b
    l = l_ ** 3
    m = m_ ** 3
    s = s_ ** 3
    linear = np.stack([
        4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
        -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
        -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s,
    ], axis=-1)
    linear = np.clip(linear, 0.0, 1.0)
    srgb = np.where(linear <= 0.0031308, linear * 12.92, 1.055 * linear ** (1 / 2.4) - 0.055)
    return srgb * 255.0


class PixelateError(Exception):
    pass


def pre_reduce(im: Image.Image) -> Image.Image:
    w, h = im.size
    k = math.ceil(max(w, h) / MAX_SIDE)
    if k <= 1:
        return im
    solid = np.asarray(im)[..., 3].min() == 255
    if solid:
        return im.reduce(k)
    return im.resize((max(w // k, 1), max(h // k, 1)), Image.NEAREST)


def flood_remove(rgb: np.ndarray) -> np.ndarray:
    border = np.concatenate([rgb[0], rgb[-1], rgb[:, 0], rgb[:, -1]])
    bg = np.median(border, axis=0)
    close = np.linalg.norm(rgb - bg, axis=2) <= TOLERANCE
    if (np.linalg.norm(border - bg, axis=1) <= TOLERANCE).mean() < BORDER_RATIO:
        raise PixelateError("背景不是纯色，且图片没有透明通道")
    reach = np.zeros(close.shape, dtype=bool)
    reach[0] = close[0]
    reach[-1] = close[-1]
    reach[:, 0] = close[:, 0]
    reach[:, -1] = close[:, -1]
    while True:
        grown = reach.copy()
        grown[1:] |= reach[:-1]
        grown[:-1] |= reach[1:]
        grown[:, 1:] |= reach[:, :-1]
        grown[:, :-1] |= reach[:, 1:]
        grown &= close
        if (grown == reach).all():
            return ~grown
        reach = grown


def object_mask(rgba: np.ndarray) -> np.ndarray:
    alpha = rgba[..., 3]
    if (alpha < ALPHA_THRESHOLD).mean() >= TRANSPARENT_FRACTION:
        return alpha >= ALPHA_THRESHOLD
    return flood_remove(rgba[..., :3].astype(np.float32))


def pixelate(path: Path, side: int, outline: bool) -> np.ndarray:
    im = Image.open(path).convert("RGBA")
    im = pre_reduce(im)
    rgba = np.asarray(im).astype(np.float32)
    mask = object_mask(rgba)
    ratio = mask.mean()
    if not OBJECT_MIN <= ratio <= OBJECT_MAX:
        raise PixelateError(f"主体占比 {ratio:.0%} 超出 {OBJECT_MIN:.0%}-{OBJECT_MAX:.0%}")

    ys, xs = np.where(mask)
    rgb = rgba[ys.min():ys.max() + 1, xs.min():xs.max() + 1, :3]
    hard = mask[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
    h, w = hard.shape
    square_side = max(h, w)
    square = np.zeros((square_side, square_side, 4), dtype=np.float32)
    y0, x0 = (square_side - h) // 2, (square_side - w) // 2
    square[y0:y0 + h, x0:x0 + w, :3] = rgb * hard[..., None]
    square[y0:y0 + h, x0:x0 + w, 3] = hard * 255.0

    # OKLab 空间做 alpha 加权的盒式平均，均值不把饱和色冲淡成灰
    alpha = square[..., 3]
    cover = np.asarray(Image.fromarray(alpha.astype(np.float32), "F").resize((side, side), Image.BOX)).astype(np.float32) / 255.0
    solid = cover >= COVER_THRESHOLD
    lab_weighted = srgb_to_oklab(square[..., :3]) * (alpha / 255.0)[..., None]
    lab_small = np.stack([
        np.asarray(Image.fromarray(lab_weighted[..., c].astype(np.float32), "F").resize((side, side), Image.BOX))
        for c in range(3)
    ], axis=-1).astype(np.float32)
    color_lab = np.zeros((side, side, 3), dtype=np.float32)
    color_lab[solid] = lab_small[solid] / cover[solid, None]

    # 主导色统计：格内按色号分桶取最大桶的均值，仅在 OKLab 空间小幅回拉
    ys, xs = np.where(square[..., 3] > 0)
    cols = square[ys, xs, :3]
    cy = np.minimum(side - 1, ys * side // square.shape[0])
    cx = np.minimum(side - 1, xs * side // square.shape[1])
    q = (cols // VOTE_BIN).astype(np.int32)
    pair = np.stack([cy * side + cx, q[..., 0] * 4096 + q[..., 1] * 64 + q[..., 2]], axis=1)
    order = np.lexsort((pair[:, 1], pair[:, 0]))
    pair = pair[order]
    cols = cols[order]
    change = np.empty(pair.shape[0], dtype=bool)
    change[0] = True
    change[1:] = np.any(pair[1:] != pair[:-1], axis=1)
    starts = np.where(change)[0]
    counts = np.diff(np.append(starts, pair.shape[0]))
    means = np.add.reduceat(cols, starts, axis=0) / counts[:, None]
    cells = pair[starts, 0]
    flat_lab = color_lab.reshape(side * side, 3)
    for cell in np.unique(cells):
        sel = cells == cell
        dom = means[sel][np.argmax(counts[sel])]
        if solid.reshape(-1)[cell]:
            flat_lab[cell] = flat_lab[cell] * (1.0 - DOMINANT_MIX) + srgb_to_oklab(dom) * DOMINANT_MIX

    color = oklab_to_srgb(color_lab)

    if outline:
        pad = np.pad(solid, 1)
        inside = pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:]
        color[solid & ~inside] *= RIM_SCALE

    out = np.zeros((side, side, 4), dtype=np.uint8)
    out[..., :3] = np.clip(np.rint(color), 0, 255)
    out[..., 3] = np.where(solid, 255, 0)
    return out


def main() -> int:
    ensure_dependencies()
    parser = argparse.ArgumentParser(description="把文件夹里的图片批量转成低分辨率像素画")
    parser.add_argument("src", help="源图片文件夹")
    parser.add_argument("out", nargs="?", default=None, help="输出文件夹（默认源文件夹旁的 images_pixel）")
    parser.add_argument("--size", type=int, default=SIDE, help="输出边长，默认 16")
    parser.add_argument("--no-outline", action="store_true", help="不压暗边缘格")
    args = parser.parse_args()

    src_dir = Path(args.src)
    out_dir = Path(args.out) if args.out else src_dir.parent / f"images_pixel{args.size}"
    out_dir.mkdir(parents=True, exist_ok=True)

    files = sorted(p for p in src_dir.iterdir() if p.suffix.lower() == ".png")
    if not files:
        print("没有找到 PNG 文件", file=sys.stderr)
        return 1

    done = 0
    failed = []
    for i, path in enumerate(files):
        try:
            out = pixelate(path, args.size, not args.no_outline)
            Image.fromarray(out, "RGBA").save(out_dir / path.name)
            done += 1
        except (PixelateError, OSError, ValueError) as e:
            failed.append((path.name, str(e)))
        if (i + 1) % 200 == 0:
            print(f"{i + 1}/{len(files)}")

    print(f"完成 {done}/{len(files)}，输出：{out_dir}")
    if failed:
        log = out_dir / "_failed.txt"
        with log.open("w", encoding="utf-8") as f:
            for name, reason in failed:
                f.write(f"{name}\t{reason}\n")
        print(f"失败 {len(failed)} 张，见 {log}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
