/*
 * @test
 * @summary JLS 7.5.3 (Java SE 8): a single-static-import declaration imports all
 *          accessible static members with a given name from a type.
 * @run main SingleStaticImport
 */

import static java.lang.Math.max;

public class SingleStaticImport {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(max(3, 5) == 5, "static method max via single-static-import");
        check(max(7L, 2L) == 7L, "overloaded max via single-static-import");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
