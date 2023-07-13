package com.uch.finalproject.model;

import java.sql.Date;
import java.util.ArrayList;

import lombok.Data;

@Data
public class FoodEntity {
    int stockId;
    int foodId;
    String name;
    String category;
    Date buyDate;
    Date expDate;
    int quantity;
    ArrayList<FoodEntity> foods;
    int total;
}

