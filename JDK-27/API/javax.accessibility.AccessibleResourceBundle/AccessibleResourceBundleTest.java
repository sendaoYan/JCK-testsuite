/*
 * @test
 * @summary javax.accessibility.AccessibleResourceBundle conformance: resource bundle.
 * @run main AccessibleResourceBundleTest
 */

public class AccessibleResourceBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleResourceBundle b = new javax.accessibility.AccessibleResourceBundle();
        Object[][] c = b.getContents();
        check(c != null, "getContents non-null");
        check(c.length > 0, "getContents non-empty");
        check(b.getKeys() != null, "getKeys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
