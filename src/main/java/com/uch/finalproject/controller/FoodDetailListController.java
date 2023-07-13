package com.uch.finalproject.controller;

import java.sql.*;
import java.util.ArrayList;

import com.uch.finalproject.model.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.uch.finalproject.model.BaseResponse;
import com.uch.finalproject.model.FoodDetailListEntity;
import com.uch.finalproject.model.FoodResponse;


@RestController
public class FoodDetailListController {
    /* 獲取食物營養資料列表 */
    @RequestMapping(value = "/foodDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodDetailListResponse foods(int page, int count, int caloriesSortMode) {
        return getFoodList(page, count, caloriesSortMode);
    }

    /* 新增食物營養資料 */
    @RequestMapping(value = "/addfoodDetail", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
    produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodDetailListResponse addfoodDetail(@RequestBody FoodDetailEntity data){
     Connection conn = null;
    PreparedStatement stmt = null;

        try{
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("INSERT INTO food_detail (name, category_no, calories, protein, saturated_fat, total_carbohydrates, dietary_fiber) " +
                    "SELECT ?, c.category_no, ?, ?, ?, ?, ? " +
                    "FROM category c " +
                    "WHERE c.category = ?");

            stmt.setString(1, data.getName());
            stmt.setInt(2, data.getCalories());
            stmt.setFloat(3, data.getProtein());
            stmt.setFloat(4, data.getSaturatedFat());
            stmt.setFloat(5, data.getTotalCarbohydrates());
            stmt.setFloat(6, data.getDietaryFiber());
            stmt.setString(7, data.getCategory());

            stmt.executeUpdate();

        return new FoodDetailListResponse(0, "資料新增成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListResponse(2,"資料新增失敗",null, 0);
        }
    }
    /* 編輯食物營養資料 */
    @RequestMapping(value = "/updatefoodDetail", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse updatefoodDetail(@RequestBody FoodDetailEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("UPDATE food_detail fd " +
                    "JOIN category c ON fd.category_no = c.category_no " +
                    "SET fd.name = ?, fd.category_no = c.category_no, fd.calories = ?, fd.protein = ?, " +
                    "fd.saturated_fat = ?, fd.total_carbohydrates = ?, fd.dietary_fiber = ? " +
                    "WHERE fd.food_id = ? AND c.category = ?");

            stmt.setString(1, data.getName());
            stmt.setInt(2, data.getCalories());
            stmt.setFloat(3, data.getProtein());
            stmt.setFloat(4, data.getSaturatedFat());
            stmt.setFloat(5, data.getTotalCarbohydrates());
            stmt.setFloat(6, data.getDietaryFiber());
            stmt.setInt(7, data.getFoodId());
            stmt.setString(8, data.getCategory());


            stmt.executeUpdate();

            return new FoodDetailListResponse(0, "資料更新成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListResponse(3,"資料更新失敗",null, 0);
        }
    }
    /* 刪除食物營養資料 */
    @RequestMapping(value = "/delfoodDetail", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse delfoodDetail(@RequestBody FoodDetailEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("DELETE FROM food_detail WHERE food_id = ?");
            stmt.setInt(1, data.getFoodId());

            stmt.executeUpdate();

            return new FoodDetailListResponse(0, "資料刪除成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListResponse(4,"資料刪除失敗",null, 0);
        }
    }
    /* 食物營養列表陣列 */
    private FoodDetailListResponse getFoodList(int page, int count, int caloriesSortMode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.createStatement();

            // ToDo: 改query:  select name, category, buy_date, exp_date, quantity  from foods f join food_detail fd where f.food_id = fd.id;
            rs = stmt.executeQuery("select fd.food_id , name, fd.category_no, category, calories , protein , saturated_fat, total_carbohydrates , dietary_fiber " +
                    "from food_detail fd join category c on c.category_no = fd.category_no " +
                    (caloriesSortMode == 0 ? "" : (caloriesSortMode == 1 ? "order by calories ASC":"order by calories DESC") ) +
                    " limit " + count + " offset " + ((page-1) * count));

            ArrayList<FoodDetailEntity> foods = new ArrayList<>();
            while(rs.next()) {
                FoodDetailEntity foodDetailEntity = new FoodDetailEntity();
                foodDetailEntity.setFoodId(rs.getInt("food_id"));
                foodDetailEntity.setName(rs.getString("name"));
                foodDetailEntity.setCategoryNo(rs.getInt("category_no"));
                foodDetailEntity.setCategory(rs.getString("category"));
                foodDetailEntity.setCalories(rs.getInt("calories"));
                foodDetailEntity.setProtein(rs.getFloat("protein"));
                foodDetailEntity.setSaturatedFat(rs.getFloat("saturated_fat"));
                foodDetailEntity.setDietaryFiber(rs.getFloat("dietary_fiber"));
                foodDetailEntity.setTotalCarbohydrates(rs.getFloat("total_carbohydrates"));

                foods.add(foodDetailEntity);
            }

            // 取得全部數量
            rs = stmt.executeQuery("select count(*) as c from food_detail");
            rs.next();
            int total = rs.getInt("c");

            return new FoodDetailListResponse(0, "成功", foods, total);
        } catch(SQLException e) {
            return new FoodDetailListResponse(e.getErrorCode(), "select fd.food_id , name, category, calories , protein , saturated_fat, total_carbohydrates , dietary_fiber " +
                    "from food_detail fd join category c on c.category_no = fd.category_no " +
                    (caloriesSortMode == 0 ? "" : (caloriesSortMode == 1 ? "order by calories ASC":"order by calories DESC") ) +
                    " limit " + count + " offset " + ((page-1) * count), null, 0);
        } catch(ClassNotFoundException e) {
            return new FoodDetailListResponse(1, "無法註冊驅動程式", null, 0);
        }
    }
}
