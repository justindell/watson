function addPlayers(team) {
  $('#recommended').empty();
  $.each(team, function(index, player) {
    contents = player['position'] + ' ' + player['name'] + ' (' + player['team'] + ')';
    $('#recommended').append('<li>' + contents + '</li>');
  });
}

$(function() {
  $('table').tablesorter();

  $('#go').click(function() {
    goBtn = $(this);
    goBtn.attr('disabled', 'disabled');
    $.get('/api/recommend', function(data) {
      team = JSON.parse(data);
      addPlayers(team);
      goBtn.removeAttr('disabled');
    });
  });

  var colorized = false;
  $('#colorize').click(function() {
    if(colorized) {
      $('#player-table > tbody > tr').attr('style', '');
      $(this).text('Colorize');
    } else {
      $('#player-table > tbody > tr').heatcolor(function() { return $("td:nth-child(5)",this).text(); });
      $(this).text('Uncolorize');
    }
    colorized = !colorized;
  });
});
