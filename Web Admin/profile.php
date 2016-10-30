<?php
include('session.php');
include('config.php');

?>
<!DOCTYPE html>
<html>
	<head>
		<link rel="import" href="css.html"> <!-- include meta header -->

		<title>Your Home Page</title>
		<link href="style.css" rel="stylesheet" type="text/css">
	</head>
<body>

	<div id="profile">
		<b id="welcome">Welcome : <i><?php echo $login_session; ?></i></b>
		<b id="logout"><a href="logout.php">Log Out</a></b>
	</div>

	<br>
	<br>
	<a href="profile.php?kirim_id= <?php echo $data['id']?>">Lokasi Tempat Sampah</a>
	<br>
	<a href="laporan.php?kirim_id= <?php echo $data['id']?>">Laporan Tempat Sampah</a>
	<br><br>
	<a href="add.php?kirim_id= <?php echo $data['id']?>">Tambah Markers</a>
	<br>

	<table border="1px" width="59%">
		<tr>
			<td width="36%">latitude</td>
			<td width="27%">longitude</td>
		</tr>

		<?php
			include 'config.php'; 
			$query = mysql_query("select * from markers order by id");  
			while ( $data = mysql_fetch_array($query) ) 
			{
		?>
			<tr>
			<?php $id = $data['id']; ?>
			<td><?php echo $data['lat'];?></td>   
			<td><?php echo $data['lng'];?></td>
			<td><a href="edit.php?kirim_id= <?php echo $data['id']?>">Edit</a></td>
			<td><a href="delete.php?kirim_id= <?php echo $data['id']?>">Delete</a></td>
			</tr>

		<?php 
			}
		?>
	</table>
</body>
</html>
