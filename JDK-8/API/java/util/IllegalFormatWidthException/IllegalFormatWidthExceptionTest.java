/*
 * @test
 * @summary java.util.IllegalFormatWidthException API conformance: getWidth.
 * @run main IllegalFormatWidthExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.IllegalFormatWidthException;

public class IllegalFormatWidthExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalFormatWidthException() {
        IllegalFormatWidthException e = new IllegalFormatWidthException(8);
        check(e instanceof IllegalFormatException, "extends IllegalFormatException");
        check(e.getWidth() == 8, "getWidth");
    }

    public static void main(String[] args) {
        testIllegalFormatWidthException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
