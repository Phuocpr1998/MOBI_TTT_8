package com.hcmus.dreamers.foodmap.model;

import java.util.List;

public class Guest extends User {
    private static Guest instance;
    private List<Restaurant> favRestaurant;

    private Guest() {
        super();
    }

    private Guest(String name, String email) {
        super(name, email);
    }

    public static Guest getInstance(){
        if (instance == null)
            instance = new Guest();
        return instance;
    }
}
