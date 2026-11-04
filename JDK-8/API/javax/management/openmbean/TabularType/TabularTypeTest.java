/*
 * @test
 * @summary javax.management.openmbean.TabularType conformance: row type and index names.
 * @run main TabularTypeTest
 */

import javax.management.openmbean.*;
public class TabularTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType rowType = new CompositeType("row", "r", new String[]{"a"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        TabularType tt = new TabularType("tab", "t", rowType, new String[]{"a"});
        check("tab".equals(tt.getTypeName()), "type name");
        check(tt.getRowType() == rowType, "row type");
        check(tt.getIndexNames().contains("a"), "index names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
