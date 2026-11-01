/*
 * @test
 * @summary java.util.FormatterClosedException API conformance: inheritance.
 * @run main FormatterClosedExceptionTest
 */

import java.util.FormatterClosedException;

public class FormatterClosedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormatterClosedException() {
        FormatterClosedException e = new FormatterClosedException();
        check(e instanceof IllegalStateException, "extends IllegalStateException");
    }

    public static void main(String[] args) {
        testFormatterClosedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
