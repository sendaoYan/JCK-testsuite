/*
 * @test
 * @summary java.util.IllegalFormatConversionException API conformance: getConversion/getArgumentClass.
 * @run main IllegalFormatConversionExceptionTest
 */

import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;

public class IllegalFormatConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatConversionException() {
        IllegalFormatConversionException e = new IllegalFormatConversionException('d', String.class);
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check('d' == e.getConversion(), "getConversion");
        check(e.getArgumentClass() == String.class, "getArgumentClass");
    }

    public static void main(String[] args) {
        testIllegalFormatConversionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
