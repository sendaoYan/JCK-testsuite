/*
 * @test
 * @summary javax.script.SimpleBindings conformance: simple bindings.
 * @run main SimpleBindingsTest
 */

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
