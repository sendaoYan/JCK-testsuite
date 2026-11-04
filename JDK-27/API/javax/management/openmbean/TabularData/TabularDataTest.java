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

    // ---- deepened: method-set lock / index order / content equality ----
    static void testDeep() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularData");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 16, "exactly 16 declared methods");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface does not extend Serializable");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        // no Map-style 3-arg convenience put (javap verified on both JDKs)
        boolean has3ArgPut = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("put".equals(m.getName()) && m.getParameterTypes().length == 3) has3ArgPut = true;
        check(!has3ArgPut, "no 3-arg convenience put");

        CompositeType rt = new CompositeType("Row", "d",
                new String[]{"name", "value"}, new String[]{"nd", "vd"},
                new OpenType[]{SimpleType.STRING, SimpleType.INTEGER});
        TabularType tt = new TabularType("T", "d", rt, new String[]{"name"});
        TabularData td = new TabularDataSupport(tt);
        CompositeData row = new CompositeDataSupport(rt, new String[]{"name", "value"}, new Object[]{"x", 1});
        check(java.util.Arrays.equals(td.calculateIndex(row), new Object[]{"x"}),
                "calculateIndex follows indexNames order");
        check(td.containsKey(new Object[]{"x"}) == false, "containsKey before put false");
        check(td.get(new Object[]{"x"}) == null, "get missing returns null");
        check(td.remove(new Object[]{"x"}) == null, "remove missing returns null");
        TabularType tt2 = new TabularType("T2", "d", rt, new String[]{"value", "name"});
        TabularData td2 = new TabularDataSupport(tt2);
        check(java.util.Arrays.equals(td2.calculateIndex(row), new Object[]{1, "x"}),
                "calculateIndex multi-index order");
        // content equality across independent instances
        TabularData a = new TabularDataSupport(tt);
        TabularData b = new TabularDataSupport(tt);
        a.put(row);
        b.put(row);
        check(a.equals(b), "equals same content");
        check(!a.equals(null), "equals null false");
        check(a.toString().contains("TabularDataSupport"), "toString via interface");
        check(td.keySet().isEmpty() && td.values().isEmpty(), "views empty on empty table");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
