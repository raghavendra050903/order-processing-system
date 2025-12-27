package service;

import db.DatabaseManager;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public synchronized void insertOrder(
            int customerId, double amount, OrderPriority priority) {

        String sql =
                "INSERT INTO orders (customer_id, order_amount, status, priority) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setDouble(2, amount);
            ps.setString(3, OrderStatus.NEW.name());
            ps.setString(4, priority.name());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Priority-based fetching (HIGH → MEDIUM → LOW)
     */
    public synchronized List<Order> fetchNewOrders() {

        List<Order> orders = new ArrayList<>();

        String sql =
                "SELECT * FROM orders WHERE status='NEW' " +
                "ORDER BY CASE priority " +
                "WHEN 'HIGH' THEN 1 " +
                "WHEN 'MEDIUM' THEN 2 " +
                "WHEN 'LOW' THEN 3 END";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getDouble("order_amount"),
                        rs.getString("order_time"),
                        OrderStatus.valueOf(rs.getString("status")),
                        OrderPriority.valueOf(rs.getString("priority")),
                        rs.getInt("retry_count")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public synchronized void updateStatus(int orderId, OrderStatus status) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "UPDATE orders SET status=? WHERE order_id=?")) {

            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void incrementRetry(int orderId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "UPDATE orders SET retry_count = retry_count + 1 WHERE order_id=?")) {

            ps.setInt(1, orderId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
