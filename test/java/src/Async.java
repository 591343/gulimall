import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Async {
    public static ExecutorService executorService = Executors.newFixedThreadPool(3);
    public static void main(String[] args) throws ExecutionException, InterruptedException {



//        CompletableFuture<Integer> future01= CompletableFuture.supplyAsync(() -> {
//            int res=5/0;
//            System.out.println(Thread.currentThread());
//            return res;
//        }, executorService).whenCompleteAsync((integer, throwable) -> {
//            System.out.println("result"+integer);
//            System.out.println("异常"+throwable);
//        }).exceptionally(throwable -> 3);

        CompletableFuture<Integer> future01= CompletableFuture.supplyAsync(() -> {
            int res=10/2;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread());
            return res;
        }, executorService);

        CompletableFuture<Integer> future02= CompletableFuture.supplyAsync(() -> {
            int res=10/5;
            System.out.println(Thread.currentThread());
            return res;
        }, executorService);

        CompletableFuture<String> stringCompletableFuture = future01.applyToEitherAsync(future02, Object::toString, executorService
        );

        System.out.println(stringCompletableFuture.get());
        executorService.shutdown();

//
//        try {
//            System.out.println(Thread.currentThread()+" "+future.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }finally {
//            executorService.shutdown();
//        }


    }
}
