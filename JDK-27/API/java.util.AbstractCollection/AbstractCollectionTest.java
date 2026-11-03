/*
 * @test
 * @summary java.util.AbstractCollection conformance: white-box structure + black-box behavior.
 * @run main AbstractCollectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AbstractCollectionTest {
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

    /** Minimal concrete subclass: implements only iterator() + size() (+ add for mutability). */
    static class C extends AbstractCollection<String> {
        final List<String> backing = new ArrayList<>();
        @Override public Iterator<String> iterator() { return backing.iterator(); }
        @Override public int size() { return backing.size(); }
        @Override public boolean add(String e) { return backing.add(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractCollection");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Collection.class.isAssignableFrom(c), "implements Collection");
        check(Modifier.isAbstract(c.getDeclaredMethod("iterator").getModifiers()), "iterator() abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("size").getModifiers()), "size() abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("isEmpty").getModifiers()), "isEmpty concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("toString").getModifiers()), "toString concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("toArray").getModifiers()), "toArray concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        C c = new C();
        check(c.isEmpty() && c.size() == 0, "empty at creation");
        c.add("a");
        c.add("b");
        c.add("c");
        check(!c.isEmpty() && c.size() == 3, "size after add");
        check(c.contains("b") && !c.contains("z"), "contains");
        check(c.containsAll(Arrays.asList("a", "c")), "containsAll");

        // toArray(): iterator order, independent of the backing list
        Object[] arr = c.toArray();
        check(arr.length == 3 && Arrays.equals(arr, new Object[] {"a", "b", "c"}), "toArray() order");

        // toArray(T[]): reuse when big enough, null-terminate when bigger, allocate when small
        String[] into = c.toArray(new String[3]);
        check(Arrays.equals(into, new String[] {"a", "b", "c"}), "toArray(T[]) exact size");
        String[] big = new String[5];
        String[] bigRet = c.toArray(big);
        check(bigRet == big, "toArray reuses array when large enough");
        check(big[3] == null, "toArray null-terminates beyond size");
        String[] small = c.toArray(new String[0]);
        check(small.length == 3 && Arrays.equals(small, new String[] {"a", "b", "c"}),
                "toArray allocates when too small");

        // remove uses iterator.remove; remove missing returns false
        check(c.remove("b"), "remove existing");
        check(!c.contains("b") && c.size() == 2, "remove shrinks");
        check(!c.remove("zz"), "remove missing returns false");

        // addAll / retainAll
        check(c.addAll(Arrays.asList("x", "y")), "addAll returns true");
        check(c.containsAll(Arrays.asList("x", "y")) && c.size() == 4, "addAll adds all");
        check(c.retainAll(Arrays.asList("a", "x")), "retainAll returns true");
        check(c.size() == 2 && c.contains("a") && c.contains("x") && !c.contains("c"), "retainAll keeps only");

        // toString format [e1, e2]
        C ordered = new C();
        ordered.add("p");
        ordered.add("q");
        check("[p, q]".equals(ordered.toString()), "toString is [e1, e2]");

        // clear empties
        c.clear();
        check(c.isEmpty() && c.size() == 0, "clear empties");

        // read-only subclass: add throws UnsupportedOperationException
        AbstractCollection<String> ro = new AbstractCollection<>() {
            @Override public Iterator<String> iterator() { return List.<String>of().iterator(); }
            @Override public int size() { return 0; }
        };
        expectThrows(() -> ro.add("x"), UnsupportedOperationException.class, "default add unsupported");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
