/*
 * @test
 * @summary java.util.prefs.InvalidPreferencesFormatException conformance.
 * @run main InvalidPreferencesFormatExceptionTest
 */

import java.util.prefs.InvalidPreferencesFormatException;

public class InvalidPreferencesFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidPreferencesFormatException() {
        InvalidPreferencesFormatException e = new InvalidPreferencesFormatException("msg");
        check(e instanceof Exception, "is Exception");
        check("msg".equals(e.getMessage()), "message");
        InvalidPreferencesFormatException e2 =
                new InvalidPreferencesFormatException(new RuntimeException("c"));
        check(e2.getCause() instanceof RuntimeException, "cause");
    }

    public static void main(String[] args) {
        testInvalidPreferencesFormatException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
