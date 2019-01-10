<?php  
include '../../private/database.php';

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

if (isset($_GET["guest_email"]))
{
	$conn = new database();
	$conn->connect();
	$listOffer = $conn->GetOfferGuest($_GET["guest_email"]);

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
	$response["status"] = 400;
	$response["message"] = "Invalid request";
}

echo json_encode($response);
?>