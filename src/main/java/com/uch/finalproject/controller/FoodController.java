package com.uch.finalproject.controller;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.uch.finalproject.FoodEntity;
import com.uch.finalproject.FoodResponse;

@RestController
public class FoodController {
    @RequestMapping(value = "/foods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodResponse foods() {
        return getFoodList();
    }

    private FoodResponse getFoodList() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.createStatement();

            // ToDo: 改query:  select name, category, buy_date, exp_date, quantity  from foods f join food_detail fd where f.food_id = fd.id;
            rs = stmt.executeQuery("select fd.food_id, name, category, buy_date, exp_date, quantity from food_stock f join food_detail fd on f.food_id = fd.food_id join category c on fd.category_no = c.category_no");
            
            ArrayList<FoodEntity> foods = new ArrayList<>();
            while(rs.next()) {
                FoodEntity foodEntity = new FoodEntity();
                foodEntity.setId(rs.getInt("food_id"));
                foodEntity.setName(rs.getString("name"));
                foodEntity.setCategory(rs.getString("category"));
                foodEntity.setBuyDate(rs.getDate("buy_date"));
                foodEntity.setExpDate(rs.getDate("exp_date"));
                foodEntity.setQuantity(rs.getInt("quantity"));

                foods.add(foodEntity);
            }

            return new FoodResponse(0, "成功", foods);
        } catch(SQLException e) {
            return new FoodResponse(e.getErrorCode(), e.getMessage(), null);
        } catch(ClassNotFoundException e) {
            return new FoodResponse(1, "無法註冊驅動程式", null);
        }
    }
}
