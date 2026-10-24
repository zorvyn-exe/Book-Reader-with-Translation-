/**
 * Roman Urdu PDF Reader - Viewer Bridge & Text Layer Interaction Engine
 */
(function() {
    'use strict';

    let pdfDoc = null;
    let totalPages = 0;
    let currentPage = 1;
    let currentScale = 1.25;
    let renderedPages = new Set();
    let currentHighlightSpan = null;
    let totalTextItemsCount = 0;
    let scannedWarningDispatched = false;

    const pagesContainer = document.getElementById('pages-container');
    const viewerContainer = document.getElementById('viewer-container');
    const loadingSpinner = document.getElementById('loading-spinner');
    const loadingText = document.getElementById('loading-text');

    // Configure PDF.js worker
    if (window.pdfjsLib) {
        window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'pdf.worker.min.js';
    }

    /**
     * Initialize document loading from URL or Base64 or Android stream
     */
    function init() {
        const urlParams = new URLSearchParams(window.location.search);
        const fileParam = urlParams.get('file');

        if (fileParam) {
            loadPdfFromUrl(fileParam);
        } else if (window.AndroidBridge && window.AndroidBridge.onRequestPdfData) {
            // Android interface can push data or trigger load
            window.AndroidBridge.onViewerReady();
        } else {
            loadingText.textContent = "Waiting for PDF document...";
        }

        setupTapListener();
        setupScrollListener();
    }

    /**
     * Load PDF from URL / File Path
     */
    window.loadPdfFromUrl = function(url) {
        if (!window.pdfjsLib) {
            console.error("PDF.js library not loaded yet");
            setTimeout(() => window.loadPdfFromUrl(url), 200);
            return;
        }

        loadingSpinner.style.display = 'flex';
        loadingText.textContent = "Opening document...";

        window.pdfjsLib.getDocument({
            url: url,
            cMapUrl: 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/cmaps/',
            cMapPacked: true
        }).promise.then(function(doc) {
            pdfDoc = doc;
            totalPages = doc.numPages;
            loadingSpinner.style.display = 'none';

            if (window.AndroidBridge && window.AndroidBridge.onDocumentLoaded) {
                window.AndroidBridge.onDocumentLoaded(totalPages);
            }

            renderAllPlaceholders();
        }).catch(function(err) {
            console.error("Error loading PDF: ", err);
            loadingText.textContent = "Failed to load PDF: " + err.message;
            if (window.AndroidBridge && window.AndroidBridge.onDocumentError) {
                window.AndroidBridge.onDocumentError(err.message || "Unknown error");
            }
        });
    };

    /**
     * Load PDF from Base64 string directly
     */
    window.loadPdfFromBase64 = function(base64) {
        if (!window.pdfjsLib) {
            setTimeout(() => window.loadPdfFromBase64(base64), 200);
            return;
        }

        try {
            const rawData = atob(base64);
            const uint8Array = new Uint8Array(rawData.length);
            for (let i = 0; i < rawData.length; i++) {
                uint8Array[i] = rawData.charCodeAt(i);
            }

            loadingSpinner.style.display = 'flex';
            window.pdfjsLib.getDocument({ data: uint8Array }).promise.then(function(doc) {
                pdfDoc = doc;
                totalPages = doc.numPages;
                loadingSpinner.style.display = 'none';

                if (window.AndroidBridge && window.AndroidBridge.onDocumentLoaded) {
                    window.AndroidBridge.onDocumentLoaded(totalPages);
                }

                renderAllPlaceholders();
            }).catch(function(err) {
                console.error("Base64 PDF load error: ", err);
                loadingText.textContent = "Error opening book: " + err.message;
            });
        } catch (e) {
            console.error("Error parsing base64 pdf: ", e);
            loadingText.textContent = "Invalid PDF data";
        }
    };

    /**
     * Render page placeholders to support virtual lazy-loading of long books
     */
    function renderAllPlaceholders() {
        pagesContainer.innerHTML = '';
        renderedPages.clear();

        for (let i = 1; i <= totalPages; i++) {
            const pageDiv = document.createElement('div');
            pageDiv.id = `page-${i}`;
            pageDiv.className = 'page-container';
            pageDiv.dataset.pageNumber = i;
            // Default placeholder height before rendering
            pageDiv.style.minHeight = '500px';
            pageDiv.style.width = '92%';
            pagesContainer.appendChild(pageDiv);
        }

        // Setup IntersectionObserver for on-demand lazy rendering of pages
        setupPageObserver();
    }

    /**
     * Lazy page observer - only renders pages when they near the viewport
     */
    let pageObserver = null;
    function setupPageObserver() {
        if (pageObserver) pageObserver.disconnect();

        pageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                const pageNum = parseInt(entry.target.dataset.pageNumber);
                if (entry.isIntersecting) {
                    if (!renderedPages.has(pageNum)) {
                        renderPage(pageNum);
                    }
                }
            });
        }, {
            root: viewerContainer,
            rootMargin: '400px 0px 400px 0px',
            threshold: 0.01
        });

        document.querySelectorAll('.page-container').forEach(el => pageObserver.observe(el));
    }

    /**
     * Render a single page: Canvas + Text Layer
     */
    function renderPage(pageNum) {
        if (!pdfDoc || renderedPages.has(pageNum)) return;
        renderedPages.add(pageNum);

        const pageDiv = document.getElementById(`page-${pageNum}`);
        if (!pageDiv) return;

        pdfDoc.getPage(pageNum).then(function(page) {
            const viewport = page.getViewport({ scale: currentScale });

            // Fit container size to viewport
            pageDiv.style.width = `${viewport.width}px`;
            pageDiv.style.height = `${viewport.height}px`;
            pageDiv.style.minHeight = `${viewport.height}px`;
            pageDiv.innerHTML = '';

            // Create canvas for PDF graphics
            const canvas = document.createElement('canvas');
            canvas.className = 'page-canvas';
            canvas.width = viewport.width * (window.devicePixelRatio || 1);
            canvas.height = viewport.height * (window.devicePixelRatio || 1);
            canvas.style.width = `${viewport.width}px`;
            canvas.style.height = `${viewport.height}px`;

            const ctx = canvas.getContext('2d');
            ctx.scale(window.devicePixelRatio || 1, window.devicePixelRatio || 1);
            pageDiv.appendChild(canvas);

            // Render PDF visual content
            const renderContext = {
                canvasContext: ctx,
                viewport: viewport
            };

            page.render(renderContext).promise.then(function() {
                // Now extract text layer for word tapping & selection
                return page.getTextContent();
            }).then(function(textContent) {
                // Scanned PDF detection check
                const textItems = textContent.items || [];
                totalTextItemsCount += textItems.length;

                if (pageNum <= 3 && totalTextItemsCount === 0 && !scannedWarningDispatched) {
                    scannedWarningDispatched = true;
                    if (window.AndroidBridge && window.AndroidBridge.onScannedPdfDetected) {
                        window.AndroidBridge.onScannedPdfDetected(pageNum);
                    }
                }

                // Build HTML text layer
                const textLayerDiv = document.createElement('div');
                textLayerDiv.className = 'textLayer';
                textLayerDiv.style.width = `${viewport.width}px`;
                textLayerDiv.style.height = `${viewport.height}px`;
                pageDiv.appendChild(textLayerDiv);

                renderTextItems(textLayerDiv, textItems, viewport);
            });
        }).catch(function(err) {
            console.error(`Error rendering page ${pageNum}:`, err);
            renderedPages.delete(pageNum);
        });
    }

    /**
     * Render text items inside TextLayer with exact bounding boxes
     */
    function renderTextItems(container, items, viewport) {
        items.forEach(function(item) {
            if (!item.str || item.str.trim() === '') return;

            const tx = window.pdfjsLib.Util.transform(viewport.transform, item.transform);
            const fontHeight = Math.sqrt((tx[2] * tx[2]) + (tx[3] * tx[3]));

            const span = document.createElement('span');
            span.textContent = item.str;
            span.style.fontSize = `${fontHeight}px`;
            span.style.fontFamily = item.fontName || 'sans-serif';
            span.style.left = `${tx[4]}px`;
            span.style.top = `${tx[5] - fontHeight}px`;

            // Calculate width scale if available
            if (item.width > 0) {
                span.style.width = `${item.width * viewport.scale}px`;
            }

            container.appendChild(span);
        });
    }

    /**
     * Tap / Click event detection for exact English word and surrounding sentence
     */
    function setupTapListener() {
        // Handle touch and click events
        document.addEventListener('click', handleWordTap, true);
    }

    function handleWordTap(e) {
        // Clear previous highlight
        clearHighlight();

        let target = e.target;
        if (!target) return;

        // Check if tap was on a textLayer or inside one
        const textLayer = target.closest('.textLayer');
        if (!textLayer) return;

        let range = null;
        let textNode = null;
        let offset = 0;

        // Modern Caret/Range determination from touch/click coordinates
        if (document.caretRangeFromPoint) {
            range = document.caretRangeFromPoint(e.clientX, e.clientY);
            if (range) {
                textNode = range.startContainer;
                offset = range.startOffset;
            }
        } else if (document.caretPositionFromPoint) {
            const pos = document.caretPositionFromPoint(e.clientX, e.clientY);
            if (pos) {
                textNode = pos.offsetNode;
                offset = pos.offset;
            }
        }

        // Fallback to text node of clicked target
        if (!textNode || textNode.nodeType !== Node.TEXT_NODE) {
            if (target.firstChild && target.firstChild.nodeType === Node.TEXT_NODE) {
                textNode = target.firstChild;
                offset = 0;
            } else {
                return;
            }
        }

        const fullText = textNode.textContent;
        if (!fullText || fullText.length === 0) return;

        // Ensure offset is within bounds
        if (offset < 0) offset = 0;
        if (offset >= fullText.length) offset = fullText.length - 1;

        // If tap was on whitespace, check adjacent character
        if (/\s/.test(fullText[offset])) {
            if (offset > 0 && !/\s/.test(fullText[offset - 1])) {
                offset--;
            } else if (offset < fullText.length - 1 && !/\s/.test(fullText[offset + 1])) {
                offset++;
            } else {
                return; // Tapped on empty whitespace
            }
        }

        // Determine word boundaries: letters, digits, internal hyphens and apostrophes
        let start = offset;
        while (start > 0 && /[a-zA-Z0-9'-]/.test(fullText[start - 1])) {
            start--;
        }

        let end = offset;
        while (end < fullText.length && /[a-zA-Z0-9'-]/.test(fullText[end])) {
            end++;
        }

        if (start >= end) return;

        let rawWord = fullText.substring(start, end);
        // Strip leading/trailing punctuation like quotes or hyphens
        let cleanWord = rawWord.replace(/^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$/g, '');

        if (!cleanWord || cleanWord.length < 1) return;

        // Extract surrounding sentence
        let sentenceStart = start;
        while (sentenceStart > 0 && !/[.!?\n]/.test(fullText[sentenceStart - 1])) {
            sentenceStart--;
        }
        let sentenceEnd = end;
        while (sentenceEnd < fullText.length && !/[.!?\n]/.test(fullText[sentenceEnd])) {
            sentenceEnd++;
        }
        let sentence = fullText.substring(sentenceStart, sentenceEnd).trim();

        // Calculate exact bounding rectangle of tapped word
        try {
            const wordRange = document.createRange();
            wordRange.setStart(textNode, start);
            wordRange.setEnd(textNode, end);
            const rect = wordRange.getBoundingClientRect();

            // Highlight word
            highlightRange(wordRange);

            // Determine page number
            const pageContainer = textLayer.closest('.page-container');
            const pageNum = pageContainer ? parseInt(pageContainer.dataset.pageNumber) : currentPage;

            // Notify native Android bridge
            if (window.AndroidBridge && window.AndroidBridge.onWordTapped) {
                window.AndroidBridge.onWordTapped(
                    cleanWord,
                    sentence,
                    rect.left,
                    rect.top,
                    rect.right,
                    rect.bottom,
                    window.innerWidth,
                    window.innerHeight,
                    window.devicePixelRatio || 1.0,
                    pageNum
                );
            }
        } catch (err) {
            console.error("Error computing word bounding rect:", err);
        }
    }

    /**
     * Highlight range temporarily
     */
    function highlightRange(range) {
        try {
            const highlightSpan = document.createElement('mark');
            highlightSpan.className = 'tapped-highlight';
            range.surroundContents(highlightSpan);
            currentHighlightSpan = highlightSpan;
        } catch (e) {
            // surroundContents can fail across node boundaries; fallback smoothly
            if (range.startContainer && range.startContainer.parentElement) {
                range.startContainer.parentElement.classList.add('tapped-highlight');
                currentHighlightSpan = range.startContainer.parentElement;
            }
        }
    }

    /**
     * Clear highlighted word
     */
    window.clearHighlight = function() {
        if (currentHighlightSpan) {
            if (currentHighlightSpan.tagName === 'MARK') {
                const parent = currentHighlightSpan.parentNode;
                if (parent) {
                    while (currentHighlightSpan.firstChild) {
                        parent.insertBefore(currentHighlightSpan.firstChild, currentHighlightSpan);
                    }
                    parent.removeChild(currentHighlightSpan);
                }
            } else {
                currentHighlightSpan.classList.remove('tapped-highlight');
            }
            currentHighlightSpan = null;
        }
    };

    /**
     * Scroll & Page Progress Tracking
     */
    function setupScrollListener() {
        let scrollTimeout = null;

        viewerContainer.addEventListener('scroll', function() {
            if (scrollTimeout) clearTimeout(scrollTimeout);
            scrollTimeout = setTimeout(updateReadingProgress, 100);
        }, { passive: true });
    }

    function updateReadingProgress() {
        if (totalPages === 0) return;

        const containerRect = viewerContainer.getBoundingClientRect();
        const midY = containerRect.top + containerRect.height / 3;

        // Find which page is at midY
        const pages = document.querySelectorAll('.page-container');
        for (let i = 0; i < pages.length; i++) {
            const rect = pages[i].getBoundingClientRect();
            if (rect.top <= midY && rect.bottom >= midY) {
                const newPage = parseInt(pages[i].dataset.pageNumber);
                if (newPage !== currentPage) {
                    currentPage = newPage;
                }
                break;
            }
        }

        const maxScroll = viewerContainer.scrollHeight - viewerContainer.clientHeight;
        const currentScroll = viewerContainer.scrollTop;
        const scrollPercent = maxScroll > 0 ? (currentScroll / maxScroll) * 100 : 0;

        if (window.AndroidBridge && window.AndroidBridge.onPageChanged) {
            window.AndroidBridge.onPageChanged(currentPage, totalPages, scrollPercent);
        }
    }

    /**
     * Jump directly to specific page number
     */
    window.jumpToPage = function(pageNum) {
        if (pageNum < 1 || pageNum > totalPages) return;
        const targetPage = document.getElementById(`page-${pageNum}`);
        if (targetPage) {
            targetPage.scrollIntoView({ behavior: 'smooth', block: 'start' });
            currentPage = pageNum;
        }
    };

    /**
     * Zoom In / Out / Fit
     */
    window.setZoom = function(scale) {
        if (scale < 0.5 || scale > 3.0) return;
        currentScale = scale;
        // Re-render currently visible pages
        renderedPages.clear();
        document.querySelectorAll('.page-container').forEach(p => {
            p.innerHTML = '';
        });
        setupPageObserver();
    };

    /**
     * Switch Theme: 'light', 'dark', 'sepia'
     */
    window.setTheme = function(themeName) {
        document.body.className = `theme-${themeName}`;
    };

    // Auto-run initialization
    document.addEventListener('DOMContentLoaded', init);
})();
