/*
 * @test
 * @summary java.util.IllegalFormatCodePointException API conformance: getCodePoint.
 * @run main IllegalFormatCodePointExceptionTest
 */

import java.util.IllegalFormatCodePointException;

public class IllegalFormatCodePointExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatCodePointException() {
        IllegalFormatCodePointException e = new IllegalFormatCodePointException(0x110000);
        check(e instanceof IllegalArgumentException, "extends IllegalArgumentException");
        check(e.getCodePoint() == 0x110000, "getCodePoint");
    }

    public static void main(String[] args) {
        testIllegalFormatCodePointException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
