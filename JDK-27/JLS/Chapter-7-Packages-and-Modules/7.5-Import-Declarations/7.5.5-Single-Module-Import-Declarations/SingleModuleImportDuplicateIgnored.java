/*
 * @test
 * @summary JLS 7.5.5: two or more single-module-import declarations naming the
 *          same module are redundant; all but one are ignored.
 * @run main SingleModuleImportDuplicateIgnored
 */

import module java.base;
import module java.base;

public class SingleModuleImportDuplicateIgnored {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        java.util.List<String> l = java.util.List.of("x");
        check(l.size() == 1, "duplicate single-module-import ignored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
