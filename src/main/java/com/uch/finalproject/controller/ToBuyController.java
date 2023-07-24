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
import java.time.LocalDate;
import java.util.ArrayList;


@RestController
public class ToBuyController {

    /* 獲取採購食物資料列表 */
    @RequestMapping(value = "/tobuy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ToBuyPageResponse foods(@RequestParam(value = "page", required = false) Integer page,
                                            @RequestParam("count") int count,
                                            @RequestParam("tobuyDateSortMode") int tobuyDateSortMode,
                                            HttpSession httpSession) {
        return getTobuyList(page, count, tobuyDateSortMode);
    }

//    /* 新增採購食物資料 */
//    @RequestMapping(value = "/addtobuy", method = RequestMethod.POST,
//            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public ToBuyPageResponse addfoodDetail(@RequestBody ToBuyEntity data){
//        Connection conn = null;
//        PreparedStatement stmt = null;
//
//        try{
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");
//
//            stmt = conn.prepareStatement("INSERT INTO food_detail (name, category_no, calories, protein, saturated_fat, total_carbohydrates, dietary_fiber) " +
//                    "SELECT ?, c.category_no, ?, ?, ?, ?, ? " +
//                    "FROM category c " +
//                    "WHERE c.category = ?");
//
//            stmt.setString(1, data.getName());
//            stmt.setInt(2, data.getCalories());
//            stmt.setFloat(3, data.getProtein());
//            stmt.setFloat(4, data.getSaturatedFat());
//            stmt.setFloat(5, data.getTotalCarbohydrates());
//            stmt.setFloat(6, data.getDietaryFiber());
//            stmt.setString(7, data.getCategory());
//
//            stmt.executeUpdate();
//
//            return new ToBuyPageResponse(0, "資料新增成功",null, 0);
//
//        }catch(SQLException e) {
//            return new ToBuyPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
//        }catch(ClassNotFoundException e) {
//            return new ToBuyPageResponse(2,"資料新增失敗",null, 0);
//        }
//    }

    /* 編輯採購食物資料 */
    @RequestMapping(value = "/updatetobuy", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ToBuyPageResponse updatetobuy(@RequestBody ToBuyEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("UPDATE food_tobuy " +
                    "SET tobuy_date = ? " +
                    "WHERE tobuy_id = ?");

            stmt.setDate(1, data.getTobuyDate());
            stmt.setInt(2, data.getTobuyId());


            stmt.executeUpdate();

            return new ToBuyPageResponse(0, "資料更新成功",null, 0);

        }catch(SQLException e) {
            return new ToBuyPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new ToBuyPageResponse(3,"資料更新失敗",null, 0);
        }
    }
    /* 刪除採購食物資料 */
    @RequestMapping(value = "/deltobuy", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE,  // 傳入的資料格式
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ToBuyPageResponse delfoodDetail(@RequestBody ToBuyEntity data) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            stmt = conn.prepareStatement("DELETE FROM food_tobuy WHERE tobuy_id = ?");
            stmt.setInt(1, data.getTobuyId());

            stmt.executeUpdate();

            return new ToBuyPageResponse(0, "資料刪除成功",null, 0);

        }catch(SQLException e) {
            return new ToBuyPageResponse(e.getErrorCode(), e.getMessage(),null, 0);
        }catch(ClassNotFoundException e) {
            return new ToBuyPageResponse(4,"資料刪除失敗",null, 0);
        }
    }

    /* 搜尋採購食物資料 */
    @RequestMapping(value = "/searchtobuy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ToBuyPageResponse searchToBuy(@RequestParam("name") String keyword, int page, int count, int tobuyDateSortMode) {
        return search("fd.name", keyword, "", page, count, tobuyDateSortMode);
    }

    private ToBuyPageResponse search(String columnName, String keyword, String keyvalue, int page, int count, int tobuyDateSortMode) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");

            String sortDirection = (tobuyDateSortMode == 1) ? "DESC" : "ASC";
            String queryString = "SELECT ft.tobuy_id, name, tobuy_date FROM food_tobuy ft JOIN food_detail fd ON ft.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'" +
                    " ORDER BY tobuy_date " + sortDirection +
                    " LIMIT " + count + " OFFSET " + ((page - 1) * count);

            // 字串搜尋
            stmt = conn.prepareStatement(queryString);
            rs = stmt.executeQuery(queryString);


            ArrayList<ToBuyEntity> foods = new ArrayList<>();
            while(rs.next()) {
                ToBuyEntity toBuyEntity = new ToBuyEntity();
                toBuyEntity.setTobuyId(rs.getInt("tobuy_id"));
                toBuyEntity.setName(rs.getString("name"));
                toBuyEntity.setTobuyDate(rs.getDate("tobuy_date"));

                foods.add(toBuyEntity);
            }

            // 取得全部數量
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_tobuy ft JOIN food_detail fd ON ft.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
                    "WHERE " + columnName + " LIKE '%" + keyword + "%'");
            rs.next();
            int total = rs.getInt("c");

            return new ToBuyPageResponse(0, "資料搜尋成功", foods, total);
        } catch(SQLException e) {
            return new ToBuyPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        } catch(ClassNotFoundException e) {
            return new ToBuyPageResponse(5, "資料搜尋成功", null, 0);
        }
    }

    /* 下載CSV資料 */
    @RequestMapping(value = "/tobuydl/{uid}/csv")
    public void exportCsv(HttpServletResponse response, @PathVariable String uid) throws IOException, ServletException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        // 獲取參數，例如 page、count 和 tobuyDateSortMode
        int page = 1; // 適當設置默認值
        int count = 10; // 適當設置默認值
        int tobuyDateSortMode = 0; // 適當設置默認值

        ToBuyPageResponse foods = getTobuyList(page, count, tobuyDateSortMode);
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("編號","名稱", "採購日");
            for (ToBuyEntity foodTobuy : foods.getData().getFoods()) {
                csvPrinter.printRecord(foodTobuy.getTobuyId(), foodTobuy.getName(), foodTobuy.getTobuyDate());
            }
        } catch (IOException e) {
            throw new ServletException("Generate CSV failed");
        }
    }

//    /* 過期食物列表 */
//    @RequestMapping(value = "/expfoodlist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    public FoodPageResponse searchExpFood(int page, int count, int expDateSortMode) {
//        return searchExp(page, count, expDateSortMode);
//    }
//
//    private FoodPageResponse searchExp(int page, int count, int expDateSortMode) {
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");
//
//            // Get the current date in the format "yyyy-MM-dd"
//            LocalDate currentDate = LocalDate.now();
//            String currentDateString = currentDate.toString();
//
//            String sortDirection = (expDateSortMode == 1) ? "DESC" : "ASC";
//            String queryString ="SELECT f.stock_id, fd.food_id, name, category, buy_date, exp_date, quantity FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
//                    "WHERE exp_date <= '" + currentDateString + "'" +
//                    " ORDER BY exp_date " + sortDirection +
//                    " LIMIT " + count + " OFFSET " + ((page - 1) * count);
//
//            stmt = conn.prepareStatement(queryString);
//            rs = stmt.executeQuery(queryString);
//
//            // 將搜尋結果存到ArrayList
//            ArrayList<FoodEntity> foods = new ArrayList<>();
//            while(rs.next()) {
//                FoodEntity foodEntity = new FoodEntity();
//                foodEntity.setStockId(rs.getInt("stock_id"));
//                foodEntity.setFoodId(rs.getInt("food_id"));
//                foodEntity.setName(rs.getString("name"));
//                foodEntity.setCategory(rs.getString("category"));
//                foodEntity.setBuyDate(rs.getDate("buy_date"));
//                foodEntity.setExpDate(rs.getDate("exp_date"));
//                foodEntity.setQuantity(rs.getInt("quantity"));
//
//                foods.add(foodEntity);
//            }
//
//            // 取得全部數量
//            rs = stmt.executeQuery("SELECT count(*) as c FROM food_stock f JOIN food_detail fd ON f.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no " +
//                    "WHERE exp_date <= '" + currentDateString + "'");
//            rs.next();
//            int total = rs.getInt("c");
//
//            return new FoodPageResponse(0, "過期食物資料載入成功", foods, total);
//        } catch (ClassNotFoundException e) {
//            return new FoodPageResponse(6, "過期食物資料載入失敗", null, 0);
//        } catch (SQLException e) {
//            return new FoodPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
//        }
//    }

    /* 食物採購列表陣列 */
    private ToBuyPageResponse getTobuyList(int page, int count, int tobuyDateSortMode) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/foods?user=root&password=0000");
            stmt = conn.createStatement();

            String sortDirection = (tobuyDateSortMode == 1) ? "DESC" : "ASC";
            rs = stmt.executeQuery("select tobuy_id, name, tobuy_date from food_tobuy ft join food_detail fd on ft.food_id = fd.food_id join category c on fd.category_no = c.category_no "+
                    "ORDER BY tobuy_date " + sortDirection  +
                    " limit " + count + " offset " + ((page-1) * count));



            ArrayList<ToBuyEntity> foods = new ArrayList<>();
            while(rs.next()) {
                ToBuyEntity toBuyEntity = new ToBuyEntity();
                toBuyEntity.setTobuyId(rs.getInt("tobuy_id"));
                toBuyEntity.setName(rs.getString("name"));
                toBuyEntity.setTobuyDate(rs.getDate("tobuy_date"));

                foods.add(toBuyEntity);
            }

            // 取得全部數量
            rs = stmt.executeQuery("SELECT count(*) as c FROM food_tobuy ft JOIN food_detail fd ON ft.food_id = fd.food_id JOIN category c ON fd.category_no = c.category_no ");
            rs.next();
            int total = rs.getInt("c");

            return new ToBuyPageResponse(0, "成功", foods, total);
        } catch(SQLException e) {
            return new ToBuyPageResponse(e.getErrorCode(), e.getMessage(), null, 0);
        } catch(ClassNotFoundException e) {
            return new ToBuyPageResponse(1, "無法註冊驅動程式", null, 0);
        }
    }
}
