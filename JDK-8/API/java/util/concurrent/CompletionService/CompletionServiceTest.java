/*
 * @test
 * @summary java.util.concurrent.CompletionService API conformance: submit/take.
 * @run main CompletionServiceTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CompletionServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCompletionService() throws Exception {
        CompletionService<String> cs =
            new ExecutorCompletionService<String>(Executors.newSingleThreadExecutor());
        cs.submit(() -> "result");
        check("result".equals(cs.take().get()), "take returns the completed task");
    }

    // ---- white-box: interface structure + generics ----
    static void testWhiteBoxStructure() throws Exception {
        Class<CompletionService> c = CompletionService.class;
        check(c.isInterface(), "CompletionService is an interface");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        check("V".equals(c.getTypeParameters()[0].getName()), "type parameter named V");
        check(c.getDeclaredMethods().length == 5, "exactly 5 declared methods (locked method face)");
        check(c.getMethod("submit", Callable.class) != null, "submit(Callable) present");
        check(c.getMethod("submit", Runnable.class, Object.class) != null,
              "submit(Runnable, V) present");
        check(c.getMethod("take") != null, "take() present");
        check(c.getMethod("poll") != null, "poll() present");
        check(c.getMethod("poll", long.class, TimeUnit.class) != null,
              "poll(long, TimeUnit) present");
        check(c.getMethod("take").getExceptionTypes().length == 1, "take() declares checked exceptions");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("take").getModifiers()),
              "take() is abstract (no default impl)");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("submit", Callable.class).getModifiers()),
              "submit(Callable) is abstract");
        check(CompletionService.class.isAssignableFrom(ExecutorCompletionService.class),
              "ExecutorCompletionService implements CompletionService");
        check(!ExecutorService.class.isAssignableFrom(CompletionService.class)
                  && !CompletionService.class.isAssignableFrom(ExecutorService.class),
              "CompletionService and ExecutorService are unrelated interfaces");
    }

    // ---- generic typing through the interface reference ----
    static void testGenericTyping() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(pool);
            // Callable<V> path
            Future<Integer> f = cs.submit(new Callable<Integer>() {
                public Integer call() { return 40 + 2; }
            });
            check(f.get() == 42, "submit(Callable) result via Future");
            // Runnable + fixed result path: get() returns the supplied V
            Future<Integer> fr = cs.submit(new Runnable() { public void run() { } }, 7);
            check(fr.get() == 7, "submit(Runnable, V) yields the fixed result");
            // the completion queue exposes the same futures
            check(cs.take() == f || cs.take() == fr,
                  "take() hands back a queued Future (completion queue populated)");
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testCompletionService();
        testWhiteBoxStructure();
        testGenericTyping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
