import model.Order;
import service.OrderService;
import worker.OrderProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        OrderService service = new OrderService();

        // how many test orders to generate
        System.out.print("Enter number of test orders to generate (0 - 1000): ");
        int orderCount = scanner.nextInt();

        if (orderCount > 0 && orderCount <= 1000) {
            service.insertBulkOrders(orderCount);
        } else if (orderCount == 0) {
            System.out.println("Skipping test order generation.");
        } else {
            System.out.println("Invalid number. Max allowed is 1000.");
            return;
        }

        System.out.print("Enter rate limit (orders/sec): ");
        int rateLimit = scanner.nextInt();

        Semaphore limiter = new Semaphore(rateLimit);
        //fixed thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);

        //batch size for fetching orders
        int batchSize = 10;
        int offset = 0;

        while (true) {

            List<Order> batch =
                    service.fetchOrdersInBatch(batchSize, offset);

            if (batch.isEmpty()) break;

            // PRIORITY APPLIED INSIDE BATCH
            // creates a comparator that sorts by priority ascending (1,2,3,4,5).
            batch.sort(Comparator.comparingInt(Order::getPriority));

            for (Order order : batch) { 
                executor.submit(
                        new OrderProcessor(order, service, limiter)
                );
            }

            //move to next batch
            offset += batchSize;
        }

        executor.shutdown();
        scanner.close();
    }
}
