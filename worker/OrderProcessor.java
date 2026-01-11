package worker;

import model.Order;
import model.OrderStatus;
import service.OrderService;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class OrderProcessor implements Runnable {

    private final Order order;
    private final OrderService service;
    private final Semaphore rateLimiter;

    private static final Random random = new Random();

    public OrderProcessor(Order order,
                          OrderService service,
                          Semaphore rateLimiter) {
        this.order = order;
        this.service = service;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {

        int attempts = 0;

        try {
            //Acquires a permit, if one is available and returns immediately, reducing the number of available permits by one
            rateLimiter.acquire(); // RATE LIMIT

            while (attempts < 3) {//retry up to 3 times
                attempts++;

                long sleepTime = 100 + random.nextInt(400);
                long threadId = Thread.currentThread().getId();

                System.out.println(
                        "[Thread-" + threadId + "] START OrderID=" +
                        order.getOrderId() +
                        ", CustomerID=" + order.getCustomerId() +
                        ", Priority=" + order.getPriority() +
                        ", Sleep=" + sleepTime + "ms"
                );

                service.updateStatus(order.getOrderId(), OrderStatus.PROCESSING);
                Thread.sleep(sleepTime);

                // 20% failure simulation
                if (random.nextInt(10) < 2) {
                    service.incrementRetry(order.getOrderId());
                    throw new RuntimeException("Failed");
                }

                service.updateStatus(order.getOrderId(), OrderStatus.COMPLETED);

                System.out.println(
                        "[Thread-" + threadId + "] DONE OrderID=" +
                        order.getOrderId()
                );
                return;
            }

            service.updateStatus(order.getOrderId(), OrderStatus.FAILED);

        } catch (Exception e) {
            service.updateStatus(order.getOrderId(), OrderStatus.FAILED);
        } finally {
            //Releases a permit, increasing the number of available permits by one
            rateLimiter.release();
        }
    }
}
