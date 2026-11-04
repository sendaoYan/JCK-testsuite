/*
 * @test
 * @summary java.util.MissingFormatWidthException API conformance: getFormatSpecifier.
 * @run main MissingFormatWidthExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.MissingFormatWidthException;

public class MissingFormatWidthExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMissingFormatWidthException() {
        MissingFormatWidthException e = new MissingFormatWidthException("%-d");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("%-d".equals(e.getFormatSpecifier()), "getFormatSpecifier");
    }

    public static void main(String[] args) {
        testMissingFormatWidthException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
