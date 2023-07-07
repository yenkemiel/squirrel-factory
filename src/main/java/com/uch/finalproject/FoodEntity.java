package com.uch.finalproject;

import java.sql.Date;

import lombok.Data;

@Data
public class FoodEntity {
    int id;
    String name;
    String category;
    Date buyDate;
    Date expDate;
    int quantity;
}
