document.body.className = ((document.body.className) ? document.body.className + ' js-enabled' : 'js-enabled');

// Find first ancestor of el with tagName
// or undefined if not found
function upTo(el, tagName) {
    tagName = tagName.toLowerCase();

    while (el && el.parentNode) {
      el = el.parentNode;
      if (el.tagName && el.tagName.toLowerCase() == tagName) {
        return el;
      }
    }

    // Many DOM methods return null if they don't
    // find the element they are searching for
    // It would be OK to omit the following and just
    // return undefined
    return null;
  }


// initialise GovUK lib
GOVUKFrontend.initAll();

if (document.querySelector('.autocomplete') != null) {
    accessibleAutocomplete.enhanceSelectElement({
        selectElement: document.querySelector('.autocomplete')
    });

    // =====================================================
    // Update autocomplete once loaded with fallback's aria attributes
    // Ensures hint and error are read out before usage instructions
    // =====================================================
    setTimeout(function(){
        var originalSelect = document.querySelector('select.autocomplete');
        if(originalSelect){
            var parentForm = upTo(originalSelect, 'form');
            console.log(parentForm)
            if(parentForm){
                var combo = parentForm.querySelector('[role="combobox"]');
                console.log(combo)
                if(combo){
                    combo.setAttribute('aria-describedby', originalSelect.getAttribute('aria-describedby') + ' ' + combo.getAttribute('aria-describedby'));
                }
            }

        }
    }, 2000)
}


