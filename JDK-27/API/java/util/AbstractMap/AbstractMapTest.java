/*
 * @test
 * @summary java.util.AbstractMap conformance: white-box structure + black-box behavior.
 * @run main AbstractMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbstractMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    /** Minimal concrete subclass: implements entrySet + put. */
    static class M extends AbstractMap<String, Integer> implements Cloneable {
        final Map<String, Integer> backing = new HashMap<>();
        @Override public Set<Map.Entry<String, Integer>> entrySet() { return backing.entrySet(); }
        @Override public Integer put(String k, Integer v) { return backing.put(k, v); }
        public M copy() {
            try { return (M) clone(); } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractMap");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Map.class.isAssignableFrom(c), "implements Map");
        Method entrySet = c.getDeclaredMethod("entrySet");
        check(Modifier.isAbstract(entrySet.getModifiers()), "entrySet() abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("get", Object.class).getModifiers()), "get concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("keySet").getModifiers()), "keySet concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("values").getModifiers()), "values concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("toString").getModifiers()), "toString concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. put/get/size/isEmpty/containsKey/containsValue
        M m = new M();
        check(m.isEmpty() && m.size() == 0, "empty at creation");
        check(m.put("a", 1) == null, "put new returns null");
        check(m.put("a", 2) == 1, "put existing returns old");
        check(m.get("a") == 2 && m.get("z") == null, "get");
        check(m.containsKey("a") && !m.containsKey("z"), "containsKey");
        check(m.containsValue(2) && !m.containsValue(1), "containsValue");
        check(m.size() == 1 && !m.isEmpty(), "size/isEmpty");

        // 2. remove/putAll/clear
        m.put("b", 3);
        check(m.remove("a") == 2, "remove returns value");
        check(!m.containsKey("a"), "remove removes");
        Map<String, Integer> other = new HashMap<>();
        other.put("x", 10);
        other.put("y", 20);
        m.putAll(other);
        check(m.size() == 3 && m.get("x") == 10 && m.get("y") == 20, "putAll");
        m.clear();
        check(m.isEmpty(), "clear empties");

        // 3. keySet/values/entrySet views (order-independent assertions)
        M m2 = new M();
        m2.put("k1", 1);
        m2.put("k2", 2);
        check(m2.keySet().size() == 2 && m2.keySet().contains("k1"), "keySet");
        check(m2.values().size() == 2 && m2.values().contains(2), "values");
        check(m2.entrySet().size() == 2, "entrySet");

        // 4. equals/hashCode order-independent
        M ma = new M(); ma.put("a", 1); ma.put("b", 2);
        M mb = new M(); mb.put("b", 2); mb.put("a", 1);
        check(ma.equals(mb) && mb.equals(ma), "equals order-independent");
        check(ma.hashCode() == mb.hashCode(), "hashCode equal for equal maps");
        M mc = new M(); mc.put("a", 1);
        check(!ma.equals(mc), "different size -> not equal");
        check(!ma.equals(null), "equals(null) false");
        check(!ma.equals("map"), "equals(other type) false");

        // 5. toString is {k1=v1, k2=v2}
        M single = new M();
        single.put("k", 1);
        check("{k=1}".equals(single.toString()), "toString single entry");

        // 6. read-only: default put/remove throw UnsupportedOperationException
        AbstractMap<String, Integer> ro = new AbstractMap<>() {
            @Override public Set<Map.Entry<String, Integer>> entrySet() {
                return Set.of(Map.entry("a", 1));
            }
        };
        expectThrows(() -> ro.put("a", 1), UnsupportedOperationException.class, "default put throws");
        expectThrows(() -> ro.remove("a"), UnsupportedOperationException.class, "default remove throws");

        // 7. clone() shallow-copies the map (via the protected method)
        M orig = new M(); orig.put("c", 3);
        M cloned = orig.copy();
        check(cloned != orig, "clone distinct");
        check(cloned.size() == 1 && cloned.get("c").equals(3), "clone copies entries");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
