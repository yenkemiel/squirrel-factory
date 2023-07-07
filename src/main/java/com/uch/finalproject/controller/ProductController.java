package com.uch.finalproject.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.uch.finalproject.model.ProductEntity;
import com.uch.finalproject.model.ProductResponse;

// 註釋annotation

@RestController
public class ProductController {

    // API入口
    @RequestMapping(value="/product", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse Product() {
        return getProductList();
    }

    // 取得產品清單並回傳ResponseProductEntity物件
    private ProductResponse getProductList() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 連接資料庫
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?" + 
                "user=root&password=0000");

            // 取得Statement物件
            stmt = conn.createStatement();

            // 查詢全部商品
            rs = stmt.executeQuery("select food_id, name, category, calories,protein, saturated_fat, total_carbohydrates, dietary_fiber from food_detail fd join category c on fd.category_no=c.category_no where food_id =");

            // 建立陣列存放所有商品用
            ArrayList<ProductEntity> products = new ArrayList<>();

            // 將每一筆商品資料讀出來存放到ArrayList內
            while(rs.next()) {
                ProductEntity productEntity = new ProductEntity();
                productEntity.setId(rs.getInt("id"));
                productEntity.setName(rs.getString("name"));
                productEntity.setDescription(rs.getString("description"));
                productEntity.setPrice(rs.getInt("price"));
                productEntity.setImageUrl(rs.getString("image_url"));
                productEntity.setStoreName(rs.getString("store_name"));
                productEntity.setCategory(rs.getString("category"));

                // 將取的商品資料存到ArrayList
                products.add(productEntity);
            }

            // 關資料庫相關資源
            rs.close();
            stmt.close();
            conn.close();

            return new ProductResponse(0, "sucess", products);


        } catch(ClassNotFoundException e) {
            // 無法註冊
            return new ProductResponse(1, "無法註冊", null);
        } catch(SQLException e) {
            return new ProductResponse(e.getErrorCode(), 
                    e.getMessage(), null);
        }
        
    }
}
