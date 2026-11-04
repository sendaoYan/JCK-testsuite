/*
 * @test
 * @summary java.util.AbstractSequentialList API conformance: get/set/add/remove/iterator
 *          delegate to listIterator, add(E) appends via add(size, e), indexOf/lastIndexOf,
 *          iterator/listIterator navigation, subList view, default add/set/remove UOE.
 * @run main AbstractSequentialListTest
 */

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class AbstractSequentialListTest {
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

    // Minimal: implements listIterator(int) + size().
    static class Minimal extends AbstractSequentialList<String> {
        final List<String> backing = new LinkedList<>();
        public ListIterator<String> listIterator(int index) { return backing.listIterator(index); }
        public int size() { return backing.size(); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    static void testDelegation() {
        Minimal m = new Minimal();
        m.fill("a", "b", "c");
        check(m.get(1).equals("b"), "get delegates to listIterator");
        check(m.set(1, "B").equals("b"), "set delegates");
        check(m.get(1).equals("B"), "set applied");
        m.add(1, "X");
        check(m.equals(Arrays.asList("a", "X", "B", "c")), "add(int,E) delegates");
        check(m.remove(1).equals("X"), "remove(int) delegates");
        check(m.equals(Arrays.asList("a", "B", "c")), "remove applied");

        check(m.add("d"), "add(E) appends");
        check(m.get(3).equals("d"), "add(E) appended at end");
    }

    static void testIndexOfIterator() {
        Minimal m = new Minimal();
        m.fill("a", null, "a");
        check(m.indexOf("a") == 0, "indexOf");
        check(m.lastIndexOf("a") == 2, "lastIndexOf");
        check(m.indexOf(null) == 1, "indexOf(null)");
        check(m.indexOf("z") == -1, "indexOf absent");

        java.util.Iterator<String> it = m.iterator();
        check(it.next().equals("a") && it.next() == null && it.next().equals("a") && !it.hasNext(),
              "iterator via listIterator order");
    }

    static void testSubList() {
        Minimal m = new Minimal();
        m.fill("1", "2", "3", "4", "5");
        List<String> sub = m.subList(1, 4);
        check(sub.equals(Arrays.asList("2", "3", "4")), "subList content");
        sub.set(0, "20");
        check(m.get(1).equals("20"), "subList set reflected");
    }

    static void testDefaultOpsUOE() {
        Minimal m = new Minimal();
        m.fill("a");
        // get/set/add(int)/remove(int) delegate to listIterator which backs a LinkedList:
        // these are supported. Default add(E) also supported via add(size, e).
        // Only a read-only minimal subclass would throw; here all are functional.
        checkThrows(IndexOutOfBoundsException.class, () -> m.get(5), "get out of range IOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> m.add(5, "x"), "add(index>size) IOBE");
    }

    // ---- deepened contract coverage II ---------------------------------------

    // Records every listIterator(int) creation to prove delegation of all ops.
    static class Recording extends AbstractSequentialList<String> {
        final List<String> backing = new LinkedList<>(Arrays.asList("a", "b", "c"));
        int iteratorCreations;
        public ListIterator<String> listIterator(int index) {
            iteratorCreations++;
            return backing.listIterator(index);
        }
        public int size() { return backing.size(); }
    }

    static void testAllOperationsRouteThroughListIterator() {
        Recording r = new Recording();
        int before = r.iteratorCreations;
        check(r.get(1).equals("b"), "get works");
        check(r.iteratorCreations == before + 1, "get(1) created exactly one listIterator");
        r.set(1, "B");
        check(r.iteratorCreations == before + 2, "set(int,E) went through listIterator");
        r.add(1, "X");
        check(r.iteratorCreations == before + 3, "add(int,E) went through listIterator");
        check(r.remove(2).equals("B"), "remove(int) returns element");
        check(r.iteratorCreations == before + 4, "remove(int) went through listIterator");
        // the equals() check itself also routes through listIterator(): +1
        check(r.equals(Arrays.asList("a", "X", "c")), "state after routed ops");
        check(r.iteratorCreations == before + 5, "equals() also drove one listIterator");
        // indexOf/lastIndexOf use listIterator()
        r.indexOf("a");
        check(r.iteratorCreations == before + 6, "indexOf drove listIterator");
        r.lastIndexOf("c");
        check(r.iteratorCreations == before + 7, "lastIndexOf drove listIterator");
        // iterator() is listIterator(0)
        java.util.Iterator<String> it = r.iterator();
        check(it.next().equals("a"), "iterator() routes to listIterator(0)");
        check(r.iteratorCreations == before + 8, "iterator() created one listIterator");
        // add(E) routes through listIterator(size)
        r.add("z");
        check(r.get(r.size() - 1).equals("z"), "add(E) appended");
        check(r.iteratorCreations >= before + 8, "add(E) routed through listIterator");
        // clear() drives the iterator's remove
        r.clear();
        check(r.isEmpty() && r.size() == 0, "clear via iterator remove");
    }

    // A read-only listIterator: all mutations must surface as UOE.
    static class ReadOnlyIterator extends AbstractSequentialList<String> {
        final List<String> backing = java.util.Collections.unmodifiableList(Arrays.asList("a", "b"));
        public ListIterator<String> listIterator(int index) { return backing.listIterator(index); }
        public int size() { return backing.size(); }
    }

    static void testReadOnlySubclassUOE() {
        ReadOnlyIterator ro = new ReadOnlyIterator();
        checkThrows(UnsupportedOperationException.class, () -> ro.set(0, "x"), "set UOE (iterator.set)");
        checkThrows(UnsupportedOperationException.class, () -> ro.add(0, "x"), "add UOE (iterator.add)");
        checkThrows(UnsupportedOperationException.class, () -> ro.remove(0), "remove UOE (iterator.remove)");
        checkThrows(UnsupportedOperationException.class, () -> ro.add("x"), "add(E) UOE");
        checkThrows(UnsupportedOperationException.class, ro::clear, "clear UOE");
        check(ro.get(0).equals("a") && ro.size() == 2, "reads still work");
    }

    static void testEqualsHashCodeListContract() {
        Minimal m = new Minimal();
        m.fill("a", "b");
        check(m.hashCode() == 4066, "[a,b] hashCode 4066 (AbstractList formula inherited)");
        Minimal mn = new Minimal();
        mn.fill("a", "b", null);
        check(mn.hashCode() == 126046, "[a,b,null] hashCode 126046");
        check(new Minimal().hashCode() == 1, "empty hashCode 1");
        check(m.equals(new java.util.ArrayList<>(Arrays.asList("a", "b"))), "equals ArrayList");
        check(m.equals(new LinkedList<>(Arrays.asList("a", "b"))), "equals LinkedList");
        check(!m.equals(Arrays.asList("b", "a")), "order sensitive");
    }

    static void testSubListWriteChannels() {
        Minimal m = new Minimal();
        m.fill("1", "2", "3", "4");
        List<String> sub = m.subList(1, 3);
        check(sub.equals(Arrays.asList("2", "3")), "subList content");
        sub.add("x");
        check(m.equals(Arrays.asList("1", "2", "3", "x", "4")), "subList.add passes through");
        sub.remove("x");
        check(m.equals(Arrays.asList("1", "2", "3", "4")), "subList.remove passes through");
        check(sub.set(0, "20").equals("2"), "subList.set returns old");
        check(m.get(1).equals("20"), "subList.set reflected");
        // subList iterator remove passes through
        java.util.Iterator<String> sit = sub.iterator();
        sit.next();
        sit.remove();
        check(m.equals(Arrays.asList("1", "3", "4")), "subList iterator.remove passes through");
        // bounds
        checkThrows(IndexOutOfBoundsException.class, () -> m.subList(0, 99), "subList(0,99) IOOBE");
        checkThrows(IllegalArgumentException.class, () -> m.subList(2, 1), "subList(2,1) IAE");
        check(m.subList(0, 0).isEmpty(), "empty subList view");
    }

    static void testEmptyAndBounds() {
        Minimal empty = new Minimal();
        checkThrows(IndexOutOfBoundsException.class, () -> empty.get(0), "empty get(0) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> empty.remove(0), "empty remove(0) IOOBE");
        check(empty.iterator() != null && !empty.iterator().hasNext(), "empty iterator exhausted");
        check(empty.add("first"), "add to empty works");
        check(empty.get(0).equals("first"), "element readable after add");
        Minimal m = new Minimal();
        m.fill("a");
        checkThrows(IndexOutOfBoundsException.class, () -> m.get(-1), "get(-1) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> m.listIterator(2), "listIterator(size+1) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> m.listIterator(-1), "listIterator(-1) IOOBE");
    }

    public static void main(String[] args) {
        testDelegation();
        testIndexOfIterator();
        testSubList();
        testDefaultOpsUOE();
        testAllOperationsRouteThroughListIterator();
        testReadOnlySubclassUOE();
        testEqualsHashCodeListContract();
        testSubListWriteChannels();
        testEmptyAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
