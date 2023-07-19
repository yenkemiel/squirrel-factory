package com.uch.finalproject.model;


import lombok.Data;

import java.util.ArrayList;

@Data
public class FoodDetailListPageResponse extends BaseResponse {
    FoodDetailListPageEntity data;

    public FoodDetailListPageResponse(int code, String message, ArrayList<FoodDetailEntity>  foods, int total) {
        super(code, message);

        this.data = new FoodDetailListPageEntity();
        this.data.foods = foods;
        this.data.total = total;
    }
}
