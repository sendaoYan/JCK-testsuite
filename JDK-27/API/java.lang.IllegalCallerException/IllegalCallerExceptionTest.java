/*
 * @test
 * @summary java.lang.IllegalCallerException conformance: exception hierarchy and constructors.
 * @run main IllegalCallerExceptionTest
 */
public class IllegalCallerExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(IllegalCallerException.class), "extends RuntimeException");
        IllegalCallerException e = new IllegalCallerException("msg"); check("msg".equals(e.getMessage()), "message");
        IllegalCallerException e0 = new IllegalCallerException(); check(e0.getMessage() == null, "no-arg");
        Throwable c = new IllegalStateException("c"); IllegalCallerException e2 = new IllegalCallerException("m", c); check(e2.getCause() == c, "cause");
        IllegalCallerException e3 = new IllegalCallerException(c); check(e3.getCause() == c, "throwable ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
