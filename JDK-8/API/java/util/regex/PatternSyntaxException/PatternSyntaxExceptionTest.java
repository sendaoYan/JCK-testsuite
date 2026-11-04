/*
 * @test
 * @summary java.util.regex.PatternSyntaxException API conformance: unchecked hierarchy and
 *          pattern/description accessors.
 * @run main PatternSyntaxExceptionTest
 */

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternSyntaxExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPatternSyntaxException() {
        boolean thrown = false;
        try {
            Pattern.compile("[");
        } catch (PatternSyntaxException e) {
            thrown = true;
            check(e instanceof RuntimeException, "unchecked");
            check("[".equals(e.getPattern()), "getPattern");
            check(e.getDescription() != null, "getDescription");
        }
        check(thrown, "bad pattern throws PatternSyntaxException");
    }

    public static void main(String[] args) {
        testPatternSyntaxException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
