<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Stick Arena Classic - Leaderboard">
  <meta name="keywords" content="Stick,Arena,MMO,MMORPG,Browser,Game">
  <title>BallistickEMU - Leaderboard</title>
</head>

<?php
require_once('../stick_arena.php');

function getLeaderboard() {
  global $db;
  $user = '';
  $html = '<tr>';
  if(!isset($_POST['username'])) { 
  $html.='<td style="padding: 8px;color: #848990;"></td>';
  }
  $html.='<td style="padding: 8px;color: #848990;">Player</td>
  <td style="padding: 8px;color: #848990;">Kills</td>
  <td style="padding: 8px;color: #848990;">Deaths</td>
  <td style="padding: 8px;color: #848990;">Wins</td>
  <td style="padding: 8px;color: #848990;">Losses</td>
  </tr>
  ';

  if(isset($_POST['username'])) {
    $username = $_POST['username'];	  
    $data = mysqli_execute_query($db, "SELECT * FROM users WHERE USERname = ?", [$username]);
  } else {
     $data = mysqli_query($db, "SELECT USERname, kills, deaths, wins, losses FROM users ORDER BY CAST(kills AS INTEGER) DESC LIMIT 100");	  
  }	  
  $key=0;
  foreach ($data as $player) {
    $key += 1; // position
	if($key % 2 == 0) { 
	    $html .='<tr style="color: #848990;">';
	} else { 
		$html .='<tr style="background-color: #e3e6e9;color: #848990;">';
    }
	if(!isset($_POST['username'])) { 
	   $html .='<td style="padding: 8px; text-align: center;">'.$key.'</td>';
    }
	$html .='<td style="padding: 8px;">'.$player['USERname'].'</td>';
    $html .='<td style="padding: 8px;">'.number_format($player['kills'], 0, '.', ',').'</td>';
    $html .='<td style="padding: 8px;">'.number_format($player['deaths'], 0, '.', ',').'</td>';
    $html .='<td style="padding: 8px;">'.number_format($player['wins'], 0, '.', ',').'</td>';
    $html .='<td style="padding: 8px;">'.number_format($player['losses'], 0, '.', ',').'</td></tr>';
  }

  return $html;
}

?>

<body>
  <div style="width: 520px; margin: 0 auto; background: #fff; box-shadow: 0px 2px 3px #98a0aa;font-family: 'PT Sans', Arial, Helvetica, sans-serif;font-size: 10pt;">
    <h1><center>BallistickEMU - Leaderboard</h1></center>
    <table cellspacing="0" cellpadding="0" align="center" style="width: 520px;">
	  	<tr>
			<td colspan="6">
				<div><a href="/dimensions.swf"><img src="/images/Season_SA_Banner.png" alt="Stick Arena - Competitive Season" /></a></div>
				<div style="float: left; width: 217px; margin-top: 20px; margin-left: 20px;"><img src="/images/Season1_SA_Trophies.png" /></div>
				<div style="float: left; width: 240px; margin-left: 33px;">
					<h2 style="color: #344357;">Play Stick Arena and Win a Lab Pass!</h2>
					<p><form method="post" action="highscoreNew.php" style="display: inline;"><input type="text" name="username" value="Player search..." style="width: 200px; color: #ccc; border: 1px #ccc solid;" onfocus="if(this.value=='Player search...') this.value='';" /><input type="submit" value="Go" style="background: none; border: 1px #ccc solid; border-left: none;" /></form></p>
				</div>
			</td>
		</tr>
      <?php echo getLeaderboard(); ?>
    </table>
  </div>
</body>

</html>
