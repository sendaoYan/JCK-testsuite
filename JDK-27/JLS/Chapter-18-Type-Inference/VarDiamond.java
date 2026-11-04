/*
 * @test
 * @summary JLS 14.4.1 / 18.5.2: a local variable declared with var infers its type
 *          from a diamond class instance creation initializer.
 * @run main VarDiamond
 */

import java.util.ArrayList;
import java.util.List;

public class VarDiamond {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        var list = new ArrayList<String>();   // var + diamond infers ArrayList<String>
        list.add("a");
        check("a".equals(list.get(0)), "var with diamond infers ArrayList<String>");

        var nested = new ArrayList<List<String>>();
        nested.add(list);
        check(nested.get(0) == list, "var with nested diamond");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
