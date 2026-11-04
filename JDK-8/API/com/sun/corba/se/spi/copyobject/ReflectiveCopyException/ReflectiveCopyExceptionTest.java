/*
 * @test
 * @summary com.sun.corba.se.spi.copyobject.ReflectiveCopyException conformance: exception hierarchy and construction (headless-safe).
 * @run main ReflectiveCopyExceptionTest
 */

public class ReflectiveCopyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.corba.se.spi.copyobject.ReflectiveCopyException x = new com.sun.corba.se.spi.copyobject.ReflectiveCopyException("msg");
        check(Throwable.class.isAssignableFrom(x.getClass()), "is a Throwable");
        check(java.lang.Exception.class.isAssignableFrom(x.getClass()), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
