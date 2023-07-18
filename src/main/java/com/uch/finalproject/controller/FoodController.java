package com.uch.finalproject.controller;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.uch.finalproject.model.BaseResponse;
import com.uch.finalproject.model.FoodDetailListResponse;
import com.uch.finalproject.model.FoodEntity;
import com.uch.finalproject.model.FoodResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class FoodController {
    @RequestMapping(value = "/foods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodResponse foods(HttpSession httpSession) {
        return getFoodList();
    }

@RequestMapping(value = "/food", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse addFood(@RequestBody FoodEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("SELECT COUNT(*) FROM food_detail WHERE name = ?");
            stmt.setString(1, data.getName());
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next(); // 移動到結果集的第一行
            int count = resultSet.getInt(1);

            if (count == 0) {
                // 如果 name 不存在，則在 food_detail 表中插入一列資料
                stmt = conn.prepareStatement("INSERT INTO food_detail (name, category_no) " +
                        "SELECT ?, c.category_no " +
                        "FROM category c " +
                        "WHERE c.category = ?");
                stmt.setString(1, data.getName());
                stmt.setString(2, data.getCategory());
                stmt.executeUpdate();
            }

            stmt = conn.prepareStatement("INSERT INTO food_stock (food_id, buy_date, exp_date, quantity) " +
                    "SELECT fd.food_id, ?, ?, ? " +
                    "FROM food_detail fd " +
                    "JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE fd.name = ? AND c.category = ? ");
            stmt.setDate(1, data.getBuyDate());
            stmt.setDate(2, data.getExpDate());
            stmt.setInt(3, data.getQuantity());
            stmt.setString(4, data.getName());
            stmt.setString(5, data.getCategory());


            stmt.executeUpdate();

            return new BaseResponse(0, "新增成功");

        }catch(SQLException e) {
            return new BaseResponse(e.getErrorCode(), e.getMessage());
        }catch(ClassNotFoundException e) {
            return new BaseResponse(1,"無法註冊驅動程式");
        }
    }

    //編輯食物
    @RequestMapping(value = "/food", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse updateFood(@RequestBody FoodEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("UPDATE food_stock " +
                    "SET buy_date = ?, exp_date = ?, quantity = ? " +
                    "WHERE stock_id = ?");
            stmt.setDate(1, data.getBuyDate());
            stmt.setDate(2, data.getExpDate());
            stmt.setInt(3, data.getQuantity());
            stmt.setInt(4, data.getStockId());

            stmt.executeUpdate();


            return new BaseResponse(0, "新增成功");

        }catch(SQLException e) {
            return new BaseResponse(e.getErrorCode(), e.getMessage());
        }catch(ClassNotFoundException e) {
            return new BaseResponse(1,"無法註冊驅動程式");
        }
    }

    @RequestMapping(value = "/delfood", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse deleteFood(@RequestBody FoodEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("DELETE FROM food_stock WHERE stock_id = ?");
            stmt.setInt(1, data.getStockId());

            stmt.executeUpdate();
            
            return new BaseResponse(0, "刪除成功");

        }catch(SQLException e) {
            return new BaseResponse(e.getErrorCode(), e.getMessage());
        }catch(ClassNotFoundException e) {
            return new BaseResponse(1,"無法註冊驅動程式");
        }
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
            rs = stmt.executeQuery("select f.stock_id, fd.food_id, name, category, buy_date, exp_date, quantity from food_stock f join food_detail fd on f.food_id = fd.food_id join category c on fd.category_no = c.category_no");
            ArrayList<FoodEntity> foods = new ArrayList<>();
            while(rs.next()) {
                FoodEntity foodEntity = new FoodEntity();
                foodEntity.setStockId(rs.getInt("stock_id"));
                foodEntity.setFoodId(rs.getInt("food_id"));
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
    
    // 取得全部數量
        //     rs = stmt.executeQuery("select count(*) as c from food_stock");
        //     rs.next();
        //     int total = rs.getInt("c");

        //     return new FoodResponse(0, "成功", data, total);
        // } catch(SQLException e) {
        //     return new FoodDetailListResponse(e.getErrorCode(), "select fd.food_id , name, category, calories , protein , saturated_fat, total_carbohydrates , dietary_fiber " +
        //             "from food_detail fd join category c on c.category_no = fd.category_no " +
        //             (caloriesSortMode == 0 ? "" : (caloriesSortMode == 1 ? "order by calories ASC":"order by calories DESC") ) +
        //             " limit " + count + " offset " + ((page-1) * count), null, 0);
        // } catch(ClassNotFoundException e) {
        //     return new FoodDetailListResponse(1, "無法註冊驅動程式", null, 0);
        // }
    
    }
}
