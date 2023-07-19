package com.uch.finalproject.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class FoodDetailListPageEntity {
    ArrayList<FoodDetailEntity> foods;
    int total;
}
