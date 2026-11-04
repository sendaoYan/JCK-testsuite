/*
 * @test
 * @summary javax.management.JMRuntimeException conformance: exception hierarchy and construction.
 * @run main JMRuntimeExceptionTest
 */

public class JMRuntimeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.JMRuntimeException x = new javax.management.JMRuntimeException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.RuntimeException").isAssignableFrom(c), "extends java.lang.RuntimeException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
