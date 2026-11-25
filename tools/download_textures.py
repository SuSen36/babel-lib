"""从清单批量下载贴图。

清单支持 GitHub tree API 的 JSON（取 tree[].path 的文件名）或每行一个文件名/完整 URL 的 txt。
URL 模板由参数提供，含 {name} 占位符；下载走标准库并保留 HTTPS 证书校验，
先写 .part 临时文件，校验 PNG 签名与最小字节数后再原子替换到输出目录。
"""

from __future__ import annotations

import argparse
import json
import os
import threading
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

# 默认模板仅用于兼容原 fexli 收藏品素材；其他来源请用 --url-template 覆盖
DEFAULT_URL_TEMPLATES = [
    "https://gh-proxy.com/https://raw.githubusercontent.com/fexli/ArknightsResource/main/rogueitem/{name}",
    "https://ghproxy.net/https://raw.githubusercontent.com/fexli/ArknightsResource/main/rogueitem/{name}",
]
UA = "Mozilla/5.0 (compatible; texture-downloader/1.0)"


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="按清单批量下载贴图，校验 PNG 签名与大小，支持断点跳过")
    parser.add_argument("manifest", help="清单：GitHub tree JSON 或每行一个文件名/完整 URL 的 txt")
    parser.add_argument("--out", required=True, help="输出目录")
    parser.add_argument(
        "--url-template", action="append", dest="url_templates", default=None,
        help="含 {name} 占位符的下载 URL 模板，可重复指定用于轮换重试；默认 fexli 仓库代理",
    )
    parser.add_argument("--include", action="append", default=[], help="只下载文件名含该子串的项，可重复（任一命中即保留）")
    parser.add_argument("--exclude", action="append", default=[], help="跳过文件名含该子串的项，可重复")
    parser.add_argument("--ext", default=".png", help="后缀过滤，默认 .png；传 * 表示不过滤")
    parser.add_argument("--min-size", type=int, default=500, help="有效文件最小字节数，默认 500")
    parser.add_argument("--workers", type=int, default=10, help="并发线程数，默认 10")
    parser.add_argument("--retries", type=int, default=3, help="重试次数，默认 3")
    parser.add_argument("--timeout", type=float, default=30.0, help="单次请求超时秒数，默认 30")
    parser.add_argument("--dry-run", action="store_true", help="只列出待下载项，不实际下载")
    return parser


def load_manifest(path: Path, include: list[str], exclude: list[str], ext: str) -> list[tuple[str, str | None]]:
    content = path.read_text(encoding="utf-8")
    raw: list[str] = []
    if path.suffix.lower() == ".json":
        data = json.loads(content)
        if isinstance(data, dict):
            for item in data.get("tree", []):
                if item.get("type") not in (None, "blob"):
                    continue
                raw.append(item["path"].split("/")[-1])
        else:
            raw = [str(item).split("/")[-1] for item in data]
    else:
        for line in content.splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                raw.append(line)

    entries: list[tuple[str, str | None]] = []
    for item in raw:
        if item.startswith(("http://", "https://")):
            name = urllib.parse.unquote(urllib.parse.urlsplit(item).path.rsplit("/", 1)[-1])
            override: str | None = item
        else:
            name = Path(item).name
            override = None
        if not name:
            continue
        if ext != "*" and not name.lower().endswith(ext.lower()):
            continue
        low = name.lower()
        if include and not any(token.lower() in low for token in include):
            continue
        if any(token.lower() in low for token in exclude):
            continue
        entries.append((name, override))
    return entries


def valid_png(path: Path, min_size: int) -> bool:
    try:
        if path.stat().st_size < min_size:
            return False
        with path.open("rb") as stream:
            return stream.read(4) == b"\x89PNG"
    except OSError:
        return False


def fetch(
    entry: tuple[str, str | None], out_dir: Path, templates: list[str],
    min_size: int, retries: int, timeout: float,
) -> tuple[str, str, str]:
    name, override = entry
    dest = out_dir / name
    if valid_png(dest, min_size):
        return "skip", name, ""

    tmp = out_dir / (name + ".part")
    urls = [override] if override else templates
    last = ""
    for attempt in range(retries):
        url = urls[attempt % len(urls)]
        if "{name}" in url:
            url = url.replace("{name}", urllib.parse.quote(name, safe=""))
        try:
            request = urllib.request.Request(url, headers={"User-Agent": UA})
            with urllib.request.urlopen(request, timeout=timeout) as response, tmp.open("wb") as stream:
                while chunk := response.read(1 << 16):
                    stream.write(chunk)
            if valid_png(tmp, min_size):
                os.replace(tmp, dest)
                return "ok", name, ""
            last = "invalid png or smaller than min-size"
        except (urllib.error.URLError, OSError, ValueError) as error:
            last = f"{type(error).__name__}: {error}"
        finally:
            if tmp.exists():
                tmp.unlink()
    return "fail", name, last


def main() -> int:
    args = build_parser().parse_args()
    manifest = Path(args.manifest)
    out_dir = Path(args.out)
    templates = args.url_templates or DEFAULT_URL_TEMPLATES

    entries = load_manifest(manifest, args.include, args.exclude, args.ext)
    print(f"manifest: {len(entries)} items", flush=True)
    if args.dry_run:
        for name, _ in entries:
            print(name)
        return 0

    out_dir.mkdir(parents=True, exist_ok=True)
    stats = {"ok": 0, "skip": 0, "fail": 0}
    failed: list[tuple[str, str]] = []
    lock = threading.Lock()

    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = [
            executor.submit(fetch, entry, out_dir, templates, args.min_size, args.retries, args.timeout)
            for entry in entries
        ]
        for future in as_completed(futures):
            status, name, reason = future.result()
            with lock:
                stats[status] += 1
                if status == "fail":
                    failed.append((name, reason))
                if status == "ok" and stats["ok"] % 50 == 0:
                    print(f"downloaded {stats['ok']}", flush=True)

    print(f"NEW {stats['ok']}  SKIP {stats['skip']}  FAIL {stats['fail']}", flush=True)
    if failed:
        log = out_dir / "_failed.txt"
        with log.open("w", encoding="utf-8") as stream:
            for name, reason in failed:
                stream.write(f"{name}\t{reason}\n")
        print(f"failures written to {log}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
