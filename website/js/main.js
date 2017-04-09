function menu(jsPart) {
  var chart = document.getElementById('chart');
  chart.style.display = 'block';
  chart.src = 'fund.html?' + jsPart;
  return false;
}
