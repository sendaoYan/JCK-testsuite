/*
 * @test
 * @summary java.lang.WrongThreadException conformance: exception hierarchy and constructors.
 * @run main WrongThreadExceptionTest
 */
public class WrongThreadExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(WrongThreadException.class), "extends RuntimeException");
        WrongThreadException e = new WrongThreadException("msg"); check("msg".equals(e.getMessage()), "message");
        check(new WrongThreadException().getMessage() == null, "no-arg");
        Throwable c = new IllegalStateException(); check(new WrongThreadException(c).getCause() == c, "throwable ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
