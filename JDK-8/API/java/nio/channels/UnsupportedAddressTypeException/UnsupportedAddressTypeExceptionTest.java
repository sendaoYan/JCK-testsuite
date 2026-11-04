/*
 * @test
 * @summary java.nio.channels.UnsupportedAddressTypeException conformance: hierarchy and constructor.
 * @run main UnsupportedAddressTypeExceptionTest
 */

import java.nio.channels.UnsupportedAddressTypeException;

public class UnsupportedAddressTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsupportedAddressTypeException() {
        check(IllegalArgumentException.class.isAssignableFrom(UnsupportedAddressTypeException.class), "extends IllegalArgumentException");
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
    }

    public static void main(String[] args) {
        testUnsupportedAddressTypeException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
