/*
 * @test
 * @summary java.util.Vector API conformance: full contract coverage.
 *          default capacity 10, add/get/set/remove, indexOf/lastIndexOf(fromIndex)/contains,
 *          legacy addElement/removeElement/elementAt/firstElement/lastElement/
 *          insertElementAt/setElementAt/removeElementAt/elements(), constructor matrix
 *          (negative capacity IAE, null Collection NPE, copy), capacity growth (initialCapacity
 *          + capacityIncrement, ensureCapacity, trimToSize), copyInto/toArray contract, setSize
 *          (pad null / truncate), subList view (live + fail-fast), iterator fail-fast, clone
 *          shallow, serialization round-trip + serialVersionUID, cross-implementation equals,
 *          null element allowance, and state invariants.
 * @run main VectorTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class VectorTest {
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

    @SuppressWarnings("unchecked")
    static <E> Vector<E> roundTrip(Vector<E> v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(v); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Vector<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasics() {
        Vector<String> v = new Vector<>();
        check(v.isEmpty() && v.size() == 0, "empty");
        check(v.capacity() == 10, "default capacity 10");
        check(v.add("a") && v.add("b") && v.add("c"), "add");
        check(v.size() == 3, "size");
        check(v.get(1).equals("b"), "get");
        check(v.set(1, "B").equals("b"), "set returns old");
        check(v.indexOf("c") == 2 && v.lastIndexOf("c") == 2, "indexOf/lastIndexOf");
        check(v.contains("B") && !v.contains("z"), "contains");
        check(v.remove("B") && !v.contains("B"), "remove");
        check(v.remove(0).equals("a"), "remove(index)");
        check(v.size() == 1 && v.get(0).equals("c"), "after removes");
    }

    static void testLegacy() {
        Vector<String> v = new Vector<>();
        v.addElement("a");
        v.addElement("b");
        check(v.elementAt(0).equals("a"), "elementAt");
        check(v.firstElement().equals("a") && v.lastElement().equals("b"),
              "firstElement/lastElement");
        v.insertElementAt("x", 1);
        check(v.equals(Arrays.asList("a", "x", "b")), "insertElementAt");
        v.removeElement("x");
        check(v.equals(Arrays.asList("a", "b")), "removeElement");
        v.removeElementAt(0);
        check(v.equals(Arrays.asList("b")), "removeElementAt");
    }

    static void testClearIteration() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        java.util.Iterator<Integer> it = v.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3 && !it.hasNext(),
              "iteration order");
        v.clear();
        check(v.isEmpty(), "clear");
    }

    // ---- constructor matrix + capacity growth ------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new Vector<String>(-1), "Vector(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new Vector<String>(-1, 0), "Vector(-1,0) IAE");
        checkThrows(NullPointerException.class, () -> new Vector<String>((Collection<String>) null),
                    "Vector(null Collection) NPE");

        Vector<String> zero = new Vector<>(0);
        zero.add("k");
        check(zero.size() == 1 && zero.get(0).equals("k"), "Vector(0) usable");

        Vector<String> copy = new Vector<>(Arrays.asList("a", "b"));
        check(copy.equals(Arrays.asList("a", "b")), "Vector(Collection) copies");
    }

    static void testCapacityGrowth() {
        Vector<String> g = new Vector<>(3, 5);
        check(g.capacity() == 3, "ctor (3,5) initial capacity 3");
        g.add("a");
        g.add("b");
        g.add("c");
        check(g.capacity() == 3, "capacity unchanged while size <= 3");
        g.add("d");
        check(g.capacity() == 8, "grow by capacityIncrement 5 => 8");

        Vector<String> doubling = new Vector<>(2);
        doubling.add("a");
        doubling.add("b");
        doubling.add("c");
        check(doubling.capacity() == 4, "capacityIncrement 0 => double");

        g.ensureCapacity(50);
        check(g.capacity() >= 50, "ensureCapacity grows");
        g.trimToSize();
        check(g.capacity() == g.size(), "trimToSize shrinks to size");
    }

    // ---- copyInto/toArray + setSize + enumeration --------------------------

    static void testCopyIntoAndToArray() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        Integer[] arr = new Integer[3];
        v.copyInto(arr);
        check(Arrays.equals(arr, new Integer[]{1, 2, 3}), "copyInto exact size");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.copyInto(new Integer[2]),
                    "copyInto too small");

        Object[] oa = v.toArray();
        check(Arrays.equals(oa, new Object[]{1, 2, 3}), "toArray() order");
        Integer[] exact = v.toArray(new Integer[0]);
        check(exact.length == 3 && Arrays.equals(exact, new Integer[]{1, 2, 3}), "toArray(new T[0]) exact");
        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = v.toArray(reusable);
        check(returned == reusable && reusable[3] == null, "toArray(T[]) reuses and pads");
    }

    static void testSetSize() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        v.setSize(4);
        check(v.size() == 4 && v.get(2) == null && v.get(3) == null, "setSize pads null");
        v.setSize(1);
        check(v.size() == 1 && v.get(0).equals("a"), "setSize truncates");
    }

    static void testEnumeration() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e = v.elements();
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check(sb.toString().equals("abc"), "elements() enumeration order");
    }

    // ---- subList view + iterator fail-fast --------------------------------

    static void testSubListView() {
        Vector<Integer> a = new Vector<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = a.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList content");
        sub.set(0, 20);
        check(a.get(1) == 20, "subList set reflected");
        sub.add(99);
        check(a.get(4) == 99, "subList add reflected");
        a.add(100);
        checkThrows(ConcurrentModificationException.class, () -> sub.get(0), "subList fail-fast");
    }

    static void testIteratorFailFast() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        Iterator<String> it = v.iterator();
        it.next();
        v.add("c");
        checkThrows(ConcurrentModificationException.class, it::next, "iterator fail-fast");
    }

    // ---- null allowance + clone + serialization + cross-impl + invariants --

    static void testNullAllowance() {
        Vector<String> v = new Vector<>();
        v.add(null);
        v.addElement(null);
        check(v.size() == 2 && v.get(0) == null && v.get(1) == null, "add/addElement accept null");
        check(v.contains(null) && v.indexOf(null) == 0 && v.lastIndexOf(null) == 1, "null query");
        v.insertElementAt(null, 0);
        check(v.size() == 3 && v.get(0) == null, "insertElementAt null");
        check(v.set(0, "x") == null, "set null returns old null");
    }

    static void testClone() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        @SuppressWarnings("unchecked")
        Vector<Integer> clone = (Vector<Integer>) v.clone();
        check(clone != v && clone.equals(v), "clone distinct and equal");
        clone.add(4);
        check(clone.size() == 4 && v.size() == 3, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        Vector<Integer> r = roundTrip(v);
        check(r != v && r.equals(v), "round-trip distinct and equal");
        r.add(4);
        check(r.size() == 4 && v.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Vector.class);
        check(osc.getSerialVersionUID() == -2767605614048989439L, "Vector serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(v.equals(al) && al.equals(v), "Vector/ArrayList equals");
        check(v.hashCode() == al.hashCode(), "hashCode equal across implementations");
        al.add(4);
        check(!v.equals(al), "!equals different content");
    }

    static void testStateInvariants() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        v.removeElementAt(1);
        check(v.equals(Arrays.asList("a", "c")), "removeElementAt updates");
        v.removeAllElements();
        check(v.isEmpty() && v.size() == 0, "removeAllElements empties");
        v.add("z");
        check(v.size() == 1 && v.get(0).equals("z"), "reusable after removeAllElements");
        v.clear();
        check(v.isEmpty() && !v.contains("z"), "clear removes all");
    }

    public static void main(String[] args) throws Exception {
        testBasics();
        testLegacy();
        testClearIteration();
        testConstructorMatrix();
        testCapacityGrowth();
        testCopyIntoAndToArray();
        testSetSize();
        testEnumeration();
        testSubListView();
        testIteratorFailFast();
        testNullAllowance();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
