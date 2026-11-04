/*
 * @test
 * @summary javax.management.RuntimeOperationsException conformance: exception hierarchy and construction.
 * @run main RuntimeOperationsExceptionTest
 */

public class RuntimeOperationsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.RuntimeOperationsException x = new javax.management.RuntimeOperationsException(new java.lang.RuntimeException("cause"));
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMRuntimeException").isAssignableFrom(c), "extends javax.management.JMRuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
