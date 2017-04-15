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

function parse_csv(content, row_handler) {
   //console.log('parse_csv', content.indexOf("\n"))
  var i = 0;
  do {
    var nl_pos = content.indexOf("\n", i);
    var line = nl_pos == -1 ? content.substr(i) : content.substr(i, nl_pos-i);
    if (i > 0 && line.length) {//skip header
      var row = line.split(',')
      //console.log('line ' + row);
      row_handler(row)
    }
    i = nl_pos + 1;
  } while(nl_pos != -1)
}

function load_csv(url, done) {
  jQuery.ajax({url:url, mimeType: 'text/csv', success: done})
}

function override_json(xhr){
    //prevents xml parse error in firefox when loaded locally (file://..)
    if (xhr.overrideMimeType)
      xhr.overrideMimeType("application/json");
}

function add_trace(csv_url, name) {
  var chart = $('#chart2')[0]
  load_csv(csv_url, function(csv){
      var dates = [], prices = [];
      parse_csv(csv, function(row){dates.push(row[4]); prices.push(+row[6])});
      //console.log(dates)
      console.log(prices);
      //lines.push({x:dates,y:prices})
      //Plotly.newPlot(document.getElementById('chart'), lines)
  //     Plotly.newPlot(document.getElementById('chart'), [{x:dates,y:prices}])
      Plotly.addTraces(chart, {x:dates,y:prices,name:name});
      align_chart();
   })
}
