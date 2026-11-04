/*
 * @test
 * @summary JLS 7.5.2 (Java SE 8): a type-import-on-demand declaration makes all
 *          accessible types of a named package available by simple name.
 * @run main TypeImportOnDemand
 */

import java.util.*;

public class TypeImportOnDemand {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        List<String> l = new ArrayList<String>();
        l.add("x");
        check(l.get(0).equals("x"), "List/ArrayList via on-demand import");

        Set<String> s = new HashSet<String>();
        s.add("y");
        check(s.contains("y"), "Set/HashSet via on-demand import");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
