/*
 * @test
 * @summary java.util.IllegalFormatFlagsException API conformance: getFlags.
 * @run main IllegalFormatFlagsExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.IllegalFormatFlagsException;

public class IllegalFormatFlagsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatFlagsException() {
        IllegalFormatFlagsException e = new IllegalFormatFlagsException("+#");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("+#".equals(e.getFlags()), "getFlags");
        check(e.getMessage().contains("+#"), "message includes flags");
    }

    public static void main(String[] args) {
        testIllegalFormatFlagsException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
