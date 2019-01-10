package com.hcmus.dreamers.foodmap.common;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.AccessToken;
import com.hcmus.dreamers.foodmap.AsyncTask.DoingTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskRequest;
import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.Model.Discount;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FoodMapApiManager {

    public static final int SUCCESS = -1;
    public static final int PARSE_FAIL = -2;
    public static final int FAIL_INFO = -3;

    public static boolean isLogin(){
        if (Owner.getInstance().getToken() == null)
            return false;
        return true;
    }


    public static boolean isGuestLogin(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        return isLoggedIn;
    }

    public static void Login(String username, String password, final TaskCompleteCallBack taskCompleteCallBack)
    {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if(Sresponse != null) {
                    ResponseJSON parseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(parseJSON.getCode() == ConstantCODE.SUCCESS) {
                        try {
                            Owner.setInstance(ParseJSON.parseOwnerFromCreateAccount(Sresponse));

                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                        }
                    }
                    else if (parseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (parseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.checkLogin(username, password)));
    }

    /*
     *  Hàm thực hiện việc xóa tài khoản owner, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *  Mã phản hồi gồm có SUCCESS, FAIL_INFO, NOTINTERNET
     *
     *  @taskCompleteCallBack: TaskCompleteCallBack  //callback-function chứa các việc cần làm sau khi thực hiện xóa tài khoản
     *
     *  @return void
     * */
    public static void deleteAcount(final TaskCompleteCallBack taskCompleteCallBack)
    {
        Owner instance = Owner.getInstance();
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();
                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);
                    if (responseJSON.getCode() == ConstantCODE.SUCCESS)
                    {
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                    Log.w("deleteAccount", Sresponse);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.deleteAccount(instance.getUsername(), instance.getToken())));
    }

    /*  Hàm thực hiện việc tạo restaurant, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *   Mã phản hồi gồm có SUCCESS, FAIL_INFO, NOTINTERNET
     *
     *   @restaurant: Restaurant                             //Restaurant chứa thông tin về nhà hàng mới
     *   @taskCompleteCallBack: TaskCompleteCallBack         //callback-function chứa các việc cần làm sau khi tạo nhà hàng
     *
     *   @return void
     * */
    public static void createRestaurant(final Restaurant restaurant,final TaskCompleteCallBack taskCompleteCallBack) {
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS)
                    {
                        try {
                            int id = ParseJSON.parseIdRestaurant(Sresponse);
                            taskCompleteCallBack.OnTaskComplete(id);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                        }

                    }
                    else {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.createRestaurant(restaurant, Owner.getInstance().getToken())));
    }

    public static void forgotPassword(String email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.resetPassword(email)));
    }

    public static void checkCode(String email, String code, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        try {
                            Owner.setInstance(ParseJSON.parseOwnerFromCreateAccount(Sresponse));
                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                        }
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.checkCode(email, code)));
    }

    /*  Hàm thực hiện việc update thông tin tài khoản lên Host server, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *   Mã phản hồi gồm có SUCCESS, NOTFOUND, NOTINTERNET
     *
     *   @owner: Owner                                       //Owner chứa thông tin cần update của tài khoản
     *   @taskCompleteCallBack: TaskCompleteCallBack         //callback-function chứa các việc cần làm sau khi update tài khoản
     *
     *   @return void
     * */
    public static void updateAccount(final Owner owner, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String data = response.toString();
                if (data != null){
                    Log.w("Update Account",data);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(data);
                    if (responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.updateAccount(owner)));
    }

    /*  Hàm thực hiện việc tạo tài khoản onwer, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *   Mã phản hồi gồm có SUCCESS, NOTFOUND, NOTINTERNET
     *
     *   @username: String                                   //Username của account
     *   @password: String                                   //Password của account
     *   @name: String                                       //name của account
     *   @phoneNumber: String                                //PhoneNumber của account
     *   @email: String                                      //Email của account
     *   @taskCompleteCallBack: TaskCompleteCallBack         //callback-function chứa các việc cần làm sau khi tạo Account
     *
     *   @return void
     * */
    public static void createAccount(String username, String password, String name, String phoneNumber, String email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.createAccount(username,password,name, phoneNumber, email)));
    }

    /*  Hàm thực hiện việc tạo tài khoản Guest khi người dùng đăng nhập bằng facebook, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *   Mã phản hồi gồm có SUCCESS, NOTFOUND, NOTINTERNET
     *
     *   @guest: Guest                                       //Guest chứa thông tin của user (name, email)
     *   @taskCompleteCallBack: TaskCompleteCallBack         //callback-function chứa các việc cần làm sau khi thêm tài khoản Guest
     *
     *   @return void
     * */
    public static void addGuest(Guest guest, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(SUCCESS); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.addGuest(guest)));
    }

    /*  Hàm thực hiện việc xóa Dish từ server, nhận phản hồi và gọi callback-function đính kèm theo phản hồi nhận được.
     *
     *   Mã phản hồi gồm có SUCCESS, NOTFOUND, NOTINTERNET
     *
     *   @id_rest: int                                       //ID của nhà hàng chứa Dish.
     *   @dishName: String                                   //Tên Dish
     *   @taskCompleteCallBack: TaskCompleteCallBack         //callback-function chứa các việc cần làm sau khi xóa Dish
     *
     *   @return void
     * */
    public static void deleteDish(int id_rest, final String dishName, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // not found on database
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.deleteDish(id_rest, dishName, Owner.getInstance().getToken())));
    }

    public static void addFavorite(String guest_email, int id_rest, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // not found on database
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.addFavorite(id_rest, guest_email)));
    }

    public static void addComment(int id_rest, Comment comment, String token, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // not found on database
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.comment(id_rest, comment, token)));
    }

    public static void updateDish(int id_rest, final Dish dish, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.updateDish(id_rest, dish, Owner.getInstance().getToken())));
    }

    public static void deleteFavorite(String guest_email, int id_rest, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // not found on database
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.deleteFavorite(id_rest, guest_email)));
    }

    public static void getFavorite(String guest_email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        try {
                            Guest.getInstance().setFavRestaurant(ParseJSON.parseFavorite(Sresponse));
                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        } catch (ParseException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        }
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.getFavorite(guest_email)));
    }

    public static void deleteRestaurant(final Restaurant restaurant, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    Log.w("deleteRestaurant", resp);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        Owner.getInstance().getListRestaurant().remove(restaurant);
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.deleteRestaurant(restaurant.getId(), Owner.getInstance().getToken())));
    }

    public static void updateRestaurant(final Restaurant rest, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    Log.w("updateRestaurant",resp);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }

            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.updateRestaurant(rest, Owner.getInstance().getToken())));
    }

    //
    public static void getDetailAddressFromString(final String address, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();
                if (resp != null)
                {
                    try {
                        List<DetailAddress> detailAddresses = new ArrayList<DetailAddress>();
                        detailAddresses.clear();
                        detailAddresses.addAll(ParseJSON.parseDetailAddress(resp));

                        taskCompleteCallBack.OnTaskComplete(detailAddresses);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        taskCompleteCallBack.OnTaskComplete(null);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(null);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getAddressFromString(address)));
    }

    public static void uploadImage(Context context, int id_rest, String name, String url, final TaskCompleteCallBack taskCompleteCallBack){

        try {
            String convert = Base64Converter.binary2Base64(url);

            TaskRequest taskRequest = new TaskRequest();
            taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
                @Override
                public void OnTaskComplete(Object response) {
                    String sResponse = response.toString();
                    if (response != null){
                        Log.w("Upload Image", sResponse);
                        ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(sResponse);
                        if (responseJSON.getCode() == ConstantCODE.SUCCESS){
                            try {
                                String urlImage = ParseJSON.parseUrlImage(sResponse);
                                taskCompleteCallBack.OnTaskComplete(urlImage);
                            } catch (JSONException e) {
                                taskCompleteCallBack.OnTaskComplete(null);
                            }
                        }
                        else{
                            taskCompleteCallBack.OnTaskComplete(null);
                        }
                    }
                    else{
                        taskCompleteCallBack.OnTaskComplete(null);
                    }
                }
            });
            taskRequest.execute(new DoingTask(GenerateRequest.upload(id_rest, name, convert)));

        } catch (Exception e) {
            e.printStackTrace();
            taskCompleteCallBack.OnTaskComplete(null);
        }
    }

    // lấy dữ liệu từ api và lưu xuống database
    public static void getRestaurant(final Context context, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        try {
                            FoodMapManager.setRestaurants(context, ParseJSON.parseRestaurant(Sresponse));
                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        } catch (ParseException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        }
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getAllRestaurant()));
    }


    public static void getRestaurantForOwner(final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        try {
                            Owner.getInstance().setlistRestaurant(ParseJSON.parseRestaurant(Sresponse));
                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        } catch (ParseException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        }
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getRestaurant(Owner.getInstance().getToken(), Owner.getInstance().getUsername())));
    }

    // lấy danh sách catalog
    public static void getCatalog(final Context context, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String Sresponse = response.toString();

                if (Sresponse != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(Sresponse);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        try {
                            FoodMapManager.setCatalogs(context, ParseJSON.parseCatalog(Sresponse));
                            taskCompleteCallBack.OnTaskComplete(SUCCESS);
                        } catch (JSONException e) {
                            taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                            e.printStackTrace();
                        }
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getCatalog()));
    }

    // dành cho guest
    public static void getDiscount(int id_rest, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                taskCompleteCallBack.OnTaskComplete(response.toString());
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.getDiscount(id_rest)));
    }


    public static void uploadImage(int restID,
                                   String imageName,
                                   String base64Data,
                                   final TaskCompleteCallBack taskCompleteCallBack){

        TaskRequest uploadingTask = new TaskRequest();

        // Implement call back
        uploadingTask.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String jsonResponseString = response.toString();

                if (jsonResponseString != null) {
                    Log.w("UploadImage", jsonResponseString);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(jsonResponseString);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){

                        // Get the public URL and send back to caller
                        String publicImageURL = "";

                        try{
                            publicImageURL = ParseJSON.parseUrlImage(jsonResponseString);
                        }catch (Exception e)
                        {
                            // This line should never be run
                            Log.e("ParseJSON", e.getMessage());
                        }

                        // Send back to caller
                        taskCompleteCallBack.OnTaskComplete(publicImageURL);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete("NOT FOUND");
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete("NO INTERNET");
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete("NO INTERNET");
                }
            }
        });

        uploadingTask.execute(new DoingTask(GenerateRequest.upload(restID, imageName, base64Data)));
    }

    public static void uploadImage(Context context,
                                   int restID,
                                   Uri imageUri,
                                   final TaskCompleteCallBack taskCompleteCallBack)
    {
        File imageFile = new File(imageUri.getPath());
        String encodedData = "";

        // Mã hóa hình theo base64
        try
        {
            encodedData = Base64Converter.encodeToBase64(context, imageUri);
        }catch (Exception e)
        {
            Log.e("ConvertBase64",e.getMessage());
        }
        Date date = Calendar.getInstance().getTime();
        String imageName = String.format("%tF_%tT_%s",date.getTime(), date.getTime(), imageFile.getName());
        uploadImage(restID, imageName,encodedData,taskCompleteCallBack);
    }

    public static void deleteImage(String imageURL, final TaskCompleteCallBack taskCompleteCallBack)
    {
        TaskRequest deletingTask = new TaskRequest();

        deletingTask.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String jsonResponseString = response.toString();

                if (jsonResponseString != null) {
                    Log.w("deleteImage", jsonResponseString);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(jsonResponseString);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        deletingTask.execute(new DoingTask(GenerateRequest.deletePicture(imageURL)));
    }



    public static void getOffer(int id_rest, final TaskCompleteCallBack taskCompleteCallBack)
    {
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                taskCompleteCallBack.OnTaskComplete(response.toString());
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.getOffer(id_rest)));
    }


    public static void addDish(int id_rest, Dish dish, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String jsonResponseString = response.toString();

                if (jsonResponseString != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(jsonResponseString);

                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                    else {
                        taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.addDish(id_rest, dish, Owner.getInstance().getToken())));
    }

    public static void addRank(String guestEmail, int restID, int star, final TaskCompleteCallBack taskCompleteCallBack)
    {
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String JsonObjectString = response.toString();

                if (JsonObjectString != null) {
                    try {
                        ResponseJSON responseJSON = ParseJSON.parseFromAllResponse(response.toString());

                        if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                            taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                        }
                        else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                            taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND);
                        }
                        else if (responseJSON.getCode() == ConstantCODE.INVALIDREQUEST) {
                            taskCompleteCallBack.OnTaskComplete(ConstantCODE.INVALIDREQUEST);
                        }
                        else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                            taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.addRank(guestEmail, restID, star)));
    }

    public static void addCheckIn(int id_rest, String guest_email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.addCheckin(id_rest, guest_email)));
    }

    public static void addShare(int id_rest, String guest_email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if(resp != null)
                {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });

        taskRequest.execute(new DoingTask(GenerateRequest.addShare(id_rest, guest_email)));
    }

    public static void addOrder(final Offer offer, int id_discount, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.addOffer(offer.getGuestEmail(), offer.getTotal(), id_discount)));
    }

    public static void deleteOffer(int idOffer, String guest_email, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.deleteOffer(idOffer, guest_email)));
    }

	public static void createDiscount(final Discount discount, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.createDiscount(discount, Owner.getInstance().getToken())));
    }
	
	public static void deleteDiscount(int id_rest, int id_discount, final TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // trường hợp đã tồn tại
                    }
                    else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET){
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                }
                else{
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.deleteDiscount(id_rest, id_discount, Owner.getInstance().getToken())));
    }

    public static void getAddressFromPoint(GeoPoint sourcePoint, final TaskCompleteCallBack taskCompleteCallBack) {
        TaskRequest getAddressTask = new TaskRequest();

        getAddressTask.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();
                if (resp != null) {
                    Log.w("getAddressFromPoint", resp);
                    String detailAddress = null;

                    try {
                        detailAddress = ParseJSON.getDisplayName(resp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    taskCompleteCallBack.OnTaskComplete(detailAddress);
                } else {
                    taskCompleteCallBack.OnTaskComplete(null);
                }
            }
        });

        getAddressTask.execute(new DoingTask(GenerateRequest.GeoPoint2Address(sourcePoint)));
    }

    public static void updateStatusOrder(int id_order, int status, final TaskCompleteCallBack taskCompleteCallBack) {
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if (responseJSON.getCode() == ConstantCODE.SUCCESS) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.SUCCESS);
                    } else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTFOUND); // trường hợp đã tồn tại
                    } else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                } else {
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.updateStatusOrder(id_order, Owner.getInstance().getToken(), status)));
    }

    public static void updateLocation(int id_rest, GeoPoint location, final TaskCompleteCallBack taskCompleteCallBack) {
        TaskRequest taskRequest = new TaskRequest();

        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();

                if (resp != null) {
                    Log.w("updateLocation", resp);
                    ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resp);
                    if (responseJSON.getCode() == ConstantCODE.SUCCESS) {
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    } else if (responseJSON.getCode() == ConstantCODE.NOTFOUND) {
                        taskCompleteCallBack.OnTaskComplete(FAIL_INFO); // trường hợp đã tồn tại
                    } else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET) {
                        taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                    }
                } else {
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.updateLocation(id_rest, location, Owner.getInstance().getToken())));
    }

    public static void getOrdersGuest(String guest_email, TaskCompleteCallBack taskCompleteCallBack){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resultJson = response.toString();
                ResponseJSON responseJSON = ParseJSON.fromStringToResponeJSON(resultJson);
                if (responseJSON.getCode() == ConstantCODE.SUCCESS)
                {
                    Guest.getInstance().getOfferList().clear();
                    try {
                        Guest.getInstance().getOfferList().addAll(ParseJSON.parseOffer(resultJson));
                        taskCompleteCallBack.OnTaskComplete(SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        taskCompleteCallBack.OnTaskComplete(PARSE_FAIL);
                    }
                }
                else if (responseJSON.getCode() == ConstantCODE.NOTINTERNET)
                {
                    taskCompleteCallBack.OnTaskComplete(ConstantCODE.NOTINTERNET);
                }
                else
                {
                    taskCompleteCallBack.OnTaskComplete(FAIL_INFO);
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getOfferGuest(guest_email)));
    }
}

