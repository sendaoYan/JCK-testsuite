/*
 * @test
 * @summary javax.swing.tree.VariableHeightLayoutCache conformance: layout cache (headless-safe).
 * @run main VariableHeightLayoutCacheTest
 */

import javax.swing.tree.VariableHeightLayoutCache;

public class VariableHeightLayoutCacheTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        VariableHeightLayoutCache c = new VariableHeightLayoutCache();
        check(c.getRowCount() == 0, "row count");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
