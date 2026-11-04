/*
 * @test
 * @summary java.nio.file.ProviderMismatchException conformance: hierarchy and constructor.
 * @run main ProviderMismatchExceptionTest
 */

import java.nio.file.ProviderMismatchException;

public class ProviderMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testProviderMismatchException() {
        check(IllegalArgumentException.class.isAssignableFrom(ProviderMismatchException.class), "extends IllegalArgumentException");
        ProviderMismatchException e = new ProviderMismatchException();
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
    }

    public static void main(String[] args) {
        testProviderMismatchException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
