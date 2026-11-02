/*
 * @test
 * @summary javax.management.openmbean.TabularDataSupport conformance: put/get by index.
 * @run main TabularDataSupportTest
 */

import javax.management.openmbean.*;
public class TabularDataSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeType rowType = new CompositeType("row", "r", new String[]{"a"}, new String[]{"d"}, new OpenType[]{SimpleType.STRING});
        TabularType tt = new TabularType("tab", "t", rowType, new String[]{"a"});
        TabularDataSupport tds = new TabularDataSupport(tt);
        CompositeDataSupport row = new CompositeDataSupport(rowType, new String[]{"a"}, new Object[]{"x"});
        tds.put(row);
        check(tds.size() == 1, "size");
        check(tds.containsKey(new Object[]{"x"}), "containsKey");
        check("x".equals(tds.get(new Object[]{"x"}).get("a")), "get by index");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
