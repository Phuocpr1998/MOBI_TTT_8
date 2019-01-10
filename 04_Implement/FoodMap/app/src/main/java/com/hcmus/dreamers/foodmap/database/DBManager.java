package com.hcmus.dreamers.foodmap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import com.hcmus.dreamers.foodmap.Model.Catalog;
import com.hcmus.dreamers.foodmap.Model.Comment;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Restaurant;

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager extends SQLiteOpenHelper {
    // Database version
    private static final int DATABASE_VERSION = 3;      // old = 2

    // Database name
    private static final String DATABASE_NAME = "FOODMAP_DATABASE";

    // Table name
    private static final String TABLE_RESTAURANT = "RESTAURANT";
    private static final String TABLE_LOCATION = "LOCATION";
    private static final String TABLE_DISH = "DISH";
    private static final String TABLE_CATALOGS = "CATALOGS";
    private static final String TABLE_COMMENTS = "COMMENTS";
    private static final String TABLE_RANK = "RANK";

    // Drop table
    private static final String DROP_TABLE_RESTAURANT = "DROP TABLE IF EXISTS " + TABLE_RESTAURANT;
    private static final String DROP_TABLE_LOCATION = "DROP TABLE IF EXISTS " + TABLE_LOCATION;
    private static final String DROP_TABLE_DISH = "DROP TABLE IF EXISTS " + TABLE_DISH;
    private static final String DROP_TABLE_CATALOGS = "DROP TABLE IF EXISTS " + TABLE_CATALOGS;
    private static final String DROP_TABLE_COMMENTS = "DROP TABLE IF EXISTS " + TABLE_COMMENTS;
    private static final String DROP_TABLE_RANK = "DROP TABLE IF EXISTS " + TABLE_RANK;

    // Common column names
    private static final String KEY_NAME = "NAME";
    private static final String KEY_ID = "ID";
    private static final String KEY_ID_REST = "ID_REST";
    private static final String KEY_URL_IMAGE = "URL_IMAGE";

    // RESTAURANT Table - column names
    //private static final String KEY_ID = "ID";
    private static final String KEY_OWNER_USERNAME = "OWNER_USERNAME";
    //private static final String KEY_NAME = "NAME";
    private static final String KEY_ADDRESS = "ADDRESS";
    private static final String KEY_PHONE_NUMBER = "PHONE_NUMBER";
    private static final String KEY_DESCRIBE_TEXT = "DESCRIBE_TEXT";
    //private static final String KEY_URL_IMAGE = "URL_IMAGE";
    private static final String KEY_TIME_OPEN = "TIME_OPEN";
    private static final String KEY_TIME_CLOSE = "TIME_CLOSE";
    private static final String KEY_TOTAL_CHECKIN = "TOTAL_CHECKIN";
    private static final String KEY_TOTAL_SHARE = "TOTAL_SHARE";
    private static final String KEY_TOTAL_FAVORITE = "TOTAL_FAVORITE";

    // LOCATION Table - column names
    //private static final String ID_REST = "ID_REST";
    private static final String KEY_LAT = "LAT";
    private static final String KEY_LON = "LON";

    // DISH Table - column names
    //private static final String KEY_NAME = "NAME";
    private static final String KEY_PRICE = "PRICE";
    //private static final String KEY_URL_IMAGE = "URL_IMAGE";
    private static final String KEY_ID_CATALOG = "ID_CATALOG";

    // CATALOGS Table - column names
    //private static final String KEY_ID = "KEY_ID";
    //private static final String KEY_NAME = "KEY_NAME";

    // COMMENTS Table - column names
    private static final String KEY_DATE_TIME = "DATE_TIME";
    //private static final String KEY_ID_REST = "ID_REST";
    private static final String KEY_GUEST_EMAIL = "GUEST_EMAIL";
    private static final String KEY_OWNER_EMAIL = "OWNER_EMAIL";
    private static final String KEY_COMMENT = "COMMENT";

    // RANK Table - column names
    //private static final String KEY_ID_REST = "ID_REST";
    private static final String KEY_STAR = "START";



    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_RESTAURANT = "CREATE TABLE " + TABLE_RESTAURANT + "("
                + KEY_ID + " INT PRIMARY KEY,"
                + KEY_OWNER_USERNAME + " VARCHAR(20),"
                + KEY_NAME + " VARCHAR(100),"
                + KEY_ADDRESS + " VARCHAR(50),"
                + KEY_PHONE_NUMBER + " VARCHAR(11) CHECK(LENGTH(" + KEY_PHONE_NUMBER + ") IN (10, 11)),"
                + KEY_DESCRIBE_TEXT + " TEXT,"
                + KEY_URL_IMAGE + " VARCHAR(200),"
                + KEY_TIME_OPEN + " CHAR(5),"       // hh:mm
                + KEY_TIME_CLOSE + " CHAR(5),"
                + KEY_TOTAL_CHECKIN + " INT,"
                + KEY_TOTAL_FAVORITE + " INT,"
                + KEY_TOTAL_SHARE + " INT);";

        final String CREATE_TABLE_LOCATION = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_ID_REST + " INT NOT NULL,"
                + KEY_LAT + " DOUBLE NOT NULL,"
                + KEY_LON + " DOUBLE NOT NULL,"
                + "PRIMARY KEY (" + KEY_LAT + ", " + KEY_LON + "), "
                + "FOREIGN KEY (" + KEY_ID_REST + ") REFERENCES " + TABLE_RESTAURANT + "(" + KEY_ID + "));";


        final String CREATE_TABLE_DISH = "CREATE TABLE " + TABLE_DISH + "("
                + KEY_NAME + " VARCHAR(50) NOT NULL,"
                + KEY_ID_REST + " INT,"
                + KEY_PRICE + " INT,"
                + KEY_URL_IMAGE + " VARCHAR(200),"
                + KEY_ID_CATALOG + " INT,"
                + "PRIMARY KEY (" + KEY_NAME + ", " + KEY_ID_REST + "), "
                + "FOREIGN KEY (" + KEY_ID_REST + ") REFERENCES " + TABLE_RESTAURANT + "(" + KEY_ID + "), "
                + "FOREIGN KEY (" + KEY_ID_CATALOG + ") REFERENCES " + TABLE_CATALOGS + "(" + KEY_ID + "));";

        final String CREATE_TABLE_CATALOGS = "CREATE TABLE " + TABLE_CATALOGS + "("
                + KEY_ID + " INT PRIMARY KEY,"
                + KEY_NAME + " VARCHAR(30) UNIQUE NOT NULL" + ")";

        final String CREATE_TABLE_COMMENTS = "CREATE TABLE " + TABLE_COMMENTS + "("
                + KEY_DATE_TIME + " DATE NOT NULL,"     // ngày comment (dd-MM-yyy HH:mm:ss)
                + KEY_ID_REST + " INT NOT NULL,"
                + KEY_GUEST_EMAIL + " VARCHAR(30),"
                + KEY_OWNER_EMAIL + " VARCHAR(30),"
                + KEY_COMMENT + " VARCHAR(200) NOT NULL,"
                + "PRIMARY KEY (" + KEY_DATE_TIME + ", " + KEY_ID_REST + "), "
                + "FOREIGN KEY (" + KEY_ID_REST + ") REFERENCES " + TABLE_RESTAURANT + "(" + KEY_ID + "));";

        final String CREATE_TABLE_RANK = "CREATE TABLE " + TABLE_RANK + "("
                + KEY_ID_REST + " INT NOT NULL,"
                + KEY_GUEST_EMAIL + " VARCHAR(30),"
                + KEY_STAR + " INT NOT NULL CHECK(" + KEY_STAR + " >= 1 AND " + KEY_STAR + " <= 5),"
                + "PRIMARY KEY(" + KEY_ID_REST + ", " + KEY_GUEST_EMAIL + "), "
                + "FOREIGN KEY (" + KEY_ID_REST + ") REFERENCES " + TABLE_RESTAURANT + "(" + KEY_ID + "));";

        // Create table(s)
        db.execSQL(CREATE_TABLE_RESTAURANT);
        db.execSQL(CREATE_TABLE_CATALOGS);
        db.execSQL(CREATE_TABLE_LOCATION);
        db.execSQL(CREATE_TABLE_DISH);
        db.execSQL(CREATE_TABLE_COMMENTS);
        db.execSQL(CREATE_TABLE_RANK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_RANK);
        db.execSQL(DROP_TABLE_COMMENTS);
        db.execSQL(DROP_TABLE_DISH);
        db.execSQL(DROP_TABLE_LOCATION);
        db.execSQL(DROP_TABLE_CATALOGS);
        db.execSQL(DROP_TABLE_RESTAURANT);

        onCreate(db);
    }

    public void addCatalog(Catalog catalog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID, catalog.getId());
        value.put(KEY_NAME, catalog.getName());

        if (db.insertWithOnConflict(TABLE_CATALOGS, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            updateCatalog(catalog);
        }

        db.close();
    }

    public void addComment(int id_rest, Comment comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        value.put(KEY_DATE_TIME, df.format(comment.getDateTime()));
        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_COMMENT, comment.getComment());
        value.put(KEY_GUEST_EMAIL, comment.getEmailGuest());
        value.put(KEY_OWNER_EMAIL, comment.getEmailOwner());

        if (db.insertWithOnConflict(TABLE_COMMENTS, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            updateComment(id_rest, comment);
        }

        db.close();
    }

    public void addDish(int id_rest, Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_NAME, dish.getName());
        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_PRICE, dish.getPrice());
        value.put(KEY_URL_IMAGE, dish.getUrlImage());
        value.put(KEY_ID_CATALOG, dish.getCatalog().getId());

        if (db.insertWithOnConflict(TABLE_DISH, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            db.update(TABLE_DISH, value, KEY_NAME + " =? AND " + KEY_ID_REST + " =?", new String[]{dish.getName(), String.valueOf(id_rest)});
        }

        db.close();
    }

    public void addLocation(int id_rest, GeoPoint location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_LAT, location.getLatitude());
        value.put(KEY_LON, location.getLongitude());

        if (db.insertWithOnConflict(TABLE_LOCATION, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            updateLocation(id_rest, location);
        }

        db.close();
    }

    public void addRank(int id_rest, String email_guest, int star) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_GUEST_EMAIL, email_guest);
        value.put(KEY_STAR, star);

        if (db.insertWithOnConflict(TABLE_RANK, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            updateRank(id_rest, email_guest, star);
        }


        db.close();
    }

    public void addRestaurant(Restaurant restaurant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        DateFormat df = new SimpleDateFormat("HH:mm");

        // add common data
        value.put(KEY_ID, restaurant.getId());
        value.put(KEY_OWNER_USERNAME, restaurant.getOwnerUsername());
        value.put(KEY_NAME, restaurant.getName());
        value.put(KEY_ADDRESS, restaurant.getAddress());
        value.put(KEY_PHONE_NUMBER, restaurant.getPhoneNumber());
        value.put(KEY_DESCRIBE_TEXT, restaurant.getDescription());
        value.put(KEY_URL_IMAGE, restaurant.getUrlImage());
        value.put(KEY_TIME_OPEN, df.format(restaurant.getTimeOpen()));
        value.put(KEY_TIME_CLOSE, df.format(restaurant.getTimeClose()));
        value.put(KEY_TOTAL_CHECKIN, restaurant.getNum_checkin());
        value.put(KEY_TOTAL_FAVORITE, restaurant.getnFavorites());
        value.put(KEY_TOTAL_SHARE, restaurant.getnShare());

        if (db.insertWithOnConflict(TABLE_RESTAURANT, null, value, SQLiteDatabase.CONFLICT_IGNORE) == -1) {
            db.update(TABLE_RESTAURANT, value, KEY_ID + " = " + restaurant.getId(), null);
        }

        db.close();

        // add Location data
        addLocation(restaurant.getId(), restaurant.getLocation());

        // add Dishes data
        List<Dish> list_dish = restaurant.getDishes();
        for (Dish dish : list_dish) {
            this.addDish(restaurant.getId(), dish);
        }

        // add Comments
        List<Comment> list_comment = restaurant.getComments();
        for (Comment comment : list_comment) {
            this.addComment(restaurant.getId(), comment);
        }

        // add Ranks
        HashMap<String, Integer> ranks = restaurant.getRanks();
        for (Map.Entry<String, Integer> entry : ranks.entrySet()) {
            addRank(restaurant.getId(), entry.getKey(), entry.getValue());
        }
    }

    public void updateCatalog(Catalog catalog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID, catalog.getId());
        value.put(KEY_NAME, catalog.getName());

        db.update(TABLE_CATALOGS,
                value,
                KEY_ID + " =?",
                new String[]{String.valueOf(catalog.getId())});

        db.close();
    }

    public void updateComment(int id_rest, Comment comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

        value.put(KEY_DATE_TIME, df.format(comment.getDateTime()));
        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_COMMENT, comment.getComment());
        value.put(KEY_GUEST_EMAIL, comment.getEmailGuest());
        value.put(KEY_OWNER_EMAIL, comment.getEmailOwner());

        db.update(TABLE_COMMENTS,
                value,
                KEY_DATE_TIME + " =? AND " + KEY_ID_REST + " =?",
                new String[] { df.format(comment.getDateTime()), String.valueOf(id_rest)});

        db.close();
    }

    public void updateDish(int id_rest, Dish dish) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_NAME, dish.getName());
        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_PRICE, dish.getPrice());
        value.put(KEY_URL_IMAGE, dish.getUrlImage());
        value.put(KEY_ID_CATALOG, dish.getCatalog().getId());

        db.update(TABLE_DISH, value, KEY_NAME + " =? AND " + KEY_ID_REST + " =?", new String[]{dish.getName(), String.valueOf(id_rest)});

        db.close();
    }

    public void updateLocation(int id_rest, GeoPoint location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_LAT, location.getLatitude());
        value.put(KEY_LON, location.getLongitude());

        db.update(TABLE_LOCATION,
                value,
                KEY_LAT + " =? AND " + KEY_LON + " =?",
                new String[] {String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())});

        db.close();
    }

    public void updateRank(int id_rest, String email_guest, int star) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();

        value.put(KEY_ID_REST, id_rest);
        value.put(KEY_GUEST_EMAIL, email_guest);
        value.put(KEY_STAR, star);

        db.update(TABLE_RANK,
                value,
                KEY_ID_REST + " =? AND " + KEY_GUEST_EMAIL + " =?",
                new String[] {String.valueOf(id_rest), email_guest});

        db.close();
    }

    public Catalog getCatalog(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Catalog catalog = null;

        Cursor cursor = db.query(TABLE_CATALOGS, null, KEY_ID + " = ?", new String[] {String.valueOf(id)},
                null, null, null);
        if (cursor == null)
            return null;
        if (cursor.moveToFirst())
        {
            catalog = new Catalog(cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_NAME)));
        }

        cursor.close();
        db.close();

        return catalog;
    }

    public List<Catalog> getAllCatalog() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Catalog> catalogs = new ArrayList<>();

        Cursor cursor = db.query(TABLE_CATALOGS, null, null, null, null,null, KEY_ID);
        if (cursor == null)
            return null;

        if (cursor.moveToFirst())
        {
            DateFormat df = new SimpleDateFormat("HH:mm");

            do {
                Catalog catalog = new Catalog(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                catalogs.add(catalog);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return catalogs;

    }

    public int getNumCheckin(int id_rest){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESTAURANT, new String[]{KEY_TOTAL_CHECKIN}, KEY_ID + " = ?", new String[] {String.valueOf(id_rest)},
                null, null, null);
        if (cursor == null)
            return 0;

        cursor.moveToFirst();
        int numCheckin = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_CHECKIN));

        cursor.close();
        db.close();

        return numCheckin;
    }

    public int getNumFavorite(int id_rest){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESTAURANT, new String[]{KEY_TOTAL_FAVORITE}, KEY_ID + " = ?", new String[] {String.valueOf(id_rest)},
                null, null, null);
        if (cursor == null)
            return 0;

        cursor.moveToFirst();
        int numCheckin = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_FAVORITE));

        cursor.close();
        db.close();

        return numCheckin;
    }

    public int getNumShare(int id_rest){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESTAURANT, new String[]{KEY_TOTAL_SHARE}, KEY_ID + " = ?", new String[] {String.valueOf(id_rest)},
                null, null, null);
        if (cursor == null)
            return 0;

        cursor.moveToFirst();
        int numCheckin = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_SHARE));

        cursor.close();
        db.close();

        return numCheckin;
    }

    public List<Comment> getComments(int id_rest) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = db.query(TABLE_COMMENTS, null, KEY_ID_REST + " = ?", new String[] {String.valueOf(id_rest)},
                null, null, KEY_DATE_TIME);
        if (cursor == null)
            return comments;

        if (cursor.moveToFirst())
        {
            DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");

            do {
                comments.add(new Comment(df.parse(cursor.getString(cursor.getColumnIndex(KEY_DATE_TIME))),
                        cursor.getString(cursor.getColumnIndex(KEY_COMMENT)),
                        cursor.getString(cursor.getColumnIndex(KEY_GUEST_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(KEY_OWNER_EMAIL))));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return comments;
    }

    public List<Dish> getDishes(int id_rest) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Dish> dishes = new ArrayList<Dish>();

        Cursor cursor = db.query(TABLE_DISH, null, KEY_ID_REST + "=?",
                new String[] {String.valueOf(id_rest)}, null, null, null);

        if (cursor == null)
            return dishes;

        if (cursor.moveToFirst())
        {
            do {
                dishes.add(new Dish(cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_PRICE)),
                        cursor.getString(cursor.getColumnIndex(KEY_URL_IMAGE)),
                        getCatalog(cursor.getInt(cursor.getColumnIndex(KEY_ID_CATALOG)))));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return dishes;
    }

    public GeoPoint getLocation(int id_rest) {
        SQLiteDatabase db = this.getReadableDatabase();
        GeoPoint location;

        Cursor cursor = db.query(TABLE_LOCATION, null, KEY_ID_REST + "=?", new String[] {String.valueOf(id_rest)}
                , null,null,null);
        if (cursor == null)
            return null;
        cursor.moveToFirst();

        location = new GeoPoint(cursor.getDouble(cursor.getColumnIndex(KEY_LAT)),
                cursor.getDouble(cursor.getColumnIndex(KEY_LON)));

        cursor.close();
        db.close();

        return location;
    }

    public HashMap<String, Integer> getRanks(int id_rest) {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String , Integer> ranks = new HashMap<>();

        Cursor cursor = db.query(TABLE_RANK, null, KEY_ID_REST + "=?", new String[] {String.valueOf(id_rest)},
                null, null,null);
        if (cursor == null)
            return  ranks;

        if (cursor.moveToFirst())
        {
            do{
                ranks.put(cursor.getString(cursor.getColumnIndex(KEY_GUEST_EMAIL)),
                        cursor.getInt(cursor.getColumnIndex(KEY_STAR)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return ranks;
    }

    public Restaurant getRestaurant(int id_rest) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RESTAURANT, null, KEY_ID + "=?", new String[] {String.valueOf(id_rest)},
                null, null,null);
        if (cursor == null)
            return null;

        DateFormat df = new SimpleDateFormat("HH:mm");

        Restaurant restaurant = new Restaurant(id_rest,
                cursor.getString(cursor.getColumnIndex(KEY_OWNER_USERNAME)),
                cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)),
                cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER)),
                cursor.getString(cursor.getColumnIndex(KEY_DESCRIBE_TEXT)),
                cursor.getString(cursor.getColumnIndex(KEY_URL_IMAGE)),
                df.parse(cursor.getString(cursor.getColumnIndex(KEY_TIME_OPEN))),
                df.parse(cursor.getString(cursor.getColumnIndex(KEY_TIME_CLOSE))),
                getLocation(id_rest),
                getNumCheckin(id_rest),
                getNumFavorite(id_rest),
                getNumShare(id_rest));

        restaurant.setDishes(getDishes(id_rest));
        restaurant.setComments(getComments(id_rest));
        restaurant.setRanks(getRanks(id_rest));
        restaurant.setCheck(true);

        cursor.close();
        db.close();

        return restaurant;
    }

    public List<Restaurant> getAllRestaurant() throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Restaurant> restaurants = new ArrayList<>();

        Cursor cursor = db.query(TABLE_RESTAURANT, null, null, null, null,null, KEY_ID);
        if (cursor == null)
            return restaurants;

        if (cursor.moveToFirst())
        {
            DateFormat df = new SimpleDateFormat("HH:mm");

            do {
                Restaurant restaurant = new Restaurant(cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_OWNER_USERNAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)),
                        cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIBE_TEXT)),
                        cursor.getString(cursor.getColumnIndex(KEY_URL_IMAGE)),
                        df.parse(cursor.getString(cursor.getColumnIndex(KEY_TIME_OPEN))),
                        df.parse(cursor.getString(cursor.getColumnIndex(KEY_TIME_CLOSE))),
                        getLocation(cursor.getInt(cursor.getColumnIndex(KEY_ID))),
                        getNumCheckin(cursor.getInt(cursor.getColumnIndex(KEY_ID))),
                        getNumFavorite(cursor.getInt(cursor.getColumnIndex(KEY_ID))),
                        getNumShare(cursor.getInt(cursor.getColumnIndex(KEY_ID))));

                restaurant.setDishes(getDishes(restaurant.getId()));
                restaurant.setComments(getComments(restaurant.getId()));
                restaurant.setRanks(getRanks(restaurant.getId()));
                restaurant.setCheck(true);

                restaurants.add(restaurant);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return restaurants;
    }

    public void clearDB(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_RANK, null, null);
        db.delete(TABLE_COMMENTS, null, null);
        db.delete(TABLE_LOCATION, null, null);
        db.delete(TABLE_DISH, null, null);
        db.delete(TABLE_CATALOGS, null, null);
        db.delete(TABLE_RESTAURANT, null, null);

        db.close();
    }
}

