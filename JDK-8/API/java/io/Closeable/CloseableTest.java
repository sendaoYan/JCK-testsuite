/*
 * @test
 * @summary java.io.Closeable conformance (pure black-box): lambda/class
 *          implementations, AutoCloseable subtyping, try-with-resources invokes
 *          close exactly once, close is idempotent for implementors that choose
 *          so, a close failure propagates out of try-with-resources, JDK classes
 *          used through the interface.
 * @run main CloseableTest
 */

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;

public class CloseableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counting implements Closeable {
        int closes;
        boolean throwOnClose;
        public void close() throws IOException {
            closes++;
            if (throwOnClose) throw new IOException("close failed");
        }
    }

    static void testBlackBox() throws Exception {
        // 1. lambda implementation
        boolean[] closed = {false};
        Closeable lambda = () -> closed[0] = true;
        lambda.close();
        check(closed[0], "lambda close ran");
        check(lambda instanceof AutoCloseable, "Closeable is AutoCloseable at runtime");

        // 2. try-with-resources calls close exactly once, on the way out
        Counting c1 = new Counting();
        try (Counting c = c1) {
            check(c.closes == 0, "not closed inside the block");
        }
        check(c1.closes == 1, "closed exactly once by try-with-resources");

        // 3. close runs on exception, before the exception surfaces
        Counting c2 = new Counting();
        try {
            try (Counting c = c2) {
                throw new IllegalStateException("boom");
            }
        } catch (IllegalStateException expected) {
            check(c2.closes == 1, "close ran before the exception reached the caller");
        }

        // 4. a failing close surfaces its IOException
        Counting c3 = new Counting();
        c3.throwOnClose = true;
        try (Counting c = c3) {
            passed++;
        } catch (IOException ioe) {
            check("close failed".equals(ioe.getMessage()), "close failure propagated");
        }

        // 5. multiple resources close in reverse order
        java.util.List<String> order = new java.util.ArrayList<>();
        try (Closeable first = () -> order.add("first");
             Closeable second = () -> order.add("second")) {
            check(order.isEmpty(), "neither closed yet");
        }
        check(order.equals(java.util.Arrays.asList("second", "first")),
              "resources close in reverse declaration order");

        // 6. idempotent close by convention: calling close twice is safe
        Counting c4 = new Counting();
        c4.close();
        c4.close();
        check(c4.closes == 2, "implementor counts both, no exception required");

        // 7. JDK classes through the interface
        try (Closeable bais = new ByteArrayInputStream(new byte[1])) {
            check(bais instanceof Closeable, "BAIS usable as Closeable");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
