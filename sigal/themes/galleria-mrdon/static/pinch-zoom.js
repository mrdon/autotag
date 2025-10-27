/**
 * Pinch-to-Zoom for Galleria Image Viewer
 * Adds touch-based pinch zoom functionality to the Galleria stage
 */
(function() {
    'use strict';

    // Pinch-to-zoom state
    var zoomState = {
        scale: 1,
        minScale: 1,
        maxScale: Infinity,
        initialDistance: 0,
        currentScale: 1,
        translateX: 0,
        translateY: 0,
        initialTranslateX: 0,
        initialTranslateY: 0,
        isPinching: false,
        isPanning: false,
        lastTouchX: 0,
        lastTouchY: 0
    };

    /**
     * Calculate distance between two touch points
     */
    function getDistance(touch1, touch2) {
        var dx = touch1.clientX - touch2.clientX;
        var dy = touch1.clientY - touch2.clientY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Get the center point between two touches
     */
    function getCenter(touch1, touch2) {
        return {
            x: (touch1.clientX + touch2.clientX) / 2,
            y: (touch1.clientY + touch2.clientY) / 2
        };
    }

    /**
     * Apply transform to image
     */
    function applyTransform(img) {
        if (!img) return;

        var transform = 'translate(' + zoomState.translateX + 'px, ' +
                       zoomState.translateY + 'px) scale(' + zoomState.scale + ')';

        img.style.transform = transform;
        img.style.webkitTransform = transform;
        img.style.transformOrigin = 'center center';
        img.style.transition = zoomState.isPinching || zoomState.isPanning ? 'none' : 'transform 0.2s ease-out';
    }

    /**
     * Reset zoom state
     */
    function resetZoom(img) {
        zoomState.scale = 1;
        zoomState.currentScale = 1;
        zoomState.translateX = 0;
        zoomState.translateY = 0;
        zoomState.initialTranslateX = 0;
        zoomState.initialTranslateY = 0;
        zoomState.isPinching = false;
        zoomState.isPanning = false;

        if (img) {
            applyTransform(img);
        }
    }

    /**
     * Constrain pan to keep image within reasonable bounds
     */
    function constrainPan(img) {
        if (!img || zoomState.scale <= 1) {
            zoomState.translateX = 0;
            zoomState.translateY = 0;
            return;
        }

        var rect = img.getBoundingClientRect();
        var container = img.parentElement;
        if (!container) return;

        var containerRect = container.getBoundingClientRect();

        // Calculate maximum pan distances
        var maxX = (rect.width - containerRect.width) / 2;
        var maxY = (rect.height - containerRect.height) / 2;

        // Constrain translation
        zoomState.translateX = Math.max(-maxX, Math.min(maxX, zoomState.translateX));
        zoomState.translateY = Math.max(-maxY, Math.min(maxY, zoomState.translateY));
    }

    /**
     * Initialize pinch-to-zoom on Galleria instance
     */
    window.initGalleriaPinchZoom = function(galleria) {
        if (!galleria) {
            console.warn('Galleria instance not provided to initGalleriaPinchZoom');
            return;
        }

        var stage = galleria.$('stage').get(0);
        if (!stage) {
            console.warn('Galleria stage not found');
            return;
        }

        // Reset zoom when image changes
        galleria.bind('image', function(e) {
            var img = e.imageTarget;
            resetZoom(img);
        });

        // Touch start handler
        stage.addEventListener('touchstart', function(e) {
            if (e.touches.length === 2) {
                // Pinch gesture starting
                e.preventDefault();

                var img = galleria.getActiveImage();
                if (!img) return;

                zoomState.isPinching = true;
                zoomState.initialDistance = getDistance(e.touches[0], e.touches[1]);
                zoomState.currentScale = zoomState.scale;
                zoomState.initialTranslateX = zoomState.translateX;
                zoomState.initialTranslateY = zoomState.translateY;
            } else if (e.touches.length === 1 && zoomState.scale > 1) {
                // Single touch for panning when zoomed
                zoomState.isPanning = true;
                zoomState.lastTouchX = e.touches[0].clientX;
                zoomState.lastTouchY = e.touches[0].clientY;
            }
        }, {passive: false});

        // Touch move handler
        stage.addEventListener('touchmove', function(e) {
            var img = galleria.getActiveImage();
            if (!img) return;

            if (e.touches.length === 2 && zoomState.isPinching) {
                // Pinch zoom
                e.preventDefault();
                e.stopPropagation();

                var currentDistance = getDistance(e.touches[0], e.touches[1]);
                var scale = (currentDistance / zoomState.initialDistance) * zoomState.currentScale;

                // Constrain scale
                zoomState.scale = Math.max(zoomState.minScale, Math.min(zoomState.maxScale, scale));

                applyTransform(img);
            } else if (e.touches.length === 1 && zoomState.isPanning && zoomState.scale > 1) {
                // Pan when zoomed
                e.preventDefault();
                e.stopPropagation();

                var deltaX = e.touches[0].clientX - zoomState.lastTouchX;
                var deltaY = e.touches[0].clientY - zoomState.lastTouchY;

                zoomState.translateX += deltaX;
                zoomState.translateY += deltaY;

                constrainPan(img);
                applyTransform(img);

                zoomState.lastTouchX = e.touches[0].clientX;
                zoomState.lastTouchY = e.touches[0].clientY;
            }
        }, {passive: false});

        // Touch end handler
        stage.addEventListener('touchend', function(e) {
            var img = galleria.getActiveImage();

            if (e.touches.length < 2) {
                zoomState.isPinching = false;
            }

            if (e.touches.length === 0) {
                zoomState.isPanning = false;

                // If zoomed out completely, reset
                if (zoomState.scale <= zoomState.minScale) {
                    resetZoom(img);
                } else if (img) {
                    // Ensure pan is constrained
                    constrainPan(img);
                    applyTransform(img);
                }
            }
        }, {passive: false});

        // Double-tap to zoom in/out
        var lastTap = 0;
        stage.addEventListener('touchend', function(e) {
            if (e.touches.length === 0) {
                var currentTime = new Date().getTime();
                var tapLength = currentTime - lastTap;

                if (tapLength < 300 && tapLength > 0) {
                    // Double tap detected
                    e.preventDefault();

                    var img = galleria.getActiveImage();
                    if (!img) return;

                    if (zoomState.scale > 1) {
                        // Zoom out
                        resetZoom(img);
                    } else {
                        // Zoom in to 2x
                        zoomState.scale = 2;
                        zoomState.translateX = 0;
                        zoomState.translateY = 0;
                        applyTransform(img);
                    }
                }
                lastTap = currentTime;
            }
        });

        console.log('Galleria pinch-to-zoom initialized');
    };
})();
