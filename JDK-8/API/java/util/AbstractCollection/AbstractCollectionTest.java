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

    public static void main(String[] args) {
        testDefaultAddUOE();
        testContainsRemove();
        testBulk();
        testNullArgs();
        testToArray();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
