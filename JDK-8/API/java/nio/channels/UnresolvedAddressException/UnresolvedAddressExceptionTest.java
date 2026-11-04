/*
 * @test
 * @summary java.nio.channels.UnresolvedAddressException conformance: hierarchy and constructor.
 * @run main UnresolvedAddressExceptionTest
 */

import java.nio.channels.UnresolvedAddressException;

public class UnresolvedAddressExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnresolvedAddressException() {
        check(IllegalArgumentException.class.isAssignableFrom(UnresolvedAddressException.class), "extends IllegalArgumentException");
        UnresolvedAddressException e = new UnresolvedAddressException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
    }

    public static void main(String[] args) {
        testUnresolvedAddressException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
