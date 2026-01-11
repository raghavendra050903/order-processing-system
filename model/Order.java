package model;

public class Order {

    private final int orderId;
    private final int customerId;
    private final int priority;

    public Order(int orderId, int customerId, int priority) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.priority = priority;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getPriority() {
        return priority;
    }
}
