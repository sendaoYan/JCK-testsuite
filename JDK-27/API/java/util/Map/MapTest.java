/*
 * @test
 * @summary java.util.Map conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main MapTest
 */

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Map");
        check(c.isInterface(), "is an interface");
        c.getDeclaredMethod("of");
        c.getDeclaredMethod("ofEntries", java.util.Map.Entry[].class);
        c.getDeclaredMethod("copyOf", Map.class);
        c.getDeclaredMethod("entry", Object.class, Object.class);
        c.getDeclaredMethod("ofLazy", Set.class, java.util.function.Function.class);
        check(!Modifier.isAbstract(c.getDeclaredMethod("getOrDefault", Object.class, Object.class)
                .getModifiers()), "getOrDefault default");
    }

    static void testBlackBox() {
        check(Map.of().isEmpty(), "of() empty");
        check(Integer.valueOf(1).equals(Map.of("a", 1).get("a")), "of single");
        check(Map.of("a", 1, "b", 2).size() == 2, "of pairs");
        expectThrows(() -> Map.of("a", 1, "a", 2), IllegalArgumentException.class, "duplicate key");
        expectThrows(() -> Map.of("a", (Integer) null), NullPointerException.class, "null value");
        expectThrows(() -> Map.of((String) null, 1), NullPointerException.class, "null key");

        Map.Entry<String, Integer> e = Map.entry("a", 1);
        check("a".equals(e.getKey()) && Integer.valueOf(1).equals(e.getValue()), "entry getKey/getValue");
        expectThrows(() -> e.setValue(2), UnsupportedOperationException.class, "entry setValue UOE");

        Map<String, Integer> base = new HashMap<>();
        base.put("x", 10);
        Map<String, Integer> cp = Map.copyOf(base);
        check(Integer.valueOf(10).equals(cp.get("x")), "copyOf");

        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        check(Integer.valueOf(9).equals(m.getOrDefault("zz", 9)), "getOrDefault default");
        check(Integer.valueOf(2).equals(m.computeIfAbsent("b", k -> 2)), "computeIfAbsent");
        check(Integer.valueOf(4).equals(m.merge("a", 3, Integer::sum)), "merge existing (1+3)");
        check(Integer.valueOf(4).equals(m.putIfAbsent("a", 99)), "putIfAbsent existing");
        m.replaceAll((k, v) -> v * 10);
        check(Integer.valueOf(40).equals(m.get("a")), "replaceAll a");
        check(Integer.valueOf(20).equals(m.get("b")), "replaceAll b");

        Map<String, String> lazy = Map.ofLazy(Set.of("a", "b"), String::toUpperCase);
        check(lazy.size() == 2 && "A".equals(lazy.get("a")) && "B".equals(lazy.get("b")), "ofLazy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
