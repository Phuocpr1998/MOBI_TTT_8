package com.hcmus.dreamers.foodmap.database;


import android.content.Context;

import com.hcmus.dreamers.foodmap.AsyncTask.LoadDataLocalTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Catalog;
import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FoodMapManager {

    private static List<Restaurant> restaurants;
    private static List<Catalog> catalogs;

    public  static List<Restaurant> getRestaurants(String username){
        List<Restaurant> restaurantList = new ArrayList<>();
        int length = restaurants.size();
        for (int i = 0; i <length; i++){
            if (restaurants.get(i).getOwnerUsername().equals(username)){
                restaurantList.add(restaurants.get(i));
            }
        }
        return restaurantList;
    }

    public static Restaurant findRestaurant(int id_rest){
        int n = restaurants.size();
        for (int i = 0; i< n; i++)
        {
            if (restaurants.get(i).getId() == id_rest)
            {
                return restaurants.get(i);
            }
        }
        return null;
    }

    public static Restaurant findRestaurant(GeoPoint point){
        int n = restaurants.size();
        for (int i = 0; i< n; i++)
        {
            if (restaurants.get(i).getLocation().equals(point))
            {
                return restaurants.get(i);
            }
        }
        return null;
    }

    public static void addRestaurant(Context context, Restaurant restaurant){
        DBManager dbManager = new DBManager(context);
        restaurants.add(restaurant);
        dbManager.addRestaurant(restaurant);
        dbManager.close();
    }

    public static List<Restaurant> getRestaurants(){
        return restaurants;
    }

    public static void setRestaurants(Context context, List<Restaurant> restaurants) {
        FoodMapManager.restaurants = restaurants;

        DBManager dbManager = new DBManager(context);
        // xóa dữ liệu cũ
        dbManager.clearDB();

        for (Restaurant rest: FoodMapManager.restaurants) {
            dbManager.addRestaurant(rest);
        }
        dbManager.close();
    }

    public static List<Comment> getComment(Context context, int id_rest){
        List<Comment> comments;
        DBManager dbManager = new DBManager(context);
        try {
            comments = dbManager.getComments(id_rest);
        } catch (ParseException e) {
            dbManager.close();
            return null;
        }
        dbManager.close();
        return comments;
    }

    public static void addComment(Context context, int id_rest, Comment comment){

        int n = restaurants.size();
        for (int i = 0; i< n; i++)
        {
            if (restaurants.get(i).getId() == id_rest)
            {
                restaurants.get(i).getComments().add(comment);
                return;
            }
        }
    }

    public static void addCheckIn(int id_rest){
        int n = restaurants.size();
        for (int i = 0; i< n; i++)
        {
            if (restaurants.get(i).getId() == id_rest)
            {
                restaurants.get(i).setNum_checkin(restaurants.get(i).getNum_checkin() + 1);
                return;
            }
        }
    }
    public static void addShare(int id_rest){
        int n = restaurants.size();
        for (int i = 0; i< n; i++)
        {
            if (restaurants.get(i).getId() == id_rest)
            {
                restaurants.get(i).setnShare(restaurants.get(i).getnShare() + 1);
                return;
            }
        }
    }

    public static List<String> getCatalogsString(){
        List<String> list = new ArrayList<>();
        int size = catalogs.size();
        for (int i = 0; i < size; i++){
            list.add(catalogs.get(i).getName());
        }
        return list;
    }

    public static Catalog findCatalog(String catalogName){
        int size = catalogs.size();
        for (int i =0;i < size;i++){
            if (catalogs.get(i).getName().equals(catalogName))
                return catalogs.get(i);
        }
        return null;
    }

    public static int getCatalogPosition(int catalogId)
    {
        int pos = -1;
        for (int i = 0; i < catalogs.size(); i++)
        {
            if (catalogId == catalogs.get(i).getId())
            {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public static List<Catalog> getCatalogs() {
        return catalogs;
    }

    public static void setCatalogs(Context context, List<Catalog> catalogs) {
        FoodMapManager.catalogs = catalogs;

        DBManager dbManager = new DBManager(context);
        for (Catalog catalog : FoodMapManager.catalogs) {
            dbManager.addCatalog(catalog);
        }
        dbManager.close();
    }

    public static void addRankRestaurant(int restID, String guestEmail, int star){
        for(int i = 0; i < restaurants.size(); i++)
        {
            if( restaurants.get(i).getId() == restID){
                restaurants.get(i).getRanks().put(guestEmail, star);
                return;
            }
        }
    }

    //use on offline mode
    public static void getDataFromDatabase(Context context, TaskCompleteCallBack taskCompleteCallBack){
        if (restaurants == null || catalogs == null){
            restaurants = new ArrayList<Restaurant>();
            catalogs = new ArrayList<Catalog>();
        }

        LoadDataLocalTask loadDataLocalTask = new LoadDataLocalTask(context, restaurants, catalogs, taskCompleteCallBack);
        loadDataLocalTask.execute();
    }

}
