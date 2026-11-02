/*
 * @test
 * @summary javax.management.openmbean.KeyAlreadyExistsException conformance: exception hierarchy and construction.
 * @run main KeyAlreadyExistsExceptionTest
 */

public class KeyAlreadyExistsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.openmbean.KeyAlreadyExistsException x = new javax.management.openmbean.KeyAlreadyExistsException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.IllegalArgumentException").isAssignableFrom(c), "extends java.lang.IllegalArgumentException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
