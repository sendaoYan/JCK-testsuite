/*
 * @test
 * @summary JLS 7.5.5 (Example 7.5.5-3): a single-type-import declaration
 *          resolves an ambiguity introduced by single-module-import
 *          declarations.
 * @run main SingleModuleImportResolvedBySingleType
 */

import module java.base;
import module java.desktop;
import java.util.List;

public class SingleModuleImportResolvedBySingleType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        List<String> l = List.of("x");
        check(l.size() == 1, "List usable");
        check("x".equals(l.get(0)), "List resolved to java.util.List");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
