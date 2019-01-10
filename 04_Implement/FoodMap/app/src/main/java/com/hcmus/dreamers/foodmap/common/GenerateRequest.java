package com.hcmus.dreamers.foodmap.common;

import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.Model.Discount;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.define.ConstantURL;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;

public class GenerateRequest {

    public static okhttp3.Request checkLogin(String username, String password) { //if login is successful then result is not equals null

        String url = ConstantURL.BASEURL + ConstantURL.LOGIN;
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request createAccount(String username, String password, String name, String phoneNumber, String email) {
        String url = ConstantURL.BASEURL + ConstantURL.CREATEACCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("name", name);
        params.put("phone_number", phoneNumber);
        params.put("email", email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;

    }

    public static okhttp3.Request addGuest(final Guest owner) {
        String url = ConstantURL.BASEURL + ConstantURL.ADDGUEST;
        Map<String, String> params = new HashMap<>();
        params.put("name", owner.getName());
        params.put("email", owner.getEmail());
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request comment(final int id_rest, final Comment comment, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.COMMENT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("comment", comment.getComment());

        if (comment.getEmailGuest() != null && !comment.getEmailGuest().equals(""))
            params.put("guest_email", comment.getEmailGuest());
        else {
            params.put("owner_email", comment.getEmailOwner());
            params.put("token", token);
        }

        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addDish(final int id_rest, final Dish dish, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.CREATEDISH;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("name", dish.getName());
        params.put("price", String.valueOf(dish.getPrice()));
        params.put("url_image", dish.getUrlImage());
        params.put("id_catalog", String.valueOf(dish.getCatalog().getId()));
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request createRestaurant(final Restaurant restaurant, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.CREATERESTAURANT;
        Map<String, String> params = new HashMap<>();

        params.put("owner_username", restaurant.getOwnerUsername());

        params.put("name", restaurant.getName());
        params.put("address", restaurant.getAddress());
        params.put("phone_number", restaurant.getPhoneNumber());
        params.put("describe_text", restaurant.getDescription());
        params.put("timeopen", TimeFormatter.format(restaurant.getTimeOpen(), true));
        params.put("timeclose", TimeFormatter.format(restaurant.getTimeClose(), true));
        params.put("lat", String.valueOf(restaurant.getLocation().getLatitude()));
        params.put("lon", String.valueOf(restaurant.getLocation().getLongitude()));
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deleteDish(final int id_rest, final String name, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEDISH;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("name", name);
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addFavorite(final int id_rest, final String guest_email) {
        String url = ConstantURL.BASEURL + ConstantURL.ADDFAVORITE;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("guest_email", guest_email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deleteFavorite(final int id_rest, final String guest_email) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEFAVORITE;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("guest_email", guest_email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getFavorite(final String guest_email) {
        String url = ConstantURL.BASEURL + ConstantURL.GETFAVORITE;
        Map<String, String> params = new HashMap<>();
        params.put("guest_email", guest_email);

        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deleteAccount(final String username, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEACCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request updateAccount(final Owner owner) {
        String url = ConstantURL.BASEURL + ConstantURL.UPDATEACCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("username", owner.getUsername());
        params.put("token", owner.getToken());
        params.put("password", owner.getPassword());
        params.put("name", owner.getName());
        params.put("phone_number", owner.getPhoneNumber());
        params.put("email", owner.getEmail());
        params.put("url_image", owner.getUrlImage());
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request updateDish(final int id_rest, final Dish dish, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.UPDATEDISH;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("name", dish.getName());
        params.put("token", token);
        params.put("price", String.valueOf(dish.getPrice()));
        params.put("url_image", dish.getUrlImage());
        params.put("id_catalog", String.valueOf(dish.getCatalog().getId()));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request updateLocation(final int id_rest, final GeoPoint location, final String token) {

        String url = ConstantURL.BASEURL + ConstantURL.UPDATELOCAION;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("lat", String.valueOf(location.getLatitude()));
        params.put("lon", String.valueOf(location.getLongitude()));
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addCheckin(final int id_rest, final String guest_email) {

        String url = ConstantURL.BASEURL + ConstantURL.ADDCHECKIN;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("guest_email", guest_email);

        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    private static String transferDateToTime(Date date) {
        String result = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
        return result;
    }

    public static okhttp3.Request updateRestaurant(final Restaurant restaurant, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.UPDATERESTAURANT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(restaurant.getId()));
        params.put("token", token);
        params.put("name", restaurant.getName());
        params.put("address", restaurant.getAddress());
        params.put("phone_number", restaurant.getPhoneNumber());
        params.put("describe_text", restaurant.getDescription());
        params.put("timeopen", TimeFormatter.format(restaurant.getTimeOpen(), true));
        params.put("timeclose", TimeFormatter.format(restaurant.getTimeClose(), true));

        params.put("url_image", restaurant.getUrlImage());
        if (restaurant.getLocation() != null) {
            params.put("lat", String.valueOf(restaurant.getLocation().getLatitude()));
            params.put("lon", String.valueOf(restaurant.getLocation().getLongitude()));
        }

        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request upload(final int id_rest, final String name, final String data) {
        String url = ConstantURL.BASEURL + ConstantURL.UPLOAD;
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("data", data);
        params.put("id", String.valueOf(id_rest));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deletePicture(final String urlImage) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEPICTURE;
        Map<String, String> params = new HashMap<>();
        params.put("url", urlImage);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request resetPassword(final String email) {
        String url = ConstantURL.BASEURL + ConstantURL.RESETPASSWORD;
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request checkCode(final String email, final String codeCheck) {
        String url = ConstantURL.BASEURL + ConstantURL.CHECKCODE;
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("code", codeCheck);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getComment(final int id_rest) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETCOMMENT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }


    public static okhttp3.Request getLocation(final int id_rest) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETLOCATION;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getAllRestaurant() {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETALLRESTAURANT;
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getRestaurant(String token, String owner_username) {
        String url = ConstantURL.BASEURL + ConstantURL.GETRESTAURANT;
        Map<String, String> params = new HashMap<>();
        params.put("owner_username", owner_username);
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getCatalog() {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETCATALOG;
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .build();
        return request;
    }

    public static okhttp3.Request getOffer(int id_rest) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETOFFER;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getOfferGuest(String guest_email) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETGUESTOFFER;
        Map<String, String> params = new HashMap<>();
        params.put("guest_email", guest_email);
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getDiscount(int id_rest) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.GETDISCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addOffer(final String guest_email, int total, int id_discount) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.ADDOFFER;
        Map<String, String> params = new HashMap<>();
        params.put("guest_email", guest_email);
        params.put("total", String.valueOf(total));
        params.put("id_discount", String.valueOf(id_discount));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addGuest(final String email, final String name) {
        String url = ConstantURL.BASEURL + ConstantURL.ADDGUEST;
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("name", name);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request directionMap(final GeoPoint start, final GeoPoint end) {
        String baseUrl = ConstantURL.URLOSM;
        Map<String, String> params = new HashMap<>();
        params.put("api_key", ConstantURL.KEY);
        params.put("coordinates", buildCoordinates(start, end));
        params.put("profile", "driving-car");
        params.put("geometry_format", "polyline");
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json; charset=utf-8") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    private static String buildCoordinates(final GeoPoint start, final GeoPoint end) {
        StringBuffer data = new StringBuffer();
        data.append(start.getLongitude());
        data.append(",");
        data.append(start.getLatitude());
        data.append("|");
        data.append(end.getLongitude());
        data.append(",");
        data.append(end.getLatitude());
        return data.toString();
    }

    public static okhttp3.Request addRank(final String guestEmail, int idRest, int star) {
        String url = ConstantURL.BASEURL + ConstantURL.ADDRANK;
        Map<String, String> params = new HashMap<>();
        params.put("guest_email", guestEmail);
        params.put("id_rest", String.valueOf(idRest));
        params.put("star", String.valueOf(star));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deleteRestaurant(int idRest, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETERESTAURANT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(idRest));
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }


    public static okhttp3.Request getAddressFromString(String address) {
        String baseUrl = ConstantURL.PHOTONAPI;
        Map<String, String> params = new HashMap<>();
        params.put("q", address);
        params.put("lang", "en");
        params.put("limit", "10");
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json; charset=utf-8") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request getAddressFromPoint(GeoPoint point) {
        String baseUrl = ConstantURL.PHOTONAPIREVERSE;
        Map<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(point.getLatitude()));
        params.put("lon", String.valueOf(point.getLongitude()));
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json; charset=utf-8") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request addShare(final int id_rest, final String guest_email) {

        String url = ConstantURL.BASEURL + ConstantURL.ADDSHARE;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(id_rest));
        params.put("guest_email", guest_email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request deleteOffer(int idOffer, String guest_email) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEOFFER;
        Map<String, String> params = new HashMap<>();
        params.put("id_offer", String.valueOf(idOffer));
        params.put("guest_email", guest_email);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }
	
    public static okhttp3.Request createDiscount(final Discount discount, final String token) {
        String baseUrl = ConstantURL.BASEURL + ConstantURL.CREATEDISCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("id_rest", String.valueOf(discount.getId_rest()));
        params.put("namedish", discount.getNameDish());
        params.put("discount_percent", String.valueOf(discount.getDiscountPercent()));
        params.put("timestart", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(discount.getTimeStart()));
        params.put("timeend", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(discount.getTimeEnd()));
        params.put("token", token);
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(baseUrl)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }
	
	    public static okhttp3.Request deleteDiscount(final int id_rest, final int id_discount, final String token) {
        String url = ConstantURL.BASEURL + ConstantURL.DELETEDISCOUNT;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("id_rest", String.valueOf(id_rest));
        params.put("id_discount", String.valueOf(id_discount));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request GeoPoint2Address(GeoPoint point) {
        String baseUrl = ConstantURL.NOMINATIM_REVERSE;
        Map<String, String> params = new HashMap<>();
        params.put("format", "jsonv2");
        params.put("lat", String.valueOf(point.getLatitude()));
        params.put("lon", String.valueOf(point.getLongitude()));
        params.put("addressdetails", "0");
        String url = Utils.buildUrl(baseUrl, params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("accept-language", "vi, en") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }

    public static okhttp3.Request updateStatusOrder(final int id_order, final String token, final int status) {
        String url = ConstantURL.BASEURL + ConstantURL.UPDATESTATUS;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("id_offer", String.valueOf(id_order));
        params.put("status", String.valueOf(status));
        RequestBody bodyRequest = Utils.buildParameter(params);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(bodyRequest)
                .addHeader("Authorization", "header value") //Notice this request has header if you don't need to send a header just erase this part
                .build();
        return request;
    }
}
