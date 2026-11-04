/*
 * @test
 * @summary javax.management.openmbean.CompositeData conformance: interface reflection.
 * @run main CompositeDataTest
 */

public class CompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeData");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "containsValue"), "declares containsValue");
        check(hasMethod(c, "equals"), "declares equals");
        testDeep(c);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: full method-set lock / contract evidenced by reference impl ----
    static void testDeep(Class<?> c) throws Exception {
        // white-box: interface shape locked (9 methods, no keySet, not Serializable, no super-interface)
        String[] expect = {"getCompositeType", "get", "getAll", "containsKey", "containsValue",
                "values", "equals", "hashCode", "toString"};
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        for (String e : expect) check(declared.contains(e), "declares " + e);
        check(declared.size() == expect.length, "exactly " + expect.length + " declared methods");
        check(!declared.contains("keySet"), "no keySet on CompositeData");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface does not extend Serializable");
        check(c.getInterfaces().length == 0, "no super-interfaces");

        // contract evidenced through the reference implementation CompositeDataSupport
        javax.management.openmbean.CompositeType ct = new javax.management.openmbean.CompositeType("T", "d",
                new String[]{"name", "value"}, new String[]{"nd", "vd"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.INTEGER});
        javax.management.openmbean.CompositeData cd = new javax.management.openmbean.CompositeDataSupport(
                ct, new String[]{"name", "value"}, new Object[]{"n", 5});
        check(cd.getCompositeType().equals(ct), "getCompositeType");
        check(cd.get("name").equals("n") && cd.get("value").equals(5), "get");
        Object[] all = cd.getAll(new String[]{"value", "name"});
        check(all[0].equals(5) && all[1].equals("n"), "getAll order follows request");
        check(cd.getAll(null).length == 0, "getAll(null) treated as empty per javadoc");
        check(cd.containsKey("name") && !cd.containsKey("nope"), "containsKey");
        check(cd.containsValue(5) && !cd.containsValue("x"), "containsValue");
        check(cd.values().size() == 2, "values size");
        javax.management.openmbean.CompositeData same = new javax.management.openmbean.CompositeDataSupport(
                ct, new String[]{"name", "value"}, new Object[]{"n", 5});
        check(cd.equals(same) && cd.hashCode() == same.hashCode(), "equals/hashCode");
        check(!cd.equals(null), "equals null false");
        check(cd.toString().contains("n"), "toString mentions value");
    }
}
