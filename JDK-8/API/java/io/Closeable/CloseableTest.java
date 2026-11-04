/*
 * @test
 * @summary java.io.Closeable conformance (pure black-box): lambda/class
 *          implementations, AutoCloseable subtyping, try-with-resources invokes
 *          close exactly once, close is idempotent for implementors that choose
 *          so, a close failure propagates out of try-with-resources, JDK classes
 *          used through the interface.
 *          Deepening: suppressed exceptions (body vs close, and two failing closes),
 *          reverse-order closing for three resources, nested try-with-resources,
 *          explicit close inside the body followed by the implicit close,
 *          AutoCloseable-not-Closeable boundary, interface structure via reflection,
 *          JDK reader/writer behavior through the interface.
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

    static class Failing implements Closeable {
        final String name;
        Failing(String n) { name = n; }
        public void close() throws IOException { throw new IOException("close-" + name); }
    }

    // ---- structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Closeable");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0].getName().equals("java.lang.AutoCloseable"),
              "extends AutoCloseable only");
        java.lang.reflect.Method m = c.getMethod("close");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "close() abstract");
        check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == IOException.class,
              "close() declares throws IOException");
        check(Closeable.class.isAssignableFrom(java.io.InputStream.class),
              "InputStream is Closeable");
        check(Closeable.class.isAssignableFrom(java.io.Reader.class), "Reader is Closeable");
        check(Closeable.class.isAssignableFrom(java.io.Writer.class), "Writer is Closeable");
    }

    // ---- close failure is suppressed when the body throws ----
    static void testSuppressedException() throws Exception {
        try (Closeable c = new Failing("x")) {
            throw new IllegalStateException("body");
        } catch (IllegalStateException e) {
            check("body".equals(e.getMessage()), "the body exception propagates");
            check(e.getSuppressed().length == 1
                  && e.getSuppressed()[0] instanceof IOException,
                  "the close failure is attached as a suppressed exception");
            check("close-x".equals(e.getSuppressed()[0].getMessage()),
                  "suppressed exception keeps its message");
        }
    }

    // ---- two failing closes: the first close exception wins, later ones are suppressed ----
    static void testTwoFailingCloses() throws Exception {
        try (Closeable c1 = new Failing("outer");
             Closeable c2 = new Failing("inner")) {
            passed++;
        } catch (IOException e) {
            // resources close in reverse order: c2 ("inner") closes first and wins
            check("close-inner".equals(e.getMessage()),
                  "the exception from the first resource closed propagates");
            check(e.getSuppressed().length == 1
                  && "close-outer".equals(e.getSuppressed()[0].getMessage()),
                  "the later close failure is suppressed");
        }
    }

    // ---- three resources close in exact reverse declaration order ----
    static void testThreeResourcesReverseOrder() throws Exception {
        java.util.List<String> order = new java.util.ArrayList<String>();
        try (Closeable a = () -> order.add("a");
             Closeable b = () -> order.add("b");
             Closeable c = () -> order.add("c")) {
            check(order.isEmpty(), "nothing closed inside the block");
        }
        check(order.equals(java.util.Arrays.asList("c", "b", "a")),
              "reverse declaration order for three resources");
    }

    // ---- nested try-with-resources: inner resources close first ----
    static void testNestedTry() throws Exception {
        final java.util.List<String> order = new java.util.ArrayList<String>();
        try (Closeable outer = () -> order.add("outer")) {
            try (Closeable inner = () -> order.add("inner")) {
                check(order.isEmpty(), "nothing closed yet");
            }
            check(order.equals(java.util.Arrays.asList("inner")),
                  "the inner resource closed when its block ended");
        }
        check(order.equals(java.util.Arrays.asList("inner", "outer")),
              "then the outer resource closed");
    }

    // ---- an explicit close() in the body is still followed by the implicit close ----
    static void testExplicitCloseInsideBody() throws Exception {
        Counting c = new Counting();
        try (Counting r = c) {
            r.close();                                  // explicit close inside the body
            check(c.closes == 1, "explicit close counted");
        }
        check(c.closes == 2, "the implicit close still runs after an explicit one");
    }

    // ---- AutoCloseable vs Closeable boundary ----
    static void testAutoCloseableBoundary() throws Exception {
        AutoCloseable ac = new AutoCloseable() {
            public void close() { }
        };
        check(!(ac instanceof Closeable),
              "a plain AutoCloseable is NOT a Closeable");
        Closeable c = new Counting();
        check(c instanceof AutoCloseable, "every Closeable is an AutoCloseable");
        // a checked-exception-free close still satisfies the interface
        boolean[] ran = {false};
        Closeable quiet = () -> ran[0] = true;
        quiet.close();
        check(ran[0], "a no-throw lambda implementation works");
    }

    // ---- JDK readers/writers through the interface: close has real effects ----
    static void testJdkImplementors() throws Exception {
        java.io.StringReader sr = new java.io.StringReader("q");
        try (Closeable r = sr) {
            check(sr.read() == 'q', "StringReader usable while managed by the interface");
        }
        try {
            sr.read();
            check(false, "closed StringReader must throw on read");
        } catch (IOException expected) {
            check("Stream closed".equals(expected.getMessage()),
                  "the JDK close contract is observable through the interface");
        }
        java.io.StringWriter sw = new java.io.StringWriter();
        try (Closeable w = sw) {
            w.hashCode();                               // resource is non-null
        }
        sw.write("still-open");                         // StringWriter.close is a no-op
        check("still-open".equals(sw.toString()),
              "StringWriter close no-op observable through the interface");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testWhiteBox();
        testSuppressedException();
        testTwoFailingCloses();
        testThreeResourcesReverseOrder();
        testNestedTry();
        testExplicitCloseInsideBody();
        testAutoCloseableBoundary();
        testJdkImplementors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
