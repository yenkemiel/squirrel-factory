package com.uch.finalproject.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class FoodResponse extends BaseResponse {
    ArrayList<FoodEntity> data;
    int total;

//    public FoodResponse(int code, String message, ArrayList<FoodEntity> data) {
//        super(code, message);
//
//        this.data = data;
//    }

    public FoodResponse(int code, String message, ArrayList<FoodEntity>  foods, int total) {
        super(code, message);


        this.data = foods;
        this.total = total;
    }

}
