(function () {
    const searchInput = document.getElementById('searchTerm');
    const suggestionList = document.getElementById('searchSuggestions');
    if (!searchInput || !suggestionList) {
        return;
    }

    let timer;
    searchInput.setAttribute('aria-expanded', 'false');

    searchInput.addEventListener('input', () => {
        clearTimeout(timer);
        const value = searchInput.value.trim();
        if (value.length < 2) {
            suggestionList.innerHTML = '';
            suggestionList.classList.add('hidden');
            searchInput.setAttribute('aria-expanded', 'false');
            return;
        }
        timer = setTimeout(() => {
            fetch(`/api/products/autocomplete?term=${encodeURIComponent(value)}`)
                .then(res => res.ok ? res.json() : [])
                .then(items => {
                    suggestionList.innerHTML = '';
                    if (!items || items.length === 0) {
                        suggestionList.classList.add('hidden');
                        searchInput.setAttribute('aria-expanded', 'false');
                        return;
                    }
                    suggestionList.classList.remove('hidden');
                    searchInput.setAttribute('aria-expanded', 'true');
                    items.forEach(title => {
                        const li = document.createElement('li');
                        li.tabIndex = 0;
                        li.role = 'option';
                        li.textContent = title;
                        li.addEventListener('click', () => {
                            searchInput.value = title;
                            suggestionList.innerHTML = '';
                            suggestionList.classList.add('hidden');
                            searchInput.setAttribute('aria-expanded', 'false');
                        });
                        li.addEventListener('keydown', (event) => {
                            if (event.key === 'Enter') {
                                searchInput.value = title;
                                suggestionList.innerHTML = '';
                                suggestionList.classList.add('hidden');
                                searchInput.setAttribute('aria-expanded', 'false');
                            }
                        });
                        suggestionList.appendChild(li);
                    });
                })
                .catch(() => {
                    suggestionList.innerHTML = '';
                    suggestionList.classList.add('hidden');
                    searchInput.setAttribute('aria-expanded', 'false');
                });
        }, 250);
    });
})();
