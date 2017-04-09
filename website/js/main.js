function menu(href) {
  document.location.href=href;
  load();
}

function load() {
  var hrefPart = document.location.href.split('#')[1];
  
  var chart = document.getElementById('chart');
  chart.style.display = 'block';
  chart.src = 'fund.html?' + hrefPart;
}

function defaultMenu(item) {
  var href = document.location.href.split('#')[1];
  
  if(href == undefined) {
    href = item;
  }

  menu('#' + href);
}