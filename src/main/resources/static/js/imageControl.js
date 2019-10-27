var imageIndex = 0;
showImage(imageIndex);

function showImage(n) {
    var x = document.getElementsByClassName("ProjectMainImage");
    for (var i = 0; i < x.length; i++) {
        x[i].style.display = "none";
    }
    x[n].style.display = "block";
}