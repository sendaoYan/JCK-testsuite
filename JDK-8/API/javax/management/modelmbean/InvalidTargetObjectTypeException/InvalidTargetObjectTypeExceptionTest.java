/*
 * @test
 * @summary javax.management.modelmbean.InvalidTargetObjectTypeException conformance: exception hierarchy and construction.
 * @run main InvalidTargetObjectTypeExceptionTest
 */

public class InvalidTargetObjectTypeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.modelmbean.InvalidTargetObjectTypeException x = new javax.management.modelmbean.InvalidTargetObjectTypeException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
