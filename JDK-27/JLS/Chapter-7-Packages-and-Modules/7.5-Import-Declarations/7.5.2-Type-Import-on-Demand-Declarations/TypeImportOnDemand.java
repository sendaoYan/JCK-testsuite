/*
 * @test
 * @summary JLS 7.5.2 Example 7.5.2-1: a type-import-on-demand declaration makes
 *          all accessible classes and interfaces of a package available as
 *          needed.
 * @run main TypeImportOnDemand
 */

import java.util.*;

public class TypeImportOnDemand {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>();
        l.add("x");
        if (!"x".equals(l.get(0))) throw new AssertionError("on-demand import");

        System.out.println("PASS");
    }
}
