package worker;

import model.Order;
import model.OrderStatus;
import service.OrderService;

import java.util.Random;

public class OrderProcessor implements Runnable {

    private final Order order;
    private final OrderService service;
    private final boolean retryEnabled;

    private static final Random random = new Random();

    public OrderProcessor(Order order, OrderService service, boolean retryEnabled) {
        this.order = order;
        this.service = service;
        this.retryEnabled = retryEnabled;
    }

    @Override
    public void run() {

        int attempts = 0;

        do {
            try {
                service.updateStatus(order.getOrderId(), OrderStatus.PROCESSING);

                // Simulate processing time (100–500 ms)
                Thread.sleep(100 + random.nextInt(400));

                // Simulate random failure (20%)
                if (random.nextInt(10) < 2) {
                    throw new RuntimeException();
                }

                service.updateStatus(order.getOrderId(), OrderStatus.COMPLETED);
                System.out.println("Order " + order.getOrderId() + " COMPLETED");
                return;

            } catch (Exception e) {
                attempts++;
                service.incrementRetry(order.getOrderId());

                if (!retryEnabled || attempts >= 3) {
                    service.updateStatus(order.getOrderId(), OrderStatus.FAILED);
                    System.out.println(
                            "Order " + order.getOrderId() +
                            " FAILED after " + attempts + " attempt(s)");
                    return;
                }
            }

        } while (retryEnabled);
    }
}
