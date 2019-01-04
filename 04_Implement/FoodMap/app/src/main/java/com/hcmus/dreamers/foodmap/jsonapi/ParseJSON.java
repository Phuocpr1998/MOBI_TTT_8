package com.hcmus.dreamers.foodmap.jsonapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hcmus.dreamers.foodmap.Model.Catalog;
import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.Model.Discount;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.common.ResponseJSON;
import com.hcmus.dreamers.foodmap.map.LocationDirection;
import com.hcmus.dreamers.foodmap.serializer.OrderSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseJSON {
    private static Gson gson = new Gson();

    private static final String ROOT = "features";
    private static final String INFO = "properties";
    private static final String GEOMETRY = "geometry";
    private static final String POINT = "coordinates";

    public static List<DetailAddress> parseDetailAddress(final String response) throws JSONException{
        List<DetailAddress> detailAddresses = new ArrayList<DetailAddress>();

        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray(ROOT);


        int lenght = array.length();
        for (int i =0 ; i < lenght; i++){
            JSONObject o = array.getJSONObject(i);
            JSONObject info = o.getJSONObject(INFO);
            JSONObject geometry  = o.getJSONObject(GEOMETRY);
            JSONArray point = geometry.getJSONArray(POINT);

            DetailAddress detailAddress = gson.fromJson(info.toString(), DetailAddress.class);
            detailAddress.setPoint(new GeoPoint(point.getDouble(1), point.getDouble(0)));
            detailAddresses.add(detailAddress);
        }

        return detailAddresses;
    }

    public static ResponseJSON fromStringToResponeJSON(String data){
        return gson.fromJson(data, ResponseJSON.class);
    }


    public static ResponseJSON parseFromAllResponse(String response) throws JSONException {// use to get ResponseJSON from all response
        JSONObject object = new JSONObject(response);
        ResponseJSON responseJSON = new ResponseJSON(Integer.parseInt(object.getString("status")), object.getString("message"));
        return responseJSON;
    }

    public static String getTokenFromCreateAccount(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        JSONObject data = object.getJSONObject("data");
        String token = data.getString("token");
        return token;
    }

    public static Owner parseOwnerFromCreateAccount(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        Owner owner = gson.fromJson(object.getString("data"), Owner.class);
        return owner;
    }

    public static List<Comment> parseComment(String response) throws JSONException, ParseException {
        List<Comment> listComments = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            Comment comment = new Comment();
            comment.setDateTime(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(o.getString("date_time")));
            comment.setEmailGuest(o.getString("guest_email"));
            comment.setEmailOwner(o.getString("owner_email"));
            comment.setComment(o.getString("comment"));
            listComments.add(comment);
        }
        return listComments;
    }

    public static List<Restaurant> parseFavorite(String response) throws JSONException, ParseException {
        List<Restaurant> listRestaurants = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            Restaurant restaurant = FoodMapManager.findRestaurant(o.getInt("id_rest"));
            listRestaurants.add(restaurant);
        }
        return listRestaurants;
    }

    public static List<Restaurant> parseLocation(String response) throws JSONException {
        List<Restaurant> listRestaurants = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            Restaurant rest = new Restaurant();
            rest.setId(o.getInt("id_rest"));
            rest.setLocation(new GeoPoint(o.getDouble("lat"), o.getDouble("lon")));
            listRestaurants.add(rest);
        }
        return listRestaurants;
    }


    private static HashMap<String, Integer> parseRanks(JSONArray array) throws JSONException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            hashMap.put(o.getString("email"),o.getInt("star"));
        }
        return hashMap;
    }


    private static List<Comment> parseComment(JSONArray array) throws JSONException, ParseException {
        List<Comment> list = new ArrayList<>();
        int length = array.length();
        for(int i=0;i<length;i++){
            JSONObject o = array.getJSONObject(i);
            Comment comment = new Comment();
            comment.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getString("date_time")));
            comment.setEmailGuest(o.getString("guest_email"));
            comment.setEmailOwner(o.getString("owner_email"));
            comment.setComment(o.getString("comment"));
            list.add(comment);
        }
        return list;
    }

    private static List<Dish> parseDish(JSONArray array) throws JSONException {
        List<Dish> list = new ArrayList<>();
        int length = array.length();
        for(int i=0;i<length;i++){
            JSONObject o = array.getJSONObject(i);
            Dish dish = new Dish();
            dish.setName(o.getString("name"));
            dish.setPrice(o.getInt("price"));
            dish.setUrlImage(o.getString("url_image"));
            Catalog catalog = new Catalog();
            catalog.setId(o.getInt("id_catalog"));
            dish.setCatalog(catalog);
            list.add(dish);
        }
        return list;
    }


    public static List<Restaurant> parseRestaurant(String response) throws JSONException, ParseException {
        List<Restaurant> listRestaurants = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            Restaurant rest = new Restaurant();
            rest.setId(o.getInt("id"));
            rest.setOwnerUsername((o.getString("owner_username")));

            rest.setName(o.getString("name"));
            rest.setAddress(o.getString("address"));
            rest.setPhoneNumber(o.getString("phone_number"));
            rest.setDescription(o.getString("describe_text"));
            rest.setUrlImage(o.getString("url_image"));
            rest.setTimeOpen(new SimpleDateFormat("HH:mm:ss").parse(o.getString("time_open")));
            rest.setTimeClose(new SimpleDateFormat("HH:mm:ss").parse(o.getString("time_close")));
            JSONObject locate = o.getJSONObject("location");
            rest.setLocation(new GeoPoint(locate.getDouble("lat"), locate.getDouble("lon")));
            rest.setRanks(parseRanks(o.getJSONArray("ranks")));
            rest.setComments(parseComment(o.getJSONArray("comments")));
            rest.setDishes(parseDish(o.getJSONArray("dishs")));
            rest.setNum_checkin(o.get("num_checkin") == null ? 0 :o.getInt("num_checkin"));
            rest.setnFavorites(o.get("num_favorite") == null ? 0 : o.getInt("num_favorite"));
            rest.setnShare(o.get("num_share") == null ? 0 : o.getInt("num_share"));
            rest.setCheck(o.getBoolean("ischeck"));
            listRestaurants.add(rest);
        }
        return listRestaurants;
    }


    public static String parseUrlImage(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        String url = object.getString("url");
        return url;
    }

    public static int parseIdRestaurant(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        int id_rest = object.getInt("id");
        return id_rest;
    }

    public static List<Catalog> parseCatalog(String response) throws JSONException {
        List<Catalog>  listCatalogs = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i=0;i<length;i++){
            JSONObject o = array.getJSONObject(i);
            Catalog catalog = gson.fromJson(o.toString(), Catalog.class);
            listCatalogs.add(catalog);
        }
        return listCatalogs;
    }


    public static  List<Offer>  parseOffer(String response) throws JSONException {
        List<Offer> list = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(OrderSerializer.class, new OrderSerializer());
        Gson gson = gsonBuilder.create();
        for(int i = 0; i < length; i++){
            Offer offer = gson.fromJson(array.getJSONObject(i).toString(), Offer.class);
            list.add(offer);
        }
        return list;
    }

    public static Offer parseOfferObject(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(OrderSerializer.class, new OrderSerializer());
        Gson gson = gsonBuilder.create();
        Offer offer = gson.fromJson(object.getString("order"), Offer.class);
        return offer;
    }


    public static List<Discount> parseDiscount(String response) throws JSONException, ParseException {
        List<Discount> list = new ArrayList<>();
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("data");
        int length = array.length();
        for(int i = 0; i < length; i++){
            JSONObject o = array.getJSONObject(i);
            Discount discount = new Discount();
            discount.setId(o.getInt("id"));
            discount.setNameDish(o.getString("namedish"));
            discount.setId_rest(o.getInt("id_rest"));
            discount.setDiscountPercent(o.getInt("discount_percent"));
            discount.setTimeStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getString("timestart")));
            discount.setTimeEnd(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o.getString("timeend")));
            list.add(discount);
        }
        return list;

    }


    public static LocationDirection parseLocationDirection(final String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("routes");
        JSONObject o = array.getJSONObject(0);
        LocationDirection locationDirection = gson.fromJson(o.toString(), LocationDirection.class);
        return locationDirection;
    }

    public static String getDisplayName(final String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        String displayName = object.getString("display_name");
        return displayName;
    }

    public static String getAddressDetail(final String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        JSONArray array = object.getJSONArray("features");
        JSONObject o = array.getJSONObject(0);
        JSONObject properties = o.getJSONObject("properties");
        String name = properties.getString("name");
        String city = properties.getString("city");
        String country = properties.getString("country");
        String result = name + ", " + city + ", " + country;
        return result;
    }

}
