import algorithm.AlgorithmType;
import model.Order;
import model.OrderPriority;
import service.OrderService;
import worker.OrderProcessor;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        OrderService service = new OrderService();

        // Sample orders for testing
        service.insertOrder(1, 5000, OrderPriority.HIGH);
        service.insertOrder(2, 1500, OrderPriority.LOW);
        service.insertOrder(3, 3000, OrderPriority.MEDIUM);
        service.insertOrder(4, 6000, OrderPriority.HIGH);
        service.insertOrder(5, 700, OrderPriority.LOW);
        service.insertOrder(6, 4300, OrderPriority.HIGH);


        // // -------- TAKE ORDERS FROM USER --------
        // System.out.print("Enter number of orders: ");
        // int n = scanner.nextInt();

        // for (int i = 1; i <= n; i++) {
        //     System.out.println("\nOrder " + i);

        //     System.out.print("Customer ID: ");
        //     int customerId = scanner.nextInt();

        //     System.out.print("Order Amount: ");
        //     double amount = scanner.nextDouble();

        //     System.out.print("Priority (HIGH / MEDIUM / LOW): ");
        //     OrderPriority priority =
        //             OrderPriority.valueOf(scanner.next().toUpperCase());

        //     service.insertOrder(customerId, amount, priority);
        // }

        // -------- FETCH NEW ORDERS --------

        List<Order> orders = service.fetchNewOrders();

        // -------- ALGORITHM SELECTION --------
        System.out.println("\nSelect Algorithm:");
        System.out.println("1. Priority-based");
        System.out.println("2. Retry-based");
        System.out.println("3. Rate-limited");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();

        AlgorithmType algo = switch (choice) {
            case 1 -> AlgorithmType.PRIORITY_BASED;
            case 2 -> AlgorithmType.RETRY_BASED;
            case 3 -> AlgorithmType.RATE_LIMITED;
            default -> throw new IllegalArgumentException("Invalid choice");
        };

        // -------- EXECUTION --------
        switch (algo) {

            case PRIORITY_BASED -> runPriorityBased(orders, service);

            case RETRY_BASED -> runRetryBased(orders, service);

            case RATE_LIMITED -> runRateLimited(orders, service);
        }

        scanner.close();
    }

    // ---------- ALGORITHM IMPLEMENTATIONS ----------

    private static void runPriorityBased(
            List<Order> orders, OrderService service) {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Order o : orders)
            executor.submit(new OrderProcessor(o, service, false));
        executor.shutdown();
    }

    // Retry-based processing with retries on failure

    private static void runRetryBased(
            List<Order> orders, OrderService service) {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Order o : orders)
            executor.submit(new OrderProcessor(o, service, true));
        executor.shutdown();
    }

    // Rate-limited processing to control order processing rate
    
    private static void runRateLimited(
            List<Order> orders, OrderService service) {

        int N = 2; // orders per second
        Semaphore limiter = new Semaphore(N);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (Order o : orders) {
            try {
                limiter.acquire();
                executor.submit(() -> {
                    try {
                        new OrderProcessor(o, service, false).run();
                    } finally {
                        limiter.release();
                    }
                });
                Thread.sleep(1000 / N);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
}
