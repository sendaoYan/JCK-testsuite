/*
 * @test
 * @summary javax.swing.ActionMap conformance: map operations (headless-safe).
 * @run main ActionMapTest
 */

import javax.swing.ActionMap;

public class ActionMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ActionMap m = new ActionMap();
        check(m.size() == 0, "empty");
        check(m.get("nope") == null, "get missing");
        check(m.keys() == null, "keys null");
        check(m.getParent() == null, "no parent");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
