<?php
if(isset($_POST['ver'])) {
	$version = $_POST['ver'];
}

function checkVersion()
{
	global $version;
	if(!isset($version)) {
		return "result=error";
	}
	if($version==598 || $version==588 || $version==558) {
	   return "result=success";
	} else {
	   return "result=error";
	}
}
?>
<?php echo checkVersion(); ?>