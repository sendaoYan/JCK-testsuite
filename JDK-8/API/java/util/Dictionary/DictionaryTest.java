/*
 * @test
 * @summary java.util.Dictionary API conformance: put/get/size/keys/elements via a Hashtable.
 * @run main DictionaryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

public class DictionaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** Minimal concrete Dictionary backed by a HashMap: proves the abstract
     *  method set is exactly the seven legacy operations. */
    static class MapDictionary extends Dictionary<String, Integer> {
        final Map<String, Integer> m = new HashMap<>();
        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public Enumeration<String> keys() { return java.util.Collections.enumeration(new java.util.ArrayList<>(m.keySet())); }
        public Enumeration<Integer> elements() { return java.util.Collections.enumeration(new java.util.ArrayList<>(m.values())); }
        public Integer get(Object key) { return m.get(key); }
        public Integer put(String key, Integer value) { return m.put(key, value); }
        public Integer remove(Object key) { return m.remove(key); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Dictionary");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(!c.isInterface(), "class not interface");
        // The public API surface is exactly the 7 legacy methods.
        for (String m : new String[] {"size", "isEmpty", "keys", "elements"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        for (String m : new String[] {"get", "remove"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()), m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("put", Object.class, Object.class).getModifiers()),
              "put abstract");
        check(c.getDeclaredMethod("keys").getReturnType() == Enumeration.class, "keys -> Enumeration");
        check(c.getDeclaredMethod("elements").getReturnType() == Enumeration.class, "elements -> Enumeration");
        int publicCount = 0;
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers())) publicCount++;
        check(publicCount == 7, "exactly 7 public methods, got " + publicCount);
        check(!Map.class.isAssignableFrom(c), "Dictionary is not a Map");
        check(Map.class.isAssignableFrom(Hashtable.class), "Hashtable is a Map");
        check(c.isAssignableFrom(Hashtable.class), "Hashtable extends Dictionary");
        check(!java.io.Serializable.class.isAssignableFrom(c), "Dictionary itself is NOT Serializable");
        check(java.io.Serializable.class.isAssignableFrom(Hashtable.class), "Hashtable is Serializable on its own");
    }

    static void testMapDictionaryContract() {
        MapDictionary d = new MapDictionary();
        check(d.isEmpty() && d.size() == 0, "custom: empty at creation");
        check(d.get("a") == null, "custom: get missing null");
        check(d.put("a", 1) == null, "custom: put new returns null");
        check(d.put("a", 2) == 1, "custom: put existing returns old");
        check(d.size() == 1 && !d.isEmpty(), "custom: size/isEmpty");
        // enumeration covers all entries
        java.util.List<String> keys = new java.util.ArrayList<>();
        Enumeration<String> ke = d.keys();
        while (ke.hasMoreElements()) keys.add(ke.nextElement());
        check(keys.equals(java.util.Arrays.asList("a")), "custom: keys enumeration");
        java.util.List<Integer> vals = new java.util.ArrayList<>();
        Enumeration<Integer> ve = d.elements();
        while (ve.hasMoreElements()) vals.add(ve.nextElement());
        check(vals.equals(java.util.Arrays.asList(2)), "custom: elements enumeration");
        check(d.remove("a") == 2, "custom: remove returns value");
        check(d.remove("a") == null && d.isEmpty(), "custom: remove missing null, empty");
        // custom impl is free to accept null keys (Dictionary imposes no null rule)
        check(d.put(null, 7) == null && d.get(null) == 7, "custom: null key tolerated by own impl");
        d.remove(null);
        // empty enumerations -> NoSuchElementException
        final Enumeration<String> ek = d.keys();
        check(!ek.hasMoreElements(), "custom: empty keys no more elements");
        checkThrows(NoSuchElementException.class, ek::nextElement, "empty keys nextElement NSEE");
        final Enumeration<Integer> ee = d.elements();
        checkThrows(NoSuchElementException.class, ee::nextElement, "empty elements nextElement NSEE");
    }

    static void testHashtableAsDictionary() {
        Dictionary<String, Integer> d = new Hashtable<>();
        d.put("x", 1);
        d.put("y", 2);
        check(d.size() == 2, "hashtable: size");
        check(d.get("y") == 2 && d.get("nope") == null, "hashtable: get hit/miss");
        // legacy null rejections surface through the Dictionary reference
        checkThrows(NullPointerException.class, () -> d.put(null, 1), "hashtable: put(null,·) NPE");
        checkThrows(NullPointerException.class, () -> d.put("k", null), "hashtable: put(·,null) NPE");
        checkThrows(NullPointerException.class, () -> d.get(null), "hashtable: get(null) NPE");
        checkThrows(NullPointerException.class, () -> d.remove(null), "hashtable: remove(null) NPE");
        // put over Hashtable reference and Dictionary reference share one table
        Hashtable<String, Integer> ht = (Hashtable<String, Integer>) d;
        ht.put("z", 3);
        check(d.get("z") == 3 && d.size() == 3, "single backing table across references");
        check(d.remove("z") == 3 && ht.size() == 2, "remove via Dictionary shrinks Hashtable");
        // enumeration consistency: keys and elements pair up to the same entries
        Enumeration<String> ks = d.keys();
        int seen = 0;
        while (ks.hasMoreElements()) { ks.nextElement(); seen++; }
        check(seen == d.size(), "keys enumeration length equals size");
        final Enumeration<String> ek = new Hashtable<String, Integer>().keys();
        checkThrows(NoSuchElementException.class, ek::nextElement, "hashtable: empty keys NSEE");
    }

    static void testDictionary() {
        Dictionary<String, Integer> d = new Hashtable<>();
        d.put("a", 1);
        check(d.size() == 1, "size");
        check(d.get("a") == 1, "get");
        check(d.keys().hasMoreElements(), "keys");
        check(d.elements().hasMoreElements(), "elements");
        check(d.remove("a") == 1, "remove");
        check(d.isEmpty(), "empty after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testMapDictionaryContract();
        testHashtableAsDictionary();
        testDictionary();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
