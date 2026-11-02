/*
 * @test
 * @summary javax.swing.tree.TreePath conformance: path (headless-safe).
 * @run main TreePathTest
 */

import javax.swing.tree.TreePath;

public class TreePathTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        TreePath p = new TreePath("root");
        check(p.getPathCount() == 1, "count");
        check("root".equals(p.getPathComponent(0)), "component");
        check("root".equals(p.getLastPathComponent()), "last");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
