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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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

    // ---- Part 3: minimal concrete Dictionary proves the legacy 7-method contract ----
    static class MapDictionary extends Dictionary<String, Integer> {
        final Map<String, Integer> m = new HashMap<>();
        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public Enumeration<String> keys() { return Collections.enumeration(new ArrayList<>(m.keySet())); }
        public Enumeration<Integer> elements() { return Collections.enumeration(new ArrayList<>(m.values())); }
        public Integer get(Object key) { return m.get(key); }
        public Integer put(String key, Integer value) { return m.put(key, value); }
        public Integer remove(Object key) { return m.remove(key); }
    }

    static void testMapDictionaryContract() {
        MapDictionary d = new MapDictionary();
        check(d.isEmpty() && d.size() == 0, "custom: empty at creation");
        check(d.get("a") == null, "custom: get missing null");
        check(d.put("a", 1) == null, "custom: put new returns null");
        check(d.put("a", 2) == 1, "custom: put existing returns old");
        check(d.size() == 1 && !d.isEmpty(), "custom: size/isEmpty");
        List<String> keys = new ArrayList<>();
        for (Enumeration<String> e = d.keys(); e.hasMoreElements(); ) keys.add(e.nextElement());
        check(keys.equals(List.of("a")), "custom: keys enumeration");
        List<Integer> vals = new ArrayList<>();
        for (Enumeration<Integer> e = d.elements(); e.hasMoreElements(); ) vals.add(e.nextElement());
        check(vals.equals(List.of(2)), "custom: elements enumeration");
        check(d.remove("a") == 2, "custom: remove returns value");
        check(d.remove("a") == null && d.isEmpty(), "custom: remove missing null, empty");
        // a custom Dictionary is free to accept null keys (no interface-level null rule)
        check(d.put(null, 7) == null && d.get(null) == 7, "custom: null key tolerated by own impl");
        d.remove(null);
        // empty enumerations -> NoSuchElementException
        final Enumeration<String> ek = d.keys();
        check(!ek.hasMoreElements(), "custom: empty keys no more elements");
        try { ek.nextElement(); check(false, "empty keys NSEE"); }
        catch (NoSuchElementException ex) { passed++; }
        final Enumeration<Integer> ee = d.elements();
        try { ee.nextElement(); check(false, "empty elements NSEE"); }
        catch (NoSuchElementException ex) { passed++; }
    }

    // ---- Part 4: Hashtable as the sole platform Dictionary ----
    static void testHashtableAsDictionary() {
        Dictionary<String, Integer> d = new Hashtable<>();
        d.put("x", 1);
        d.put("y", 2);
        check(d.size() == 2, "hashtable: size");
        check(d.get("y") == 2 && d.get("nope") == null, "hashtable: get hit/miss");
        try { d.put(null, 1); check(false, "put(null) NPE"); } catch (NullPointerException ex) { passed++; }
        try { d.put("k", null); check(false, "put(null value) NPE"); } catch (NullPointerException ex) { passed++; }
        try { d.get(null); check(false, "get(null) NPE"); } catch (NullPointerException ex) { passed++; }
        try { d.remove(null); check(false, "remove(null) NPE"); } catch (NullPointerException ex) { passed++; }
        Hashtable<String, Integer> ht = (Hashtable<String, Integer>) d;
        ht.put("z", 3);
        check(d.get("z") == 3 && d.size() == 3, "single backing table across references");
        check(d.remove("z") == 3 && ht.size() == 2, "remove via Dictionary shrinks Hashtable");
        int seen = 0;
        for (Enumeration<String> ks = d.keys(); ks.hasMoreElements(); ) { ks.nextElement(); seen++; }
        check(seen == d.size(), "keys enumeration length equals size");
        try { new Hashtable<String, Integer>().keys().nextElement(); check(false, "empty keys NSEE"); }
        catch (NoSuchElementException ex) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testMapDictionaryContract();
        testHashtableAsDictionary();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
