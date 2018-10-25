package com.hcmus.dreamers.foodmap.common;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.hcmus.dreamers.foodmap.model.Comment;
import com.hcmus.dreamers.foodmap.model.Dish;
import com.hcmus.dreamers.foodmap.model.Owner;
import com.hcmus.dreamers.foodmap.model.Restaurant;
import com.hcmus.dreamers.foodmap.define.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FoodMapAPI {

    private static Gson gson = new Gson();

    //cac kieu tra ve
    private static Owner result = null;
    private static int code;
    private static String urlImage;
    /////////////////////////////////////


    public static Owner checkLogin(final Owner owner, Context context){ //if login is successful then result is not equals null
        result = null;
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.LOGIN;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals(Constant.SUCCESS)){
                                JSONObject object = response.getJSONObject("data");
                                result = gson.fromJson(object.toString(), Owner.class);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", owner.getUsername());
                params.put("password", owner.getPassword());
                return params;
            }
        };
        requestQueue.add(request);
        return owner;
    }

    public static int createAccount(final Owner owner, final Context context){

        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.CREATEACCOUNT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Toast.makeText(context, owner.getUsername(), Toast.LENGTH_LONG).show();
                            String status = response.getString("status");

                            code = Integer.parseInt(status);
                            Toast.makeText(context, "" + code, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", owner.getUsername());
                params.put("password", owner.getPassword());
                params.put("name", owner.getName());
                params.put("phone_number", owner.getPhoneNumber());
                params.put("email", owner.getEmail());
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int comment(final String id_rest, final Comment comment, @Nullable final String guest_email,@Nullable final String owner_email, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.COMMENT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(id_rest));
                params.put("comment", comment.getComment());
                if(guest_email == null)
                    params.put("guest_email", guest_email);
                else
                    params.put("owner_email", owner_email);
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int createDish(final int id_rest, final Dish dish, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.CREATEDISH;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(id_rest));
                params.put("name", dish.getName());
                params.put("price", String.valueOf(dish.getPrice()));
                params.put("url_image", dish.getUrlImage());
                params.put("id_catalog", String.valueOf(dish.getCatalog().getId()));
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int createRestaurant(final String id_user, final Restaurant restaurant,final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.CREATERESTAURANT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_user", id_user);
                params.put("name", restaurant.getName());
                params.put("address", restaurant.getAddress());
                params.put("phone_number", restaurant.getPhoneNumber());
                params.put("describe_text", restaurant.getDescription());
                params.put("timeopen", restaurant.getTimeOpen().toString());
                params.put("timeclose", restaurant.getTimeClose().toString());
                params.put("lat", String.valueOf(restaurant.getLocation().getLatitude()));
                params.put("lon", String.valueOf(restaurant.getLocation().getLongitude()));
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int deleteDish(final int id_rest, final String name, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.DELETEDISH;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(id_rest));
                params.put("name", name);
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int deleteAccount(final String username, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.DELETEACCOUNT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int updateAccount(final Owner owner, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.UPDATEACCOUNT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", owner.getUsername());
                params.put("token", token);
                params.put("password", owner.getPassword());
                params.put("name", owner.getName());
                params.put("phone_number", owner.getPhoneNumber());
                params.put("email", owner.getEmail());
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int updateDish(final int id_rest, final Dish dish, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.UPDATEDISH;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(id_rest));
                params.put("name", dish.getName());
                params.put("token", token);
                params.put("price", String.valueOf(dish.getPrice()));
                params.put("url_image", dish.getUrlImage());
                params.put("id_catalog", String.valueOf(dish.getCatalog().getId()));
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int updateLocation(final int id_rest, final Location location, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.UPDATELOCAION;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(id_rest));
                params.put("lat", String.valueOf(location.getLatitude()));
                params.put("lon", String.valueOf(location.getLongitude()));
                params.put("token", token);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int updateRestaurant(final Restaurant restaurant, final String token, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.UPDATERESTAURANT;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_rest", String.valueOf(restaurant.getId()));
                params.put("token", token);
                params.put("name", restaurant.getName());
                params.put("address", restaurant.getAddress());
                params.put("phone_number", restaurant.getPhoneNumber());
                params.put("describe_text", restaurant.getDescription());
                params.put("timeopen", restaurant.getTimeOpen().toString());
                params.put("timeclose", restaurant.getTimeClose().toString());
                params.put("lat", String.valueOf(restaurant.getLocation().getLatitude()));
                params.put("lon", String.valueOf(restaurant.getLocation().getLongitude()));
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static String upload(final int id_rest,final String name, final String data, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.UPLOAD;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("200")){
                                urlImage = response.getString("url");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
                    @Override
                    public byte[] getBody() {
                        byte[] body = data.getBytes();
                        return body;
                    }
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", name);
//                      params.put("data", data);
                        params.put("id", String.valueOf(id_rest));
                        return params;
                    }
                 };
        requestQueue.add(request);
        return urlImage;
    }

    public static int deletePicture(final String urlImage, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.DELETEPICTURE;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("url", urlImage);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int resetPassword(final String email, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.RESETPASSWORD;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }

    public static int checkCode(final String email, final String codeCheck, Context context){
        //create a request
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String url = Constant.BASEURL + Constant.CHECKCODE;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            code = Integer.parseInt(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("code", codeCheck);
                return params;
            }
        };
        requestQueue.add(request);
        return code;
    }
}
