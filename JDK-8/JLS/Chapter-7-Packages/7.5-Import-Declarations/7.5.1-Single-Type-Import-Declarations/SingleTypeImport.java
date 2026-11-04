/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): a single-type-import declaration makes a type
 *          available by its simple name; a generic imported type can be used as
 *          a parameterized type or a raw type.
 * @run main SingleTypeImport
 */

import java.util.Vector;

public class SingleTypeImport {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Vector<String> v = new Vector<String>();   // parameterized type
        v.add("a");
        check(v.size() == 1, "size");
        check("a".equals(v.get(0)), "element");

        Vector raw = new Vector();                  // raw type
        raw.add("b");
        check(raw.size() == 1, "raw type usable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
