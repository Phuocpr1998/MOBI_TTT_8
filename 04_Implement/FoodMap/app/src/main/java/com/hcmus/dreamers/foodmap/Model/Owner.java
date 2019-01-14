package com.hcmus.dreamers.foodmap.Model;

import android.util.Log;

import com.hcmus.dreamers.foodmap.AsyncTask.DoingTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskRequest;

import com.hcmus.dreamers.foodmap.common.GenerateRequest;
import com.hcmus.dreamers.foodmap.common.ResponseJSON;
import com.hcmus.dreamers.foodmap.common.SendRequest;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Owner extends com.hcmus.dreamers.foodmap.Model.User {
    private static Owner instance;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("phone_number")
    private String phoneNumber;
    @SerializedName("url_image")
    private String urlImage;
    @SerializedName("token")
    private String token;

    private List<Restaurant> listRestaurant;


    private Owner() {
        super();
        this.username = "";
        listRestaurant = new ArrayList<Restaurant>();
    }

    private Owner(String name, String email) {
        super(name, email);
        listRestaurant = new ArrayList<Restaurant>();
    }

    private Owner(String name, String email, String phoneNumber) {
        super(name, email);
        this.phoneNumber = phoneNumber;
        listRestaurant = new ArrayList<Restaurant>();
    }

    public void setlistRestaurant(List<Restaurant> listRestaurant) {
        this.listRestaurant.clear();
        this.listRestaurant.addAll(listRestaurant);
    }

    public List<Restaurant> getListRestaurant() {
        return listRestaurant;
    }

    public Restaurant getRestaurant(int index) {
        if (listRestaurant == null || listRestaurant.size() == 0)
            return null;
        return listRestaurant.get(index);
    }

    public void addRestaurant(Restaurant restaurant) {
        listRestaurant.add(restaurant);
    }

    public static void setInstance(Owner instance) {
        Owner.instance = instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static Owner getInstance() {
        if (instance == null)
            instance = new Owner();
        return instance;
    }

    public String getToken() {
        return token;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
