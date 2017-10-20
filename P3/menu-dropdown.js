function menu() {
    if(document.getElementById('dropdown').className == 'show') {
        document.getElementById('dropdown').className = 'hidden';
        document.getElementById('dropdown').setAttribute('aria-hidden', 'true');
    } else {
        document.getElementById('dropdown').className = 'show';
        document.getElementById('dropdown').setAttribute('aria-hidden', 'false');
    }
}

function change_filter() {
    var filter = document.getElementById('table_selection').value;

    if (filter == 'default') {
        show_all();
    } else {
        filter_tables(filter);
    }
}

function show_all() {
    var rows = document.getElementsByTagName('tr');

    for(var i = 0; i < rows.length; i++) {
        rows[i].removeAttribute('style');
        rows[i].removeAttribute('aria-hidden');
    }
}

function filter_tables(filter) {
    var rows = document.getElementsByTagName('tr');

    for(var i = 0; i < rows.length; i++) {
        if (rows[i].classList.contains('head') || rows[i].classList.contains(filter)) {
            rows[i].removeAttribute('style');
            rows[i].setAttribute('aria-hidden', 'false');
        } else {
            rows[i].style.display = 'none';
            rows[i].setAttribute('aria-hidden', 'true');
        }
    }
}

function update_aria() {
    if(window.innerWidth < 767) {
        // Hidden aria
        if(document.getElementById('dropdown').className == 'hidden') {
            document.getElementById('dropdown').setAttribute('aria-hidden', 'true');
        } else {
            document.getElementById('dropdown').setAttribute('aria-hidden', 'false');
        }
        document.getElementsByClassName('dropdown_btn')[0].setAttribute('aria-hidden', 'false');

        // Dropdown
        document.getElementById('dropdown_menu').setAttribute('role', 'menu');
        var items = document.getElementsByClassName('item_menu');
        var i;

        for(i = 0; i < items.length; i++) {
          items[i].setAttribute('role', 'menuitemradio');
          if (items[i].classList.contains('selected_menu')) {
            items[i].setAttribute('aria-checked', 'true');
          } else {
            items[i].setAttribute('aria-checked', 'false');
          }
        }
    } else {
        // Hidden aria
        document.getElementById('dropdown').setAttribute('aria-hidden', 'false');
        document.getElementsByClassName('dropdown_btn')[0].setAttribute('aria-hidden', 'true');

        // Dropdown
        document.getElementById('dropdown_menu').removeAttribute('role');
        var items = document.getElementsByClassName('item_menu');
        var i;

        for(i = 0; i < items.length; i++) {
          items[i].removeAttribute('role');
          items[i].removeAttribute('aria-checked');
        }
    }
}


update_aria();
window.addEventListener("resize", update_aria);
