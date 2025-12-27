package model;

public class Order {

    private int orderId;
    private int customerId;
    private double orderAmount;
    private String orderTime;
    private OrderStatus status;
    private OrderPriority priority;
    private int retryCount;

    public Order(int orderId, int customerId, double orderAmount,
                 String orderTime, OrderStatus status,
                 OrderPriority priority, int retryCount) {

        this.orderId = orderId;
        this.customerId = customerId;
        this.orderAmount = orderAmount;
        this.orderTime = orderTime;
        this.status = status;
        this.priority = priority;
        this.retryCount = retryCount;
    }

    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public OrderPriority getPriority() { return priority; }
}
