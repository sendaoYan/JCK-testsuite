/*
 * @test
 * @summary java.util.FormatFlagsConversionMismatchException API conformance: getFlags/getConversion.
 * @run main FormatFlagsConversionMismatchExceptionTest
 */

import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatException;

public class FormatFlagsConversionMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormatFlagsConversionMismatchException() {
        FormatFlagsConversionMismatchException e = new FormatFlagsConversionMismatchException("+#", 'd');
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("+#".equals(e.getFlags()), "getFlags");
        check('d' == e.getConversion(), "getConversion");
        check(e.getMessage().contains("+#"), "message includes flags");
        check(e.getMessage().contains("d"), "message includes conversion");
    }

    public static void main(String[] args) {
        testFormatFlagsConversionMismatchException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
