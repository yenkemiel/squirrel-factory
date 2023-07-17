package com.uch.finalproject.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.uch.finalproject.model.FoodDetailEntity;
import com.uch.finalproject.model.FoodDetailResponse;
import com.uch.finalproject.model.FoodResponse;
import com.uch.finalproject.model.StringArrayResponse;

@RestController
@RequestMapping("/food")
public class DemoRemoteSelect {   
    @RequestMapping(value = "/name", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public StringArrayResponse getFoodName(String keyword) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select fd.name from food_detail fd where fd.name like '%" + keyword + "%'");
            ArrayList<String> data = new ArrayList<>();
            while(rs.next()) {
                
                data.add(rs.getString("name"));
            }

            return new StringArrayResponse(0, "成功", data);
        } catch(SQLException e) {
            return new StringArrayResponse(e.getErrorCode(), e.getMessage(), null);
        } catch(ClassNotFoundException e) {
            return new StringArrayResponse(1, "無法註冊驅動程式", null);
        }
    }
}
