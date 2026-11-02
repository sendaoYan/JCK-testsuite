/*
 * @test
 * @summary java.lang.invoke.StringConcatException conformance: exception hierarchy and constructors.
 * @run main StringConcatExceptionTest
 */
import java.lang.invoke.StringConcatException;
public class StringConcatExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(java.lang.Exception.class.isAssignableFrom(StringConcatException.class), "extends Exception");
        StringConcatException e = new StringConcatException("msg"); check("msg".equals(e.getMessage()), "message");
        Throwable c = new IllegalStateException("c"); check(new StringConcatException("m", c).getCause() == c, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
