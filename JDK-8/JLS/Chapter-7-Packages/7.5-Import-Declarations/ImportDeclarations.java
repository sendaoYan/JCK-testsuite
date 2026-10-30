/*
 * @test
 * @summary JLS 7.5 (Java SE 8): single-type, type-import-on-demand, single-static,
 *          and static-import-on-demand declarations bring in types and members.
 * @run main ImportDeclarations
 */
import java.util.List;             // single-type import
import java.util.*;                // type-import-on-demand
import static java.lang.Math.max;  // single-static import
import static java.lang.Math.*;    // static-import-on-demand

public class ImportDeclarations {
    public static void main(String[] args) {
        List<String> l = new ArrayList<String>();   // List via single import, ArrayList via on-demand
        l.add("x");
        if (!l.get(0).equals("x")) throw new AssertionError("type imports");

        if (max(3, 5) != 5) throw new AssertionError("single-static import");
        if (abs(-7) != 7) throw new AssertionError("static-import-on-demand");

        System.out.println("PASS");
    }
}
