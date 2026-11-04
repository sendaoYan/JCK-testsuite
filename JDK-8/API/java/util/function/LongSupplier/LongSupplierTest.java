/*
 * @test
 * @summary java.util.function.LongSupplier API conformance: getAsLong.
 * @run main LongSupplierTest
 */

import java.util.function.LongSupplier;

public class LongSupplierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongSupplier() {
        LongSupplier s = () -> 42L;
        check(s.getAsLong() == 42L, "getAsLong");
    }

    public static void main(String[] args) {
        testLongSupplier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
