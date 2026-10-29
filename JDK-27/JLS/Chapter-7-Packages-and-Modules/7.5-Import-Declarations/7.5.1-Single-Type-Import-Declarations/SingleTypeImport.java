/*
 * @test
 * @summary JLS 7.5.1 Example 7.5.1-1: a single-type-import declaration makes a
 *          class available by its simple name; a generic imported type can be
 *          used as a parameterized type or a raw type.
 * @run main SingleTypeImport
 */

import java.util.Vector;

public class SingleTypeImport {
    public static void main(String[] args) {
        Vector<String> v = new Vector<>();
        v.add("a");
        if (v.size() != 1 || !"a".equals(v.get(0)))
            throw new AssertionError("single-type-import");

        System.out.println("PASS");
    }
}
