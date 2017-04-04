var trace1 = {
  mode: 'lines+markers',
  name: 'Начало',
  x: [], y: []
};

var trace2 = {
  mode: 'lines+markers',
  name: 'Край',
  x: [], y: []
};

var data = [trace1, trace2];
data = [trace2];

var layout = {
  title: 'Договорен Фонд',
  __xaxis: {
    title: 'Седмица'
  },
  __yaxis: {
    title: 'Дялове'
  },
  legend: {
    orientation: 'h'
  }
}

layout.title = 'Дялове на ' + o.f + ' от ' + o.e[0].d + ' до ' + o.e[o.e.length - 1].d
document.title = layout.title;

var plotlyBar = {
  displayModeBar: true
}

for(var i = 0; i < o.b.length; i++) {
  trace1.x.push(o.b[i].d);
  trace1.y.push(o.b[i].s);
}

for(var i = 0; i < o.e.length; i++) {
  trace2.x.push(o.e[i].d);
  trace2.y.push(o.e[i].s);
}
