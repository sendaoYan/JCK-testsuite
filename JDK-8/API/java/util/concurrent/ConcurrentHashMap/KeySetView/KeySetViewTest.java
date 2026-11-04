/*
 * @test
 * @summary java.util.concurrent.ConcurrentHashMap.KeySetView conformance: key-set view.
 * @run main KeySetViewTest
 */

import java.util.concurrent.ConcurrentHashMap;

public class KeySetViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testKeySetView() {
        ConcurrentHashMap.KeySetView<String, Boolean> ks = ConcurrentHashMap.newKeySet();
        check(ks.isEmpty(), "newKeySet starts empty");
        check(ks.add("a"), "add returns true");
        check(!ks.add("a"), "add duplicate returns false");
        check(ks.contains("a"), "contains");
        check(ks.size() == 1, "size");
        check(ks.remove("a"), "remove returns true");
        check(ks.isEmpty(), "removed");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("x", 1);
        ConcurrentHashMap.KeySetView<String, Integer> view = map.keySet();
        check(view.contains("x"), "keySet view contains");
        check(!view.contains("y"), "keySet view does not contain missing");
        check(view.size() == 1, "keySet view size");
    }

    public static void main(String[] args) {
        testKeySetView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
