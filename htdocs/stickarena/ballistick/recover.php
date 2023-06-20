<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

<head>
	<title>XGen Studios - Online Flash Games - Recover Account</title>
	<link rel="stylesheet" type="text/css" href="/styles/default.css" />
        <link rel="shortcut icon" href="/images/favicon.ico" type="image/x-icon" />
        <meta name="Description" content="Free Online Flash Games at XGen Studios - Play Stick Arena, Motherload, StickRPG, Defend Your Castle, Fishy, and More!" />
        <meta name="Keywords" content="Free, Online, Flash, Games, XGen, Studios, Stick Arena, StickRPG, Stick, RPG, Defend, Your, Castle, Defend Your Castle, Motherload, Fishy, XGenStudios, XGen Studios, StickRPG 2, Cinderfall" />
</head>

<body>


	<div id="content">

        <div class="box-756">
                <div class="box-content-top"><img src="/images/box-full-top.gif" /></div>
                <div class="box-756-content" style="text-align: center;">
                 <?php     echo recover();                ?>
                <div><img src="/images/box-full-bot.gif" /></div>
        </div>

	<div style="clear: both;"></div>

	</div>

</body>
</html>	
<?php

function recover() {
global $db;
include '../stick_arena.php';
if(isset($_GET['uid'])){
	$uid=$_GET['uid'];
}
if(isset($_GET['key'])){
	$key=$_GET['key'];
}
if(isset($_POST['email'])){
	$email=$_POST['email'];
}

if(isset($key) && isset($uid)) {
	   $resultVerified = mysqli_execute_query($db, "SELECT * FROM `users` WHERE uid=? AND verified=1", [$uid]);
	   if($resultVerified) 
	   {
		   	   $milliseconds = floor(microtime(true) * 1000);
	           $resultValidation = mysqli_execute_query($db, "SELECT * FROM `pending_verifications` WHERE userid=? AND validationkey=? AND verificationtype=1 ORDER BY id DESC LIMIT 1", [$uid, $key]);
	           if($resultValidation) {
	           foreach ($resultValidation as $k => $entry) {
		        if($entry['expirydate']>$milliseconds) {
 				// key and uid is ok and not expired 
	              $resultValidation = mysqli_execute_query($db, "DELETE FROM `pending_verifications` WHERE userid=? AND verificationtype=1", [$uid]);
				  $pw= bin2hex(random_bytes(4));
				  $resultPWRecovery = mysqli_execute_query($db, "UPDATE `users` SET USERpass=? WHERE uid=?", [md5($pw), $uid]);
				  return "<p>Your password has been reset. Your new temporary password is ".$pw.". Please log in and change it.</p>";
			 } else {
				 // expired validation
	              $resultValidation = mysqli_execute_query($db, "DELETE FROM `pending_verifications` WHERE userid=? AND verificationtype=1", [$uid]);
			 }				 
		 }
	   } 
	   }
       } else if(!empty($email)) {
			  $resultValidation = mysqli_execute_query($db, "SELECT email_address,uid,username FROM `users` WHERE email_address=? AND verified=1 ORDER BY uid DESC LIMIT 1", [$email]);
			  if(isset($resultValidation) && mysqli_num_rows($resultValidation)>0) {
				  $randomkey= bin2hex(random_bytes(16));
				  $row = mysqli_fetch_row($resultValidation);
				  $username= $row[2];
				  $uid=$row[1];
				  $milliseconds = floor(microtime(true) * 1000) + 300000;
			      $headers[] = 'MIME-Version: 1.0';
                  $headers[] = 'Content-type: text/html; charset=iso-8859-1';
                  // additional headers
                  $headers[] = $emailFromHeader;
				  $subject = "StickEMU Account Password Reset";
				  $message = "<p>You&#39;ve received this e-mail as a result of a password reset request from ".$host." for the account: ".$username.". Click the link below to complete the password reset:</p><p><a href=\"http://".$host."/ballistick/recover.php?uid=".$uid."&amp;key=".$randomkey."\" target=\"_blank\">http://".$host."/ballistick/recover.php?uid=".$uid."amp;key=".$randomkey."</a></p>";
				  mail($email, $subject, $message ,implode("\r\n", $headers));
			      $removePreExistingVerifications = mysqli_execute_query($db, "DELETE FROM `pending_verifications` WHERE userid=? AND verificationtype=1",[$uid]);
				  $resultGenKey = mysqli_execute_query($db, "INSERT INTO `pending_verifications` (userid,validationkey,verificationtype,expirydate) VALUES (?,?,1,?)", [$uid, $randomkey, $milliseconds]);
				  // send mail, create verify key on database, remove any preexisting verifications
				return "<p>Check your e-mail for further instructions on how to reset your password.</p></div>";
			  } else {
			    return "<p>There is no account associated with that e-mail address.</p></div>";
			  }
		   		   
	   }
	   return "	 
       			<p>Enter the e-mail address linked to your account and we will e-mail you a link to reset your password.</p>
			<table style=\"text-align: left; margin: 0 auto;\">
			<form method=\"POST\" action=\"/ballistick/recover.php\">
			<tr>
				<td>E-mail Address</td>
				<td><input type=\"text\" name=\"email\" style=\"width: 140px;\" /></td>
			</tr>
			<tr>
				<td></td>
				<td><input type=\"submit\" value=\"Recover Account\" /></td>
			</tr>
			</form>
			</table>
		</div> 	"; 
}
?>