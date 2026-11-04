/*
 * @test
 * @summary java.util.function.BooleanSupplier API conformance: getAsBoolean.
 * @run main BooleanSupplierTest
 */

import java.util.function.BooleanSupplier;

public class BooleanSupplierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBooleanSupplier() {
        BooleanSupplier always = () -> true;
        check(always.getAsBoolean(), "getAsBoolean");
        BooleanSupplier never = () -> false;
        check(!never.getAsBoolean(), "getAsBoolean false");
    }

    public static void main(String[] args) {
        testBooleanSupplier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
