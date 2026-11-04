/*
 * @test
 * @summary javax.management.RuntimeErrorException conformance: exception hierarchy and construction.
 * @run main RuntimeErrorExceptionTest
 */

public class RuntimeErrorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.RuntimeErrorException x = new javax.management.RuntimeErrorException(new java.lang.Error("cause"));
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMRuntimeException").isAssignableFrom(c), "extends javax.management.JMRuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
