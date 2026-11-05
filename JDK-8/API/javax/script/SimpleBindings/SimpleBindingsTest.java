/*
 * @test
 * @summary javax.script.SimpleBindings conformance: simple bindings.
 * @run main SimpleBindingsTest
 */


import javax.script.Bindings;
import javax.script.SimpleBindings;
public class SimpleBindingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.SimpleBindings b = new javax.script.SimpleBindings();
        check(b.isEmpty(), "empty");
        b.put("k", "v");
        check(!b.isEmpty(), "not empty");
        check(b.size() == 1, "size");
        check("v".equals(b.get("k")), "get");
        check(b.containsKey("k"), "containsKey");
        check(b.containsValue("v"), "containsValue");
        check(b.keySet().contains("k"), "keySet");
        java.util.HashMap<String,Object> m = new java.util.HashMap<String,Object>();
        m.put("a", Integer.valueOf(1));
        b.putAll(m);
        check(b.size() == 2, "putAll");
        check(Integer.valueOf(1).equals(b.remove("a")), "remove");
        check(b.values().size() == 1, "values");
        check(b.entrySet().size() == 1, "entrySet");
        boolean threw = false;
        try { b.put(null, "x"); } catch (NullPointerException e) { threw = true; }
        check(threw, "null key NPE");
        javax.script.SimpleBindings b2 = new javax.script.SimpleBindings(new java.util.HashMap<String,Object>());
        check(b2.isEmpty(), "ctor map");
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSbConstructors();
        dSbMapContract();
        dSbNulls();
        dSbViews();
        System.out.println("deep: SimpleBindings contracts OK");
    }

    static void dSbConstructors() {
        SimpleBindings empty = new SimpleBindings();
        check(empty.isEmpty(), "empty ctor");
        java.util.Map<String, Object> seed = new java.util.HashMap<>();
        seed.put("a", 1);
        SimpleBindings seeded = new SimpleBindings(seed);
        check(seeded.size() == 1 && Integer.valueOf(1).equals(seeded.get("a")),
                "map ctor seeds entries");
        boolean npe = false;
        try { new SimpleBindings(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null map ctor -> NPE");
    }

    static void dSbMapContract() {
        SimpleBindings b = new SimpleBindings();
        check(b.put("k", "v") == null, "first put returns null");
        check("v".equals(b.put("k", "v2")), "replace returns old value");
        check("v2".equals(b.get("k")), "get sees replacement");
        check(b.containsKey("k") && !b.containsKey("x"), "containsKey");
        check(b.containsValue("v2"), "containsValue");
        check("v2".equals(b.remove("k")), "remove returns value");
        check(b.remove("k") == null, "remove absent -> null");
        check(b.size() == 0 && b.isEmpty(), "size/isEmpty");
        b.put("a", 1); b.put("b", 2);
        check(b.size() == 2, "size counts entries");
        check(b.keySet().containsAll(java.util.Arrays.asList("a", "b")), "keySet");
        check(b.values().containsAll(java.util.Arrays.asList(1, 2)), "values");
        check(b.entrySet().size() == 2, "entrySet");
        // putAll merges
        java.util.Map<String, Object> more = new java.util.HashMap<>();
        more.put("c", 3);
        more.put("a", 9);
        b.putAll(more);
        check(b.size() == 3 && Integer.valueOf(9).equals(b.get("a")), "putAll merges");
        b.clear();
        check(b.isEmpty(), "clear empties");
    }

    static void dSbNulls() {
        SimpleBindings b = new SimpleBindings();
        boolean npe = false;
        try { b.put(null, "v"); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "put(null key) -> NPE");
        check(b.put("k", null) == null, "null value allowed");
        check(b.containsKey("k") && b.get("k") == null, "null value retrievable");
        check(b.get("missing") == null, "get absent -> null");
        boolean npeGet = false;
        try { b.get(null); } catch (NullPointerException x) { npeGet = true; }
        check(npeGet, "get(null) -> NPE");
        boolean npeCk = false;
        try { b.containsKey(null); } catch (NullPointerException x) { npeCk = true; }
        check(npeCk, "containsKey(null) -> NPE");
    }

    static void dSbViews() {
        SimpleBindings b = new SimpleBindings();
        b.put("x", 1);
        b.put("y", 2);
        b.keySet().remove("x");
        check(!b.containsKey("x") && b.size() == 1, "keySet remove writes through");
        b.put("z", 3);
        java.util.Iterator<java.util.Map.Entry<String, Object>> it = b.entrySet().iterator();
        it.next();
        it.remove();
        check(b.size() == 1, "entrySet iterator remove writes through");
        b.values().remove(3);
        check(b.isEmpty(), "values remove writes through");
        // Bindings is a Map<String,Object>
        java.util.Map<String, Object> asMap = b;
        asMap.put("m", 4);
        check(Integer.valueOf(4).equals(b.get("m")), "Map view is the same object");
        check(Bindings.class.isAssignableFrom(SimpleBindings.class),
                "SimpleBindings implements Bindings");
    }

}
