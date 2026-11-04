/*
 * @test
 * @summary java.util.UnknownFormatConversionException API conformance: getConversion.
 * @run main UnknownFormatConversionExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.UnknownFormatConversionException;

public class UnknownFormatConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnknownFormatConversionException() {
        UnknownFormatConversionException e = new UnknownFormatConversionException("y");
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check("y".equals(e.getConversion()), "getConversion");
        check(e.getMessage().contains("y"), "message includes the conversion");
    }

    public static void main(String[] args) {
        testUnknownFormatConversionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
