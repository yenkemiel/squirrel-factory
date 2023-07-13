package com.uch.finalproject.model;

import lombok.Data;

@Data
public class FoodDetailEntity {
    int foodId;
    String name;
    int categoryNo;
    String category;
    int calories;
    float protein;
    float saturatedFat;
    float totalCarbohydrates;
    float dietaryFiber;
}
