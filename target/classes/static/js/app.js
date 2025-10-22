(function () {
    const CART_KEY = 'orzon.cart';
    const FAVORITES_KEY = 'orzon.favorites';
    const COUPON_KEY = 'orzon.coupon';

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
        minimumFractionDigits: 2
    });

    const toNumber = (value) => {
        if (value == null) return 0;
        if (typeof value === 'number') return value;
        const parsed = Number(value);
        return Number.isNaN(parsed) ? 0 : parsed;
    };

    const formatCurrency = (value) => currencyFormatter.format(toNumber(value));

    function parseJSON(value, fallback) {
        try {
            return value ? JSON.parse(value) : fallback;
        } catch (err) {
            console.warn('Unable to parse JSON from storage', err);
            return fallback;
        }
    }

    function getCartItems() {
        return parseJSON(localStorage.getItem(CART_KEY), []);
    }

    function setCartItems(items) {
        localStorage.setItem(CART_KEY, JSON.stringify(items));
        updateCartCount();
    }

    function normalizeFavorites(raw) {
        const source = Array.isArray(raw)
            ? raw
            : raw && typeof raw === 'object'
                ? Object.values(raw)
                : [];
        const seen = new Map();
        source.forEach(entry => {
            let productId;
            let title = '';
            let cover = '';
            let price = null;
            let author = '';
            if (typeof entry === 'number' || typeof entry === 'string') {
                productId = Number(entry);
            } else if (entry && typeof entry === 'object') {
                productId = Number(entry.productId ?? entry.id);
                title = entry.title || '';
                cover = entry.cover || entry.coverImageUrl || '';
                author = entry.author || '';
                if (entry.price !== undefined && entry.price !== null && entry.price !== '') {
                    const parsed = Number(entry.price);
                    price = Number.isNaN(parsed) ? null : parsed;
                }
            }
            if (!productId || Number.isNaN(productId)) {
                return;
            }
            if (seen.has(productId)) {
                const existing = seen.get(productId);
                if (!existing.title && title) existing.title = title;
                if (!existing.cover && cover) existing.cover = cover;
                if (!existing.author && author) existing.author = author;
                if (existing.price == null && price != null) existing.price = price;
            } else {
                seen.set(productId, { productId, title, cover, author, price });
            }
        });
        return Array.from(seen.values());
    }

    function getFavorites() {
        return normalizeFavorites(parseJSON(localStorage.getItem(FAVORITES_KEY), []));
    }

    function saveFavorites(list) {
        const normalized = normalizeFavorites(list);
        localStorage.setItem(FAVORITES_KEY, JSON.stringify(normalized));
        updateFavoritesBadge(normalized);
        renderFavoritesDropdown(normalized);
        updateFavoriteButtons(normalized);
    }

    function getStoredCoupon() {
        return localStorage.getItem(COUPON_KEY) || '';
    }

    function setStoredCoupon(code) {
        if (code) {
            localStorage.setItem(COUPON_KEY, code);
        } else {
            localStorage.removeItem(COUPON_KEY);
        }
    }

    function updateCartCount() {
        const total = getCartItems().reduce((sum, item) => sum + Number(item.quantity || 0), 0);
        document.querySelectorAll('.amzn-cart-count').forEach(el => {
            el.textContent = total;
        });
    }

    function updateFavoritesBadge(favorites = getFavorites()) {
        const count = favorites.length;
        document.querySelectorAll('.amzn-favorites-count').forEach(el => {
            el.textContent = count;
        });
    }

    function flashCartLink() {
        document.querySelectorAll('[data-cart-link]').forEach(link => {
            link.classList.remove('flash');
            // trigger reflow to restart animation
            void link.offsetWidth;
            link.classList.add('flash');
        });
    }

    function notify(message) {
        if (!message) return;
        window.setTimeout(() => alert(message), 0);
    }

    function attachAddToCartHandlers() {
        document.querySelectorAll('[data-add-to-cart]').forEach(button => {
            if (button.dataset.cartBound) {
                return;
            }
            button.dataset.cartBound = 'true';
            button.addEventListener('click', () => {
                const host = button.closest('[data-product-id]');
                if (!host) {
                    return;
                }
                const quantityInput = host.querySelector('.qty-input');
                const quantity = Math.max(1, Number(quantityInput?.value || 1));
                const productId = Number(host.dataset.productId);
                const items = getCartItems();
                const existing = items.find(item => item.productId === productId);
                if (existing) {
                    existing.quantity += quantity;
                } else {
                    items.push({ productId, quantity });
                }
                setCartItems(items);
                flashCartLink();
            });
        });
    }

    function updateFavoriteButtons(favorites = getFavorites()) {
        const favoriteIds = new Set(favorites.map(item => item.productId));
        document.querySelectorAll('[data-favorite-id]').forEach(button => {
            const productId = Number(button.dataset.favoriteId);
            button.classList.toggle('is-favorite', favoriteIds.has(productId));
        });
    }

    function renderFavoritesDropdown(favorites = getFavorites()) {
        const listEl = document.querySelector('[data-favorites-list]');
        const emptyEl = document.querySelector('[data-favorites-empty]');
        if (!listEl || !emptyEl) {
            return;
        }
        if (!favorites.length) {
            listEl.innerHTML = '';
            listEl.classList.add('hidden');
            emptyEl.classList.remove('hidden');
            return;
        }
        emptyEl.classList.add('hidden');
        listEl.classList.remove('hidden');
        const itemsHtml = favorites.map(item => {
            const cover = item.cover || 'https://via.placeholder.com/80x120?text=Buch';
            const title = item.title || 'Unbekannter Titel';
            const price = item.price != null ? formatCurrency(item.price) : '';
            return `
                <div class="favorite-item" data-favorite-id="${item.productId}">
                    <img src="${cover}" alt="Cover von ${title}">
                    <div>
                        <p class="favorite-item-title"><a href="/products/${item.productId}">${title}</a></p>
                        ${price ? `<span class="favorite-item-price">${price}</span>` : ''}
                    </div>
                    <button type="button" data-remove-favorite="${item.productId}" aria-label="Favorit entfernen">✕</button>
                </div>
            `;
        }).join('');
        listEl.innerHTML = itemsHtml;
        listEl.querySelectorAll('[data-remove-favorite]').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number(btn.dataset.removeFavorite);
                removeFavorite(id);
            });
        });
    }

    function removeFavorite(productId) {
        const updated = getFavorites().filter(item => item.productId !== productId);
        saveFavorites(updated);
    }

    function toggleFavorite(button) {
        const productId = Number(button.dataset.favoriteId);
        if (!productId) {
            return;
        }
        const favorites = getFavorites();
        const existsIndex = favorites.findIndex(item => item.productId === productId);
        if (existsIndex >= 0) {
            favorites.splice(existsIndex, 1);
            saveFavorites(favorites);
            return;
        }
        const host = button.closest('[data-product-id]');
        const title = host?.dataset.productTitle || button.dataset.favoriteTitle || '';
        const cover = host?.dataset.productCover || button.dataset.favoriteCover || '';
        const author = host?.dataset.productAuthor || '';
        const priceValue = host?.dataset.productPrice;
        const price = priceValue !== undefined ? Number(priceValue) : null;
        favorites.push({
            productId,
            title,
            cover,
            author,
            price: Number.isNaN(price) ? null : price
        });
        saveFavorites(favorites);
    }

    function attachFavoriteHandlers() {
        document.querySelectorAll('[data-favorite-id]').forEach(button => {
            if (button.dataset.favoriteBound) {
                return;
            }
            button.dataset.favoriteBound = 'true';
            button.addEventListener('click', () => toggleFavorite(button));
        });
        updateFavoriteButtons();
    }

    function summarizeCart(items, couponCode) {
        return fetch('/api/cart/summary', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                items: items.map(item => ({ productId: item.productId, quantity: item.quantity })),
                couponCode: couponCode || null
            })
        }).then(res => {
            if (!res.ok) {
                throw new Error('Konnte Warenkorb nicht laden.');
            }
            return res.json();
        });
    }

    function renderCartPage() {
        const container = document.getElementById('cart-page');
        if (!container) {
            return;
        }
        const emptyState = document.getElementById('cart-empty');
        const content = document.getElementById('cart-content');
        const itemsContainer = document.getElementById('cart-items');
        const subtotalEl = document.getElementById('cart-subtotal');
        const discountEl = document.getElementById('cart-discount');
        const totalEl = document.getElementById('cart-total');
        const couponInput = document.getElementById('couponCode');
        const couponFeedback = document.getElementById('coupon-feedback');

        const items = getCartItems();
        const coupon = getStoredCoupon();
        if (couponInput) {
            couponInput.value = coupon;
        }

        if (items.length === 0) {
            emptyState?.classList.remove('hidden');
            content?.classList.add('hidden');
            subtotalEl.textContent = formatCurrency(0);
            discountEl.textContent = formatCurrency(0);
            totalEl.textContent = formatCurrency(0);
            return;
        }

        summarizeCart(items, coupon).then(summary => {
            const validIds = new Set(summary.items.map(line => Number(line.productId)));
            if (summary.items.length !== items.length) {
                const filtered = items.filter(entry => validIds.has(Number(entry.productId)));
                if (filtered.length !== items.length) {
                    setCartItems(filtered);
                    renderCartPage();
                    return;
                }
            }

            emptyState?.classList.add('hidden');
            content?.classList.remove('hidden');
            subtotalEl.textContent = formatCurrency(summary.subtotal);
            discountEl.textContent = formatCurrency(summary.discount);
            totalEl.textContent = formatCurrency(summary.total);

            if (couponFeedback) {
                const discountValue = toNumber(summary.discount);
                if (coupon && !summary.couponCode) {
                    couponFeedback.textContent = 'Gutschein nicht gefunden.';
                    couponFeedback.classList.add('error');
                } else if (coupon && discountValue === 0) {
                    couponFeedback.textContent = 'Gutschein aktuell ohne Wirkung.';
                    couponFeedback.classList.remove('error');
                } else if (coupon && discountValue > 0) {
                    couponFeedback.textContent = `Gutschein ${coupon} angewendet.`;
                    couponFeedback.classList.remove('error');
                } else {
                    couponFeedback.textContent = '';
                    couponFeedback.classList.remove('error');
                }
            }

            if (itemsContainer) {
                itemsContainer.innerHTML = summary.items.map(line => `
                    <article class="cart-item" data-product-id="${line.productId}">
                        <div class="cart-item-header">
                            <img src="${line.coverImageUrl || ''}" alt="Cover von ${line.title}" class="cart-item-image">
                            <div>
                                <h3>${line.title}</h3>
                                <p class="cart-item-author">${line.author || ''}</p>
                            </div>
                        </div>
                    <div class="cart-item-meta">
                            <span>Einzelpreis: <strong>${formatCurrency(line.unitPrice)}</strong></span>
                            <span>Zwischensumme: <strong>${formatCurrency(line.subtotal)}</strong></span>
                        </div>
                        <div class="cart-item-actions">
                            <label>
                                Menge
                                <input class="qty-input" type="number" min="1" value="${line.quantity}">
                            </label>
                            <button type="button" class="button-secondary" data-update-item>Aktualisieren</button>
                            <button type="button" class="button-secondary" data-remove-item>Entfernen</button>
                        </div>
                    </article>
                `).join('');
            }

            itemsContainer?.querySelectorAll('[data-update-item]').forEach(btn => {
                btn.addEventListener('click', () => {
                    const itemEl = btn.closest('.cart-item');
                    const productId = Number(itemEl?.dataset.productId);
                    const newQty = Math.max(1, Number(itemEl?.querySelector('.qty-input')?.value || 1));
                    const updated = getCartItems().map(entry =>
                        entry.productId === productId ? { ...entry, quantity: newQty } : entry);
                    setCartItems(updated);
                    renderCartPage();
                });
            });

            itemsContainer?.querySelectorAll('[data-remove-item]').forEach(btn => {
                btn.addEventListener('click', () => {
                    const itemEl = btn.closest('.cart-item');
                    const productId = Number(itemEl?.dataset.productId);
                    const filtered = getCartItems().filter(entry => entry.productId !== productId);
                    setCartItems(filtered);
                    renderCartPage();
                });
            });
        }).catch(err => {
            console.error(err);
                    notify('Warenkorb konnte nicht aktualisiert werden.');
        });
    }

    function initCartPage() {
        const couponInput = document.getElementById('couponCode');
        const applyCouponBtn = document.getElementById('apply-coupon');
        const checkoutBtn = document.getElementById('checkout-button');
        if (applyCouponBtn) {
            applyCouponBtn.addEventListener('click', () => {
                const code = couponInput?.value.trim() || '';
                setStoredCoupon(code);
                renderCartPage();
            });
        }
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', () => {
                window.location.href = '/checkout';
            });
        }
        renderCartPage();
    }

    function loadAddresses() {
        return fetch('/api/addresses', {
            headers: { 'Accept': 'application/json' }
        }).then(res => {
            if (res.status === 401) {
                throw new Error('unauthorized');
            }
            if (!res.ok) {
                throw new Error('Adresse konnte nicht geladen werden.');
            }
            return res.json();
        });
    }

    function populateAddressSelect(select, addresses) {
        if (!select) return;
        select.innerHTML = addresses.map(addr => `
            <option value="${addr.id}">
                ${addr.street}, ${addr.postalCode} ${addr.city}
            </option>
        `).join('');
    }

    function updateCheckoutSummary(summary) {
        const itemsContainer = document.getElementById('checkout-items');
        const subtotalEl = document.getElementById('checkout-subtotal');
        const discountEl = document.getElementById('checkout-discount');
        const shippingEl = document.getElementById('checkout-shipping');
        const totalEl = document.getElementById('checkout-total');

        if (itemsContainer) {
            itemsContainer.innerHTML = summary.cart.items.map(line => `
                <article class="checkout-item">
                    <img src="${line.coverImageUrl || ''}" alt="Cover von ${line.title}">
                    <div>
                        <h4>${line.title}</h4>
                        <p>${line.quantity} × ${formatCurrency(line.unitPrice)}</p>
                    </div>
                    <div class="checkout-item-price">${formatCurrency(line.subtotal)}</div>
                </article>
            `).join('');
        }

        subtotalEl.textContent = formatCurrency(summary.cart.subtotal);
        discountEl.textContent = formatCurrency(summary.cart.discount);
        shippingEl.textContent = formatCurrency(summary.shippingCost);
        totalEl.textContent = formatCurrency(summary.grandTotal);
    }

    function submitCheckout(confirmPurchase) {
        const shippingSelect = document.getElementById('shippingSelect');
        const billingSelect = document.getElementById('billingSelect');
        const couponInput = document.getElementById('checkoutCoupon');
        const successBanner = document.getElementById('checkout-success');

        const shippingAddressId = Number(shippingSelect?.value);
        const billingAddressId = Number(billingSelect?.value);
        const items = getCartItems();

        if (!shippingAddressId || !billingAddressId) {
            notify('Bitte wählen Sie Versand- und Rechnungsadresse.');
            return;
        }

        if (items.length === 0) {
            notify('Ihr Warenkorb ist leer.');
            return;
        }

        fetch('/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                shippingAddressId,
                billingAddressId,
                confirmPurchase,
                couponCode: couponInput?.value || null,
                items: items.map(item => ({ productId: item.productId, quantity: item.quantity }))
            })
        }).then(res => {
            if (res.status === 401) {
                throw new Error('unauthorized');
            }
            if (!res.ok) {
                return res.json().then(body => {
                    throw new Error(body.error || 'Checkout fehlgeschlagen');
                });
            }
            return res.json();
        }).then(summary => {
            setStoredCoupon(couponInput?.value.trim() || '');
            updateCheckoutSummary(summary);
            if (confirmPurchase) {
                successBanner?.classList.remove('hidden');
                setCartItems([]);
                setStoredCoupon('');
                if (couponInput) {
                    couponInput.value = '';
                }
                updateCartCount();
            }
        }).catch(err => {
            if (err.message === 'unauthorized') {
                notify('Bitte melden Sie sich an, um den Kauf abzuschließen.');
                window.location.href = `/login?redirect=${encodeURIComponent('/checkout')}`;
                return;
            }
            notify(err.message);
        });
    }

    function initCheckoutPage() {
        const root = document.getElementById('checkout-page');
        if (!root) {
            return;
        }
        const content = document.getElementById('checkout-content');
        const warning = document.getElementById('checkout-auth-warning');
        const previewBtn = document.getElementById('checkout-preview');
        const confirmBtn = document.getElementById('checkout-confirm');
        const couponInput = document.getElementById('checkoutCoupon');

        const items = getCartItems();
        if (items.length === 0) {
            notify('Ihr Warenkorb ist leer.');
            window.location.href = '/cart';
            return;
        }

        if (couponInput) {
            couponInput.value = getStoredCoupon();
        }

        loadAddresses().then(addresses => {
            warning?.classList.add('hidden');
            content?.classList.remove('hidden');
            populateAddressSelect(document.getElementById('shippingSelect'), addresses);
            populateAddressSelect(document.getElementById('billingSelect'), addresses);
            previewBtn?.addEventListener('click', () => submitCheckout(false));
            confirmBtn?.addEventListener('click', () => submitCheckout(true));
            submitCheckout(false);
        }).catch(err => {
            if (err.message === 'unauthorized') {
                warning?.classList.remove('hidden');
                content?.classList.add('hidden');
            } else {
                notify(err.message);
            }
        });
    }

    function toggleUserMenu() {
        document.querySelectorAll('[data-user-menu]').forEach(menu => {
            const trigger = menu.querySelector('.amzn-user-trigger');
            const dropdown = menu.querySelector('.amzn-user-dropdown');
            if (!trigger || !dropdown) return;
            trigger.addEventListener('click', () => {
                const expanded = trigger.getAttribute('aria-expanded') === 'true';
                trigger.setAttribute('aria-expanded', String(!expanded));
                dropdown.classList.toggle('open', !expanded);
            });
            document.addEventListener('click', (event) => {
                if (!menu.contains(event.target)) {
                    trigger.setAttribute('aria-expanded', 'false');
                    dropdown.classList.remove('open');
                }
            });
        });
    }

    function initFavoritesMenu() {
        const menu = document.querySelector('[data-favorites-menu]');
        if (!menu) {
            return;
        }
        const trigger = menu.querySelector('.amzn-favorites-link');
        const dropdown = menu.querySelector('.favorites-dropdown');
        if (!trigger || !dropdown) {
            return;
        }
        trigger.addEventListener('click', () => {
            const expanded = trigger.getAttribute('aria-expanded') === 'true';
            trigger.setAttribute('aria-expanded', String(!expanded));
            dropdown.classList.toggle('open', !expanded);
        });
        document.addEventListener('click', event => {
            if (!menu.contains(event.target)) {
                trigger.setAttribute('aria-expanded', 'false');
                dropdown.classList.remove('open');
            }
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        updateCartCount();
        attachAddToCartHandlers();
        attachFavoriteHandlers();
        toggleUserMenu();
        initFavoritesMenu();
        updateFavoritesBadge();
        renderFavoritesDropdown();

        const { error, success } = document.body.dataset;
        if (error) {
            notify(error);
        }
        if (success) {
            notify(success);
        }

        if (document.getElementById('cart-page')) {
            initCartPage();
        }
        if (document.getElementById('checkout-page')) {
            initCheckoutPage();
        }
    });
})();
