<?php  
include '../../private/checkToken.php';

class Offer{
	function Offer($namedish, $discount_percent, $guest_email, $total, $status, $id, $date_order){
		$this->namedish = $namedish;
		$this->discount_percent = $discount_percent;
		$this->guest_email = $guest_email;
		$this->total = $total;
		$this->status = $status;
		$this->id = $id;
		$this->date_order = $date_order;
	}
}

$response = array();

if (isset($_GET["id_rest"]) && isset($_GET["token"]))
{
	$check = checkTokenForRestaurant($_GET["id_rest"], $_GET["token"]);
	if ($check)
	{
		$conn = new database();
		$conn->connect();
		$listOffer = $conn->GetOffer($_GET["id_rest"]);

		if ($listOffer != -1)
		{
			$offers = array();
			foreach ($listOffer as $row) {
				array_push($offers, new Offer($row["NAMEDISH"], $row["DISCOUNT_PERCENT"], $row["GUEST_EMAIL"], $row["TOTAL"], $row["STATUS"],$row["ID"] , $row["DATEORDER"]));
			}
			
			$response["status"] = 200;
			$response["message"] = "Success";
			$response["data"] = $offers;
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
		$response["message"] = "Invalid token";
	}
	
}
else
{
	$response["status"] = 400;
	$response["message"] = "Invalid request";
}

echo json_encode($response);
?>