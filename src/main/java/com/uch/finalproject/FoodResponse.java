package com.uch.finalproject;

import java.util.ArrayList;

import lombok.Data;

@Data
public class FoodResponse extends BaseResponse {
    ArrayList<FoodEntity> data;

    public FoodResponse(int code, String message, ArrayList<FoodEntity> data) {
        super(code, message);

        this.data = data;
    }

}
