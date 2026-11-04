/*
 * @test
 * @summary java.util.function.Supplier API conformance: get.
 * @run main SupplierTest
 */

import java.util.function.Supplier;

public class SupplierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSupplier() {
        Supplier<String> s = () -> "x";
        check("x".equals(s.get()), "get");
    }

    public static void main(String[] args) {
        testSupplier();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
