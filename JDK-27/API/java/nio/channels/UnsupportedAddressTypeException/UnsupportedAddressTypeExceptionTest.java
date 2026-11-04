/*
 * @test
 * @summary java.nio.channels.UnsupportedAddressTypeException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedAddressTypeExceptionTest
 */

import java.nio.channels.UnsupportedAddressTypeException;

public class UnsupportedAddressTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(UnsupportedAddressTypeException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new UnsupportedAddressTypeException() != null, "no-arg constructor");
        check(new UnsupportedAddressTypeException().getMessage() == null, "no-arg message is null");
        check(new UnsupportedAddressTypeException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        try {
            throw e;
        } catch (UnsupportedAddressTypeException again) {
            check(again == e, "rethrow preserves identity");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
