<!DOCTYPE html>
<?php
		
function changePassword()
{
	global $db;
	include '../stick_arena.php';
	if(isset($_POST['manageuser'])) {
		$username = $_POST['manageuser'];
	}
	if(isset($_POST['oldpassword'])) {
		$oldpassword = $_POST['oldpassword']; 
	}
	if(isset($_POST['password']))  {
		$password = $_POST['password']; 
	}
	if(isset($_POST['password2'])) {
		$password2 = $_POST['password2']; 
	}
	
    if(empty($username) || empty($oldpassword) || empty($password) || empty($password2)) {
		return "";
	}
	$exists = mysqli_execute_query($db, "SELECT uid FROM users WHERE username=? AND userpass=?", [$username,md5($oldpassword)]);
	if(isset($exists) && mysqli_num_rows($exists)>0) {
		if(strcmp($password,$password2)!=0) {
			return "<div style=\"text-align: center;\">New passwords do not match.</div>";
		} else {
			$changePWResult = mysqli_execute_query($db, "UPDATE users SET userpass=? WHERE username=? AND userpass=?", [md5($password),$username,$oldpassword]);
			return "<div style=\"text-align: center;\">Password changed.</div>";
		}
	} else {
		return "<div style=\"text-align: center;\">Invalid username/password.</div>";
	}
}
?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

<head>
	<title>StickEMU - Manage Account</title>
	<link rel="stylesheet" type="text/css" href="/styles/default.css" />
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />	
<style type="text/css">
.ballistick
{
        font-size:14px;
}

.ballistick a
{
        color: #0E4584;
        text-decoration: none;
        font-weight: bold;
}

.ballistick h1
{
        color: #0E4584;
        font-weight: bold;
        font-size: 26px;
}
        </style>
</head>

<body>

<div id="content" class="ballistick">
        <div class="box-756" style="width: 756px; background-image: url('/images/box-full-mid.gif'); float: left; margin: 4px 0 0 4px; display: inline;">
                <div class="box-content-top"><img src="/images/box-full-top.gif" /></div>

                <div class="box-756-content">

                <div style="text-align: center;"><img src="/images/labpass-header.jpg" /></div>
                <hr />
		<center>
                <table style="width: 90%">
                <tr>
             <td style="text-align: center;"><a href="index.php"><u>Get a Pass</u></a></td>
			<td style="text-align: center;"><a href="features.php"><u>Features</u></a></td>
			<td style="text-align: center;"><a href="faq.php"><u>FAQ</u></a></td>
			<td style="text-align: center;"><a href="manage.php"><u>Manage Account</u></a></td>
			<td style="text-align: center;"><a href="offers.php"><u>Free Pass?</u></a></td>
		</tr>
		</table>
		</center>
		<hr />

<div style="text-align: center;"></div>	<?php echo changePassword(); ?>	<br />
		<center>
			<table style="font-size: 10pt;">
			<form method="post" action="manage.php">
			<tr><td>Username</td><td><input type="text" name="manageuser" value="" /></td></tr>
			<tr><td>Old Password</td><td><input type="password" name="oldpassword" /></td></tr>
			<tr><td>New Password</td><td><input type="password" name="password" /></td></tr>
			<tr><td>(Again)</td><td><input type="password" name="password2" /></td></tr>
			<tr><td colspan="2" style="text-align: center;"><input type="submit" value="Change Password" /></td></tr>
			</form>
			</table>
		</center>
			</div>
			<div><img src="/images/box-full-bot.gif" /></div>

		</div>
	<div style="clear: both;"></div>

</div>
</body>
</html>	