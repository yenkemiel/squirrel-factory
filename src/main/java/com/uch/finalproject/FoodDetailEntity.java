package com.uch.finalproject;

import lombok.Data;

@Data
public class FoodDetailEntity {
    int id;
    String name;
    String category;
    int calories;
    float protein;
    float saturatedFat;
    float totalCarbohydrates;
    float dietaryFiber;
}
