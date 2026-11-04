/*
 * @test
 * @summary java.util.ListResourceBundle API conformance: getContents/getString/getObject/getKeys.
 * @run main ListResourceBundleTest
 */

import java.util.ListResourceBundle;

public class ListResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class LRB extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                { "k", "v" },
                { "n", Integer.valueOf(3) },
                { "arr", new String[] { "a", "b" } },
            };
        }
    }

    static void testListResourceBundle() {
        ListResourceBundle b = new LRB();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(3).equals(b.getObject("n")), "getObject");
        check(b.containsKey("arr"), "containsKey");
        check(b.getKeys().hasMoreElements(), "getKeys has elements");
    }

    public static void main(String[] args) {
        testListResourceBundle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
