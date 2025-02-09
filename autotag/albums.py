import os
import subprocess
from datetime import datetime, tzinfo

import pytz
from slugify import slugify


def create_album(base_dir: str, name: str | None = None) -> str:
    if not name:
        now = datetime.now(pytz.timezone("America/Denver"))
        album_path = now.strftime('%Y-%m-%d')
        album_name = now.strftime('%b %d, %Y')
    else:
        album_path = slugify(name)
        album_name = name

    album_dir = os.path.join(base_dir, album_path)
    if not os.path.isdir(album_dir):
        os.makedirs(album_dir, exist_ok=True)
        with open(os.path.join(album_dir, 'index.md'), 'w') as w:
            w.write(f"""
title: {album_name}
categories: [travel,tech,foo,bar,baz]
                    """)
    return album_dir


def regenerate_site() -> bool:
    proc = subprocess.run(["venv/bin/sigal", "build", "../web/content/galleries", "public"], cwd="sigal")
    return proc.returncode == 0
