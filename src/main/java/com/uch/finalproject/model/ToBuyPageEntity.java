package com.uch.finalproject.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ToBuyPageEntity {
    ArrayList<ToBuyEntity> foods;
    int total;
}
