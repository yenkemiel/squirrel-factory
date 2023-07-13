package com.uch.finalproject.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class FoodDetailListEntity {
    ArrayList<FoodDetailEntity> foods;
    int total;
}
