package com.hcmus.dreamers.foodmap.Model;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*  Lớp Owner dùng để chứa thông tin của chủ quán ăn
 *
 *   @ownerUsername: String           //Username của chủ nhà hàng
 *   @name: String                    //Tên nhà hàng
 *   @phoneNumber: String             //chứa số điện thoại chủ quán
 *   @description: String             //Mô tả của quán ăn
 *   @address: String                 //Địa chỉ của quán ăn
 *   @urlImage: String                //chứa đường dẫn hình đại diện trên server
 *   @timeOpen: Date                  //Thời gian mở cửa
 *   @timeClose: Date                 //Thời gian đóng cửa
 *   @location: GeoPoint              //Tọa độ vị trí của quán ăn
 *   @dishes: List                    //Danh sách món ăn
 *   @comments: List                  //Danh sách các comments của quán ăn
 *   @nFavorites: int                 //Số lượt yêu thích của quán ăn
 *   num_checkin: int                 //Số lượt check in tại quán ăn
 *   @nShare: int                     //Số lượt share quán ăn trên facebook
 *   ranks: HashMap                   //Chứa email và số sao của khách hàng đánh giá quán ăn.
 *
 * */
public class Restaurant implements Serializable {
    private int id;
    private String ownerUsername;
    private String name;
    private String phoneNumber;
    private String description;
    private String urlImage;
    private Date timeOpen;
    private Date timeClose;
    private GeoPoint location;
    private List<Dish> dishes;
    private List<Comment> comments;
    private String address;
    //so luong guest da yeu thich
    private int nFavorites;
    private int nShare;
    // bảng lưu thông tin người đánh giá
    // keyvalue: <email, star>
    private HashMap<String, Integer> ranks;

    private int num_checkin;
    private boolean isCheck;

    //
    public Restaurant() {
        num_checkin = 0;
        dishes = new ArrayList<Dish>();
        comments = new ArrayList<Comment>();
        ranks = new HashMap<String, Integer>();
    }
	
    public Restaurant(int id_rest,
                      String ownerUsername,
                      String name,
                      String address,
                      String phoneNumber,
                      String description,
                      String urlImage,
                      Date timeOpen,
                      Date timeClose,
                      GeoPoint location,
                      int num_checkin,
                      int nFavorites,
                      int nShare) {
        this.id = id_rest;
        this.ownerUsername = ownerUsername;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.urlImage = urlImage;
        this.timeOpen = timeOpen;
        this.timeClose = timeClose;
        this.location = location;
        this.num_checkin = num_checkin;
        this.nFavorites = nFavorites;
        this.nShare = nShare;
        this.isCheck = true; // đã được kiểm duyệt
	}

    public int getNum_checkin() {
        return num_checkin;
    }

    public void setNum_checkin(int num_checkin) {
        this.num_checkin = num_checkin;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public HashMap<String, Integer> getRanks() {
        return ranks;
    }

    public void setRanks(HashMap<String, Integer> ranks) {
        this.ranks = ranks;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Date getTimeOpen() {
        return timeOpen;
    }

    public void setTimeOpen(Date timeOpen) {
        this.timeOpen = timeOpen;
    }

    public Date getTimeClose() {
        return timeClose;
    }

    public void setTimeClose(Date timeClose) {
        this.timeClose = timeClose;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwnerUsername() { return ownerUsername; }

    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public int getnFavorites() {
        return nFavorites;
    }

    public void setnFavorites(int nFavorites) {
        this.nFavorites = nFavorites;
    }

    // -1 là chưa đánh giá
    public int findRank(String email){
        if (ranks.containsValue(email))
            return ranks.get(email);
        return -1;
    }

    public int getnShare() {
        return nShare;
    }

    public void setnShare(int nShare) {
        this.nShare = nShare;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public double getAverageRate(){
        if (this.getRanks().isEmpty())
        {
            return 0D;
        }

        double averageRate = 0;
        for(Map.Entry<String, Integer> kvp : this.getRanks().entrySet()) {
            averageRate += kvp.getValue();
        }
        return averageRate / this.getRanks().size();
    }
}
