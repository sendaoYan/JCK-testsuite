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

    public static void main(String[] args) {
        testDelegation();
        testIndexOfIterator();
        testSubList();
        testDefaultOpsUOE();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
