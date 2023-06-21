<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Stick Arena - Email Verification">
  <meta name="keywords" content="Stick,Arena,MMO,MMORPG,Browser,Game">
  <title>BallistickEMU - Email Verficiations</title>
  <link rel="stylesheet" type="text/css" href="/styles/default.css">
  <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />  
</head>

<?php
require_once('../stick_arena.php');

if(isset($_POST['uid'])){
	$uid=$_POST['uid'];
}
if(isset($_POST['key'])){
	$key=$_POST['key'];
}

global $key;
global $uid;

function validate() {
if(isset($key) && isset($uid)) {
	   $resultVerified = mysqli_execute_query($db, "SELECT * FROM `users` WHERE uid=? AND verified=1", [$uid]);
	   if($resultVerified) 
	   {
		   // already verified
		   return "Email verification failed, please try again.";
	   }
	
	   $milliseconds = floor(microtime(true) * 1000);
       $resultValidation = mysqli_execute_query($db, "SELECT * FROM `pending_verifications` WHERE uid=? AND validationkey=? AND verification_type=0 ORDER BY id DESC LIMIT 1", [$uid, $key]);
	   if(!$resultValidation) {
	        // no matching key found for process 
	       return "Email verification failed, please try again.";
	   } 
	     foreach ($resultValidation as $k => $entry) {
		     if($entry['expirydate']<$milliseconds) {
                // link expired
                return "Email verification failed, please try again.";
			 }	else {
 				// key and uid is ok and not expired 
	              $resultValidation = mysqli_execute_query($db, "DELETE FROM `pending_verifications` WHERE uid=? AND validationkey=? AND verification_type=0", [$uid, $key]);
				  $updateVerifiedQuery = mysqli_execute_query($db,"UPDATE `users` SET verified=1 cred=cred+500 WHERE uid=?", [$uid]);
				  return "Email verified successfully. Please log out and back in to receive your Cred.";
             }			 
		 }
       }
		 // missing params
		 return "Email verification failed, please try again.";
}
?>
 
<body> 
<div id="content">
            <div style="float: left; margin: 22px 40px;">
                <div style="float: left; width: 810px; padding: 35px; background: #fff; box-shadow: 0px 2px 3px #98a0aa;">

			<h1>Email Verification</h1>
			<p><?php echo validate();  ?></p>

                </div>
            </div>
        </div>
</body>
</html>
