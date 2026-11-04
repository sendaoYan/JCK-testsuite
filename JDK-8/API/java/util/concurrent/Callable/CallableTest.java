/*
 * @test
 * @summary java.util.concurrent.Callable API conformance: call returns a value.
 * @run main CallableTest
 */

import java.util.concurrent.Callable;

public class CallableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCallable() throws Exception {
        Callable<Integer> c = new Callable<Integer>() {
            public Integer call() {
                return 7;
            }
        };
        check(c.call() == 7, "call returns the computed value");
    }

    // ---- white-box: functional interface shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<Callable> c = Callable.class;
        check(c.isInterface(), "Callable is an interface");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        check("V".equals(c.getTypeParameters()[0].getName()), "type parameter named V");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method (functional interface)");
        check(c.getMethod("call").getReturnType() == Object.class, "call() returns V (erasure Object)");
        check(c.getMethod("call").getExceptionTypes().length == 1
                  && c.getMethod("call").getExceptionTypes()[0] == Exception.class,
              "call() declares throws Exception (checked exceptions allowed)");
        check(c.isAnnotationPresent(FunctionalInterface.class),
              "@FunctionalInterface annotation present");
    }

    // ---- lambda / method reference forms ----
    static void testLambdaForms() throws Exception {
        Callable<Integer> lambda = () -> 21 * 2;
        check(lambda.call() == 42, "lambda call returns the computed value");
        Callable<Integer> ctorRef = "abc"::length;
        check(ctorRef.call() == 3, "method reference call");
        Callable<Object[]> arrayHolder = () -> { return new Object[] { "x" }; };
        check(arrayHolder.call().length == 1, "lambda returning an array");
        Object[] arr = { "shared" };
        Callable<Object[]> identity = () -> arr;
        check(identity.call() == arr, "lambda returns the exact same array instance");
    }

    // ---- checked exception propagation (identity) ----
    static void testCheckedExceptionPropagation() throws Exception {
        final java.io.IOException boom = new java.io.IOException("io");
        Callable<String> failing = new Callable<String>() {
            public String call() throws java.io.IOException { throw boom; }
        };
        boolean got = false;
        try { failing.call(); }
        catch (java.io.IOException expected) {
            check(expected == boom, "the exact same checked exception object propagates");
            got = true;
        }
        check(got, "checked exception thrown from call() is declared and relayed");

        final InterruptedException interrupted = new InterruptedException("stop");
        Callable<Void> blocking = () -> { throw interrupted; };
        boolean got2 = false;
        try { blocking.call(); }
        catch (InterruptedException expected) {
            check(expected == interrupted, "InterruptedException propagates with identity");
            got2 = true;
        }
        check(got2, "InterruptedException (declared by throws Exception) propagates");
    }

    // ---- unchecked exception / error propagation ----
    static void testUncheckedPropagation() throws Exception {
        final IllegalStateException boom = new IllegalStateException("state");
        Callable<Integer> failing = () -> { throw boom; };
        boolean got = false;
        try { failing.call(); }
        catch (IllegalStateException expected) {
            check(expected == boom, "unchecked exception propagates with identity");
            got = true;
        }
        check(got, "unchecked exception from call() is relayed unwrapped");

        final LinkageError err = new LinkageError("link");
        Callable<Integer> erring = () -> { throw err; };
        boolean got2 = false;
        try { erring.call(); }
        catch (LinkageError expected) {
            check(expected == err, "Error propagates with identity");
            got2 = true;
        }
        check(got2, "Error from call() is relayed unwrapped");
    }

    // ---- null / Void semantics ----
    static void testNullSemantics() throws Exception {
        Callable<Void> voidTask = () -> null;
        check(voidTask.call() == null, "Callable<Void> may return null");
        Callable<String> nullString = () -> null;
        check(nullString.call() == null, "null result passes through unchanged");
    }

    // ---- stateless reuse: repeated calls ----
    static void testRepeatedCalls() throws Exception {
        final int[] counter = { 0 };
        Callable<Integer> counting = () -> ++counter[0];
        check(counting.call() == 1 && counting.call() == 2 && counting.call() == 3,
              "repeated call() invocations behave like a normal method");
    }

    public static void main(String[] args) throws Exception {
        testCallable();
        testWhiteBoxStructure();
        testLambdaForms();
        testCheckedExceptionPropagation();
        testUncheckedPropagation();
        testNullSemantics();
        testRepeatedCalls();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
