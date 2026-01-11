package service;

import db.DatabaseManager;
import model.Order;
import model.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    // Insert one order
    public synchronized void insertOrder(
            int customerId, double amount, int priority) {

        String sql = """
            INSERT INTO orders (customer_id, order_amount, status, priority)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setDouble(2, amount);
            ps.setString(3, OrderStatus.NEW.name());
            ps.setInt(4, priority);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Generate 1000 orders using Java
    public void insertBulkOrders(int count) {
        for (int i = 1; i <= count; i++) {
            insertOrder(
                    1000 + i, // unique customer ID
                    100 + (i * 10),// incremental amount
                    (i % 5) + 1   // priority 1–5
            );
        }
        System.out.println(count + " orders inserted");
    }

    // Batch fetch WITHOUT priority ordering
    //limit: max number of rows to return.
    //offset: number of rows to skip from the start. This implements pagination.

    public List<Order> fetchOrdersInBatch(int limit, int offset) {

        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT order_id, customer_id, priority
            FROM orders
            WHERE status = 'NEW'
            ORDER BY order_id ASC
            LIMIT ? OFFSET ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                //inserting fetched orders into list
                orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("priority")
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
