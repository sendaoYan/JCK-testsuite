/*
 * @test
 * @summary java.util.IllformedLocaleException API conformance: getErrorIndex.
 * @run main IllformedLocaleExceptionTest
 */

import java.util.IllformedLocaleException;

public class IllformedLocaleExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllformedLocaleException() {
        IllformedLocaleException e = new IllformedLocaleException("msg", 3);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check(e.getErrorIndex() == 3, "getErrorIndex");
        check(new IllformedLocaleException("m").getErrorIndex() == -1, "no-index ctor uses -1");
    }

    public static void main(String[] args) {
        testIllformedLocaleException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
