/*
 * @test
 * @summary javax.management.openmbean.CompositeType conformance: item type access.
 * @run main CompositeTypeTest
 */

import javax.management.openmbean.*;
public class CompositeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType ct = new CompositeType("type", "desc", new String[]{"a"}, new String[]{"a desc"}, new OpenType[]{SimpleType.STRING});
        check("type".equals(ct.getTypeName()), "type name");
        check(ct.containsKey("a"), "containsKey");
        check(ct.getType("a") == SimpleType.STRING, "getType");
        check("a desc".equals(ct.getDescription("a")), "getDescription");
        check(ct.keySet().contains("a"), "keySet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
