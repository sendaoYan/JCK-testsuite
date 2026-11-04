/*
 * @test
 * @summary JLS 7.5.3 (Java SE 8): it is permissible for one single-static-import
 *          declaration to import several methods with the same name (overloads).
 * @run main SingleStaticImportOverloadedMethods
 */

import static java.lang.Math.abs;

public class SingleStaticImportOverloadedMethods {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(abs(-3) == 3, "abs(int)");
        check(abs(-3L) == 3L, "abs(long)");
        check(abs(-3.5f) == 3.5f, "abs(float)");
        check(abs(-3.5d) == 3.5d, "abs(double)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
