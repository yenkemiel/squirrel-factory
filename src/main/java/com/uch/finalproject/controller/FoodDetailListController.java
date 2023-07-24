package com.uch.finalproject.controller;

import com.uch.finalproject.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;


@RestController
public class FoodDetailListController {

    /* 獲取食物營養資料列表 */
    @RequestMapping(value = "/foodDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodDetailListPageResponse foods(@RequestParam(value = "page", required = false) Integer page,
                                            @RequestParam("count") int count,
                                            @RequestParam("foodIdSortMode") int foodIdSortMode,
                                            HttpSession httpSession) {
        return getFoodList(page, count, foodIdSortMode);
    }

    /* 新增食物營養資料 */
    @RequestMapping(value = "/addfoodDetail", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodDetailListPageResponse addfoodDetail(@RequestBody FoodDetailEntity data){
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

            return new FoodDetailListPageResponse(0, "資料新增成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListPageResponse(2,"資料新增失敗",null, 0);
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

            return new FoodDetailListPageResponse(0, "資料更新成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListPageResponse(3,"資料更新失敗",null, 0);
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

            return new FoodDetailListPageResponse(0, "資料刪除成功",null, 0);

        }catch(SQLException e) {
            return new FoodDetailListPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new FoodDetailListPageResponse(4,"資料刪除失敗",null, 0);
        }
    }

    /* 搜尋食物營養資料 */
    @RequestMapping(value = "/searchfoodDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodDetailListPageResponse searchFoodDetail(@RequestParam("name") String keyword, int page, int count, int foodIdSortMode) {
        return search("fd.name", keyword, "", page, count, foodIdSortMode);
    }

    private FoodDetailListPageResponse search(String columnName, String keyword, String keyvalue, int page, int count, int foodIdSortMode) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            String sortDirection = (foodIdSortMode == 1) ? "DESC" : "ASC";
            String queryString = "SELECT fd.food_id, name, fd.category_no, category, calories, protein, saturated_fat, total_carbohydrates, dietary_fiber " +
                    "FROM food_detail fd JOIN category c ON c.category_no = fd.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'" +
                    " ORDER BY food_id " + sortDirection +
                    " LIMIT " + count + " OFFSET " + ((page - 1) * count);

            // 字串搜尋
            stmt = conn.prepareStatement(queryString);
            rs = stmt.executeQuery(queryString);


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
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_detail fd JOIN category c ON c.category_no = fd.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'");
            rs.next();
            int total = rs.getInt("c");

            return new FoodDetailListPageResponse(0, "資料搜尋成功", foods, total);
        } catch(SQLException e) {
            return new FoodDetailListPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        } catch(ClassNotFoundException e) {
            return new FoodDetailListPageResponse(5, "資料搜尋成功", null, 0);
        }
    }

    /* 下載CSV資料 */
    @RequestMapping(value = "/foodDetails/{uid}/csv")
    public void exportCsv(HttpServletResponse response, @PathVariable String uid) throws IOException, ServletException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        // 獲取參數，例如 page、count 和 foodIdSortMode
        int page = 1; // 適當設置默認值
        int count = 10; // 適當設置默認值
        int foodIdSortMode = 0; // 適當設置默認值

        FoodDetailListPageResponse foods = getFoodList(page, count, foodIdSortMode);
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("編號","名稱", "類別編號", "類別", "熱量(大卡)", "蛋白質(公克)", "飽和脂肪(公克)", "總碳水化合物(公克)", "膳食纖維(公克)");
            for (FoodDetailEntity foodDetail : foods.getData().getFoods()) {
                csvPrinter.printRecord(foodDetail.getFoodId(), foodDetail.getName(), foodDetail.getCategoryNo(), foodDetail.getCategory(), foodDetail.getCalories(), foodDetail.getProtein(), foodDetail.getSaturatedFat(), foodDetail.getTotalCarbohydrates(), foodDetail.getDietaryFiber());
            }
        } catch (IOException e) {
            throw new ServletException("Generate CSV failed");
        }
    }

    /* 食物營養列表陣列 */
    private FoodDetailListPageResponse getFoodList(int page, int count, int foodIdSortMode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.createStatement();

            String sortDirection = (foodIdSortMode == 1) ? "DESC" : "ASC";
            rs = stmt.executeQuery("SELECT fd.food_id, name, fd.category_no, category, calories, protein, saturated_fat, total_carbohydrates, dietary_fiber " +
                    "FROM food_detail fd JOIN category c ON c.category_no = fd.category_no " +
                    "ORDER BY food_id " + sortDirection + " " +
                    "LIMIT " + count + " OFFSET " + ((page - 1) * count));


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

            return new FoodDetailListPageResponse(0, "成功", foods, total);
        } catch(SQLException e) {
            return new FoodDetailListPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        } catch(ClassNotFoundException e) {
            return new FoodDetailListPageResponse(1, "無法註冊驅動程式", null, 0);
        }
    }
}
