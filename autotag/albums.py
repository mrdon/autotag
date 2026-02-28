import os
import re
import subprocess
import time
from datetime import datetime, tzinfo

import pytz
from slugify import slugify

GAP_SECONDS = 3600  # 1 hour


def _newest_file_mtime(directory: str) -> float | None:
    """Return the most recent mtime of any file in directory, or None if empty."""
    latest = None
    for entry in os.scandir(directory):
        if entry.is_file() and entry.name != 'index.md':
            mt = entry.stat().st_mtime
            if latest is None or mt > latest:
                latest = mt
    return latest


def _find_todays_albums(base_dir: str, date_str: str) -> list[tuple[str, int]]:
    """Find albums matching date_str, date_str-2, date_str-3, etc.

    Returns list of (dir_path, suffix_number) sorted by suffix ascending.
    Suffix 1 means the base date_str with no numeric suffix.
    """
    pattern = re.compile(rf'^{re.escape(date_str)}(?:-(\d+))?$')
    matches = []
    if not os.path.isdir(base_dir):
        return matches
    for entry in os.scandir(base_dir):
        if not entry.is_dir():
            continue
        m = pattern.match(entry.name)
        if m:
            suffix = int(m.group(1)) if m.group(1) else 1
            matches.append((entry.path, suffix))
    matches.sort(key=lambda x: x[1])
    return matches


def create_album(base_dir: str, name: str | None = None) -> str:
    if not name:
        now = datetime.now(pytz.timezone("America/Denver"))
        date_str = now.strftime('%Y-%m-%d')
        date_display = now.strftime('%b %d, %Y')

        todays = _find_todays_albums(base_dir, date_str)

        if todays:
            latest_dir, latest_suffix = todays[-1]
            newest_mtime = _newest_file_mtime(latest_dir)
            if newest_mtime is not None and (time.time() - newest_mtime) < GAP_SECONDS:
                return latest_dir
            # Need a new album — pick next suffix
            next_suffix = latest_suffix + 1
        else:
            next_suffix = 1

        if next_suffix == 1:
            album_path = date_str
            album_name = date_display
        else:
            album_path = f"{date_str}-{next_suffix}"
            album_name = f"{date_display} ({next_suffix})"

        album_dir = os.path.join(base_dir, album_path)
        os.makedirs(album_dir, exist_ok=True)
        with open(os.path.join(album_dir, 'index.md'), 'w') as w:
            w.write(f"""title: {album_name}
categories: [travel,tech,foo,bar,baz]""")
        return album_dir
    else:
        album_path = slugify(name)
        album_name = name

        album_dir = os.path.join(base_dir, album_path)
        if not os.path.isdir(album_dir):
            os.makedirs(album_dir, exist_ok=True)
            with open(os.path.join(album_dir, 'index.md'), 'w') as w:
                w.write(f"""title: {album_name}
categories: [travel,tech,foo,bar,baz]""")
        return album_dir


def regenerate_site() -> bool:
    proc = subprocess.run(["venv/bin/sigal", "build", "../web/content/galleries", "public"], cwd="sigal")
    return proc.returncode == 0
