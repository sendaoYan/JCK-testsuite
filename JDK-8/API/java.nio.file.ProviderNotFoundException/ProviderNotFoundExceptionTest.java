/*
 * @test
 * @summary java.nio.file.ProviderNotFoundException conformance: hierarchy and constructor.
 * @run main ProviderNotFoundExceptionTest
 */

import java.nio.file.ProviderNotFoundException;

public class ProviderNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testProviderNotFoundException() {
        check(RuntimeException.class.isAssignableFrom(ProviderNotFoundException.class), "extends RuntimeException");
        ProviderNotFoundException e = new ProviderNotFoundException();
        check(e instanceof RuntimeException, "instanceof RuntimeException");
    }

    public static void main(String[] args) {
        testProviderNotFoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
