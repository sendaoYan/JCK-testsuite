/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.RequestCanceledException conformance: exception hierarchy and construction (headless-safe).
 * @run main RequestCanceledExceptionTest
 */

public class RequestCanceledExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.corba.se.impl.protocol.RequestCanceledException x = new com.sun.corba.se.impl.protocol.RequestCanceledException(0);
        check(Throwable.class.isAssignableFrom(x.getClass()), "is a Throwable");
        check(java.lang.RuntimeException.class.isAssignableFrom(x.getClass()), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
