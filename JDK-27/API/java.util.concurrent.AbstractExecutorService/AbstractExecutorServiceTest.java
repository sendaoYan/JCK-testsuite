/*
 * @test
 * @summary java.util.concurrent.AbstractExecutorService conformance: white-box structure + black-box behavior.
 * @run main AbstractExecutorServiceTest
 */

import java.util.*;
import java.util.concurrent.*;

public class AbstractExecutorServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.AbstractExecutorService");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(ExecutorService.class.isAssignableFrom(c), "implements ExecutorService");
        hasMethod(c, "submit", Runnable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "invokeAll", Collection.class);
        hasMethod(c, "invokeAll", Collection.class, long.class, TimeUnit.class);
        hasMethod(c, "invokeAny", Collection.class);
        hasMethod(c, "invokeAny", Collection.class, long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DirectExecutor ses = new DirectExecutor();
        check(ses.submit(() -> 3).get() == 3, "submit(Callable) returns result");
        check(ses.submit(() -> { }).get() == null, "submit(Runnable) returns null");
        check(ses.submit(() -> { }, "v").get().equals("v"), "submit(Runnable, T) returns T");

        List<Callable<Integer>> tasks = Arrays.asList(() -> 1, () -> 2, () -> 3);
        List<Future<Integer>> futures = ses.invokeAll(tasks);
        check(futures.size() == 3, "invokeAll returns all futures");
        check(futures.get(0).get() == 1 && futures.get(1).get() == 2 && futures.get(2).get() == 3,
              "invokeAll preserves order");
        check(futures.stream().allMatch(Future::isDone), "invokeAll tasks all done");

        List<Future<Integer>> timed = ses.invokeAll(tasks, 1, TimeUnit.SECONDS);
        check(timed.size() == 3 && timed.stream().allMatch(Future::isDone), "invokeAll(timeout) all done");

        Integer any = ses.invokeAny(Arrays.asList(() -> 7));
        check(any == 7, "invokeAny returns a result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static class DirectExecutor extends AbstractExecutorService {
        public void shutdown() {}
        public List<Runnable> shutdownNow() { return Collections.emptyList(); }
        public boolean isShutdown() { return false; }
        public boolean isTerminated() { return false; }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return false; }
        public void execute(Runnable command) { command.run(); }
    }}
