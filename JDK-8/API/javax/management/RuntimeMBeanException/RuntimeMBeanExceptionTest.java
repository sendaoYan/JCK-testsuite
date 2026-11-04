/*
 * @test
 * @summary javax.management.RuntimeMBeanException conformance: exception hierarchy and construction.
 * @run main RuntimeMBeanExceptionTest
 */

public class RuntimeMBeanExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.RuntimeMBeanException x = new javax.management.RuntimeMBeanException(new java.lang.RuntimeException("cause"));
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMRuntimeException").isAssignableFrom(c), "extends javax.management.JMRuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
