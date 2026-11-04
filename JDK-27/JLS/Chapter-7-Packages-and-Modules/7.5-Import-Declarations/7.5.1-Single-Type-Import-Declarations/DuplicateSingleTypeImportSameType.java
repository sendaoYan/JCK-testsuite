/*
 * @test
 * @summary JLS 7.5.1: two single-type-import declarations importing
 *          the same type are redundant; the duplicate is ignored.
 * @run main DuplicateSingleTypeImportSameType
 */

import java.util.List;
import java.util.List;

public class DuplicateSingleTypeImportSameType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        List<String> l = java.util.Collections.singletonList("x");
        check(l.size() == 1, "List via duplicate import");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
