package com.uch.finalproject.model;

import java.sql.Date;
import java.util.ArrayList;

import lombok.Data;

@Data
public class FoodEntity {
//    public ArrayList<FoodEntity> foods;
    int stockId;
    int foodId;
    String name;
    String category;
    Date buyDate;
    Date expDate;
    int quantity;

}

