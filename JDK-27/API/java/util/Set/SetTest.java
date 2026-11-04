/*
 * @test
 * @summary java.util.Set conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main SetTest
 */

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SetTest {
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
        Class<?> c = Class.forName("java.util.Set");
        check(c.isInterface(), "is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "extends Collection");
        c.getDeclaredMethod("of");
        c.getDeclaredMethod("copyOf", java.util.Collection.class);
        c.getDeclaredMethod("ofLazy", Set.class, java.util.function.Predicate.class);
        check(Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()),
                "add abstract");
    }

    static void testBlackBox() {
        check(Set.of().isEmpty(), "of() empty");
        check(Set.of("a", "b").size() == 2, "of varargs");
        expectThrows(() -> Set.of("a", "a"), IllegalArgumentException.class, "duplicate element");
        expectThrows(() -> Set.of("a", (String) null), NullPointerException.class, "null element");

        Set<String> cp = Set.copyOf(new HashSet<>(java.util.List.of("x", "y")));
        check(cp.equals(Set.of("x", "y")), "copyOf");

        Set<Integer> lazy = Set.ofLazy(Set.of(0, 1, 2, 3), i -> i % 2 == 0);
        check(lazy.equals(Set.of(0, 2)), "ofLazy filter");
    }

    static void testBasicContract() {
        Set<String> s = new HashSet<>();
        check(s.isEmpty(), "new set empty");
        check(s.add("a") && !s.add("a"), "add true then false for duplicate");
        check(s.size() == 1, "size excludes duplicate");
        check(s.contains("a") && !s.contains("z"), "contains");
        check(s.remove("a") && s.isEmpty(), "remove true and empties");

        Set<String> u = new HashSet<>(List.of("a", "b"));
        check(u.addAll(new HashSet<>(List.of("b", "c"))), "addAll returns true");
        check(u.equals(new HashSet<>(List.of("a", "b", "c"))), "addAll union");
        check(!u.addAll(new HashSet<>(List.of("a"))), "addAll all-present returns false");
        check(u.containsAll(List.of("a", "c")), "containsAll");
        check(u.removeAll(List.of("a", "b")), "removeAll returns true");
        check(u.equals(new HashSet<>(List.of("c"))), "removeAll difference");

        Set<String> r = new HashSet<>(List.of("a", "b", "c"));
        check(r.retainAll(List.of("a", "c")), "retainAll returns true");
        check(r.equals(new HashSet<>(List.of("a", "c"))), "retainAll intersection");

        Iterator<String> it = u.iterator();
        check("c".equals(it.next()) && !it.hasNext(), "iterator covers all");
        check(u.toArray().length == 1, "toArray length");
        u.clear();
        check(u.isEmpty(), "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBasicContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
