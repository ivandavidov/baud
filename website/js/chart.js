var trace1 = {
  mode: 'lines+markers',
  name: 'Дялове',
  x: [], y: []
};

var trace2 = {
  mode: 'lines+markers',
  name: 'Цена',
  yaxis: 'y2',
  x: [], y: []
};

var data = [trace1, trace2];

var layout = {
  title: 'Договорен Фонд',
  titlefont: {
      size: 22
  },
  xaxis: {
    title: 'Дата',
    titlefont: {
      size: 18
    }
  },
  yaxis: {
    title: 'Дялове',
    titlefont: {
      size: 18
    }
  },
  yaxis2: {
    title: 'Цена',
    titlefont: {
      size: 18
    },
    overlaying: 'y',
    side: 'right',
    showgrid: false
  },
  showlegend: true,
  legend: {
    orientation: 'h',
    x: 0.01,
    y: -0.09
  }
}

var plotlyBar = {
  displayModeBar: true,
  displaylogo: false
}

function draw() {
  var linesDiv = document.getElementById('linesDiv');
  
  linesDiv.style.width = '100%';
  linesDiv.style.height = '99%';  
  linesDiv.style.borderStyle = 'groove';
  linesDiv.style.borderWidth = '1px';  

  for(var i = 0; i < o.b.length; i++) {
    trace1.x.push(o.e[i].d);
    trace1.y.push(o.e[i].s);
  }

  for(var i = 0; i < o.e.length; i++) {
    trace2.x.push(o.e[i].d);
    trace2.y.push(o.e[i].p);
  }

  layout.title = o.f + ' от ' + o.e[0].d + ' до ' + o.e[o.e.length - 1].d
  document.title = layout.title;
  
  Plotly.newPlot(linesDiv, data, layout, plotlyBar);
}

function replot(dataDir) {
  var search = document.location.href;
  var jsPart = search.split('?')[1];
  var jsFile = dataDir + '/' + jsPart + '.js';
  
  var script = document.createElement('script');  
  script.onload = draw;
  script.src = jsFile;
  
  document.body.appendChild(script);
}
