<?php
 include 'config.php';
 
 $id =$_REQUEST['kirim_id'];
 $result = mysql_query("DELETE FROM markers WHERE id = '$id'");
	if($result == TRUE)
	{
		header('location:profile.php');
	}
	else
	{
		echo "Gagal menghapus";
	}
?>
