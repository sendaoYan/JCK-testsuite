/*
 * @test
 * @summary com.sun.corba.se.impl.io.TypeMismatchException conformance: exception hierarchy and construction (headless-safe).
 * @run main TypeMismatchExceptionTest
 */

public class TypeMismatchExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.corba.se.impl.io.TypeMismatchException x = new com.sun.corba.se.impl.io.TypeMismatchException("msg");
        check(Throwable.class.isAssignableFrom(x.getClass()), "is a Throwable");
        check(java.lang.Error.class.isAssignableFrom(x.getClass()), "extends java.lang.Error");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
