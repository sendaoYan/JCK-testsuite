/*
 * @test
 * @summary JLS 7.5.2: two or more type-import-on-demand declarations
 *          naming the same package are redundant; all but one are ignored.
 * @run main TypeImportOnDemandDuplicateIgnored
 */

import java.util.*;
import java.util.*;

public class TypeImportOnDemandDuplicateIgnored {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        List<String> l = new ArrayList<String>();
        l.add("x");
        check(l.size() == 1, "duplicate on-demand import ignored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
