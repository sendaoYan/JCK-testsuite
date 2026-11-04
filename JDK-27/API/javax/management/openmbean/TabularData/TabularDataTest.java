/*
 * @test
 * @summary javax.management.openmbean.TabularData conformance: white-box structure + light black-box behavior.
 * @run main TabularDataTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class TabularDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularData");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getTabularType"), "has getTabularType");
        check(has(mm, "calculateIndex"), "has calculateIndex");
        check(has(mm, "size"), "has size");
        check(has(mm, "isEmpty"), "has isEmpty");
        check(has(mm, "containsKey"), "has containsKey");
        check(has(mm, "containsValue"), "has containsValue");
        check(has(mm, "get"), "has get");
        check(has(mm, "put"), "has put");
        check(has(mm, "remove"), "has remove");
        check(has(mm, "putAll"), "has putAll");
        check(has(mm, "clear"), "has clear");
        check(has(mm, "keySet"), "has keySet");
        check(has(mm, "values"), "has values");
    }

    // ---- Part 2: black-box (light behavior via TabularDataSupport) ----
    static void testBlackBox() throws Exception {
        CompositeType rt = new CompositeType("Row", "d",
            new String[]{"name"}, new String[]{"nd"}, new OpenType[]{SimpleType.STRING});
        TabularType tt = new TabularType("T", "d", rt, new String[]{"name"});
        TabularData td = new TabularDataSupport(tt);

        check(td.getTabularType().equals(tt), "getTabularType");
        check(td.isEmpty() && td.size() == 0, "initially empty");
        CompositeData row = new CompositeDataSupport(rt, new String[]{"name"}, new Object[]{"x"});
        td.put(row);
        check(td.size() == 1 && !td.isEmpty(), "size after put");
        check(td.containsKey(new Object[]{"x"}), "containsKey");
        check(td.get(new Object[]{"x"}).equals(row), "get");
        check(td.remove(new Object[]{"x"}).equals(row), "remove");
        check(td.isEmpty(), "empty after remove");
        td.put(row);
        td.clear();
        check(td.isEmpty(), "empty after clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
