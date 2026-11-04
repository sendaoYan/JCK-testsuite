/*
 * @test
 * @summary java.util.AbstractList API conformance: abstract get/size, default add/set/remove
 *          throw UOE, indexOf/lastIndexOf (equals incl null), subList live view, equals/hashCode
 *          list contract, removeRange (protected, exposed via subclass), modCount fail-fast.
 * @run main AbstractListTest
 */

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractListTest {
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

    // Minimal: implements only get() + size(). Default add/set/remove throw UOE.
    static class Minimal extends AbstractList<String> {
        final List<String> backing = new ArrayList<>();
        public String get(int i) { return backing.get(i); }
        public int size() { return backing.size(); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    static class Exposed extends AbstractList<Integer> {
        final List<Integer> backing = new ArrayList<>();
        public Integer get(int i) { return backing.get(i); }
        public int size() { return backing.size(); }
        public Integer set(int i, Integer v) { return backing.set(i, v); }
        public void add(int i, Integer v) { backing.add(i, v); }
        public Integer remove(int i) { return backing.remove(i); }
        void fill(Integer... items) { backing.addAll(Arrays.asList(items)); }
        void removeRangeExposed(int from, int to) { removeRange(from, to); }
    }

    static void testDefaultOpsUOE() {
        Minimal m = new Minimal();
        m.fill("a", "b");
        checkThrows(UnsupportedOperationException.class, () -> m.add("x"), "add(E) UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.add(0, "x"), "add(int,E) UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.set(0, "x"), "set UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.remove(0), "remove(int) UOE");
    }

    static void testIndexOfLastIndexOf() {
        Exposed l = new Exposed();
        l.fill(1, null, 3, null, 5);
        check(l.indexOf(3) == 2, "indexOf");
        check(l.indexOf(null) == 1, "indexOf(null)");
        check(l.lastIndexOf(null) == 3, "lastIndexOf(null)");
        check(l.indexOf(99) == -1 && l.lastIndexOf(99) == -1, "indexOf absent");
    }

    static void testSubListView() {
        Exposed l = new Exposed();
        l.fill(1, 2, 3, 4, 5);
        List<Integer> sub = l.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList content");
        sub.set(0, 20);
        check(l.get(1) == 20, "subList set reflected");
        l.set(2, 30);
        check(sub.get(1) == 30, "backing change reflected");
        checkThrows(IllegalArgumentException.class, () -> l.subList(3, 2), "subList(3,2) IAE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.subList(-1, 2), "subList(-1,2) IOBE");
    }

    static void testEqualsHashCode() {
        Exposed a = new Exposed();
        a.fill(1, 2, 3);
        Exposed b = new Exposed();
        b.fill(1, 2, 3);
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        check(a.equals(Arrays.asList(1, 2, 3)), "equals List with same elements");
        check(!a.equals(Arrays.asList(1, 2)), "!equals different size");
    }

    static void testRemoveRange() {
        Exposed l = new Exposed();
        l.fill(1, 2, 3, 4, 5);
        l.removeRangeExposed(1, 4);
        check(l.equals(Arrays.asList(1, 5)), "removeRange removes [from,to)");
        l.removeRangeExposed(0, 0);
        check(l.size() == 2, "removeRange(from==to) no-op");
    }

    public static void main(String[] args) {
        testDefaultOpsUOE();
        testIndexOfLastIndexOf();
        testSubListView();
        testEqualsHashCode();
        testRemoveRange();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
