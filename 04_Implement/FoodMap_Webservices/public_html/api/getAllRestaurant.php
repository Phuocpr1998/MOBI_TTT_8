<?php 
//import library
include "../../private/database.php";

//create class Restaurant
class Restaurant{
	function Restaurant($id, $owner_username, $name, $address, $phone_number, $describe_text, $url_image, $time_open, $time_close, $lat, $lon, $rank, $comments, $dishs,  $num_favorite, $num_checkin, $num_share){
		$this->id = $id;
		$this->owner_username= $owner_username;
		$this->name = $name;
		$this->address = $address;
		$this->phone_number = $phone_number;
		$this->describe_text = $describe_text;
		$this->url_image = $url_image;
		$this->time_open = $time_open;
		$this->time_close = $time_close;
		$this->location["lat"] = $lat;
		$this->location["lon"] = $lon;
		$this->ranks = $rank;
		$this->comments = $comments;
		$this->dishs = $dishs;
		$this->num_favorite = $num_favorite;
		$this->num_checkin = $num_checkin;
		$this->num_share = $num_share;
		$this->ischeck = true;
	}
}
class Comment{
	function Comment($date_time, $guest_email, $owner_email, $comment){
		$this->date_time = $date_time;
		$this->guest_email = $guest_email;
		$this->owner_email = $owner_email;
		$this->comment = $comment;
	}
}

class Rank
{
	function Rank($email, $star)
	{
		$this->email = $email;
		$this->star = $star;
	}
}

class Dish
{	
	function Dish($name, $price, $url_image, $id_catalog)
	{
		$this->name = $name;
		$this->price = $price;
		$this->url_image = $url_image;
		$this->id_catalog = $id_catalog;
	}
}

//create connection
$conn = new database();
//connect
$conn->connect();
//get result
$listRestaurants = $conn->GetAllRestaurant();
$response = array();

if ($listRestaurants != -1)
{
	$res = array();
	foreach ($listRestaurants as $row) 
	{
		$id_rest = $row['ID'];

		$comments = array();
		$dishs = array();
		$ranks = array();
		// get comment
		$listComments = $conn->GetComment($id_rest);
		if ($listComments != -1)
		{
			foreach ($listComments as $comment) {
				array_push($comments, new Comment($comment["DATE_TIME"], $comment["GUEST_EMAIL"], $comment["OWNER_EMAIL"], $comment["COMMENT"]));
			}
		}
	
		// get dishs
		$listDishs = $conn->GetDish($id_rest);
		if ($listDishs != -1)
		{
			foreach ($listDishs as $dish) 
			{
				array_push($dishs, new Dish($dish["NAME"], $dish["PRICE"], $dish["URL_IMAGE"], $dish["ID_CATALOG"]));
			}
		}
		
		// get ranks
		$listRanks = $conn->GetRank($id_rest);
		if ($listRanks != -1)
		{
			foreach ($listRanks as $rank) {
				array_push($ranks, new Rank($rank["EMAIL_GUEST"], $rank["STAR"]));
			}
		}
		
		// get number checkin
		$countCheckin = $conn->GetCheckin($id_rest);
		$checkin = 0;
		if ($countCheckin != -1)
		{
			foreach ($countCheckin as $rowCheckin) {
				$checkin = $rowCheckin["COUNT"];
				if (is_null($checkin))
					$checkin = 0;
				break;
			}
		}

		// get number share
		$countShare = $conn->GetShare($id_rest);
		$share = 0;
		if ($countShare != -1)
		{
			foreach ($countShare as $rowShare) {
				$share = $rowShare["COUNT"];
				if (is_null($share))
					$share = 0;
				break;
			}
		}

		// get number favortie
		$favorite = $conn->GetSumFavorite($id_rest);
		if ($favorite == -1 || is_null($favorite))
			$favorite = 0;
		
		//
		array_push($res, new Restaurant($id_rest, $row['OWNER_USERNAME'], $row['NAME'], $row['ADDRESS'], $row['PHONE_NUMBER'], 
			$row['DESCRIBE_TEXT'], $row['URL_IMAGE'], $row['TIMEOPEN'], $row['TIMECLOSE'],
			$row['LAT'], $row['LON'], $ranks, $comments, $dishs, $favorite, $checkin, $share));
	}

	$response["status"] = 200;
	$response["message"] = "Success";
	$response["data"] = $res;
}
else
{
	$response["status"] = 404;
	$response["message"] = "Exec fail";
}
//close conn
$conn->disconnect();
//response
echo json_encode($response);
?>