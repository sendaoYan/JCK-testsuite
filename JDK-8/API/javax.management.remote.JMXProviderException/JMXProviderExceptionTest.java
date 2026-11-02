/*
 * @test
 * @summary javax.management.remote.JMXProviderException conformance: exception hierarchy and construction.
 * @run main JMXProviderExceptionTest
 */

public class JMXProviderExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.remote.JMXProviderException x = new javax.management.remote.JMXProviderException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.io.IOException").isAssignableFrom(c), "extends java.io.IOException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
