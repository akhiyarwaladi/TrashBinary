<?php
 include 'config.php'; 
 
 $id =$_REQUEST['id'];
 $result = mysql_query("DELETE FROM laporan_sampah WHERE uid = '$id'");
	if($result == TRUE)
	{
		header('location:laporan_sampah.php');
	}
	else
	{
		echo "Gagal menghapus";
	}
?>
