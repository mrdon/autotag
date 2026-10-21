"""Plugin to pick album thumbnails from in-camera star ratings.

Uses the first 2-star photo as the album thumbnail, falling back to the first
1-star photo. Albums with neither (or with a thumbnail set in index.md) keep
sigal's default choice.
"""

import logging
import re

from PIL import Image
from sigal import signals

logger = logging.getLogger(__name__)

PREFERRED_RATINGS = (2, 1)
RATING_RE = re.compile(rb'xmp:Rating(?:>|=["\'])\s*(-?\d+)')


def get_rating(path: str) -> int | None:
    """Read the XMP star rating written by the camera, if any."""
    try:
        with Image.open(path) as im:
            xmp = im.info.get('xmp')
    except Exception as e:
        logger.debug("Could not read rating from %s: %s", path, e)
        return None
    if not xmp:
        return None
    if isinstance(xmp, str):
        xmp = xmp.encode()
    m = RATING_RE.search(xmp)
    return int(m.group(1)) if m else None


def pick_thumbnail(album):
    if album.meta.get('thumbnail') or not album.medias:
        return

    ratings = {}
    for media in album.medias:
        if media.type == 'image':
            ratings[media.src_filename] = get_rating(media.src_path)

    for wanted in PREFERRED_RATINGS:
        for media in album.medias:
            if ratings.get(media.src_filename) == wanted:
                logger.debug("Using %d-star %s as thumbnail for %r",
                             wanted, media.src_filename, album)
                album.meta['thumbnail'] = [media.src_filename]
                return


def register(settings):
    signals.medias_sorted.connect(pick_thumbnail)
