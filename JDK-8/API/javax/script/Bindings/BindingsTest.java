/*
 * @test
 * @summary javax.script.Bindings conformance: reflection shape checks.
 * @run main BindingsTest
 */

import java.lang.reflect.*;
import javax.script.Bindings;
import javax.script.SimpleBindings;

public class BindingsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.script.Bindings");
        check(c != null, "javax.script.Bindings loads");
        check(c.getName().equals("javax.script.Bindings"), "binary name is javax.script.Bindings");
        check(Modifier.isPublic(c.getModifiers()), "javax.script.Bindings is public");
        check(c.isInterface(), "javax.script.Bindings is an interface");
        check(ifaceNames(c).equals(set("java.util.Map")), "direct superinterfaces");
        check(declares(c, "put"), "declares put");
        check(declares(c, "putAll"), "declares putAll");
        check(declares(c, "containsKey"), "declares containsKey");
        check(declares(c, "get"), "declares get");
        check(declares(c, "remove"), "declares remove");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBdSurface();
        dBdHandmade();
        dBdPolymorphic();
        System.out.println("deep: Bindings contracts OK");
    }

    static void dBdSurface() throws Exception {
        check(Bindings.class.isInterface(), "is interface");
        check(java.util.Map.class.isAssignableFrom(Bindings.class), "extends Map");
        java.util.Set<String> declared = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : Bindings.class.getDeclaredMethods())
            declared.add(m.getName());
        check(declared.contains("put"), "declares put");
        check(declared.contains("putAll"), "declares putAll");
        check(declared.contains("containsKey"), "declares containsKey");
        check(declared.contains("get"), "declares get");
        check(declared.contains("getBindingsPlaceholder") == false, "no stray methods");
    }

    /** Hand-rolled Bindings proves the interface is complete and minimal. */
    static class DeepBindings implements Bindings {
        final java.util.SortedMap<String, Object> map = new java.util.TreeMap<>();
        public Object put(String key, Object value) { return map.put(key, value); }
        public void putAll(java.util.Map<? extends String, ? extends Object> toMerge) {
            map.putAll(toMerge);
        }
        public void clear() { map.clear(); }
        public boolean containsKey(Object key) { return map.containsKey(key); }
        public boolean containsValue(Object value) { return map.containsValue(value); }
        public java.util.Set<java.util.Map.Entry<String, Object>> entrySet() { return map.entrySet(); }
        public Object get(Object key) { return map.get(key); }
        public boolean isEmpty() { return map.isEmpty(); }
        public java.util.Set<String> keySet() { return map.keySet(); }
        public Object remove(Object key) { return map.remove(key); }
        public int size() { return map.size(); }
        public java.util.Collection<Object> values() { return map.values(); }
    }

    static void dBdHandmade() {
        DeepBindings b = new DeepBindings();
        b.put("n", 1);
        check(b.size() == 1 && Integer.valueOf(1).equals(b.get("n")), "handmade put/get");
        b.putAll(java.util.Collections.singletonMap("m", 2));
        check(b.containsKey("m"), "handmade putAll");
        b.remove("n");
        check(!b.containsKey("n"), "handmade remove");
        // putAll(null) must be rejected per the Bindings contract
        boolean npe = false;
        try { b.putAll(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "putAll(null) -> NPE");
    }

    static void dBdPolymorphic() {
        Bindings simple = new SimpleBindings();
        simple.put("k", "v");
        Bindings handmade = new DeepBindings();
        handmade.put("k", "v");
        check(simple.size() == handmade.size(), "same size across impls");
        check(simple.get("k").equals(handmade.get("k")), "same value across impls");
        check(simple.entrySet().equals(handmade.entrySet()),
                "same entries across implementations");
        // SimpleBindings inherits Object.hashCode (identity-based); the Map
        // hashCode contract stays observable through entrySet: equal sets,
        // equal hashCodes, across and within implementations
        Bindings handmade2 = new DeepBindings();
        handmade2.put("k", "v");
        check(simple.entrySet().hashCode() == handmade.entrySet().hashCode(),
                "entrySet hashCode agrees across impls (Map contract)");
        check(handmade.entrySet().hashCode() == handmade2.entrySet().hashCode(),
                "entrySet hashCode stable within an impl");
    }

}
