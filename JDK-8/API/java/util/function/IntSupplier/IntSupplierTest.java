/*
 * @test
 * @summary java.util.function.IntSupplier API conformance: getAsInt.
 * @run main IntSupplierTest
 */

import java.util.function.IntSupplier;

public class IntSupplierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntSupplier() {
        IntSupplier s = () -> 7;
        check(s.getAsInt() == 7, "getAsInt");
    }

    public static void main(String[] args) {
        testIntSupplier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
