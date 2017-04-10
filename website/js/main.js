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

function generateMenu(fundId, fundName) {
  var scriptNode = document.getElementById(fundId);
  var mainTag = scriptNode.parentNode;
  mainTag.removeChild(scriptNode);
  
  var li = document.createElement('li');
  mainTag.appendChild(li);
  
  var a = document.createElement('a');
  var t = document.createTextNode(fundName);
  a.appendChild(t);
  a.href = '#' + fundId;
  a.setAttribute('onclick', 'javascript:menu(this.href);');
  li.appendChild(a);
  
  var ul2 = document.createElement('ul');
  li.appendChild(ul2);
  
  var li2 = document.createElement('li');
  ul2.appendChild(li2);
  
  var ad = document.createElement('a');
  t = document.createTextNode('Дневни данни');
  ad.appendChild(t);
  ad.href = 'data/' + fundId + '-dnevni-danni.csv';
  ad.download = fundId + '-dnevni-danni.csv';
  li2.appendChild(ad);
  
  var li3 = document.createElement('li');
  ul2.appendChild(li3);

  var aw = document.createElement('a');
  t = document.createTextNode('Седмични данни');
  aw.appendChild(t);
  aw.href = 'data/' + fundId + '-sedmichni-danni.csv';
  aw.download = fundId + '-sedmichni-danni.csv';
  li3.appendChild(aw);
}