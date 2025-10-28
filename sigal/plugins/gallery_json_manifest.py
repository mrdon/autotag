"""Plugin to generate JSON manifest files for live gallery polling.

This plugin generates a simple `data.json` file in each album directory containing
just the list of media files and their modification dates. This allows efficient
client-side polling to detect new images.
"""

import json
import logging
import os
from datetime import datetime

from sigal import signals

logger = logging.getLogger(__name__)


def generate_json_manifest(gallery):
    """Generate simple JSON manifest files for all albums in the gallery."""
    logger.info("Generating JSON manifest files for live polling")

    for album_path, album in gallery.albums.items():
        # Skip albums with no media
        if not album.medias:
            continue

        # Generate simple list of files and dates
        manifest = []
        for media in album.medias:
            try:
                # Get the source file's modification time
                mtime = os.path.getmtime(media.src_path)
                file_date = datetime.fromtimestamp(mtime).isoformat()

                manifest.append({
                    "file": media.url,  # The generated image filename/URL
                    "date": file_date
                })
            except Exception as e:
                logger.warning(f"Failed to process media {media}: {e}")
                continue

        # Write the JSON file
        json_path = os.path.join(album.dst_path, "data.json")
        try:
            with open(json_path, 'w', encoding='utf-8') as f:
                json.dump(manifest, f, ensure_ascii=False, indent=2)
            logger.debug(f"Created JSON manifest: {json_path} ({len(manifest)} items)")
        except Exception as e:
            logger.error(f"Failed to write JSON manifest {json_path}: {e}")


def register(settings):
    """Register the plugin with sigal."""
    signals.gallery_build.connect(generate_json_manifest)
