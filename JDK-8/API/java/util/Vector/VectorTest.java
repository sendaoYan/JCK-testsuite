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
import java.util.NoSuchElementException;
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

    // ---- deepened contract coverage II ---------------------------------------

    static void testHashCodeFormula() {
        check(new Vector<String>().hashCode() == 1, "hashCode of empty vector is 1");
        check(new Vector<>(Arrays.asList("a")).hashCode() == 128, "hashCode [a] = 128");
        check(new Vector<>(Arrays.asList("a", "b")).hashCode() == 4066, "hashCode [a,b] = 4066");
        check(new Vector<>(Arrays.asList("a", "b", null)).hashCode() == 126046,
              "hashCode [a,b,null] = 126046 (AbstractList algorithm)");
        check(new Vector<String>().capacity() == 10, "default capacity exactly 10");
        check(new Vector<String>(7).capacity() == 7, "Vector(7) capacity 7");
    }

    static void testEnumerationContract() {
        // empty enumeration: nextElement throws NoSuchElementException
        checkThrows(NoSuchElementException.class,
                    new Vector<String>().elements()::nextElement, "empty enumeration next NSEE");
        // Vector's legacy Enumeration is NOT fail-fast (unlike its Iterator)
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        Enumeration<String> e = v.elements();
        check(e.nextElement().equals("a"), "first element");
        v.add("d");
        check(e.hasMoreElements(), "enumeration still reports more after structural add (not fail-fast)");
        // contrast: the iterator IS fail-fast for the same modification
        Vector<String> v2 = new Vector<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = v2.iterator();
        it.next();
        v2.add("d");
        checkThrows(ConcurrentModificationException.class, it::next, "iterator CME for same change");
        // asIterator-style loop via enumeration over live vector copy of contents
        Vector<Integer> vi = new Vector<>(Arrays.asList(1, 2, 3));
        Enumeration<Integer> en = vi.elements();
        int sum = 0;
        while (en.hasMoreElements()) sum += en.nextElement();
        check(sum == 6, "enumeration sums all elements");
    }

    static void testElementAtBounds() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b"));
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(2), "elementAt(size) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.elementAt(-1), "elementAt(-1) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.insertElementAt("z", 3),
                    "insertElementAt(size+1) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.removeElementAt(2),
                    "removeElementAt(size) AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> v.setElementAt("z", 2),
                    "setElementAt(size) AIOOBE");
        check(v.equals(Arrays.asList("a", "b")), "failed legacy ops leave vector intact");
        v.insertElementAt("z", 2); // at size == append
        check(v.equals(Arrays.asList("a", "b", "z")), "insertElementAt(size) appends");
        v.setElementAt("Z", 2);
        check(v.get(2).equals("Z"), "setElementAt replaces in place");
        checkThrows(NoSuchElementException.class, new Vector<String>()::firstElement,
                    "firstElement empty NSEE");
        checkThrows(NoSuchElementException.class, new Vector<String>()::lastElement,
                    "lastElement empty NSEE");
    }

    static void testIndexOfFromIndex() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "a", "b", "a"));
        check(v.indexOf("a", 1) == 2, "indexOf from index skips earlier");
        check(v.indexOf("a", 4) == 4, "indexOf from last position");
        check(v.indexOf("a", 5) == -1, "indexOf from size returns -1");
        check(v.indexOf("z", 0) == -1, "indexOf absent -1");
        check(v.lastIndexOf("a", 3) == 2, "lastIndexOf bounded above by 3");
        check(v.lastIndexOf("a", 1) == 0, "lastIndexOf scans backward from bound");
        check(v.lastIndexOf("a", 0) == 0, "lastIndexOf at 0");
        check(v.lastIndexOf("a") == 4, "lastIndexOf unbounded");
        // null search
        Vector<String> vn = new Vector<>(Arrays.asList("x", null, "y"));
        check(vn.indexOf(null, 1) == 1, "indexOf(null, from)");
        check(vn.lastIndexOf(null, 2) == 1, "lastIndexOf(null, bound)");
        // fromIndex > size tolerated for indexOf (returns -1)
        check(v.indexOf("a", 100) == -1, "indexOf fromIndex > size yields -1");
    }

    static void testListIteratorContract() {
        Vector<String> v = new Vector<>(Arrays.asList("a", "b", "c"));
        java.util.ListIterator<String> li = v.listIterator(1);
        check(li.previous().equals("a"), "previous from index 1");
        check(li.next().equals("a") && li.next().equals("b"), "forward pair");
        li.set("B");
        check(v.get(1).equals("B"), "listIterator set reflected");
        li.add("X");
        check(v.equals(Arrays.asList("a", "B", "X", "c")), "listIterator add");
        check(li.previousIndex() == 2 && li.nextIndex() == 3, "cursor after add");
        checkThrows(IllegalStateException.class, () -> li.set("y"), "set after add ISE");
        // iterator remove passes through
        Iterator<String> it = v.iterator();
        it.next();
        it.remove();
        check(v.equals(Arrays.asList("B", "X", "c")), "iterator.remove");
        // forEachRemaining
        StringBuilder sb = new StringBuilder();
        v.listIterator(1).forEachRemaining(sb::append);
        check(sb.toString().equals("Xc"), "forEachRemaining from index 1");
        // fail-fast for listIterator
        java.util.ListIterator<String> li2 = v.listIterator();
        li2.next();
        v.addElement("n");
        checkThrows(ConcurrentModificationException.class, li2::next, "listIterator CME after addElement");
    }

    static void testSubListContract() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3, 4));
        List<Integer> sub = v.subList(1, 3);
        check(sub.hashCode() == Arrays.asList(2, 3).hashCode(), "subList hashCode own content");
        check(sub.equals(Arrays.asList(2, 3)) && Arrays.asList(2, 3).equals(sub), "subList equals segment");
        sub.remove(0);
        check(v.equals(Arrays.asList(1, 3, 4)), "subList remove shrinks parent");
        check(v.subList(0, 0).isEmpty(), "empty subList view");
        check(v.subList(v.size(), v.size()).isEmpty(), "tail empty subList (size,size)");
        checkThrows(IllegalArgumentException.class, () -> v.subList(2, 1), "subList(from>to) IAE");
        checkThrows(IndexOutOfBoundsException.class, () -> v.subList(-1, 2),
                    "subList(-1,2) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> v.subList(0, 5),
                    "subList(0,size+1) IOOBE");
    }

    static void testNegativeIncrementAndLegacy() {
        // negative capacityIncrement is accepted (grows by doubling when increment <= 0)
        Vector<String> v = new Vector<>(2, -1);
        check(v.capacity() == 2, "Vector(2,-1) initial capacity 2");
        v.add("a"); v.add("b"); v.add("c");
        check(v.size() == 3 && v.capacity() >= 3, "grows despite negative increment");
        v.removeElement("c");
        check(v.equals(Arrays.asList("a", "b")), "removeElement(Object) returns removed state");
        check(!v.removeElement("zz"), "removeElement absent returns false");
        check(v.removeElement("a"), "removeElement present returns true");
        // removeAllElements resets
        v.addElement("q");
        v.removeAllElements();
        check(v.isEmpty(), "removeAllElements empties");
    }

    static void testNullQueriesAndBulk() {
        Vector<String> v = new Vector<>(Arrays.asList("a", null, "b"));
        check(v.containsAll(Arrays.asList("a", null)), "containsAll with null");
        check(v.removeAll(Arrays.asList((String) null)), "removeAll(null element)");
        check(v.equals(Arrays.asList("a", "b")), "null removed by removeAll");
        check(v.retainAll(Arrays.asList("b")), "retainAll true when changed");
        check(v.equals(Arrays.asList("b")), "retainAll keeps b");
        check(!v.retainAll(Arrays.asList("b")), "retainAll false when unchanged");
        v.insertElementAt(null, 0);
        check(v.firstElement() == null, "firstElement null element");
        check(v.lastElement().equals("b"), "lastElement");
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
        testHashCodeFormula();
        testEnumerationContract();
        testElementAtBounds();
        testIndexOfFromIndex();
        testListIteratorContract();
        testSubListContract();
        testNegativeIncrementAndLegacy();
        testNullQueriesAndBulk();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
