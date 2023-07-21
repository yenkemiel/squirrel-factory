package com.uch.finalproject.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import com.uch.finalproject.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class FoodController {

    /* 獲取食物倉庫資料列表 */
    @RequestMapping(value = "/foods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse foods(int page, int count, int expDateSortMode, HttpSession httpSession) {
        return getFoodList(page, count, expDateSortMode);
    }

    /* 新增食物資料 */
    @RequestMapping(value = "/addfood", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse addFood(@RequestBody FoodEntity data) {
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

            return new FoodPageResponse(0, "資料新增成功",null,0);

        }catch(SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(),null,0);
        }catch(ClassNotFoundException e) {
            return new FoodPageResponse(2,"資料新增失敗",null,0);
        }
    }

    /* 編輯食物資料 */
    @RequestMapping(value = "/food", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse updateFood(@RequestBody FoodEntity data) {
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


            return new FoodPageResponse(0, "資料更新成功",null,0);

        }catch(SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(),null,0);
        }catch(ClassNotFoundException e) {
            return new FoodPageResponse(3,"資料更新失敗",null,0);
        }
    }

    /* 刪除食物資料 */
    @RequestMapping(value = "/delfood", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse deleteFood(@RequestBody FoodEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("DELETE FROM food_stock WHERE stock_id = ?");
            stmt.setInt(1, data.getStockId());

            stmt.executeUpdate();

            return new FoodPageResponse(0, "資料刪除成功",null,0);

        }catch(SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(),null,0);
        }catch(ClassNotFoundException e) {
            return new FoodPageResponse(4,"資料刪除失敗",null,0);
        }
    }

    /* 搜尋食物資料 */
    @RequestMapping(value = "/food", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse searchFood(@RequestParam("name") String keyword, int page, int count, int expDateSortMode) {
        return search("fd.name", keyword, "", page, count, expDateSortMode);
    }

    private FoodPageResponse search(String columnName, String keyword, String keyvalue, int page, int count, int expDateSortMode) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            String sortDirection = (expDateSortMode == 1) ? "DESC" : "ASC";
            String queryString ="SELECT f.stock_id, fd.food_id, name, category, buy_date, exp_date, quantity FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'" +
                    " ORDER BY exp_date " + sortDirection +
                    " LIMIT " + count + " OFFSET " + ((page - 1) * count);
//            return new FoodResponse(0, queryString, null);

            // 字串搜尋
            stmt = conn.prepareStatement(queryString);
            rs = stmt.executeQuery(queryString);

            // 將搜尋結果存到ArrayList
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

            // 取得全部數量
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'");
            rs.next();
            int total = rs.getInt("c");

            return new FoodPageResponse(0, "資料搜尋成功", foods, total);
        } catch (ClassNotFoundException e) {
            return new FoodPageResponse(5, "資料搜尋失敗", null, 0);
        } catch (SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        }
    }

    /* 過期食物通知 */
    @RequestMapping(value = "/expfood", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodPageResponse searchExpFood(int page, int count, int expDateSortMode) {
        return searchExp(page, count, expDateSortMode);
    }

    private FoodPageResponse searchExp(int page, int count, int expDateSortMode) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            // Get the current date in the format "yyyy-MM-dd"
            LocalDate currentDate = LocalDate.now();
            String currentDateString = currentDate.toString();

            String sortDirection = (expDateSortMode == 1) ? "DESC" : "ASC";
            String queryString ="SELECT f.stock_id, fd.food_id, name, category, buy_date, exp_date, quantity FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE exp_date <= '" + currentDateString + "'" +
                    " ORDER BY exp_date " + sortDirection +
                    " LIMIT " + count + " OFFSET " + ((page - 1) * count);

            stmt = conn.prepareStatement(queryString);
            rs = stmt.executeQuery(queryString);

            // 將搜尋結果存到ArrayList
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

            // 取得全部數量
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE exp_date <= '" + currentDateString + "'");
            rs.next();
            int total = rs.getInt("c");

            return new FoodPageResponse(0, "過期食物資料載入成功", foods, total);
        } catch (ClassNotFoundException e) {
            return new FoodPageResponse(6, "過期食物資料載入失敗", null, 0);
        } catch (SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        }
    }

    /* 下載CSV資料 */
    @RequestMapping(value = "/foods/{uid}/csv")
    public void exportCsv(HttpServletResponse response, @PathVariable String uid) throws IOException, ServletException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        // 獲取參數，例如 page、count 和 expDateSortMode
        int page = 1; // 適當設置默認值
        int count = 10; // 適當設置默認值
        int expDateSortMode = 0; // 適當設置默認值

        FoodPageResponse foods = getFoodList(page, count, expDateSortMode);
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("名稱", "類別", "採購日", "到期日", "數量");
            for (FoodEntity food : foods.getData().getFoods()) {
                csvPrinter.printRecord(food.getName(), food.getCategory(), food.getBuyDate(), food.getExpDate(), food.getQuantity());
            }
        } catch (IOException e) {
            throw new ServletException("Generate CSV failed");
        }
    }

    /* 食物倉庫列表陣列 */
    private FoodPageResponse getFoodList(int page, int count, int expDateSortMode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");
            stmt = conn.createStatement();

            String sortDirection = (expDateSortMode == 1) ? "DESC" : "ASC";
            rs = stmt.executeQuery("select f.stock_id, fd.food_id, name, category, buy_date, exp_date, quantity from food_stock f join food_detail fd on f.food_id = fd.food_id join category c on fd.category_no = c.category_no "+
                    "ORDER BY exp_date " + sortDirection  +
                    " limit " + count + " offset " + ((page-1) * count));

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

            // 取得全部數量
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no ");
            rs.next();
            int total = rs.getInt("c");

            return new FoodPageResponse(0, "成功", foods, total);
        } catch(SQLException e) {
            return new FoodPageResponse(e.getErrorCode(), e.getMessage(), null,0);
        } catch(ClassNotFoundException e) {
            return new FoodPageResponse(1, "無法註冊驅動程式", null,0);
        }
    }
}
