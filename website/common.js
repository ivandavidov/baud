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

layout.title = 'Статистика за ' + o.f + ' от ' + o.e[0].d + ' до ' + o.e[o.e.length - 1].d
document.title = layout.title;

var plotlyBar = {
  displayModeBar: true,
  displaylogo: false
}

for(var i = 0; i < o.b.length; i++) {
  trace1.x.push(o.e[i].d);
  trace1.y.push(o.e[i].s);
}

for(var i = 0; i < o.e.length; i++) {
  trace2.x.push(o.e[i].d);
  trace2.y.push(o.e[i].p);
}

function replot() {
  Plotly.newPlot('linesDiv', data, layout, plotlyBar);
}
