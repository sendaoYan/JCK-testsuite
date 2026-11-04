/*
 * @test
 * @summary javax.management.openmbean.TabularData conformance: interface reflection.
 * @run main TabularDataTest
 */

public class TabularDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularData");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "calculateIndex"), "declares calculateIndex");
        check(hasMethod(c, "clear"), "declares clear");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        testDeep(c);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: method-set lock / contract evidenced by reference impl ----
    static void testDeep(Class<?> c) throws Exception {
        // full interface method set locked
        String[] expect = {"getTabularType", "calculateIndex", "size", "isEmpty", "containsKey",
                "containsValue", "get", "put", "remove", "putAll", "clear", "keySet", "values",
                "equals", "hashCode", "toString"};
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        for (String e : expect) check(declared.contains(e), "declares " + e);
        check(declared.size() == expect.length, "exactly " + expect.length + " declared methods");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface does not extend Serializable");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        // no Map-style put(CompositeType,String[],Object[]) convenience on the interface (javap verified)
        boolean has3ArgPut = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("put".equals(m.getName()) && m.getParameterTypes().length == 3) has3ArgPut = true;
        check(!has3ArgPut, "no 3-arg convenience put");

        // contract evidenced through the reference implementation
        javax.management.openmbean.CompositeType rt = new javax.management.openmbean.CompositeType("Row", "d",
                new String[]{"name", "value"}, new String[]{"nd", "vd"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.INTEGER});
        javax.management.openmbean.TabularType tt = new javax.management.openmbean.TabularType(
                "T", "d", rt, new String[]{"name"});
        javax.management.openmbean.TabularData td = new javax.management.openmbean.TabularDataSupport(tt);
        check(td.getTabularType().equals(tt), "getTabularType");
        check(td.isEmpty() && td.size() == 0, "initially empty");
        javax.management.openmbean.CompositeData row = new javax.management.openmbean.CompositeDataSupport(
                rt, new String[]{"name", "value"}, new Object[]{"x", 1});
        check(java.util.Arrays.equals(td.calculateIndex(row), new Object[]{"x"}),
                "calculateIndex follows indexNames order");
        td.put(row);
        check(td.size() == 1 && !td.isEmpty(), "size after put");
        check(td.containsKey(new Object[]{"x"}), "containsKey");
        check(td.containsValue(row), "containsValue");
        check(td.get(new Object[]{"x"}).equals(row), "get by index");
        check(td.keySet().size() == 1, "keySet size");
        check(td.values().size() == 1, "values size");
        javax.management.openmbean.CompositeData row2 = new javax.management.openmbean.CompositeDataSupport(
                rt, new String[]{"name", "value"}, new Object[]{"y", 2});
        td.putAll(new javax.management.openmbean.CompositeData[]{row2});
        check(td.size() == 2, "putAll adds rows");
        check(td.remove(new Object[]{"y"}).equals(row2), "remove returns the row");
        td.clear();
        check(td.isEmpty() && td.size() == 0 && td.keySet().isEmpty() && td.values().isEmpty(),
                "clear empties everything");
        // equals across independent instances with identical content
        javax.management.openmbean.TabularData a = new javax.management.openmbean.TabularDataSupport(tt);
        javax.management.openmbean.TabularData b = new javax.management.openmbean.TabularDataSupport(tt);
        a.put(row);
        b.put(row);
        check(a.equals(b), "equals same content");
        check(!a.equals(null), "equals null false");
        check(a.toString().contains("TabularDataSupport"), "toString via interface");
        // multi-index calculateIndex order
        javax.management.openmbean.TabularType tt2 = new javax.management.openmbean.TabularType(
                "T2", "d", rt, new String[]{"value", "name"});
        javax.management.openmbean.TabularData td2 = new javax.management.openmbean.TabularDataSupport(tt2);
        check(java.util.Arrays.equals(td2.calculateIndex(row), new Object[]{1, "x"}),
                "calculateIndex multi-index order");
    }
}
