/*
 * @test
 * @summary JLS 7.5.4 (Java SE 8): a static-import-on-demand declaration makes
 *          all accessible static members of a named type available by simple
 *          name.
 * @run main StaticImportOnDemand
 */

import static java.lang.Math.*;

public class StaticImportOnDemand {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(sqrt(16.0) == 4.0, "static method via on-demand");
        check(abs(-7) == 7, "another static method via on-demand");
        check(PI > 3.14 && PI < 3.15, "static field via on-demand");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
