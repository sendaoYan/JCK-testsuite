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

    // ---- deepened contract coverage II ---------------------------------------

    // records get() calls to prove the inherited iterator is built on get()
    static class CountingGet extends AbstractList<String> {
        int getCalls;
        public String get(int i) { getCalls++; return "e" + i; }
        public int size() { return 2; }
    }

    // implements only add(int,E): proves add(E) delegates to add(int,E)
    static class AddOnly extends AbstractList<String> {
        final List<String> backing = new ArrayList<>(Arrays.asList("m"));
        int addCalls;
        public String get(int i) { return backing.get(i); }
        public int size() { return backing.size(); }
        public void add(int i, String v) { addCalls++; backing.add(i, v); }
    }

    static void testInheritedIteratorUsesGet() {
        CountingGet c = new CountingGet();
        StringBuilder sb = new StringBuilder();
        for (String s : c) sb.append(s);
        check(sb.toString().equals("e0e1"), "inherited iterator iterates via get()");
        check(c.getCalls == 2, "iterator drove exactly size() get() calls");
        // iterator().remove() before next() -> ISE (from the inherited Itr)
        checkThrows(IllegalStateException.class, () -> c.iterator().remove(),
                    "inherited iterator remove before next ISE");
        // empty-iterator next -> NoSuchElementException
        EmptyList empty = new EmptyList();
        checkThrows(java.util.NoSuchElementException.class,
                    empty.iterator()::next, "empty AbstractList iterator next NSEE");
        // forEachRemaining on the inherited iterator
        StringBuilder sb2 = new StringBuilder();
        c.iterator().forEachRemaining(sb2::append);
        check(sb2.toString().equals("e0e1"), "forEachRemaining via inherited iterator");
    }

    static class EmptyList extends AbstractList<String> {
        // next() catches IndexOutOfBoundsException from get() and converts it to NSEE
        public String get(int i) { throw new IndexOutOfBoundsException("empty"); }
        public int size() { return 0; }
    }

    static void testAddDelegation() {
        AddOnly a = new AddOnly();
        check(a.add("x"), "add(E) returns true (Collection default delegates to add(int,E))");
        check(a.addCalls == 1, "add(E) invoked add(int,E) exactly once");
        check(a.equals(Arrays.asList("m", "x")), "element appended at end");
        a.add(0, "head");
        check(a.addCalls == 2 && a.get(0).equals("head"), "direct add(int,E) also counted");
        // addAll delegates per element to add(int,E)
        check(a.addAll(Arrays.asList("p", "q")), "addAll returns true");
        check(a.addCalls == 4, "addAll of 2 elements drove 2 more add(int,E) calls");
        check(a.equals(Arrays.asList("head", "m", "x", "p", "q")), "addAll appended in order");
    }

    static void testIteratorRemoveDelegation() {
        // Minimal does not implement remove(int): inherited iterator.remove() throws UOE
        Minimal m = new Minimal();
        m.fill("a", "b");
        java.util.Iterator<String> it = m.iterator();
        it.next();
        checkThrows(UnsupportedOperationException.class, it::remove,
                    "iterator.remove falls through to unimplemented remove(int) UOE");
        // Exposed implements remove(int): iterator.remove removes through the subclass
        Exposed e = new Exposed();
        e.fill(1, 2, 3);
        java.util.Iterator<Integer> it2 = e.iterator();
        it2.next();
        it2.remove();
        check(e.equals(Arrays.asList(2, 3)), "iterator.remove removes via subclass remove(int)");
        checkThrows(IllegalStateException.class, it2::remove, "double remove ISE");
    }

    static class ExposedStr extends AbstractList<String> {
        final List<String> backing = new ArrayList<>();
        public String get(int i) { return backing.get(i); }
        public int size() { return backing.size(); }
        public String set(int i, String v) { return backing.set(i, v); }
        public void add(int i, String v) { backing.add(i, v); }
        public String remove(int i) { return backing.remove(i); }
    }

    static void testHashCodeFormula() {
        // AbstractList hashCode = List formula: 1; 31*r + hash(e)
        check(new Minimal().hashCode() == 1, "empty hashCode 1");
        ExposedStr l = new ExposedStr();
        l.add("a"); l.add("b");
        check(l.hashCode() == 4066, "[a,b] hashCode 4066 (31*(31*1+97)+98)");
        ExposedStr ln = new ExposedStr();
        ln.add("a"); ln.add("b"); ln.add(null);
        check(ln.hashCode() == 126046, "[a,b,null] hashCode 126046");
        // matches other implementations for equal content
        check(l.hashCode() == new ArrayList<>(Arrays.asList("a", "b")).hashCode()
              && l.hashCode() == new java.util.LinkedList<>(Arrays.asList("a", "b")).hashCode(),
              "AbstractList hashCode matches ArrayList/LinkedList");
    }

    static void testSubListWriteChannels() {
        Exposed l = new Exposed();
        l.fill(1, 2, 3, 4);
        List<Integer> sub = l.subList(1, 3);
        sub.add(0, 99);
        check(l.equals(Arrays.asList(1, 99, 2, 3, 4)), "subList.add passes through");
        sub.remove(Integer.valueOf(99));
        check(l.equals(Arrays.asList(1, 2, 3, 4)), "subList.remove(Object) passes through");
        check(sub.remove(1) == 3, "subList.remove(int) returns removed via subclass");
        check(l.equals(Arrays.asList(1, 2, 4)), "subList.remove(int) shrinks backing");
        check(sub.set(0, 30) == 2, "subList.set returns old value");
        check(l.get(1) == 30, "subList.set reflected in backing");
        // subList iterator remove passes through too
        java.util.Iterator<Integer> sit = sub.iterator();
        sit.next(); // 30
        sit.remove();
        check(l.equals(Arrays.asList(1, 4)), "subList iterator.remove passes through");
        // empty subLists
        check(l.subList(0, 0).isEmpty() && l.subList(l.size(), l.size()).isEmpty(), "empty subList views");
        // bounds + order violations
        checkThrows(IndexOutOfBoundsException.class, () -> l.subList(0, 99), "subList(0,99) IOOBE");
        checkThrows(IllegalArgumentException.class, () -> l.subList(3, 2), "subList(3,2) IAE");
    }

    static void testEqualsCrossImpl() {
        ExposedStr e = new ExposedStr();
        e.add("x"); e.add(null); e.add("y");
        List<String> al = new ArrayList<>(Arrays.asList("x", null, "y"));
        java.util.LinkedList<String> ll = new java.util.LinkedList<>(Arrays.asList("x", null, "y"));
        check(e.equals(al) && al.equals(e), "AbstractList equals ArrayList (incl null)");
        check(e.equals(ll) && ll.equals(e), "AbstractList equals LinkedList");
        check(e.hashCode() == al.hashCode() && e.hashCode() == ll.hashCode(),
              "one hashCode across impls");
        check(!e.equals(Arrays.asList("x", null)), "prefix not equal");
        check(!e.equals(Arrays.asList("x", null, "z")), "last element difference not equal");
        // set returns the previous value
        Exposed s = new Exposed();
        s.fill(1, 2);
        check(s.set(0, 9) == 1, "set returns old value");
    }

    public static void main(String[] args) {
        testDefaultOpsUOE();
        testIndexOfLastIndexOf();
        testSubListView();
        testEqualsHashCode();
        testRemoveRange();
        testInheritedIteratorUsesGet();
        testAddDelegation();
        testIteratorRemoveDelegation();
        testHashCodeFormula();
        testSubListWriteChannels();
        testEqualsCrossImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
