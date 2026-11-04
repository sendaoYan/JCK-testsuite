/*
 * @test
 * @summary java.rmi.activation.UnknownGroupException conformance: exception hierarchy and construction.
 * @run main UnknownGroupExceptionTest
 */

public class UnknownGroupExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.rmi.activation.UnknownGroupException x = new java.rmi.activation.UnknownGroupException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.rmi.activation.ActivationException").isAssignableFrom(c), "extends java.rmi.activation.ActivationException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
