<?php 

class database
{
	private $username = 'id7274214_thedreamers';
	private $password = 'thedreamers';
	private $host = 'localhost';
	private $databaseName = 'id7274214_foodmap';

	private $connection = null;

	public function connect()
	{
		try
		{
			$this->connection = new PDO("mysql:dbname=$this->databaseName;host=$this->host;charset=UTF8", $this->username, $this->password);
			// set the PDO error mode to exception
            $this->connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		}
		catch(PDOException $e)
		{
			return -1;
		}
		return true;
	}


	// execute query
	public function query($queryStr)
	{
		$str = explode(" ", $queryStr)[0];
		try
		{
			if (strtolower($str) == 'select' || strtolower($str) == 'call')
			{
				$stmt = $this->connection->prepare($queryStr);
				if ($stmt->execute())
				{
					$data = $stmt->fetchAll();
					$stmt->closeCursor();
					return $data;
				}
				else
					return -1;
			}
			else
			{
				 return $this->connection->exec($queryStr);
			}
		}
		catch (PDOException $e)
		{
			return -1;
		}
	}


	public function AddComment($date, $id_rest, $email, $comment, $isOwner)
	{
		$strQuery = '';
		if ($isOwner == true)
		{
			$strQuery = 'INSERT INTO COMMENTS (DATE_TIME, ID_REST, OWNER_EMAIL, COMMENT) VALUES ("'.$date.'", '.$id_rest.', "'.$email.'","'.$comment.'")';
		}
		else
		{
			$strQuery = 'INSERT INTO COMMENTS (DATE_TIME, ID_REST, GUEST_EMAIL, COMMENT) VALUES ("'.$date.'", '.$id_rest.', "'.$email.'","'.$comment.'")';
		}
		return $this->query($strQuery);
	}

	public function CreateAccount($username, $password, $name, $phone_number, $email)
	{
		$strQuery = 'INSERT INTO OWNER (USERNAME, PASSWORD, NAME, PHONE_NUMBER, EMAIL) VALUES ("'.$username.'", "'.$password.'", "'.$name.'", "'.$phone_number.'", "'.$email.'")';

		return $this->query($strQuery);
	}

	public function CreateDish($name, $id_rest, $price, $url_image, $id_catalog)
	{
		$strQuery = 'INSERT INTO DISH (NAME, ID_REST, PRICE, URL_IMAGE, ID_CATALOG) VALUES ("' . $name.'", '.$id_rest.', '.$price.',"'.$url_image.'", '.$id_catalog.')';
		return $this->query($strQuery);
	}


	public function CreateRestaurant($valueRes, $lat, $lon)
	{
		// lấy id của restaurant và tạo sẳn restaurant trong quá trình get ID
		$queryGetID = 'SELECT FC_GETID_REST() AS ID';

		$data = $this->query($queryGetID);
		if ($data != -1)
		{
			$id = "";

			foreach ($data as $row) {
				$id = $row["ID"];
				break;
			}
			
			// Update thông tin cho restaurant
    		$queryUpdateRest = 'UPDATE RESTAURANT SET ' . $valueRes . ' WHERE ID = ' . $id;
    		
    		if ($this->query($queryUpdateRest) != -1)
    		{
    			// thêm vị trí tọa độ của nhà hàng vào bảng location
    			$queryAddLocation = 'INSERT INTO LOCATION (ID_REST, LAT, LON) VALUES ('.$id.', '.$lat.', '.$lon.')';

    			if ($this->query($queryAddLocation) != -1)
    			{
    				$queryAddToPreRestaurant = 'INSERT INTO PRE_RESTAURANT (ID_REST, STATUS) VALUES ('.$id.', 0)';
    				if ($this->query($queryAddToPreRestaurant) != -1)
    				{
    					return $id;
    				}
    				else
    				{
    					$queryDeleteRest = 'DELETE FROM RESTAURANT WHERE RESTAURANT.ID = '.$id;
	    				$this->query($queryDeleteRest);
	    				return -1;
    				}
    			}
    			else
    			{
    				// trường hợp thêm tọa độ thất bại thì xóa luôn restaurant vừa ms tạo ở trên
    				$queryDeleteRest = 'DELETE FROM RESTAURANT WHERE RESTAURANT.ID = '.$id;
    				$this->query($queryDeleteRest);
    				return -1;
    			}
    		}
    		else
    		{
    			// trường hợp thêm thông tin restaurant thất bại thì xóa luôn restaurant được tạo trước đó
    			$queryDeleteRest = 'DELETE FROM RESTAURANT WHERE RESTAURANT.ID = '.$id;
    			$this->query($queryDeleteRest);
    			return -1;
    		}
		}
		else
		{
		    return -1;
		}
	}

	public function AddRank($id_rest, $email, $star)
	{
		$strQuery = 'CALL SP_ADDRANK('.$id_rest.', "'.$email.'", '.$star.')';
		return $this->query($strQuery);
	}

	public function AddFavorite($id_rest, $guest_email)
	{
		$strQuery = 'DELETE FROM FAVORITE WHERE FAVORITE.ID_REST = '.$id_rest.' AND FAVORITE.GUEST_EMAIL = "'.$guest_email.'"';
		$this->query($strQuery);
		$strQuery = 'INSERT INTO FAVORITE (ID_REST, GUEST_EMAIL) VALUES ('.$id_rest.', "'.$guest_email.'")';
		return $this->query($strQuery);
	}

	public function GetFavorite($guest_email)
	{
		$strQuery = 'SELECT * FROM FAVORITE WHERE GUEST_EMAIL="'.$guest_email.'"';
		return $this->query($strQuery); 
	}

	public function GetSumFavorite($id_rest)
	{
		$strQuery = 'SELECT COUNT(*) AS NUMBERFAV FROM FAVORITE WHERE ID_REST ='.$id_rest;
		$result = $this->query($strQuery);

		if ($result != -1)
		{
			foreach($result as $row)
			{
				return $row["NUMBERFAV"];
			}
		}
		return -1;
	}

	public function DeleteFavorite($id_rest, $guest_email)
	{
		$strQuery = 'DELETE FROM FAVORITE WHERE ID_REST = '.$id_rest.' AND GUEST_EMAIL = "'.$guest_email.'"';
		return $this->query($strQuery);
	}

	public function AddGuest($email, $name)
	{
		$strQuery = 'INSERT INTO GUEST (EMAIL, NAME) VALUES ("'.$email.'", "'.$name.'")';
		return $this->query($strQuery);
	}

	public function DeleteDish($id_rest, $name)
	{
		$strQuery = 'DELETE FROM OFFER WHERE ID_DISCOUNT IN (SELECT D.ID FROM DISCOUNT D WHERE ID_REST = '.$id_rest.' AND NAMEDISH = "'.$name.'")';
		$this->query($strQuery);

		$strQuery = 'DELETE FROM DISCOUNT WHERE  ID_REST = '.$id_rest.' AND NAMEDISH = "'.$name.'"';
		$this->query($strQuery);

		$strQuery = 'DELETE FROM DISH WHERE ID_REST = '.$id_rest.' AND NAME = "'.$name.'"';
		return $this->query($strQuery);
	}

	public function DeleteOwner($username)
	{
		$strQuery = 'SELECT FC_DELETE_OWNER("'.$username.'") AS RESULT';
		$result = $this->query($strQuery);
		if ($result == -1)
			return -1;
		
		foreach($result as $row)
		{
			return $row["RESULT"];
		}
		return -1;
	}

	public function DeleteRestaurant($id_rest)
	{
		$strQuery = 'SELECT FC_DELETE_REST('.$id_rest.') AS RESULT';
		$result = $this->query($strQuery);
		if ($result == -1)
			return -1;
		
		foreach($result as $row)
		{
			return 1;
		}
		return -1;
	}

	public function GetComment($id_rest)
	{
		//create query string
		$strQuery = "SELECT * FROM COMMENTS WHERE ID_REST = " . $id_rest . " ORDER BY DATE_TIME";
		return $this->query($strQuery); 
	}

	public function GetDish($id_rest)
	{
		//create query string
		$strQuery = "SELECT * FROM DISH WHERE ID_REST = " . $id_rest . " ORDER BY PRICE";
		return $this->query($strQuery); 
	}

	public function GetLocation($id_rest)
	{
		$strQuery = "SELECT * FROM LOCATION WHERE ID_REST = ".$id_rest;
		return $this->query($strQuery); 
	}

	public function GetRank($id_rest)
	{
		$strQuery = "SELECT * FROM RANK WHERE ID_REST = ".$id_rest;
		return $this->query($strQuery); 
	}

	// lấy tất cả các restaurant
	public function GetAllRestaurant()
	{
		$strQuery = "SELECT RST.*, LC.LAT LAT, LC.LON LON FROM (RESTAURANT RST JOIN LOCATION LC ON RST.ID = LC.ID_REST) WHERE RST.ID NOT IN (SELECT PR.ID_REST FROM PRE_RESTAURANT PR)";
		return $this->query($strQuery); 
	}

	// lấy thông tin của 1 restaurant
	public function GetRestaurant($owner_username)
	{
		$strQuery = 'SELECT RST.*, LC.LAT LAT, LC.LON LON FROM (RESTAURANT RST JOIN LOCATION LC ON RST.ID = LC.ID_REST) WHERE RST.OWNER_USERNAME = "'.$owner_username.'"';
		return $this->query($strQuery); 
	}

	public function GetToken($username)
	{
		$strQuery = 'SELECT FC_GETTOKEN("'.$username.'") AS TOKEN;';
		$token = $this->query($strQuery);
		if ($token == -1)
			return -1;
		
		foreach($token as $row)
		{
			return $row["TOKEN"];
		}
		return -1;
	}

	public function Login($username, $password)
	{
		//create query string
		$strQuery = 'CALL SP_LOGIN("'.$username.'" , "'.$password.'")';
		//get result
		$account = $this->query($strQuery);

		if ($account != -1 && $account != null)
		{
			return $account;
		}
		return -1;
	}


	public function UpdateAccount($username, $value)
	{
		$strQuery = 'UPDATE OWNER SET '.$value.' WHERE USERNAME = "'.$username.'"';
		return $this->query($strQuery);
	}

	public function UpdateDish($id_rest, $name, $value)
	{
		$strQuery = "UPDATE DISH SET ".$value." WHERE ID_REST = ".$id_rest .' AND NAME = "'. $name.'"';
		return $this->query($strQuery);
	}

	public function UpdateLocation($id_rest, $lat, $lon)
	{
		$strQuery = "UPDATE LOCATION SET LAT = ".$lat.", LON = ".$lon." WHERE ID_REST = ". $id_rest;
		return $this->query($strQuery);
	}

	public function UpdateRestaurant($id_rest, $value)
	{
		$strQuery = "UPDATE RESTAURANT SET ".$value." WHERE ID = ".$id_rest;
		$check = $this->query($strQuery);

		$strQuery = "UPDATE PRE_RESTAURANT SET STATUS = 0 WHERE ID_REST = ".$id_rest;
		$this->query($strQuery);

		return $check;
	}

	// lấy code reset password
	public function GetCode($email)
	{
		$strQuery = 'SELECT FC_GETCODE("'.$email.'") AS CODE';
		$result = $this->query($strQuery);

		foreach($result as $row)
		{
			return $row["CODE"];
		}
		return -1;
	}

	// kiểm tra xem mã code đã đúng chưa
	public function CheckCode($email, $code)
	{
		$strQuery = 'SELECT FC_CHECKCODE("'.$email.'",'.$code.') AS RESULT';
		$result = $this->query($strQuery);

		foreach($result as $row)
		{
			if ($row["RESULT"] === "1")
			{
				$strQuery = 'SELECT * FROM OWNER OW WHERE OW.EMAIL = "'.$email.'"';
				return $this->query($strQuery);
			}
		}
		return -1;
	}

	// get catalog
	public function GetCatalog()
	{
		$strQuery = "SELECT * FROM CATALOGS";
		return $this->query($strQuery);
	}

	// thêm offer
	public function AddOffer($guest_email, $total, $id_discount)
	{
		$strQuery = 'SELECT FC_ADDOFFER("'.$guest_email.'",'.$total.','.$id_discount.') AS RESULT';
		$result = $this->query($strQuery);
		
		if ($result == -1)
			return -1;

		foreach($result as $row)
		{
			if ($row["RESULT"] == 1)
			{
				return 0;
			}
		}
		
		return -1;
	}

	// tạo discount
	public function CreateDiscount($id_rest, $namedish, $discount_percent, $timestart, $timeend)
	{
		$strQuery = 'INSERT INTO DISCOUNT (ID_REST, NAMEDISH, DISCOUNT_PERCENT, TIMESTART, TIMEEND) VALUES ('.$id_rest.', "'.$namedish.'", '.$discount_percent.', "'.$timestart.'", "'.$timeend.'")';
		return $this->query($strQuery);
	}

	// xóa discount
	public function DeleteDiscount($id_discount)
	{
		$strQuery = 'DELETE FROM DISCOUNT WHERE ID = '.$id_discount;
		return $this->query($strQuery);
	}

	// lấy discount của một nhà hàng
	public function GetDiscount($id_rest)
	{
		$strQuery = 'SELECT * FROM DISCOUNT DC WHERE DC.ID_REST = '.$id_rest;
		return $this->query($strQuery);
	}

	// lấy order của nhà hàng
	public function GetOffer($id_rest)
	{
		$strQuery = 'SELECT DC.NAMEDISH, DC.DISCOUNT_PERCENT, OF.* FROM DISCOUNT DC JOIN OFFER OF ON DC.ID = OF.ID_DISCOUNT WHERE DC.ID_REST = '.$id_rest;
		return $this->query($strQuery);
	}

	// lấy order của guest
	public function GetOfferGuest($guest_email)
	{
		$strQuery = 'SELECT DC.NAMEDISH, DC.DISCOUNT_PERCENT, OF.* FROM DISCOUNT DC JOIN OFFER OF ON DC.ID = OF.ID_DISCOUNT WHERE OF.GUEST_EMAIL = "'.$guest_email.'"';
		return $this->query($strQuery);
	}

	public function DeleteOffer($id_offer, $guest_email)
	{
		$strQuery = 'UPDATE FROM OFFER SET STATUS = -2 WHERE ID = '.$id_offer.' AND GUEST_EMAIL = "'.$guest_email.'"';
		return $this->query($strQuery);
	}

	public function AddCheckin($id_rest, $guest_email)
	{
		$strQuery = 'SELECT FC_ADDCHECKIN('.$id_rest.', "'.$guest_email.'") AS RESULT';
		$result = $this->query($strQuery);
		
		if ($result == -1)
			return -1;

		foreach($result as $row)
		{
			return $row["RESULT"];
		}
		
		return -1;
	}

	public function GetCheckin($id_rest)
	{
		$strQuery = 'SELECT SUM(TOTAL_CHECKIN) COUNT FROM CHECKIN WHERE ID_REST = '.$id_rest;
		return $this->query($strQuery);
	}

	public function AddShare($id_rest, $guest_email)
	{
		$strQuery = 'SELECT FC_ADDSHARE('.$id_rest.', "'.$guest_email.'") AS RESULT';
		$result = $this->query($strQuery);
		
		if ($result == -1)
			return -1;

		foreach($result as $row)
		{
			return $row["RESULT"];
		}
		
		return -1;
	}

	public function GetShare($id_rest)
	{
		$strQuery = 'SELECT SUM(TOTAL_SHARE) COUNT FROM SHARE WHERE ID_REST = '.$id_rest;
		return $this->query($strQuery);
	}


	public function CheckExitsRestaurant($id_rest, $username)
	{
		$strQuery = 'SELECT ID FROM RESTAURANT WHERE ID ='.$id_rest.' AND OWNER_USERNAME="'.$username.'"';
		$check = $this->query($strQuery);
		if ($check == -1 || $check == null)
			return false;
		return true;		
	}

	// admin control
	public function LoginAdmin($username, $password)
	{
		$strQuery = 'SELECT FC_LOGIN_ADMIN("'.$username.'","'.$password.'") AS RESULT';
		$result = $this->query($strQuery);
		if ($result == -1)
			return -1;

		foreach($result as $row)
		{
			if ($row["RESULT"] == 1)
				return 1;
			break;
		}

		return -1;
	}

	// 
	public function GetRestaurantForAdmin()
	{
		$strQuery = 'SELECT R.*, LC.* FROM (PRE_RESTAURANT PR JOIN RESTAURANT R ON PR.ID_REST = R.ID) JOIN LOCATION LC ON R.ID = LC.ID_REST WHERE PR.STATUS = 0';
		return $this->query($strQuery);
	}

	// 
	public function DeletePreRestaurantAdmin($id_rest)
	{
		$strQuery = 'DELETE FROM PRE_RESTAURANT WHERE ID_REST = '.$id_rest;
		return $this->query($strQuery);
	}

	public function UpdatePreRestaurant($id_rest, $status)
	{
		$strQuery = "UPDATE PRE_RESTAURANT SET STATUS = ".$status." WHERE ID_REST = ".$id_rest;
		return $this->query($strQuery);
	}

	public function GetEmailOwner($id_rest)
	{
		$strQuery = "SELECT O.EMAIL AS EMAIL FROM RESTAURANT R JOIN OWNER O ON R.OWNER_USERNAME = O.USERNAME WHERE R.ID = ".$id_rest;
		$result = $this->query($strQuery);
		if ($result == -1)
			return - 1;
		foreach ($result as $row) {
			return $row["EMAIL"];
		}
		return -1;
	}

	public function GetIsCheck($id_rest)
	{
		$strQuery = "SELECT * FROM PRE_RESTAURANT WHERE ID_REST = ".$id_rest;
		$result = $this->query($strQuery);
		if ($result == -1 || $result == null)
			return true;
		return false;
	}

	public function UpdateStatusOffer($id_offer, $status)
	{
		$strQuery = 'UPDATE OFFER SET STATUS = '.$status.' WHERE ID = '.$id_offer;
		$this->query($strQuery);
	}

	// close connection
	public function disconnect()
	{
		$this->connection = null;
	}
}

?>