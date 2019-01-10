<?php  
include "../../private/checkToken.php";

$response = array();

if (isset($_POST["token"]) && isset($_POST["id_offer"]) && isset($_POST["status"]))
{
	$token = $_POST["token"];
	$id_offer = $_POST["id_offer"];
	$status = $_POST["status"];

	$check = checkTokenForOffer($id_offer, $token);
	if ($check)
	{
		$conn = new database();
		$conn->connect();

		if ($conn->UpdateStatusOffer($id_offer, $status) != -1)
		{
			$response["status"] = 200;
			$response["message"] = "Success";
		}
		else
		{
			$response["status"] = 404;
			$response["message"] = "Exec fail";
		}

		$conn->disconnect();
	}
	else
	{
		$response["status"] = 444;
		$response["message"] = "Token Invalid";
	}
}
else
{
	$response["status"] = 400;
	$response["message"] = "Invalid request";
}

echo json_encode($response);
?>