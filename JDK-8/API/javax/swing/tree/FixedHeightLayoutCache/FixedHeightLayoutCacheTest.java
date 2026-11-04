/*
 * @test
 * @summary javax.swing.tree.FixedHeightLayoutCache conformance: layout cache (headless-safe).
 * @run main FixedHeightLayoutCacheTest
 */

import javax.swing.tree.FixedHeightLayoutCache;

public class FixedHeightLayoutCacheTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        FixedHeightLayoutCache c = new FixedHeightLayoutCache();
        check(c.getRowCount() == 0, "row count");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
