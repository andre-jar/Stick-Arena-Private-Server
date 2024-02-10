<?php
//My login Script
// mysql connection variables
require_once('./class.rc4crypt.php');
$host = '127.0.0.1';
$dbuser = 'root';
$dbpass = '123';
$dbname = 'ballistickemu';
$table = 'users';
//
// connect to db
$db = new mysqli($host,$dbuser,$dbpass, $dbname, 3306) or die("result=error");
if(!$db)
{
print "result=error";
exit;
}
// Email to send from for verifications.
$emailFromHeader = 'From: StickEMU <no-reply@localhost.com>';

global $action;

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
if(isset($_POST['email_address'])) {
	$email_address=$_POST['email_address']; 
}

if($action=="authenticate")
{
    global $db;
  // check table
   $query = mysqli_execute_query($db, "SELECT * FROM ? WHERE USERname = ? AND USERpass = ?", [$table, $username, $userpass]);
   $milliseconds = floor(microtime(true) * 1000);
   if(isset($query) && mysqli_num_rows($query)>0)
   {
	  while ($row = mysqli_fetch_array($query, MYSQLI_BOTH)) {
			if($row["ban"] == 1)
			{
				$uid = $row['UID'];
				$banrecord = mysqli_execute_query($db, "SELECT id, enddate FROM bans WHERE userid = ? ORDER BY id DESC LIMIT 1", [$uid]);
				while($banrow = mysqli_fetch_array($banrecord, MYSQLI_BOTH)) {
				   if($banrow['enddate'] < floor($milliseconds)) {
					   $res = mysqli_execute_query($db, "UPDATE users SET ban = 0 WHERE Username = ?", [$username]);
				   } else {
					 echo "result=banned";
				     exit;
				   }
				}   
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
	global $db;
	global $username;
	global $userpass;
	global $colour;
	global $email_address;
	if($usercol == "000000000")
		$usercol = "000000001";
	$milliseconds = floor(microtime(true) * 1000);

	if (!isValidStrings([$username])) {
		$message  = 'result=error';
		die($message);
	}	
	
	$colour = str_split($usercol, 3);
	$result = null;
	if(isset($email_address)) {
	   $resultemail = mysqli_execute_query($db, "SELECT email_address FROM `users` WHERE email_address=?", [$email_address]);
	   if(isset($resultemail) && mysqli_num_rows($resultemail)!=0) {
	       $message = 'result=email_duplicate';
		   die($message);
	   }	
	   $result = mysqli_execute_query($db, "INSERT INTO `users` (USERname, USERpass, red, green, blue, cash, email_address, creationdate) VALUES(?,?,?,?,?,?,?,?)", [$username, $password, $colour[0], $colour[1], $colour[2], 0, $email_address, $milliseconds]);
	} else {
	   $result = mysqli_execute_query($db, "INSERT INTO `users` (USERname, USERpass, red, green, blue, creationdate) VALUES(?,?,?,?,?,?)", [$username, $password, $colour[0], $colour[1], $colour[2], $milliseconds]);
	}
	if (!$result) {
		$message  = 'result=error';
		die($message);
	}
	
	$data = mysqli_execute_query($db, "SELECT UID FROM `users` WHERE USERname = ? LIMIT 1", [$username]);
	$id = mysqli_fetch_array($data);
    $result2 = mysqli_execute_query($db, "INSERT INTO `inventory` (id,userid, itemid, itemtype, red1, green1, blue1, red2, blue2, green2, selected) VALUES(DEFAULT,?,100,1,?,?,?,?,?,?,1)", [$id['UID'], $colour[0], $colour[1], $colour[2],$colour[0], $colour[1], $colour[2]]);	
	$result3 = mysqli_execute_query($db, "INSERT INTO `inventory` (id,userid, itemid, itemtype, red1, green1, blue1, red2, blue2, green2, selected) VALUES(DEFAULT,?,200,2,?,?,?,?,?,?,1)", [$id['UID'], $colour[0], $colour[1], $colour[2],$colour[0], $colour[1], $colour[2]]);
	if (!$result2 || !$result3) {
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
	global $db;
	global $stats;
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

	$result = mysqli_execute_query($db, "UPDATE USERS set `kills` = `kills` + ?, `deaths` = `deaths` + ?, `rounds` = `rounds` + ?, `wins` = `wins` + ?, `losses` = `losses` + ? WHERE `USERname` = ? AND `USERpass` = ?", [$kills, $deaths, $roundsplayed, $wins, $losses, $username, $password]);
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
	global $db;
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

function isValidStrings($keys) {
  if(!isset($keys)) {
	return false;
  }
  for ($i = 0; $i < count($keys); $i++) {
    if (!isset($keys[$i]) || !preg_match('/^[a-zA-Z0-9.,]{3,20}+$/', $keys[$i])) {
      return false;
    }
  }
  return true;
}
?>
