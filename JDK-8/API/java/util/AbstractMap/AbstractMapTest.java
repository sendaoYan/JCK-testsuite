/*
 * @test
 * @summary java.util.AbstractMap API conformance: SimpleEntry/SimpleImmutableEntry
 *          (getKey/getValue/setValue returns old/setValue UOE for immutable/equals/hashCode/
 *          toString), AbstractMap equals/hashCode/toString, default put UOE, entrySet/keySet/
 *          values.
 * @run main AbstractMapTest
 */

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AbstractMapTest {
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

    static void testSimpleEntry() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check("k".equals(e.getKey()) && e.getValue() == 1, "SimpleEntry getKey/getValue");
        check(e.setValue(10) == 1, "setValue returns old value");
        check(e.getValue() == 10, "setValue updates value");

        AbstractMap.SimpleEntry<String, Integer> same = new AbstractMap.SimpleEntry<>("k", 10);
        check(e.equals(same) && e.hashCode() == same.hashCode(), "SimpleEntry equals/hashCode");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k", 11)), "!equals different value");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k2", 10)), "!equals different key");
        check(e.toString().equals("k=10"), "SimpleEntry toString key=value");

        AbstractMap.SimpleEntry<String, Integer> fromMap =
                new AbstractMap.SimpleEntry<>(new AbstractMap.SimpleEntry<>("m", 5));
        check("m".equals(fromMap.getKey()) && fromMap.getValue() == 5, "SimpleEntry(Map.Entry) copy ctor");

        // SimpleEntry permits null key and value.
        AbstractMap.SimpleEntry<String, Integer> nulls = new AbstractMap.SimpleEntry<>(null, null);
        check(nulls.getKey() == null && nulls.getValue() == null, "SimpleEntry(null,null) allowed");
        check(nulls.equals(new AbstractMap.SimpleEntry<String, Integer>(null, null)),
              "SimpleEntry equals with null key/value");
    }

    static void testSimpleImmutableEntry() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check("k".equals(e.getKey()) && e.getValue() == 1, "SimpleImmutableEntry getKey/getValue");
        checkThrows(UnsupportedOperationException.class, () -> e.setValue(10),
                    "SimpleImmutableEntry.setValue UOE");

        AbstractMap.SimpleImmutableEntry<String, Integer> same = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check(e.equals(same) && e.hashCode() == same.hashCode(), "SimpleImmutableEntry equals/hashCode");
        check(e.toString().equals("k=1"), "SimpleImmutableEntry toString");

        AbstractMap.SimpleImmutableEntry<String, Integer> fromMap =
                new AbstractMap.SimpleImmutableEntry<>(new AbstractMap.SimpleEntry<>("m", 5));
        check("m".equals(fromMap.getKey()) && fromMap.getValue() == 5, "SimpleImmutableEntry(Map.Entry) copy");
    }

    static void testDefaultPutUOE() {
        AbstractMap<String, Integer> m = new AbstractMap<String, Integer>() {
            public java.util.Set<java.util.Map.Entry<String, Integer>> entrySet() {
                return new java.util.HashSet<java.util.Map.Entry<String, Integer>>();
            }
        };
        checkThrows(UnsupportedOperationException.class, () -> m.put("k", 1), "default put UOE");
    }

    static void testAbstractMapBehavior() {
        AbstractMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        check(m.size() == 2, "size");
        check(m.get("a") == 1, "get");
        check(m.containsKey("a") && m.containsValue(2), "containsKey/containsValue");
        check(m.keySet().contains("a"), "keySet");
        check(m.values().contains(2), "values");
        check(m.entrySet().size() == 2, "entrySet");

        AbstractMap<String, Integer> same = new HashMap<>();
        same.put("b", 2);
        same.put("a", 1);
        check(m.equals(same), "AbstractMap.equals order-independent");
        check(m.hashCode() == same.hashCode(), "AbstractMap.hashCode order-independent");

        m.remove("a");
        check(!m.containsKey("a") && m.size() == 1, "remove");

        // toString is {k=v, ...} (order may vary); just check it contains both entries
        String s = m.toString();
        check(s.startsWith("{") && s.endsWith("}") && s.contains("b=2"), "AbstractMap.toString");
    }

    public static void main(String[] args) {
        testSimpleEntry();
        testSimpleImmutableEntry();
        testDefaultPutUOE();
        testAbstractMapBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
