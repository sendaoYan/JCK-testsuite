/*
 * @test
 * @summary javax.management.AttributeNotFoundException conformance: exception hierarchy and construction.
 * @run main AttributeNotFoundExceptionTest
 */

public class AttributeNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.AttributeNotFoundException x = new javax.management.AttributeNotFoundException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.OperationsException").isAssignableFrom(c), "extends javax.management.OperationsException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
