/*
 * @test
 * @summary java.util.UnknownFormatFlagsException API conformance: getFlags.
 * @run main UnknownFormatFlagsExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.UnknownFormatFlagsException;

public class UnknownFormatFlagsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnknownFormatFlagsException() {
        UnknownFormatFlagsException e = new UnknownFormatFlagsException("z");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("z".equals(e.getFlags()), "getFlags");
    }

    public static void main(String[] args) {
        testUnknownFormatFlagsException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
