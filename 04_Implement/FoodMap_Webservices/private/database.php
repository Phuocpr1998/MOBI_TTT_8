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
    				return true;
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


	public function DeleteDish($id_rest, $name)
	{
		$strQuery = 'DELETE FROM DISH WHERE ID_REST = '.$id_rest.' AND NAME = '.$name;
		return $this->query($strQuery);
	}

	public function DeleteAccount($username)
	{
		$strQuery = 'CALL SP_DELETE_ACCOUNT('.$username.')';
		return $this->query($strQuery);
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
		$strQuery = "SELECT RST.*, LC.LAT LAT, LC.LON LON FROM (RESTAURANT RST JOIN LOCATION LC ON RST.ID = LC.ID_REST)";
		return $this->query($strQuery); 
	}

	// lấy thông tin của 1 restaurant
	public function GetRestaurant($id_rest)
	{
		$strQuery = "SELECT RST.*, LC.LAT LAT, LC.LON LON FROM (RESTAURANT RST JOIN LOCATION LC ON RST.ID = LC.ID_REST) WHERE ID = ".$id_rest;
		return $this->query($strQuery); 
	}

	public function GetToken($username)
	{
		$strQuery = 'SELECT FC_GETTOKEN("'.$username.'") AS TOKEN;';
		$token = $this->query($strQuery);
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
		$strQuery = "UPDATE ACCOUNT SET ".$value." WHERE ID = ".$username;
		return $this->query($strQuery);
	}

	public function UpdateDish($id_rest, $name, $value)
	{
		$strQuery = "UPDATE DISH SET ".$value." WHERE ID = ".$id_rest ." AND NAME = ". $name;
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
		return $this->query($strQuery);
	}

	// lấy code reset password
	public function GetCode($email)
	{
		$strQuery = 'SELECT FC_GETCODE("'.$email.'") AS CODE';
		foreach($token as $row)
		{
			return $row["CODE"];
			break;
		}
		return -1;
	}

	// kiểm tra xem mã code đã đúng chưa
	public function CheckCode($email, $code)
	{
		$strQuery = 'SELECT FC_CHECKCODE("'.$email.'","'.$code.'") AS RESULT';
		$check = false;
		foreach($token as $row)
		{
			if ($row["RESULT"] == 1)
				$check = true;
			break;
		}
		return $check;
	}

	// get catalog
	public function GetCatalog()
	{
		$strQuery = "SELECT * FROM CATALOG";
		return $this->query($strQuery);
	}

	// close connection
	public function disconnect()
	{
		$this->connection = null;
	}
}

?>