/*
 * @test
 * @summary java.lang.MatchException conformance: exception hierarchy and constructors.
 * @run main MatchExceptionTest
 */
public class MatchExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.RuntimeException.class.isAssignableFrom(MatchException.class), "extends RuntimeException");
        Throwable c = new IllegalArgumentException("c"); MatchException e = new MatchException("m", c); check("m".equals(e.getMessage()), "message"); check(e.getCause() == c, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
