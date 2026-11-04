/*
 * @test
 * @summary java.util.Hashtable API conformance: put/get/remove, null key/value rejection
 *          (NullPointerException), contains/containsKey/containsValue, key/value/entry
 *          views, size/isEmpty/clear, and equals/hashCode.
 * @run main HashtableTest
 */

import java.util.Hashtable;

public class HashtableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testPutGetRemove() {
        Hashtable<String, Integer> t = new Hashtable<>();
        check(t.isEmpty() && t.size() == 0, "empty");
        check(t.put("a", 1) == null, "put new returns null");
        check(t.put("a", 2) == 1, "put existing returns old");
        check(t.get("a") == 2, "get");
        check(t.get("missing") == null, "get missing");
        check(t.remove("a") == 2 && !t.containsKey("a"), "remove");
        check(t.put("b", 3) == null && t.size() == 1, "size after put");
    }

    static void testNullRejection() {
        Hashtable<String, Integer> t = new Hashtable<>();
        expectNPE(() -> t.put(null, 1), "put null key");
        expectNPE(() -> t.put("k", null), "put null value");
        expectNPE(() -> t.get(null), "get null key");
        expectNPE(() -> t.containsKey(null), "containsKey null");
        expectNPE(() -> t.contains(null), "contains null");
        expectNPE(() -> t.remove(null), "remove null key");
    }

    static void testContains() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.containsKey("a") && t.containsKey("b"), "containsKey");
        check(!t.containsKey("z"), "!containsKey");
        check(t.containsValue(1) && t.containsValue(2), "containsValue");
        // Legacy Hashtable.contains(Object) is an alias for containsValue, not containsKey.
        check(t.contains(1) && t.contains(2), "contains (value)");
        check(!t.contains(999), "!contains");
    }

    static void testViews() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        t.put("c", 3);
        check(t.keySet().size() == 3 && t.keySet().contains("a"), "keySet");
        check(t.values().size() == 3, "values");
        check(t.entrySet().size() == 3, "entrySet");
        t.clear();
        check(t.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        Hashtable<String, Integer> a = new Hashtable<>();
        a.put("x", 1);
        Hashtable<String, Integer> b = new Hashtable<>();
        b.put("x", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
    }

    public static void main(String[] args) {
        testPutGetRemove();
        testNullRejection();
        testContains();
        testViews();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
