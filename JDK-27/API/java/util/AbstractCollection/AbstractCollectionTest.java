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
import java.util.NoSuchElementException;

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

    static void testNullArgs() {
        C c = new C();
        c.add("a");
        expectThrows(() -> c.addAll(null), NullPointerException.class, "addAll(null) NPE");
        expectThrows(() -> c.containsAll(null), NullPointerException.class, "containsAll(null) NPE");
        expectThrows(() -> c.removeAll(null), NullPointerException.class, "removeAll(null) NPE");
        expectThrows(() -> c.retainAll(null), NullPointerException.class, "retainAll(null) NPE");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    /** Records how the skeleton drives the iterator: proves default dispatch contract. */
    static class Rec extends AbstractCollection<String> {
        final List<String> data = new ArrayList<>();
        int iteratorCreated, iteratorRemoves;
        Rec(String... xs) { data.addAll(Arrays.asList(xs)); }
        public Iterator<String> iterator() {
            iteratorCreated++;
            return new Iterator<String>() {
                int i; boolean removable;
                public boolean hasNext() { return i < data.size(); }
                public String next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    removable = true;
                    return data.get(i++);
                }
                public void remove() {
                    if (!removable) throw new IllegalStateException();
                    data.remove(--i);
                    removable = false;
                    iteratorRemoves++;
                }
            };
        }
        public int size() { return data.size(); }
    }

    /** Iterator without remove(): mutating skeleton ops must surface the UOE. */
    static class Fixed extends AbstractCollection<String> {
        final List<String> data;
        Fixed(String... xs) { data = new ArrayList<>(Arrays.asList(xs)); }
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                final Iterator<String> src = data.iterator();
                public boolean hasNext() { return src.hasNext(); }
                public String next() { return src.next(); }
                public void remove() { throw new UnsupportedOperationException(); }
            };
        }
        public int size() { return data.size(); }
    }

    /** add() overridden as a counted no-op: proves addAll delegates per element. */
    static class CountedNoAdd extends AbstractCollection<String> {
        final List<String> data = new ArrayList<>();
        int addCalls;
        CountedNoAdd(String... xs) { data.addAll(Arrays.asList(xs)); }
        public Iterator<String> iterator() { return data.iterator(); }
        public int size() { return data.size(); }
        public boolean add(String s) { addCalls++; return false; }
    }

    static void testDefaultDispatchRecording() {
        Rec r = new Rec("a", "b", "c");
        check(r.contains("b") && r.iteratorCreated >= 1, "contains scans via iterator()");
        check(r.remove("a") && r.iteratorRemoves == 1, "remove(Object) delegates to iterator.remove");
        check(r.data.equals(List.of("b", "c")), "remove removed the element");
        check(!r.remove("zz") && r.iteratorRemoves == 1, "remove miss does not call iterator.remove");
        check(r.removeAll(List.of("b")) && r.iteratorRemoves == 2,
              "removeAll removals go through iterator.remove");
        check(r.data.equals(List.of("c")), "removeAll removed matching");
        Rec r2 = new Rec("a", "b", "c", "d");
        check(r2.retainAll(List.of("b", "d")) && r2.iteratorRemoves == 2,
              "retainAll removals go through iterator.remove");
        check(r2.data.equals(List.of("b", "d")), "retainAll kept the intersection");
        r2.clear();
        check(r2.data.isEmpty() && r2.iteratorRemoves == 4,
              "clear removes each element via iterator.remove");
        Rec r3 = new Rec("x", null);
        check(r3.contains(null), "contains(null) uses the == branch");
        check(r3.toString().equals("[x, null]"), "toString renders null element");
        check(r3.remove(null) && r3.data.equals(List.of("x")), "remove(null) works");
        check(r3.removeIf(s -> s.equals("x")) && r3.isEmpty(), "removeIf default drives iterator.remove");
        check(r3.iteratorRemoves == 2, "removeIf removal counted");
    }

    static void testUoePropagationFromIterator() {
        Fixed f = new Fixed("a", "b");
        expectThrows(() -> f.remove("a"), UnsupportedOperationException.class,
                "remove surfaces iterator UOE");
        check(f.size() == 2, "failed remove left collection unchanged");
        expectThrows(f::clear, UnsupportedOperationException.class, "clear surfaces iterator UOE");
        check(f.size() == 2, "failed clear left collection unchanged");
        expectThrows(() -> f.removeIf(x -> true), UnsupportedOperationException.class,
                "removeIf surfaces iterator UOE");
        check(f.size() == 2, "failed removeIf left collection unchanged");
        check(f.contains("a"), "queries still work on read-only iterator collection");
    }

    static void testAddAllDelegation() {
        CountedNoAdd n = new CountedNoAdd("a");
        check(!n.addAll(List.of("x", "y")), "addAll false when add never succeeds");
        check(n.addCalls == 2, "addAll delegated add for each element");
        check(n.size() == 1, "size unchanged when adds are no-ops");
        check(!n.addAll(List.of()), "addAll(empty) returns false");
        check(n.addCalls == 2, "no add calls for an empty argument");
    }

    static void testArrayStoreException() {
        // the probe holds Integers; asking for a String[] must fail at store time
        @SuppressWarnings("rawtypes")
        AbstractCollection raw = new AbstractCollection<Integer>() {
            final List<Integer> data = Arrays.asList(1, 2);
            public Iterator<Integer> iterator() { return data.iterator(); }
            public int size() { return data.size(); }
        };
        expectThrows(() -> raw.toArray(new String[5]), ArrayStoreException.class,
                "toArray wrong component type ASE");
    }

    static void testEqualsIsIdentityByDefault() {
        Rec a = new Rec("x");
        Rec b = new Rec("x");
        check(!a.equals(b), "AbstractCollection does not override equals: identity semantics");
        check(a.equals(a), "identity equals holds for self");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullArgs();
        testDefaultDispatchRecording();
        testUoePropagationFromIterator();
        testAddAllDelegation();
        testArrayStoreException();
        testEqualsIsIdentityByDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
