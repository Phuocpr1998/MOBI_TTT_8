CREATE TABLE ADMIN
(
	USERNAME VARCHAR(20) PRIMARY KEY,
	PASSWORD VARCHAR(20)
);


CREATE TABLE OWNER
(
	USERNAME VARCHAR(20) PRIMARY KEY,
	PASSWORD VARCHAR(20),
	NAME VARCHAR(40) CHARSET utf8,
	PHONE_NUMBER VARCHAR(11),
	URL_IMAGE VARCHAR(200) CHARSET utf8, -- LINK IMAGE
	EMAIL VARCHAR(30) UNIQUE
);

CREATE TABLE CODERESET
(
	EMAIL VARCHAR(30) NOT NULL,
	CODE INT NOT NULL,
	PRIMARY KEY(EMAIL, CODE)
);


CREATE TABLE AUTHENTICATED
(
	USERNAME VARCHAR(20) NOT NULL,
	TOKEN VARCHAR(200),
	PRIMARY KEY (USERNAME, TOKEN)
);

CREATE TABLE GUEST
(
	EMAIL VARCHAR(30) PRIMARY KEY,
	NAME VARCHAR(40) CHARSET utf8
);

CREATE TABLE PRE_RESTAURANT
(
	ID_REST INT PRIMARY KEY,
	STATUS INT -- 0 la chua xu ly, 1 la da su ly
); 

CREATE TABLE RESTAURANT
(
	ID INT PRIMARY KEY,
	OWNER_USERNAME VARCHAR(20),
	NAME VARCHAR(100) CHARSET utf8,
	ADDRESS VARCHAR(50) CHARSET utf8,
	PHONE_NUMBER VARCHAR(11),
	DESCRIBE_TEXT TEXT,
	URL_IMAGE VARCHAR(200) CHARSET utf8, -- LINK IMAGE
	TIMEOPEN TIME,
	TIMECLOSE TIME
);

CREATE TABLE LOCATION
(
	ID_REST INT NOT NULL,
	LAT FLOAT NOT NULL, -- latitude
	LON FLOAT NOT NULL,	-- longitude
	PRIMARY KEY(LAT, LON)
);

CREATE TABLE DISH
(
	NAME VARCHAR(50) CHARSET utf8 NOT NULL,
	ID_REST INT,
	PRICE INT,
	URL_IMAGE VARCHAR(200) CHARSET utf8, -- LINK IMAGE
	ID_CATALOG INT,
	PRIMARY KEY(NAME, ID_REST)
);

CREATE TABLE CATALOGS
(
	ID INT AUTO_INCREMENT PRIMARY KEY,
	NAME VARCHAR(30) CHARSET utf8 UNIQUE NOT NULL
);

CREATE TABLE COMMENTS
(
	DATE_TIME DATETIME NOT NULL, -- ngày comment // dd-MM-yyyy HH:mm:ss
	ID_REST INT NOT NULL,
	GUEST_EMAIL VARCHAR(30) NULL, -- người dùng bình thường
	OWNER_EMAIL VARCHAR(30) NULL, -- là chủ quán ăn comment
	COMMENT VARCHAR(200) CHARSET utf8 NOT NULL,
	PRIMARY KEY (DATE_TIME, ID_REST)
);

CREATE TABLE FAVORITE
(
	ID_REST INT NOT NULL,
	GUEST_EMAIL VARCHAR(30) NOT NULL,
	PRIMARY KEY (ID_REST, GUEST_EMAIL)
);

CREATE TABLE CHECKIN
(
	ID_REST INT NOT NULL,
	GUEST_EMAIL VARCHAR(30) NOT NULL,
	TOTAL_CHECKIN INT NOT NULL,
	PRIMARY KEY (ID_REST, GUEST_EMAIL)
);

CREATE TABLE SHARE
(
	ID_REST INT NOT NULL,
	GUEST_EMAIL VARCHAR(30) NOT NULL,
	TOTAL_SHARE INT NOT NULL,
	PRIMARY KEY (ID_REST, GUEST_EMAIL)
);

CREATE TABLE RANK
(
	ID_REST INT NOT NULL,
	EMAIL_GUEST VARCHAR(30),
	STAR INT NOT NULL, -- 1 tới 5
	PRIMARY KEY(ID_REST, EMAIL_GUEST)
);

CREATE TABLE DISCOUNT
(
	ID INT AUTO_INCREMENT PRIMARY KEY,
	ID_REST INT NOT NULL, -- ID CỦA NHÀ HÀNG
	NAMEDISH VARCHAR(50) CHARSET utf8 NOT NULL, -- TÊN CỦA MÓN ĂN ĐƯỢC GIẢM GIÁ
	DISCOUNT_PERCENT INT NOT NULL, -- PHẦN TRĂM GIẢM GIÁ SO VỚI GIÁ GỐC
	TIMESTART DATETIME NOT NULL, -- THỜI GIAN BẮT ĐẦU /// dd-MM-yyyy HH:mm:ss
	TIMEEND DATETIME NOT NULL  -- THỜI GIAN KẾT THỨC // dd-MM-yyyy HH:mm:ss
);

-- LƯU THÔNG TIN NGƯỜI DÙNG ĐÃ ĐẶT TRƯỚC
CREATE TABLE OFFER
(
	ID INT AUTO_INCREMENT PRIMARY KEY,
	ID_DISCOUNT INT NOT NULL,
	GUEST_EMAIL VARCHAR(30) NOT NULL,
	TOTAL INT NOT NULL, -- SỐ LƯỢNG ĐẶT
	DATEORDER DATETIME,
	STATUS INT NOT NULL --  TRẠNG THÁI CỦA OFFER 1 LÀ ĐÃ XỬ LÝ, 0 LÀ CHƯA XỬ LÝ, -1 là bị từ chối, -2 là bị hủy
);

-- SET FOREIGN KEY
ALTER TABLE AUTHENTICATED ADD CONSTRAINT FK_AUTHENTICATED_OWNER FOREIGN KEY(USERNAME) REFERENCES OWNER(USERNAME);

ALTER TABLE RESTAURANT ADD CONSTRAINT FK_RESTAURANT_OWNER FOREIGN KEY(OWNER_USERNAME) REFERENCES OWNER(USERNAME);

ALTER TABLE DISH ADD CONSTRAINT FK_DISH_RESTAURANT FOREIGN KEY(ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE DISH ADD CONSTRAINT FK_DISH_CATALOGS FOREIGN KEY(ID_CATALOG) REFERENCES CATALOGS(ID);

ALTER TABLE COMMENTS ADD CONSTRAINT FK_COMMENTS_RESTAURANT FOREIGN KEY(ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE COMMENTS ADD CONSTRAINT FK_COMMENTS_GUEST FOREIGN KEY(GUEST_EMAIL) REFERENCES GUEST(EMAIL);

ALTER TABLE COMMENTS ADD CONSTRAINT FK_COMMENTS_OWNER FOREIGN KEY(OWNER_EMAIL) REFERENCES OWNER(EMAIL);

ALTER TABLE RANK ADD CONSTRAINT FK_RANK_RESTAURANT FOREIGN KEY(ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE RANK ADD CONSTRAINT FK_RANK_GUEST FOREIGN KEY(EMAIL_GUEST) REFERENCES GUEST(EMAIL);

ALTER TABLE LOCATION ADD CONSTRAINT FK_LOCATION_RESTAURANT FOREIGN KEY(ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE DISCOUNT ADD CONSTRAINT FK_DISCOUNT_DISH FOREIGN KEY(ID_REST, NAMEDISH) REFERENCES DISH(ID_REST, NAME);

ALTER TABLE OFFER ADD CONSTRAINT FK_OFFER_GUEST FOREIGN KEY (GUEST_EMAIL) REFERENCES GUEST(EMAIL);

ALTER TABLE OFFER ADD CONSTRAINT FK_OFFER_DISCOUNT FOREIGN KEY (ID_DISCOUNT) REFERENCES DISCOUNT(ID);

ALTER TABLE CODERESET ADD CONSTRAINT FK_CODERESET_OWNER FOREIGN KEY (EMAIL) REFERENCES OWNER(EMAIL);

ALTER TABLE FAVORITE ADD CONSTRAINT FK_FAVORITE_RESTAURANT FOREIGN KEY (ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE FAVORITE ADD CONSTRAINT FK_FAVORITE_GUEST FOREIGN KEY (GUEST_EMAIL) REFERENCES GUEST(EMAIL);

ALTER TABLE CHECKIN ADD CONSTRAINT FK_CHECKIN_RESTAURANT FOREIGN KEY (ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE CHECKIN ADD CONSTRAINT FK_CHECKIN_GUEST FOREIGN KEY (GUEST_EMAIL) REFERENCES GUEST(EMAIL);

ALTER TABLE SHARE ADD CONSTRAINT FK_SHARE_RESTAURANT FOREIGN KEY (ID_REST) REFERENCES RESTAURANT(ID);

ALTER TABLE SHARE ADD CONSTRAINT FK_SHARE_GUEST FOREIGN KEY (GUEST_EMAIL) REFERENCES GUEST(EMAIL);

ALTER TABLE PRE_RESTAURANT ADD CONSTRAINT FK_PRE_RESTAURANT_RESTAURANT FOREIGN KEY (ID_REST) REFERENCES RESTAURANT(ID);

-- SET RULES
ALTER TABLE OWNER ADD CONSTRAINT C_OWNER_PHONENUMBER CHECK(LEN(PHONE_NUMBER) IN (10,11));

ALTER TABLE RESTAURANT ADD CONSTRAINT C_RESTAURANT_PHONENUMBER CHECK(LEN(PHONE_NUMBER) IN (10,11));

ALTER TABLE RANK ADD CONSTRAINT C_RANK_STAR CHECK(STAR >= 1 AND STAR <= 5);

-- check login
DELIMITER //
CREATE PROCEDURE SP_LOGIN(IN USERNAME VARCHAR(20) , IN PASS VARCHAR(20))
BEGIN
	SELECT * FROM OWNER OW WHERE OW.USERNAME = USERNAME AND OW.PASSWORD = PASS;
END //
DELIMITER ;

-- add và update rank
DELIMITER //
CREATE PROCEDURE SP_ADDRANK(IN ID_REST INT, IN EMAIL VARCHAR(30), IN STAR INT)
BEGIN
	-- xóa đánh giá cũ nếu có
	DELETE FROM RANK WHERE RANK.ID_REST = ID_REST AND RANK.EMAIL_GUEST = EMAIL;
	-- thêm mới vào
	INSERT INTO RANK (ID_REST, EMAIL_GUEST, STAR) VALUES (ID_REST, EMAIL, STAR);
	-- trả về 1
	SELECT 1;
END //
DELIMITER ;

-- xóa restaurant
DELIMITER //
CREATE FUNCTION FC_DELETE_REST(ID_REST INT) RETURNS INT
BEGIN
	DELETE FROM PRE_RESTAURANT WHERE PRE_RESTAURANT.ID_REST = ID_REST;
	DELETE FROM SHARE WHERE SHARE.ID_REST = ID_REST;
	DELETE FROM CHECKIN WHERE CHECKIN.ID_REST = ID_REST;
	DELETE FROM RANK WHERE RANK.ID_REST = ID_REST;
	DELETE FROM OFFER WHERE OFFER.ID_DISCOUNT IN (SELECT ID FROM DISCOUNT WHERE DISCOUNT.ID_REST = ID_REST);
	DELETE FROM DISCOUNT WHERE DISCOUNT.ID_REST = ID_REST;
	DELETE FROM FAVORITE  WHERE FAVORITE.ID_REST = ID_REST;
	DELETE FROM COMMENTS  WHERE COMMENTS.ID_REST = ID_REST;
	DELETE FROM LOCATION WHERE LOCATION.ID_REST = ID_REST;
	DELETE FROM DISH WHERE DISH.ID_REST = ID_REST;
	DELETE FROM RESTAURANT WHERE RESTAURANT.ID = ID_REST;
	
	RETURN 1;
END //
DELIMITER ;

-- xóa owner
DELIMITER //
CREATE FUNCTION FC_DELETE_OWNER(USERNAME VARCHAR(20)) RETURNS INT
BEGIN
	DELETE FROM PRE_RESTAURANT WHERE PRE_RESTAURANT.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM SHARE WHERE SHARE.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM CHECKIN WHERE CHECKIN.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM RANK WHERE RANK.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM OFFER WHERE OFFER.ID_DISCOUNT IN (SELECT ID FROM DISCOUNT WHERE DISCOUNT.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME));
	DELETE FROM DISCOUNT WHERE DISCOUNT.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM FAVORITE  WHERE FAVORITE.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM COMMENTS  WHERE COMMENTS.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM COMMENTS  WHERE COMMENTS.OWNER_EMAIL = (SELECT OW.EMAIL FROM OWNER OW WHERE OW.USERNAME = USERNAME);
	DELETE FROM LOCATION WHERE LOCATION.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM DISH WHERE DISH.ID_REST IN (SELECT RES.ID FROM RESTAURANT RES WHERE RES.OWNER_USERNAME = USERNAME);
	DELETE FROM RESTAURANT WHERE RESTAURANT.OWNER_USERNAME = USERNAME;
	
	DELETE FROM AUTHENTICATED WHERE AUTHENTICATED.USERNAME = USERNAME;
	DELETE FROM CODERESET WHERE CODERESET.EMAIL = (SELECT OW.EMAIL FROM OWNER OW WHERE OW.USERNAME = USERNAME);
	
	DELETE FROM OWNER WHERE OWNER.USERNAME = USERNAME;
	
	RETURN 1;
END //
DELIMITER ;

-- lấy id rest tự động
DELIMITER //
CREATE FUNCTION FC_GETID_REST() RETURNS INT 
BEGIN

	DECLARE ID INT DEFAULT 1;
	
	SELECT RES.ID + 1 INTO ID FROM RESTAURANT RES WHERE NOT EXISTS 
							(
								SELECT * FROM RESTAURANT RES2 WHERE RES2.ID = RES.ID + 1
							)
							LIMIT 1;
							
	-- thêm luôn id vào bảng Restaurant
	INSERT INTO RESTAURANT (ID) VALUE (ID);
							
	RETURN ID;
END //
DELIMITER ;


-- lấy token sau khi đăng nhập
DELIMITER //
CREATE FUNCTION FC_GETTOKEN(USERNAME VARCHAR(20)) RETURNS VARCHAR(60) 
BEGIN
	DECLARE TOKEN VARCHAR(32) DEFAULT NULL;
	DECLARE USERNAME_ENCODE VARCHAR(28) DEFAULT NULL;
	DECLARE TOKEN_USERNAME VARCHAR(60) DEFAULT NULL;
	
	-- xóa token cũ nếu có
	DELETE FROM AUTHENTICATED WHERE AUTHENTICATED.USERNAME = USERNAME;
	-- thêm token ms vào
	SELECT MD5(NOW()) INTO TOKEN; -- lấy token 
	SELECT TO_BASE64(USERNAME) INTO USERNAME_ENCODE; -- lấy token 
	-- thêm token vào bảng AUTHENTICATED
	INSERT INTO AUTHENTICATED VALUES (USERNAME, TOKEN);
	
	SELECT CONCAT(TOKEN,USERNAME_ENCODE) INTO TOKEN_USERNAME;
	
	RETURN TOKEN_USERNAME;
END //
DELIMITER ;


-- kiểm tra token có tồn tại hay không
DELIMITER //
CREATE FUNCTION FC_CHECKTOKEN(TOKEN_USERNAME VARCHAR(60)) RETURNS INT 
BEGIN
	
	DECLARE TOKEN VARCHAR(32) DEFAULT NULL;
	DECLARE USERNAME_ENCODE VARCHAR(28) DEFAULT NULL;
	DECLARE USERNAME VARCHAR(20) DEFAULT NULL;
	
	SELECT SUBSTRING(TOKEN_USERNAME, 33) INTO USERNAME_ENCODE;
	SELECT SUBSTRING(TOKEN_USERNAME, 1, 32) INTO TOKEN;
	
	SELECT FROM_BASE64(USERNAME_ENCODE) INTO USERNAME;
	
	IF EXISTS (SELECT * FROM AUTHENTICATED WHERE AUTHENTICATED.TOKEN = TOKEN AND AUTHENTICATED.USERNAME = USERNAME) THEN
		RETURN 1;
	ELSE
		RETURN 0;
	END IF;
END //
DELIMITER ;

-- lấy mã code để reset password
-- code là một số có 6 chữ số
DELIMITER //
CREATE FUNCTION FC_GETCODE(EMAIL VARCHAR(60)) RETURNS INT 
BEGIN
	DECLARE CODE INT DEFAULT 0;
	SELECT RAND()*(1000000 - 100000) + 100000 INTO CODE;
	
	-- kiểm tra xem email có tồn tại hay ko
	IF NOT EXISTS(SELECT * FROM OWNER WHERE OWNER.EMAIL = EMAIL) THEN
		RETURN -1;
	END IF;
	
	-- xóa code tồn tại trước đó
	IF EXISTS (SELECT * FROM CODERESET WHERE CODERESET.EMAIL = EMAIL) THEN
		DELETE FROM CODERESET WHERE CODERESET.EMAIL = EMAIL;
	END IF;
		
	-- thêm code vào bản codereset
	INSERT INTO CODERESET VALUES (EMAIL, CODE);
	
	RETURN CODE;
END //
DELIMITER ;

-- check code
DELIMITER //
CREATE FUNCTION FC_CHECKCODE(EMAIL VARCHAR(60), CODE INT) RETURNS INT 
BEGIN
	-- xóa code tồn tại trước đó
	IF EXISTS (SELECT * FROM CODERESET WHERE CODERESET.EMAIL = EMAIL AND CODERESET.CODE = CODE) THEN
		-- xóa code đi
		DELETE FROM CODERESET WHERE CODERESET.EMAIL = EMAIL;
		RETURN 1; -- true
	END IF;
	
	RETURN 0; -- false
END //
DELIMITER ;

-- add offer
DELIMITER //
CREATE FUNCTION FC_ADDOFFER(GUESTEMAIL VARCHAR(60), TOTAL INT, IDDISCOUNT INT) RETURNS INT 
BEGIN
	-- kiểm tra xem discount có hợp lệ hay không
	IF EXISTS (SELECT * FROM DISCOUNT WHERE DISCOUNT.ID = IDDISCOUNT AND DISCOUNT.TIMEEND > NOW()) THEN
		-- thêm offer vào
		INSERT INTO OFFER (ID_DISCOUNT, GUEST_EMAIL, TOTAL, DATEORDER, STATUS) VALUES (IDDISCOUNT, GUESTEMAIL, TOTAL, NOW() ,0); 
		RETURN 1; -- true
	END IF;
	
	RETURN 0; -- false
END //
DELIMITER ;


-- add checking
DELIMITER //
CREATE FUNCTION FC_ADDCHECKIN(ID_REST INT, GUESTEMAIL VARCHAR(60)) RETURNS INT 
BEGIN
	-- kiểm tra xem guest đã có lần nào checkin trước đó chưa, nếu rồi thì cập nhật lại số lần checkin
	IF EXISTS (SELECT * FROM CHECKIN WHERE CHECKIN.GUEST_EMAIL = GUESTEMAIL AND CHECKIN.ID_REST = ID_REST) THEN
		-- cập nhật checkin
		UPDATE CHECKIN SET TOTAL_CHECKIN = TOTAL_CHECKIN + 1 WHERE CHECKIN.GUEST_EMAIL = GUESTEMAIL AND CHECKIN.ID_REST = ID_REST; 
	ELSE
		INSERT INTO CHECKIN (ID_REST, GUEST_EMAIL, TOTAL_CHECKIN) VALUES (ID_REST, GUESTEMAIL, 1);
	END IF;
	
	RETURN 1; -- false
END //
DELIMITER ;

-- add share
DELIMITER //
CREATE FUNCTION FC_ADDSHARE(ID_REST INT, GUESTEMAIL VARCHAR(60)) RETURNS INT 
BEGIN
	-- kiểm tra xem guest đã có lần nào share trước đó chưa, nếu rồi thì cập nhật lại số lần share
	IF EXISTS (SELECT * FROM SHARE WHERE SHARE.GUEST_EMAIL = GUESTEMAIL AND SHARE.ID_REST = ID_REST) THEN
		-- cập nhật share
		UPDATE SHARE SET TOTAL_SHARE = TOTAL_SHARE + 1 WHERE SHARE.GUEST_EMAIL = GUESTEMAIL AND SHARE.ID_REST = ID_REST; 
	ELSE
		INSERT INTO SHARE (ID_REST, GUEST_EMAIL, TOTAL_SHARE) VALUES (ID_REST, GUESTEMAIL, 1);
	END IF;
	
	RETURN 1; -- false
END //
DELIMITER ;


-- đăng nhập admin
DELIMITER //
CREATE FUNCTION FC_LOGIN_ADMIN(USERNAME VARCHAR(20), PASSWORD VARCHAR(20)) RETURNS INT
BEGIN
	IF EXISTS (SELECT * FROM ADMIN WHERE ADMIN.USERNAME = USERNAME AND ADMIN.PASSWORD = PASSWORD) THEN
		RETURN 1;
	ELSE
		RETURN 0;
	END IF;
END //
DELIMITER ;












