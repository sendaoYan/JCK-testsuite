/*
 * @test
 * @summary java.util.AbstractCollection API conformance: default add() throws UOE,
 *          remove(Object)/contains(Object)/containsAll, addAll/removeAll/retainAll/clear
 *          bulk ops, toArray()/toArray(T[]) contract, toString(), isEmpty().
 * @run main AbstractCollectionTest
 */

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

    // Minimal implementation: only iterator() + size(). add() is NOT overridden,
    // so AbstractCollection.add() throws UnsupportedOperationException.
    static class NoAdd extends AbstractCollection<String> {
        final ArrayList<String> backing = new ArrayList<>();
        public Iterator<String> iterator() { return backing.iterator(); }
        public int size() { return backing.size(); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    // Mutable implementation: iterator()/size()/add() overridden for bulk ops.
    static class Mutable extends AbstractCollection<String> {
        final ArrayList<String> backing = new ArrayList<>();
        public Iterator<String> iterator() { return backing.iterator(); }
        public int size() { return backing.size(); }
        public boolean add(String s) { return backing.add(s); }
        public boolean remove(Object o) { return backing.remove(o); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    static void testDefaultAddUOE() {
        NoAdd m = new NoAdd();
        check(m.isEmpty(), "isEmpty via size==0");
        checkThrows(UnsupportedOperationException.class, () -> m.add("x"), "default add() UOE");
        m.fill("a", "b");
        check(!m.isEmpty(), "!isEmpty via size");
    }

    static void testContainsRemove() {
        Mutable m = new Mutable();
        m.fill("a", null, "c");
        check(m.contains("a") && m.contains(null) && !m.contains("z"), "contains incl null");
        check(m.containsAll(Arrays.asList("a", "c")), "containsAll true");
        check(!m.containsAll(Arrays.asList("a", "z")), "containsAll false");
        check(m.remove("a"), "remove returns true");
        check(!m.contains("a"), "remove removes");
        check(!m.remove("absent"), "remove absent returns false");
        check(m.remove(null), "remove(null)");
        check(!m.contains(null), "remove(null) removes");
    }

    static void testBulk() {
        Mutable m = new Mutable();
        m.fill("a", "b");
        check(m.addAll(Arrays.asList("c", "d")), "addAll returns true");
        check(m.size() == 4, "addAll appends");
        check(m.removeAll(Arrays.asList("a", "d")), "removeAll returns true");
        check(m.size() == 2 && m.contains("b") && m.contains("c"), "removeAll removes matching");

        Mutable m2 = new Mutable();
        m2.fill("b", "c", "x");
        check(m2.retainAll(Arrays.asList("b", "c")), "retainAll returns true");
        check(m2.size() == 2 && m2.contains("b") && m2.contains("c"), "retainAll keeps matching");

        m2.clear();
        check(m2.isEmpty(), "clear empties");
    }

    static void testNullArgs() {
        Mutable m = new Mutable();
        m.fill("a");
        checkThrows(NullPointerException.class, () -> m.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> m.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> m.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> m.retainAll(null), "retainAll(null) NPE");
    }

    static void testToArray() {
        Mutable m = new Mutable();
        m.fill("a", "b");
        Object[] oa = m.toArray();
        check(Arrays.equals(oa, new Object[]{"a", "b"}), "toArray()");

        String[] sized = m.toArray(new String[0]);
        check(Arrays.equals(sized, new String[]{"a", "b"}), "toArray(T[]) sized");

        String[] padded = m.toArray(new String[5]);
        check(padded.length == 5 && padded[2] == null, "toArray(T[]) pads trailing null");

        checkThrows(NullPointerException.class, () -> m.toArray((String[]) null), "toArray(null) NPE");
    }

    static void testToString() {
        Mutable m = new Mutable();
        m.fill("a", "b", "c");
        check(m.toString().equals("[a, b, c]"), "toString brackets elements");
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
        check(r.data.equals(Arrays.asList("b", "c")), "remove removed the element");
        check(!r.remove("zz") && r.iteratorRemoves == 1, "remove miss does not call iterator.remove");
        check(r.removeAll(Arrays.asList("b")) && r.iteratorRemoves == 2,
              "removeAll removals go through iterator.remove");
        check(r.data.equals(Arrays.asList("c")), "removeAll removed matching");
        Rec r2 = new Rec("a", "b", "c", "d");
        check(r2.retainAll(Arrays.asList("b", "d")) && r2.iteratorRemoves == 2,
              "retainAll removals go through iterator.remove");
        check(r2.data.equals(Arrays.asList("b", "d")), "retainAll kept the intersection");
        r2.clear();
        check(r2.data.isEmpty() && r2.iteratorRemoves == 4,
              "clear removes each element via iterator.remove");
        Rec r3 = new Rec("x", null);
        check(r3.contains(null), "contains(null) uses the == branch");
        check(r3.toString().equals("[x, null]"), "toString renders null element");
        check(r3.remove(null) && r3.data.equals(Arrays.asList("x")), "remove(null) works");
        check(r3.removeIf(s -> s.equals("x")) && r3.isEmpty(), "removeIf default drives iterator.remove");
        check(r3.iteratorRemoves == 2, "removeIf removal counted");
    }

    static void testUoePropagationFromIterator() {
        Fixed f = new Fixed("a", "b");
        checkThrows(UnsupportedOperationException.class, () -> f.remove("a"),
                "remove surfaces iterator UOE");
        check(f.size() == 2, "failed remove left collection unchanged");
        checkThrows(UnsupportedOperationException.class, f::clear, "clear surfaces iterator UOE");
        check(f.size() == 2, "failed clear left collection unchanged");
        checkThrows(UnsupportedOperationException.class, () -> f.removeIf(x -> true),
                "removeIf surfaces iterator UOE");
        check(f.size() == 2, "failed removeIf left collection unchanged");
        check(f.contains("a"), "queries still work on read-only iterator collection");
    }

    static void testAddAllDelegation() {
        CountedNoAdd n = new CountedNoAdd("a");
        check(!n.addAll(Arrays.asList("x", "y")), "addAll false when add never succeeds");
        check(n.addCalls == 2, "addAll delegated add for each element");
        check(n.size() == 1, "size unchanged when adds are no-ops");
        check(!n.addAll(new ArrayList<String>()), "addAll(empty) returns false");
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
        checkThrows(ArrayStoreException.class, () -> raw.toArray(new String[5]),
                "toArray wrong component type ASE");
    }

    static void testEqualsIsIdentityByDefault() {
        Rec a = new Rec("x");
        Rec b = new Rec("x");
        check(!a.equals(b), "AbstractCollection does not override equals: identity semantics");
        check(a.equals(a), "identity equals holds for self");
    }

    public static void main(String[] args) {
        testDefaultAddUOE();
        testContainsRemove();
        testBulk();
        testNullArgs();
        testToArray();
        testToString();
        testDefaultDispatchRecording();
        testUoePropagationFromIterator();
        testAddAllDelegation();
        testArrayStoreException();
        testEqualsIsIdentityByDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
