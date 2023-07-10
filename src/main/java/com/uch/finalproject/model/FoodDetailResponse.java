package com.uch.finalproject.model;

import lombok.Data;

@Data
public class FoodDetailResponse extends BaseResponse {
    FoodDetailEntity data;

    public FoodDetailResponse(int code, String message, FoodDetailEntity data) {
        super(code, message);

        this.data = data;
    }
    
}
