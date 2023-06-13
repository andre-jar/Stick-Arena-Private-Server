<?php
//My login Script
// mysql connection variables
require_once('./class.rc4crypt.php');
$host = 'localhost';
$dbuser = 'root';
$dbpass = '';
$dbname = 'ballistickemu';
$table = 'users';
//
// connect to db
$db = mysqli_connect($host,$dbuser,$dbpass, $dbname) or die("result=error");
if(!$db)
{
print "result=error";
exit;
}
// Email to send from for verifications.
$emailFromHeader = 'From: StickEMU <no-reply@localhost.com>';

// declare variables
if(isset($_POST['username'])){
	$username=sanitize($_POST['username']);
}
if(isset($_POST['userpass'])){
	$password=sanitize(md5($_POST['userpass']));
}
if(isset($_POST['action'])){
	$action=sanitize($_POST['action']);
}
if(isset($_POST['usercol'])){
	$usercol=sanitize($_POST['usercol']);
}
if(isset($_POST['stats'])){
	$stats=$_POST['stats'];
}

if($action=="authenticate")
{
//
  // check table
   $query = mysqli_query($db, "SELECT * FROM $table WHERE USERname = '$username' AND USERpass = '$password'");
   $num = mysqli_num_rows($query);
   if($num>0)
   {
	  while ($row = mysqli_fetch_array($query, MYSQL_BOTH)) {
			if($row["ban"] == 1)
			{
				echo "result=banned";
				exit;
			}
		  printf("result=success&usercol=%s", colstring($row["red"]).colstring($row["green"]).colstring($row["blue"]));
	  }
   } else {
      print "result=error";
   }
}

if($action=="player_stats")
{
         $query = mysqli_query($db, "SELECT * FROM users WHERE USERname = '$username'");
         while ($row = mysqli_fetch_array($query, MYSQL_BOTH)) {
               printf ("rounds=%s&wins=%s&losses=%s&kills=%s&deaths=%s&user_level=%s&result=success", $row["rounds"], $row["wins"], $row["losses"], $row["kills"], $row["deaths"], $row["user_level"]);
         }
}

if($action=="create")
{
	if($usercol == "000000000")
		$usercol = "000000001";

	$colour = str_split($usercol, 3);
	$querystring = sprintf("INSERT INTO `users` (USERname, USERpass, red, green, blue) VALUES('%s','%s','%s','%s','%s')", $username, $password, $colour[0], $colour[1], $colour[2]);
	$result = mysqli_query($db, $querystring);
	

	if (!$result) {
		$message  = 'result=error';
		die($message);
	}
	echo "result=success";
}

if($action=="start_round")
{
	echo "result=success";
}

if($action=="round_stats")
{
	//$ = rc4Encrypt(hex2bin($stats), "8fJ3Ki8Fy6rX1l0J"); 
	$stats_decrypted = rc4crypt::decrypt("8fJ3Ki8Fy6rX1l0J", hex2bin($stats)); // Assuming the key is binary (what you typed)
	$kills = get_string_between($stats_decrypted, "KILLS=", "&DE");
	$deaths = sanitize(get_string_between($stats_decrypted, "DEATHS=", "&ROUNDSP"));

	if($kills > 50)
		$kills = 0;
	
	if($deaths < 0)
		$deaths = 0;

	$kills = sanitize($kills);
	$deaths = sanitize($deaths);
	
	$roundsplayed = sanitize(get_string_between($stats_decrypted, "PLAYED=", "&WIN"));
	$winner = get_string_between($stats_decrypted, "WINNER=", "X");

	if($winner == "1")
	{
		$wins = "1";
		$losses = "0";
	} else if ($winner == "0")
	{
		$wins = "0";
		$losses = "1";
	}

$querystring = sprintf("UPDATE USERS set `kills` = `kills` + '%s', `deaths` = `deaths` + '%s', `rounds` = `rounds` + '%s', `wins` = `wins` + '%s', `losses` = `losses` + '%s' WHERE `USERname` = '%s' AND `USERpass` = '%s'", $kills, $deaths, $roundsplayed, $wins, $losses, $username, $password);

$result = mysqli_query($db, $querystring);
	if (!$result) {
		$message  = 'result=error';
		die($message);
	}
	echo "result=success";
}

  
//------------------------------------------------------------------------------
//Functions
function colstring($col)
{
	return str_pad($col, 3, "0", STR_PAD_LEFT);
}

function cleanInput($input) {
 
$search = array(
    '@<script[^>]*?>.*?</script>@si',   // Strip out javascript
    '@<[\/\!]*?[^<>]*?>@si',            // Strip out HTML tags
    '@<style[^>]*?>.*?</style>@siU',    // Strip style tags properly
    '@<![\s\S]*?--[ \t\n\r]*>@'         // Strip multi-line comments
);
 
    $output = preg_replace($search, '', $input);
    return $output;
}

function sanitize($input) {

    if (is_array($input)) {
        foreach($input as $var=>$val) {
            $output[$var] = sanitize($val);
        }
    }
    else {
        $input  = cleanInput($input);
        $output = mysqli_real_escape_string($db, $input);
    }
    return $output;
}




function get_string_between($string, $start, $end){ 
    $string = " ".$string; 
    $ini = strpos($string,$start); 
    if ($ini == 0) return ""; 
    $ini += strlen($start); 
    $len = strpos($string,$end,$ini) - $ini; 
    return substr($string,$ini,$len); 
}


?>
