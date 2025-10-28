/**
 * Live Gallery Polling
 * Polls for new images and notifies the user with a toast
 */
(function() {
    'use strict';

    var pollingState = {
        currentManifest: [],
        pollInterval: 15000, // 15 seconds
        pollTimeout: 30 * 60 * 1000, // 30 minutes
        timerId: null,
        timeoutId: null,
        isPolling: false,
        galleria: null,
        newImagesCount: 0,
        firstNewImageIndex: 0
    };

    /**
     * Fetch the manifest with cache busting
     */
    function fetchManifest() {
        var cacheBuster = '?t=' + new Date().getTime();
        return fetch('data.json' + cacheBuster)
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('Failed to fetch manifest');
                }
                return response.json();
            })
            .catch(function(error) {
                console.error('Error fetching manifest:', error);
                return null;
            });
    }

    /**
     * Compare manifests and detect new images
     * Returns an object with newFiles array and firstNewIndex
     */
    function detectNewImages(oldManifest, newManifest) {
        if (!oldManifest || !newManifest) return null;

        // Create a set of existing files
        var existingFiles = new Set(oldManifest.map(function(item) {
            return item.file;
        }));

        // Find new files and track their indices
        var newFiles = [];
        var firstNewIndex = -1;

        for (var i = 0; i < newManifest.length; i++) {
            if (!existingFiles.has(newManifest[i].file)) {
                newFiles.push(newManifest[i]);
                if (firstNewIndex === -1) {
                    firstNewIndex = i;
                }
            }
        }

        return newFiles.length > 0 ? {
            files: newFiles,
            count: newFiles.length,
            firstIndex: firstNewIndex
        } : null;
    }

    /**
     * Show toast notification
     */
    function showToast(count, firstNewImageIndex) {
        var existingToast = document.getElementById('gallery-update-toast');
        if (existingToast) {
            existingToast.remove();
        }

        var toast = document.createElement('div');
        toast.id = 'gallery-update-toast';
        toast.className = 'gallery-toast';

        var message = count === 1 ? '1 new image available' : count + ' new images available';
        toast.innerHTML = '<span>' + message + '</span><button class="toast-close">×</button>';

        document.body.appendChild(toast);

        // Show the toast with animation
        setTimeout(function() {
            toast.classList.add('show');
        }, 10);

        // Store state for click handler
        pollingState.newImagesCount = count;
        pollingState.firstNewImageIndex = firstNewImageIndex;

        // Click to reload and view
        toast.addEventListener('click', function(e) {
            if (!e.target.classList.contains('toast-close')) {
                handleToastClick();
            }
        });

        // Close button
        toast.querySelector('.toast-close').addEventListener('click', function(e) {
            e.stopPropagation();
            hideToast();
        });
    }

    /**
     * Hide toast notification
     */
    function hideToast() {
        var toast = document.getElementById('gallery-update-toast');
        if (toast) {
            toast.classList.remove('show');
            setTimeout(function() {
                toast.remove();
            }, 300);
        }
    }

    /**
     * Handle toast click - refresh page and jump to first new image
     */
    function handleToastClick() {
        console.log('Toast clicked! First new image index:', pollingState.firstNewImageIndex);
        hideToast();

        // Use the Galleria history plugin format: #/index
        if (pollingState.firstNewImageIndex >= 0) {
            var hash = '#/' + pollingState.firstNewImageIndex;
            console.log('Setting hash and reloading:', hash);

            // Set the hash first, then reload so it's preserved
            window.location.hash = hash;
            window.location.reload();
        } else {
            console.log('No valid index, reloading page');
            window.location.reload();
        }
    }

    /**
     * Reset the polling timeout
     */
    function resetTimeout() {
        // Clear existing timeout
        if (pollingState.timeoutId) {
            clearTimeout(pollingState.timeoutId);
        }

        // Set new timeout
        pollingState.timeoutId = setTimeout(function() {
            console.log('Polling timeout reached (30 minutes), stopping polling');
            stopPolling();
        }, pollingState.pollTimeout);
    }

    /**
     * Poll for updates
     */
    function poll() {
        if (!pollingState.isPolling) return;

        fetchManifest().then(function(manifest) {
            if (!manifest) return;

            // Check for new images
            var result = detectNewImages(pollingState.currentManifest, manifest);

            if (result && result.count > 0) {
                console.log('Detected ' + result.count + ' new images at index ' + result.firstIndex);
                pollingState.currentManifest = manifest;
                showToast(result.count, result.firstIndex);

                // Reset the 30-minute timeout when new images are found
                resetTimeout();
            }
        });
    }

    /**
     * Start polling
     */
    function startPolling(galleria) {
        if (pollingState.isPolling) return;

        pollingState.galleria = galleria;
        pollingState.isPolling = true;

        // Load initial manifest
        fetchManifest().then(function(manifest) {
            if (manifest) {
                pollingState.currentManifest = manifest;
                console.log('Gallery polling started with ' + manifest.length + ' images');
            }
        });

        // Start polling interval
        pollingState.timerId = setInterval(poll, pollingState.pollInterval);

        // Start the 30-minute timeout
        resetTimeout();

        // Pause polling when page is hidden (Page Visibility API)
        document.addEventListener('visibilitychange', function() {
            if (document.hidden) {
                if (pollingState.timerId) {
                    clearInterval(pollingState.timerId);
                    pollingState.timerId = null;
                }
                // Don't clear the timeout - keep it running
            } else {
                if (pollingState.isPolling && !pollingState.timerId) {
                    pollingState.timerId = setInterval(poll, pollingState.pollInterval);
                    // Poll immediately when page becomes visible
                    poll();
                }
            }
        });
    }

    /**
     * Stop polling
     */
    function stopPolling() {
        pollingState.isPolling = false;
        if (pollingState.timerId) {
            clearInterval(pollingState.timerId);
            pollingState.timerId = null;
        }
        if (pollingState.timeoutId) {
            clearTimeout(pollingState.timeoutId);
            pollingState.timeoutId = null;
        }
    }

    // Export functions
    window.GalleryPoller = {
        start: startPolling,
        stop: stopPolling
    };
})();
