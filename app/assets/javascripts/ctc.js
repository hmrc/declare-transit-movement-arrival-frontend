// initialise GovUK lib
GOVUKFrontend.initAll();

// back link
var backLink = document.querySelector('.govuk-back-link');
if(backLink){
    backLink.addEventListener('click', function(e){
        e.preventDefault();
        if (window.history && window.history.back && typeof window.history.back === 'function'){
            window.history.back();
        }
    });
}