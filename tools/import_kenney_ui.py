#!/usr/bin/env python3
"""Import the original Kenney UI Pack into Android resources without editing artwork."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import unicodedata
import zipfile
from pathlib import Path, PurePosixPath

PACK_NAME = "Kenney UI Pack"
SOURCE_URL = "https://kenney.nl/assets/ui-pack"
LICENSE = "CC0-1.0"
SUPPORTED_IMAGES = {".png", ".webp", ".jpg", ".jpeg"}
SUPPORTED_FONTS = {".ttf", ".otf"}
SUPPORTED_AUDIO = {".wav", ".ogg", ".mp3"}


def resource_name(archive_path: str) -> str:
    """Return a valid, stable Android resource name from an archive path."""
    normalized = unicodedata.normalize("NFKD", archive_path)
    ascii_path = normalized.encode("ascii", "ignore").decode("ascii").lower()
    stem = str(PurePosixPath(ascii_path).with_suffix(""))
    token = re.sub(r"[^a-z0-9_]+", "_", stem.replace("/", "_")).strip("_")
    token = re.sub(r"_+", "_", token) or "asset"
    return ("kenney_" + token)[:75].rstrip("_")


def safe_archive_path(raw_path: str) -> PurePosixPath | None:
    path = PurePosixPath(raw_path.replace("\\", "/"))
    if path.is_absolute() or any(part in ("", ".", "..") for part in path.parts):
        return None
    return path


def destination_for(relative_path: PurePosixPath, res_root: Path, assets_root: Path):
    suffix = relative_path.suffix.lower()
    name = resource_name(relative_path.as_posix())
    if suffix in SUPPORTED_IMAGES:
        return res_root / "drawable-nodpi" / f"{name}{suffix}", "drawable-nodpi"
    if suffix in SUPPORTED_FONTS:
        return res_root / "font" / f"{name}{suffix}", "font"
    if suffix in SUPPORTED_AUDIO:
        return res_root / "raw" / f"{name}{suffix}", "raw"
    return assets_root / "source" / Path(*relative_path.parts), "assets"


def import_pack(archive: Path, repo_root: Path) -> dict:
    app_root = repo_root / "android" / "app" / "src" / "main"
    res_root = app_root / "res"
    assets_root = app_root / "assets" / "kenney-ui-pack"
    archive_copy = repo_root / "android" / "assets" / "kenney" / "kenney_ui-pack.zip"
    archive_copy.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(archive, archive_copy)

    used_names: set[str] = set()
    manifest_files = []
    with zipfile.ZipFile(archive) as bundle:
        for item in bundle.infolist():
            if item.is_dir():
                continue
            relative = safe_archive_path(item.filename)
            if relative is None:
                continue
            destination, resource_type = destination_for(relative, res_root, assets_root)
            if resource_type != "assets":
                base = destination.stem
                if base in used_names:
                    suffix = hashlib.sha1(relative.as_posix().encode("utf-8")).hexdigest()[:8]
                    destination = destination.with_name(f"{base[:64]}_{suffix}{destination.suffix}")
                used_names.add(destination.stem)
            destination.parent.mkdir(parents=True, exist_ok=True)
            with bundle.open(item) as source, destination.open("wb") as target:
                shutil.copyfileobj(source, target)
            manifest_files.append({
                "archive_path": item.filename,
                "installed_path": destination.relative_to(repo_root).as_posix(),
                "type": resource_type,
                "size": item.file_size,
            })

    manifest = {
        "pack": PACK_NAME,
        "source": SOURCE_URL,
        "license": LICENSE,
        "original_archive": "android/assets/kenney/kenney_ui-pack.zip",
        "files": manifest_files,
    }
    manifest_path = assets_root / "asset-index.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    readme = repo_root / "android" / "assets" / "kenney" / "README.md"
    readme.write_text(
        "# Kenney UI Pack\\n\\n"
        "Original artwork by Kenney. Source: " + SOURCE_URL + "\\n\\n"
        "License: CC0 1.0 Universal. The original ZIP is retained unchanged at "
        "`kenney_ui-pack.zip`. PNG/WebP/JPEG files are copied into Android "
        "drawable resources; fonts and sounds are placed in their Android resource "
        "folders. Other original source files are preserved under the app assets folder.\\n",
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("archive", type=Path)
    parser.add_argument("--repo-root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    manifest = import_pack(args.archive, args.repo_root.resolve())
    counts = {}
    for item in manifest["files"]:
        counts[item["type"]] = counts.get(item["type"], 0) + 1
    print(json.dumps({"files": len(manifest["files"]), "by_type": counts}, ensure_ascii=False))


if __name__ == "__main__":
    main()
