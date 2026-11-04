/*
 * @test
 * @summary javax.management.openmbean.OpenDataException conformance: exception hierarchy and construction.
 * @run main OpenDataExceptionTest
 */

public class OpenDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.openmbean.OpenDataException x = new javax.management.openmbean.OpenDataException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMException").isAssignableFrom(c), "extends javax.management.JMException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
