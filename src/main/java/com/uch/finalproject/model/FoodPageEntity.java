package com.uch.finalproject.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class FoodPageEntity {
    ArrayList<FoodEntity> foods;
    int total;
}
