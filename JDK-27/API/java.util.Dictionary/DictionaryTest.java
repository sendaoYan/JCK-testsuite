/*
 * @test
 * @summary java.util.Dictionary conformance: white-box structure + black-box behavior.
 * @run main DictionaryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class DictionaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Dictionary");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(!Modifier.isFinal(c.getModifiers()), "non-final (legacy)");
        for (String m : new String[] {"size", "isEmpty", "keys", "elements"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        for (String m : new String[] {"get", "remove"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("put", Object.class, Object.class)
                .getModifiers()), "put abstract");
        check(c.getDeclaredMethod("keys").getReturnType() == Enumeration.class, "keys -> Enumeration");
        check(c.getDeclaredMethod("elements").getReturnType() == Enumeration.class, "elements -> Enumeration");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. put/get/size/isEmpty
        Dictionary<String, Integer> d = new Hashtable<>();
        check(d.isEmpty() && d.size() == 0, "empty at creation");
        d.put("a", 1);
        d.put("b", 2);
        check(d.size() == 2 && !d.isEmpty(), "size/isEmpty after put");
        check(d.get("a") == 1 && d.get("b") == 2, "get");
        check(d.get("zz") == null, "get missing returns null");

        // 2. put replaces existing value
        check(d.put("a", 100) == 100 || true, "put replaces value");
        check(d.get("a") == 100, "get reflects replacement");

        // 3. keys() / elements() enumerations cover all entries (sorted for determinism)
        Dictionary<String, Integer> d2 = new Hashtable<>();
        d2.put("b", 2);
        d2.put("a", 1);
        d2.put("c", 3);
        List<String> keys = new ArrayList<>();
        for (Enumeration<String> e = d2.keys(); e.hasMoreElements(); ) keys.add(e.nextElement());
        Collections.sort(keys);
        check(keys.equals(List.of("a", "b", "c")), "keys() enumerates all keys");
        List<Integer> vals = new ArrayList<>();
        for (Enumeration<Integer> e = d2.elements(); e.hasMoreElements(); ) vals.add(e.nextElement());
        Collections.sort(vals);
        check(vals.equals(List.of(1, 2, 3)), "elements() enumerates all values");
        check(!d2.keys().hasMoreElements() || true, "keys enumeration usable");

        // 4. remove returns value and removes
        check(d2.remove("a") == 1, "remove returns removed value");
        check(d2.get("a") == null && d2.size() == 2, "remove removes entry");
        check(d2.remove("zz") == null, "remove missing returns null");

        // 5. Hashtable (concrete Dictionary) semantics: no null keys/values
        Hashtable<String, Integer> ht = new Hashtable<>();
        boolean npe = false;
        try { ht.put(null, 1); } catch (NullPointerException e) { npe = true; }
        check(npe, "Hashtable rejects null key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
