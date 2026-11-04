/*
 * @test
 * @summary javax.management.NotCompliantMBeanException conformance: exception hierarchy and construction.
 * @run main NotCompliantMBeanExceptionTest
 */

public class NotCompliantMBeanExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.NotCompliantMBeanException x = new javax.management.NotCompliantMBeanException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.OperationsException").isAssignableFrom(c), "extends javax.management.OperationsException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
