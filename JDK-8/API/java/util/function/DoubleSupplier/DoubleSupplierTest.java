/*
 * @test
 * @summary java.util.function.DoubleSupplier API conformance: getAsDouble.
 * @run main DoubleSupplierTest
 */

import java.util.function.DoubleSupplier;

public class DoubleSupplierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleSupplier() {
        DoubleSupplier s = () -> 2.5;
        check(s.getAsDouble() == 2.5, "getAsDouble");
    }

    public static void main(String[] args) {
        testDoubleSupplier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
