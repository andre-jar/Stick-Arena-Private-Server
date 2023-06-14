<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Stick Arena Classic - Leaderboard">
  <meta name="keywords" content="Stick,Arena,MMO,MMORPG,Browser,Game">
  <title>BallistickEMU - Leaderboard</title>
  <link rel="stylesheet" type="text/css" href="/styles/default.css">
</head>

<?php
require_once('../stick_arena.php');

function getRankRow($kills)
{
	$rankImg = 'Rank0.gif';
	$rank = 0;
    if($kills >= 5) {  $rankImg = 'Rank1.gif'; $rank=1;  }
	if($kills >= 25) {  $rankImg = 'Rank2.gif'; $rank=2;  }
	if($kills >= 100) {  $rankImg = 'Rank3.gif'; $rank=3;  }
	if($kills >= 300) {  $rankImg = 'Rank4.gif'; $rank=4;  }
	if($kills >= 750) {  $rankImg = 'Rank5.gif'; $rank=5;  }
	if($kills >= 2000) {  $rankImg = 'Rank6.gif'; $rank=6;  }
	if($kills >= 5000) {  $rankImg = 'Rank7.gif'; $rank=7;  }
	if($kills >= 10000) {  $rankImg = 'Rank8.gif'; $rank=8;  }
	if($kills >= 20000) {  $rankImg = 'Rank9_x9ac.gif'; $rank=9;  }
	if($kills >= 40000) {  $rankImg = 'Rank10_p1oa.gif'; $rank=10;  }
	if($kills >= 60000) {  $rankImg = 'Rank11_kdj2.gif'; $rank=11;  }
	if($kills >= 80000) {  $rankImg = 'Rank12_12k3.gif'; $rank=12;  }
	if($kills >= 100000) {  $rankImg = 'Rank13_pl92.gif'; $rank=13;  }
	if($kills >= 125000) {  $rankImg = 'Rank14_ll2d.gif'; $rank=14;  }	
	if($kills >= 150000) {  $rankImg = 'Rank15_20ty.gif'; $rank=15;  }
	if($kills >= 1000000) {  $rankImg = 'Rank0.tif'; $rank=17;  }	
	$rankImg = '/images/'.$rankImg;
    $html = '<td align="center"><img src="'.$rankImg.'" alt="'.$rank.'"></td>';	
    return $html; 
}

function getLeaderboard() {
  global $db;
  $html = '
  <tr bgcolor="#4f95c4">
	<td style="font-weight: bold; color: white" align="center"></td>
    <td style="font-weight: bold; color: white" align="center">Rank</td>
    <td style="font-weight: bold; color: white">Player</td>
    <td style="font-weight: bold; color: white">Kills</td>
    <td style="font-weight: bold; color: white">Deaths</td>
    <td style="font-weight: bold; color: white">Wins</td>
    <td style="font-weight: bold; color: white">Losses</td>
  </tr>
  ';

  $data = mysqli_query($db, "SELECT username, kills, deaths, wins, losses,red,green,blue FROM users ORDER BY CAST(kills AS INTEGER) DESC LIMIT 100");	  

  foreach ($data as $key => $player) {
    $color = 'rgb('.$player['red'].' '.$player['green'].' '.$player['blue']. ')';
    $key += 1; // position
	$html .='<tr bgcolor="#D6EDFF">';
	$html .='<td style="border-left: 8px '.$color.' '.'solid;" align="center"><div style="font-size: 10pt; font-weight: bold;">'.$key.'</div></td>';
    $html .=getRankRow($player['kills']);
	$html .='<td><div style="font-weight: bold; color: '.$color.'; font-size: 11pt;">'.$player['username'].'</div></td>';
    $html .='<td><b>'.number_format($player['kills'], 0, '.', ',').'</b></td>';
    $html .='<td>'.number_format($player['deaths'], 0, '.', ',').'</td>';
    $html .='<td>'.number_format($player['wins'], 0, '.', ',').'</td>';
    $html .='<td>'.number_format($player['losses'], 0, '.', ',').'</td></tr>';
  }

  return $html;
}

?>
<body>
  <div id="content">
	<div class="box-583">
		<div class="box-header">
			<div><img src="/images/box-tl.gif"></div>
			<div class="box-583-header-content"><h1 class="box-header-content">Stick Arena High Scores</h1></div>
			<div><img src="/images/box-tr.gif"></div>
		</div>
		<div class="box-content-top"><img src="/images/box-large-top.gif"></div>
		<div class="box-content">
            <table cellpadding="8" align="center" width="568px">
				<tbody>
					<tr bgcolor="#4f95c4">
						<td colspan="7" style="font-weight: bold; color: white" align="center">Top 100 Stick Slayers</td>
                    </tr>
					<?php echo getLeaderboard(); ?>
				</tbody>
			</table>
		</div>	
	    <div><img src="/images/box-large-bot.gif"></div>
    </div>
    <div style="clear: both;"></div>
     Dont't like the old Style?
  <a href="highscoreNew.php">Click here</a>
  </div>
</body>

</html>
