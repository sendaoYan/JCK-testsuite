/*
 * @test
 * @summary java.lang.RuntimeException conformance: white-box structure + black-box behavior.
 * @run main RuntimeExceptionTest
 */

public class RuntimeExceptionTest {
    static int passed;

    // Exposes the protected (String, Throwable, boolean, boolean) constructor.
    static class SuppressionRuntimeException extends RuntimeException {
        SuppressionRuntimeException(String m, Throwable c, boolean enableSuppression, boolean writableStackTrace) {
            super(m, c, enableSuppression, writableStackTrace);
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.RuntimeException");
        check(c.getSuperclass() == Exception.class, "RuntimeException extends Exception");
        check(!c.isInterface(), "RuntimeException is a class");
        check(c.getConstructor() != null && c.getConstructor(String.class) != null
              && c.getConstructor(String.class, Throwable.class) != null
              && c.getConstructor(Throwable.class) != null, "four constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testHierarchy() {
        RuntimeException e = new RuntimeException();
        check(e instanceof Exception, "RuntimeException is an Exception");
        check(e instanceof Throwable, "RuntimeException is a Throwable");
    }

    static void testConstructors() {
        RuntimeException e1 = new RuntimeException();
        check(e1.getMessage() == null && e1.getCause() == null, "no-arg ctor");
        RuntimeException e2 = new RuntimeException("msg");
        check("msg".equals(e2.getMessage()) && e2.getCause() == null, "(String) ctor");
        RuntimeException cause = new RuntimeException("cause");
        RuntimeException e3 = new RuntimeException("msg", cause);
        check("msg".equals(e3.getMessage()) && e3.getCause() == cause, "(String,Throwable) ctor");
        RuntimeException e4 = new RuntimeException(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()),
              "(Throwable) ctor message == cause.toString()");
    }

    // ---- Part 2b: black-box for the protected (String, Throwable, boolean, boolean) ctor ----
    static void testBlackBox() {
        RuntimeException cause = new RuntimeException("cause");

        SuppressionRuntimeException e = new SuppressionRuntimeException("m", cause, false, false);
        check("m".equals(e.getMessage()) && e.getCause() == cause, "4-arg ctor sets message and cause");
        e.addSuppressed(new RuntimeException("s"));
        check(e.getSuppressed().length == 0, "enableSuppression=false -> addSuppressed is a no-op");
        e.setStackTrace(new StackTraceElement[]{ new StackTraceElement("C", "m", "F.java", 1) });
        check(e.getStackTrace().length == 0, "writableStackTrace=false -> stack trace stays empty");

        SuppressionRuntimeException e2 = new SuppressionRuntimeException("m2", cause, true, true);
        e2.addSuppressed(new RuntimeException("s2"));
        check(e2.getSuppressed().length == 1, "enableSuppression=true -> addSuppressed records");
        check(e2.getStackTrace().length > 0, "writableStackTrace=true -> stack trace filled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testHierarchy();
        testConstructors();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
