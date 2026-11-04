/*
 * @test
 * @summary javax.management.MBeanRegistrationException conformance: exception hierarchy and construction.
 * @run main MBeanRegistrationExceptionTest
 */

public class MBeanRegistrationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.MBeanRegistrationException x = new javax.management.MBeanRegistrationException(new java.lang.Exception("cause"));
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.MBeanException").isAssignableFrom(c), "extends javax.management.MBeanException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
