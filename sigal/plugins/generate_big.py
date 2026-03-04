"""Plugin to generate intermediate "big" images for mobile performance.

Generates 1920x1440 intermediate images in a big/ subdirectory of each album.
These are used for fullscreen viewing instead of the original multi-MB photos,
reducing load from ~10MB to ~500KB-1MB.
"""

import logging
import os

from sigal import signals
from sigal.image import generate_image

logger = logging.getLogger(__name__)

BIG_DIR = 'big'
BIG_IMG_SIZE = (1920, 1440)
BIG_JPG_OPTIONS = {'quality': 85, 'optimize': True, 'progressive': True}


def generate_big_images(gallery):
    """Generate intermediate-sized images for all albums in the gallery."""
    logger.info("Generating intermediate 'big' images for mobile performance")

    settings = gallery.settings
    count = 0

    for album_path, album in gallery.albums.items():
        if not album.medias:
            continue

        big_dir = os.path.join(album.dst_path, BIG_DIR)

        for media in album.medias:
            if media.type != 'image':
                continue

            # Create big/ directory only when we have images to process
            if not os.path.isdir(big_dir):
                os.makedirs(big_dir, exist_ok=True)

            outname = os.path.join(big_dir, media.dst_filename)

            # Skip if output exists and is newer than source
            if os.path.isfile(outname):
                if os.path.getmtime(outname) >= os.path.getmtime(media.src_path):
                    logger.debug("Skipping up-to-date big image: %s", outname)
                    continue

            logger.debug("Generating big image: %s", outname)

            # Build settings override for the intermediate size
            big_settings = dict(settings)
            big_settings['img_size'] = BIG_IMG_SIZE

            generate_image(media.src_path, outname, big_settings,
                           options=BIG_JPG_OPTIONS)
            count += 1

    logger.info("Generated %d intermediate 'big' images", count)


def register(settings):
    """Register the plugin with sigal."""
    signals.gallery_build.connect(generate_big_images)
