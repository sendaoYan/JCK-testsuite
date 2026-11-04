/*
 * @test
 * @summary java.util.DuplicateFormatFlagsException API conformance: getFlags.
 * @run main DuplicateFormatFlagsExceptionTest
 */

import java.util.DuplicateFormatFlagsException;
import java.util.IllegalFormatException;

public class DuplicateFormatFlagsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDuplicateFormatFlagsException() {
        DuplicateFormatFlagsException e = new DuplicateFormatFlagsException("+#");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("+#".equals(e.getFlags()), "getFlags");
        check(e.getMessage().contains("+#"), "message includes the flags");
    }

    public static void main(String[] args) {
        testDuplicateFormatFlagsException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
