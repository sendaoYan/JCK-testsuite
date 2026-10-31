/*
 * @test
 * @summary java.util.MissingFormatArgumentException API conformance: getFormatSpecifier.
 * @run main MissingFormatArgumentExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;

public class MissingFormatArgumentExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMissingFormatArgumentException() {
        MissingFormatArgumentException e = new MissingFormatArgumentException("s");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("s".equals(e.getFormatSpecifier()), "getFormatSpecifier");
        check(e.getMessage().contains("s"), "message includes the specifier");
    }

    public static void main(String[] args) {
        testMissingFormatArgumentException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
