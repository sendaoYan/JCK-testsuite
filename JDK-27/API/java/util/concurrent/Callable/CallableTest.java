/*
 * @test
 * @summary java.util.concurrent.Callable conformance: white-box structure + black-box behavior.
 * @run main CallableTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CallableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Callable");
        check(c.isInterface(), "Callable is an interface");
        check(c.getDeclaredMethods().length == 1, "single abstract method (functional interface)");
        hasMethod(c, "call");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Callable<Integer> c = new Callable<Integer>() {
            public Integer call() { return 7; }
        };
        check(c.call() == 7, "call returns the computed value");

        // call() may throw checked exceptions
        Callable<String> boom = () -> { throw new java.io.IOException("io"); };
        try {
            boom.call();
            check(false, "expected exception");
        } catch (java.io.IOException expected) {
            check("io".equals(expected.getMessage()), "call() propagates a checked exception");
        }

        // used via an ExecutorService
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> f = exec.submit(() -> 21 * 2);
            check(f.get() == 42, "Callable submitted to executor");
        } finally {
            exec.shutdown();
        }
        check(exec.awaitTermination(10, TimeUnit.SECONDS), "executor terminates");
    }

    // ---- Part 3: functional-interface annotation + lambda/method-ref forms ----
    static void testAnnotationAndLambdas() throws Exception {
        Class<?> c = cls("java.util.concurrent.Callable");
        check(c.isAnnotationPresent(FunctionalInterface.class),
              "@FunctionalInterface annotation present");
        Callable<Integer> lambda = () -> 21 * 2;
        check(lambda.call() == 42, "lambda call returns the computed value");
        Callable<Integer> ref = "abc"::length;
        check(ref.call() == 3, "method reference call");
        Object[] arr = { "shared" };
        Callable<Object[]> identity = () -> arr;
        check(identity.call() == arr, "lambda returns the exact same array instance");
    }

    // ---- Part 4: exception propagation identity (checked/unchecked/Error) ----
    static void testExceptionPropagation() throws Exception {
        final java.io.IOException boom = new java.io.IOException("io");
        Callable<String> failing = () -> { throw boom; };
        boolean got = false;
        try { failing.call(); }
        catch (java.io.IOException expected) {
            check(expected == boom, "the exact same checked exception object propagates");
            got = true;
        }
        check(got, "checked exception from call() is relayed with identity");

        final IllegalStateException ise = new IllegalStateException("state");
        Callable<Integer> failing2 = () -> { throw ise; };
        boolean got2 = false;
        try { failing2.call(); }
        catch (IllegalStateException expected) {
            check(expected == ise, "unchecked exception propagates with identity");
            got2 = true;
        }
        check(got2, "unchecked exception from call() is relayed unwrapped");

        final LinkageError err = new LinkageError("link");
        Callable<Integer> erring = () -> { throw err; };
        boolean got3 = false;
        try { erring.call(); }
        catch (LinkageError expected) {
            check(expected == err, "Error propagates with identity");
            got3 = true;
        }
        check(got3, "Error from call() is relayed unwrapped");
    }

    // ---- Part 5: null/Void semantics + repeated calls ----
    static void testNullSemantics() throws Exception {
        Callable<Void> voidTask = () -> null;
        check(voidTask.call() == null, "Callable<Void> may return null");
        final int[] counter = { 0 };
        Callable<Integer> counting = () -> ++counter[0];
        check(counting.call() == 1 && counting.call() == 2 && counting.call() == 3,
              "repeated call() invocations behave like a normal method");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAnnotationAndLambdas();
        testExceptionPropagation();
        testNullSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
