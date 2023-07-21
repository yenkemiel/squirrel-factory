package com.uch.finalproject.model;


import lombok.Data;

import java.util.ArrayList;

@Data
public class ToBuyPageResponse extends BaseResponse {
    ToBuyPageEntity data;

    public ToBuyPageResponse(int code, String message, ArrayList<ToBuyEntity> foods, int total) {
        super(code, message);

        this.data = new ToBuyPageEntity();
        this.data.foods = foods;
        this.data.total = total;
    }
}
