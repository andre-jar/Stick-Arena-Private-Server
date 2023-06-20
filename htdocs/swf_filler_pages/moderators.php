<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

<head>
	<title>StickEMU - Moderator</title>
	<link rel="stylesheet" type="text/css" href="/styles/default.css" />
</head>

<body>


	<div id="content">

        <div class="box-756">
                <div class="box-content-top"><img src="/images/box-full-top.gif" /></div>
                <div class="box-756-content" style="text-align: center;">
                  <h1>Moderator</h1>
				  <p>Trusted players can get promoted to a moderator.</p>
				  <p>The current moderators are as follows:</p>
				  <ul style="display: table;margin: 0 auto;">
				  <?php echo currentMods(); ?>
				  </ul></div>
                <div><img src="/images/box-full-bot.gif" /></div>
        </div>

	<div style="clear: both;"></div>

	</div>

	</div>
</body>
</html>	

<?php 
  global $db;

  function currentMods()
  {
	  include 'stick_arena.php';

	  $html = "";
	  $modsResult = mysqli_query($db, "SELECT username FROM users WHERE user_level>0");
	  while ($row = mysqli_fetch_row($modsResult)) {
		$html .= "<li><font color=\"red\">".$row[0]."</font></li>";
	}
	return $html;
  }
?>