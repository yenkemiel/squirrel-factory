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
import com.uch.finalproject.model.FoodEntity;
import com.uch.finalproject.model.FoodResponse;

@RestController
public class FoodController {
    @RequestMapping(value = "/foods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public FoodResponse foods() {
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
<<<<<<< Updated upstream

    private FoodResponse getFoodList() {
=======
    //搜尋食物
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

            return new FoodPageResponse(0, "搜尋成功", foods, total);
        } catch (ClassNotFoundException e) {
            return new FoodPageResponse(1, "找不到驅動程式", null, 0);
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


    private FoodPageResponse getFoodList(int page, int count, int expDateSortMode) {
>>>>>>> Stashed changes
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
    }
}
