/*
 * @test
 * @summary javax.management.openmbean.CompositeDataSupport conformance: get/containsKey.
 * @run main CompositeDataSupportTest
 */

import javax.management.openmbean.*;
public class CompositeDataSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType ct = new CompositeType("type", "desc", new String[]{"a"}, new String[]{"a desc"}, new OpenType[]{SimpleType.STRING});
        CompositeDataSupport cd = new CompositeDataSupport(ct, new String[]{"a"}, new Object[]{"x"});
        check("x".equals(cd.get("a")), "get");
        check(cd.containsKey("a"), "containsKey");
        check(cd.getCompositeType() == ct, "getCompositeType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
