/*
 * @test
 * @summary java.util.IllegalFormatPrecisionException API conformance: getPrecision.
 * @run main IllegalFormatPrecisionExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.IllegalFormatPrecisionException;

public class IllegalFormatPrecisionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatPrecisionException() {
        IllegalFormatPrecisionException e = new IllegalFormatPrecisionException(5);
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check(e.getPrecision() == 5, "getPrecision");
    }

    public static void main(String[] args) {
        testIllegalFormatPrecisionException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
