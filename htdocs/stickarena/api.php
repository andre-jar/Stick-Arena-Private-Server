<?php
require_once('./stick_arena.php');

$missing_args = "<rsp stat=\"fail\">\n\t<err code=\"3\" msg=\"Missing required arguments\"/>\n</rsp>";
$failed_login = "<rsp stat=\"fail\">\n\t<err code=\"98\" msg=\"Login failed\" />\n</rsp>";
// should maps also be retrieved from xgen servers if there is no user on local server registered?
$getMapsXGenServer = true;

global $db;
global $method;
global $response;
global $missing_args;
global $failed_login;
global $getMapsXGenServer;

if(isset($_REQUEST['method']))
{
	$method = $_REQUEST['method'];
} else {
    $method = "";
}
$response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

function httpPost($url)
{
    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($curl);
    curl_close($curl);
    return $response;
}

if($method == "xgen.stickarena.maps.list")
{
  if(!isset($_REQUEST['username'])) {
     $response .= $missing_args;
  } else {
	$response .= "<rsp stat=\"ok\">\n"."\t<maps>\n";
    $username = $_REQUEST['username'];
	$resultuid = mysqli_execute_query($db, "SELECT UID FROM `users` WHERE USERname=?", [$username]);
	if(isset($resultuid) && mysqli_num_rows($resultuid)>0) {
		$row = mysqli_fetch_row($resultuid);
		$uid = $row[0];
		$resultMaps = mysqli_execute_query($db, "SELECT slot_id,name FROM maps WHERE userid=? ORDER BY slot_id ASC", [$uid]);
		while ($currentRow = mysqli_fetch_row($resultMaps)) {
			$response .= "\t\t<map slot_id=\"".$currentRow[0]."\">\n\t\t\t<name>".$currentRow[1]."</name>\n\t\t</map>\n";
        }
	} else {
		if($getMapsXGenServer) {
			$responseHttp = httpPost("http://api.xgenstudios.com/?method=xgen.stickarena.maps.list&username=".$username);
			if($responseHttp && !empty($responseHttp)) {
				$response = $responseHttp;
			} else {
				$response .= " \t</maps>\n</rsp>";
			}
		} else {
			$response .= " \t</maps>\n</rsp>";
		}
	}
  }  
} else if($method == "xgen.stickarena.maps.get") 
{
  if(!isset($_REQUEST['username']) || !isset($_REQUEST['slot_id'])) {
     $response .= $missing_args;
  } else {
	$username = $_REQUEST['username'];
	$slot_id = $_REQUEST['slot_id'];
	$resultuid =  mysqli_execute_query($db, "SELECT uid FROM `users` WHERE username=?", [$username]);
    	if(isset($resultuid) && mysqli_num_rows($resultuid)>0) {
			$rowUID = mysqli_fetch_row($resultuid);
			$uid = $rowUID[0]; 
			$resultMap = mysqli_execute_query($db, "SELECT mapdata,name FROM maps WHERE userid=? and slot_id=?", [$uid, $slot_id]);
		    if(isset($resultMap) && mysqli_num_rows($resultMap)>0) {
				$row = mysqli_fetch_row($resultMap);
				$mapdata = $row[0];
				$name = $row[1];
			    $response .= "<rsp stat=\"ok\">\n\t<maps>\n\t\t<map>\n\t\t\t<name>".$name."</name>\n\t\t\t<data>".$mapdata."</data>\n\t\t</map>\n\t</maps>\n</rsp>";
			} else {
				$response .= "<rsp stat=\"fail\">\n\t<err code=\"4\" msg=\"Map not found\"/>\n</rsp>";
			}
		} else {
		    if($getMapsXGenServer) {
				$responseHttp = httpPost("http://api.xgenstudios.com/?method=xgen.stickarena.maps.get&username=".$username."&slot_id=".$slot_id);
				if($responseHttp && !empty($responseHttp)) {
					$response = $responseHttp;
				} else {
					$response .= " \t</maps>\n</rsp>";
				}
			} else {
				$response .= "<rsp stat=\"fail\">\n\t<err code=\"4\" msg=\"Map not found\"/>\n</rsp>";
			}
		}
  }
} else if($method == "xgen.stickarena.maps.save") 
{
	if(!isset($_REQUEST['username']) || !isset($_REQUEST['password']) || !isset($_REQUEST['slot_id']) || !isset($_REQUEST['name'])) {
	   $response .= $missing_args;
	} else {
		if(empty($_POST['data'])) {
		   $response .= "<rsp stat=\"fail\">\n\t<err code=\"4\" msg=\"Missing map data\" />\n</rsp>";
		} else {
		   $username = $_REQUEST['username'];
		   $password = $_REQUEST['password'];
		   $resultuid = mysqli_execute_query($db, "SELECT uid,labpass FROM `users` WHERE username=? AND userpass=?", [$username, md5($password)]);
		   if(isset($resultuid) && mysqli_num_rows($resultuid)>0) {
			    $row = mysqli_fetch_row($resultuid);
				$uid = $row[0];
				$labpass = $row[1];
				$maxMapSlotIDResult = mysqli_execute_query($db, "SELECT COUNT(id) AS maxMapSlotID FROM inventory WHERE userid=? AND itemid=240", [$uid]);
				$row2 = mysqli_fetch_row($maxMapSlotIDResult);
				$maxMapSlotID = $row2[0];
				$slot_id = $_REQUEST['slot_id'];
				if($labpass!=1 || (is_int($slot_id) && $slot_id > $maxMapSlotID)) {
					$response .= "<rsp stat=\"fail\">\n\t<err code=\"5\" msg=\"Invalid map slot\" />\n</rsp>";
				} else {
				    $response .= "<rsp stat=\"ok\" />";
					$data = $_POST['data'];
					$name = $_REQUEST['name'];
					$checkExisting = mysqli_execute_query($db, "SELECT slot_id FROM maps WHERE userid=? AND slot_id=?", [$uid, $slot_id]);
					if(isset($checkExisting) && mysqli_num_rows($checkExisting)>0) {
						$insertMap = mysqli_execute_query($db, "UPDATE maps SET name=?, mapdata=? WHERE userid=? AND slot_id=?", [$name, $data, $uid, $slot_id]);
					} else {
						$insertMap = mysqli_execute_query($db, "INSERT INTO maps (userid,slot_id,name,mapdata) VALUES(?,?,?,?)", [$uid, $slot_id, $name, $data]); 
					}
				}
		   } else {
		        $response .= $failed_login;
		   }
		}
	}
} else if($method=="xgen.users.addEmail") 
{
	if(!isset($_REQUEST['username']) || !isset($_REQUEST['password']) || !isset($_REQUEST['email']))
	{
		$response .= $missing_args;
	} else {
	    $username = $_REQUEST['username'];
		$password = $_REQUEST['password'];
		$resultVerified = mysqli_execute_query($db, "SELECT verified,uid FROM `users` WHERE username=? AND userpass=?", [$username, md5($password)]);
		if(isset($resultVerified) && mysqli_num_rows($resultVerified)>0) {
		   	$row = mysqli_fetch_row($resultVerified);
			$verified = $row[0];
			$uid = $row[1];
			if($verified == 1) {
			    $response .= "<rsp stat=\"fail\">\n\t<err code=\"5\" msg=\"Account is already verified\"/>\n</rsp> ";
			} else {
				$email = $_REQUEST['email'];
				$resultEmail = mysqli_execute_query($db,"SELECT email_address FROM users WHERE verified=1 AND email_address=?",[$email]);
				if(isset($resultEmail) && mysqli_num_rows($resultEmail)>0) {
					$response .= "<rsp stat=\"fail\">\n\t<err code=\"4\" msg=\"E-mail address already in use\"/>\n</rsp>";
				} else {
					$randomkey= bin2hex(random_bytes(16));
					$insertMail = mysqli_execute_query($db, "UPDATE users SET email_address=? WHERE uid=?", [$email,$uid]);
				    $milliseconds = floor(microtime(true) * 1000) + 300000;
			        $headers[] = 'MIME-Version: 1.0';
                    $headers[] = 'Content-type: text/html; charset=iso-8859-1';
                    // additional headers
                    $headers[] = $emailFromHeader;
				    $subject = "StickEMU  Email Verification";
				    $message = "<p>Hi ".$username.",</p><p>Thanks for adding an e-mail address to your XGen account. <a href=\"http://127.0.01/stickarena/verifyEmail.php?uid=".$uid."&amp;key=".$randomkey."\" target=\"_blank\">Click here to complete verification and receive your 500 Cred!</a></p><p>Cheers,<br/>Team XGen</p>";
				    mail($email, $subject, $message ,implode("\r\n", $headers));
			        $removePreExistingVerifications = mysqli_execute_query($db, "DELETE FROM `pending_verifications` WHERE userid=? AND verificationtype=0",[$uid]);
				    $resultGenKey = mysqli_execute_query($db, "INSERT INTO `pending_verifications` (userid,validationkey,verificationtype,expirydate) VALUES (?,?,0,?)", [$uid, $randomkey, $milliseconds]);
				    // send mail, create verify key on database, remove any preexisting verify keys
					$response .= "<rsp stat=\"ok\"/>";
				}
			}
		} else {
			$response .= $failed_login;
		}			
	}
	
} else if($method=="xgen.users.authenticate") 
{
	if(!isset($_REQUEST['username']) || !isset($_REQUEST['password']))
	{
		$response .= $missing_args;
	} else {
		$username = $_REQUEST['username'];
		$password = $_REQUEST['password'];
		$userdata = mysqli_execute_query($db, "SELECT uid,username,ban,user_level FROM users WHERE username=? AND userpass=?",[$username,md5($password)]);
	    if(isset($userdata) && mysqli_num_rows($userdata)>0) {
			$row = mysqli_fetch_row($userdata);
			$uid = $row[0];
			$username = $row[1];
			$perms = 0;
			if($row[2]==1) {
				$perms=-1;
			} else {
				$perms=$row[3];
			}
			$response .= "<rsp stat=\"ok\">\n\t<user id=\"".$uid."\">\n\t\t<username>".$username."</username>\n\t\t<perms>".$perms."</perms>\n\t\t<points>0</points>\n\t</user>\n</rsp>";
		} else {
			$response .= $failed_login;
		}
	}
	
} else if($method=="xgen.users.changeName") 
{
	if(!isset($_REQUEST['username']) || !isset($_REQUEST['password']) || !isset($_REQUEST['new_username']))
	{
		$response .= $missing_args;
	} else {
		$username = $_REQUEST['username'];
		$password = $_REQUEST['password'];
		$verifiedQuery = mysqli_execute_query($db, "SELECT verified,user_level FROM users WHERE username=? userpass=?", [$username,md5($password)]);
		if(isset($verifiedQuery) && mysqli_num_rows($verifiedQuery)>0) {
			$row = mysqli_fetch_row($verifiedQuery);
			$verified = $row[0];
			$user_level = $row[1];
			if($verified == 1) {
				if($user_level>1) {
					$new_username = $_REQUEST['new_username'];
					$response .= "<rsp stat=\"ok\"/>";
					if(isValidStrings([$new_username])) {
						$alreadyExisting = mysqli_execute_query($db, "SELECT username FROM users WHERE username=?",[$new_username]);
						if(isset($alreadyExisting) && mysqli_num_rows($alreadyExisting)>0) {
							// message does not exist on original api
							$response .= "<rsp stat=\"fail\">\n\t<err code=\"\" msg=\"Username already exists\"/>\n</rsp>";
						} else {
							$renameResult = mysqli_execute_query($db, "UPDATE users SET username=? WHERE username=? userpass=?", [$new_username,$username,md5($password)]);
						}
					}
				} else {
					$response .= "<rsp stat=\"fail\">\n\t<err code=\"6\" msg=\"Username change not allowed for this account\"/>\n</rsp>";
				}
			} else {
				$response .= "<rsp stat=\"fail\">\n\t<err code=\"7\" msg=\"Username change not allowed for non-email verified accounts\"/>\n</rsp>";
			}
		} else {
			$response .= $failed_login;
		}
	}
} else if($method=="xgen.users.changePassword") 
{
	if(!isset($_REQUEST['username']) || !isset($_REQUEST['password']) || !isset($_REQUEST['new_password']))
	{
		$response .= $missing_args;
	} else {
		$username = $_REQUEST['username'];
		$password = $_REQUEST['password'];
		$new_password = $_REQUEST['new_password'];
		$exists = mysqli_execute_query($db, "SELECT uid FROM users WHERE username=? userpass=?", [$username,md5($password)]);
		if(isset($exists) && mysqli_num_rows($exists)>0) {
			$response .= "<rsp stat=\"ok\"/>";
			$changePWResult = mysqli_execute_query($db, "UPDATE users SET userpass=? WHERE username=? userpass=?", [md5($new_password),$username,$password]);
		} else {
			$response .= $failed_login;
		}
	}
//} else if($method=="xgen.users.items.list") 
//{
// 		arguments: game_id, username
// } else if($method=="xgen.stats.get") 
//{
// 		arguments: username	
} else if($method=="xgen.stickarena.stats.get") 
{
	if(!isset($_REQUEST['username']))
	{
		$response .= $missing_args;
	} else {
		$statsQuery = mysqli_execute_query($db, "SELECT username,user_level,ban,uid,wins,losses,kills,deaths,rounds FROM users WHERE username=?", [$_REQUEST['username']]);
		if(isset($statsQuery) && mysqli_num_rows($statsQuery)>0) {
			$row = mysqli_fetch_row($statsQuery);
			$displayedname = $row[0];
			$ballistick = "0";
			$id = $row[3];
			$wins = $row[4];
			$losses = $row[5];
			$kills = $row[6];
			$deaths = $row[7]; 
			$rounds = $row[8];
			$perms = 0;
			if($row[2]==1) {
				$perms=-1;
			} else {
				$perms=$row[1];
			}
			$response .= "<rsp stat=\"ok\">\n\t<stats>\n\t\t<game id=\"stickarena\">\n\t\t\t<user username=\"".$displayedname."\" perms=\"".$perms."\" id=\"".$id."\">\n\t\t\t\t<stat id=\"wins\">".$wins."</stat>\n\t\t\t\t<stat id=\"losses\">".$losses."</stat>\n\t\t\t\t<stat id=\"kills\">".$kills."</stat>\n\t\t\t\t<stat id=\"deaths\">".$deaths."</stat>\n\t\t\t\t<stat id=\"rounds\">".$rounds."</stat>\n\t\t\t\t<stat id=\"ballistick\">0</stat>\n\t\t\t</user>\n\t\t</game>\n\t</stats>\n</rsp>";
		} else {
			$response .= "<rsp stat=\"ok\">\n\t<stats>\n\t\t<game id=\"stickarena\">\n\t\t\t<user username=\"\" perms=\"\" id=\"\">\n\t\t\t\t<stat id=\"wins\"/>\n\t\t\t\t<stat id=\"losses\"/>\n\t\t\t\t<stat id=\"kills\"/>\n\t\t\t\t<stat id=\"deaths\"/>\n\t\t\t\t<stat id=\"rounds\"/>\n\t\t\t\t<stat id=\"ballistick\"/>\n\t\t\t</user>\n\t\t</game>\n\t</stats>\n</rsp>";
	    }
	}
	
//} else if($method=="xgen.stickarena.kongregate.purchase") 
//{	
// 		arguments username, key, api_key, kongregate_username, game_auth_token
//} else if($method=="xgen.stickarena.kongregate.authenticate") 
//{   
// 		arguments: username, game_auth_token	
} else {
  $response .= "<rsp stat=\"fail\">\n";
  $response .= "<err code=\"112\" msg=\"Method &quot;".$method."&quot; not found\"/>\n";
  $response .= "</rsp>";
}

 header("Content-type: text/xml; charset=utf-8");
 echo $response;

  
  ?>