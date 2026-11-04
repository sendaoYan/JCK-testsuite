/*
 * @test
 * @summary JLS 7.5.4 (Java SE 8): two or more static-import-on-demand
 *          declarations naming the same type are redundant; all but one are
 *          ignored.
 * @run main StaticImportOnDemandDuplicateIgnored
 */

import static java.lang.Math.*;
import static java.lang.Math.*;

public class StaticImportOnDemandDuplicateIgnored {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(sqrt(9.0) == 3.0, "duplicate static-import-on-demand ignored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
