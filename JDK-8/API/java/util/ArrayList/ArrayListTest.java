/*
 * @test
 * @summary java.util.ArrayList API conformance: construction/size/isEmpty, add (tail
 *          and indexed, incl. null), get/set/remove, contains/indexOf/lastIndexOf,
 *          addAll/removeAll/retainAll/containsAll, toArray variants, iterator/listIterator
 *          order and fail-fast (ConcurrentModificationException), subList view semantics,
 *          clone (shallow), equals/hashCode, clear, and capacity methods.
 * @run main ArrayListTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ArrayListTest {
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

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> ArrayList<T> roundTrip(ArrayList<T> list) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(list); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ArrayList<T>) ois.readObject();
        }
    }

    static void testConstruction() {
        ArrayList<String> a = new ArrayList<>();
        check(a.size() == 0 && a.isEmpty(), "default ctor empty");
        ArrayList<String> b = new ArrayList<>(10);
        check(b.size() == 0, "capacity ctor empty");
        ArrayList<String> c = new ArrayList<>(Arrays.asList("a", "b"));
        check(c.size() == 2 && c.get(0).equals("a"), "Collection ctor copies");
    }

    static void testAddGetSetRemove() {
        ArrayList<String> list = new ArrayList<>();
        check(list.add("a") && list.add("b") && list.add(null), "add returns true");
        check(list.size() == 3, "size after add");
        check(list.get(0).equals("a") && list.get(1).equals("b") && list.get(2) == null,
              "get preserves order incl null");
        check(list.set(1, "B").equals("b"), "set returns old value");
        check(list.get(1).equals("B"), "set updates element");
        check(list.remove(0).equals("a"), "remove(int) returns removed");
        check(list.size() == 2 && list.get(0).equals("B"), "remove shifts left");
        check(list.remove("B"), "remove(Object) returns true");
        check(!list.remove("absent"), "remove(absent) returns false");
        check(list.remove(null), "remove(null)");
        check(list.size() == 0, "empty after removes");

        list.add(0, "x");
        check(list.get(0).equals("x"), "add(0, x) inserts front");
        list.add(1, "y");
        check(list.equals(Arrays.asList("x", "y")), "add(index) inserts");
        expectIOOBE(() -> list.get(5), "get out of range");
        expectIOOBE(() -> list.set(-1, "z"), "set negative index");
        expectIOOBE(() -> list.remove(5), "remove out of range");
    }

    static void testContainsIndex() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null));
        check(list.contains("a") && list.contains("b") && list.contains(null),
              "contains incl null");
        check(!list.contains("z"), "!contains");
        check(list.indexOf("a") == 0 && list.indexOf(null) == 1, "indexOf");
        check(list.lastIndexOf(null) == 3, "lastIndexOf(null)");
        check(list.indexOf("z") == -1 && list.lastIndexOf("z") == -1, "indexOf missing");
    }

    static void testBulk() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(list.addAll(Arrays.asList(4, 5)), "addAll returns true");
        check(list.equals(Arrays.asList(1, 2, 3, 4, 5)), "addAll appends");
        check(list.addAll(0, Arrays.asList(0)), "addAll at index");
        check(list.equals(Arrays.asList(0, 1, 2, 3, 4, 5)), "addAll(index) inserts");

        check(list.containsAll(Arrays.asList(0, 5)), "containsAll");
        check(!list.containsAll(Arrays.asList(0, 99)), "!containsAll");
        check(list.removeAll(Arrays.asList(0, 5)), "removeAll returns true");
        check(list.equals(Arrays.asList(1, 2, 3, 4)), "removeAll removes matching");

        list.retainAll(Arrays.asList(1, 3));
        check(list.equals(Arrays.asList(1, 3)), "retainAll keeps matching");
    }

    static void testToArray() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        Object[] oa = list.toArray();
        check(oa.length == 2 && oa[0].equals("a") && oa[1].equals("b"), "toArray()");
        String[] sa = list.toArray(new String[0]);
        check(Arrays.equals(sa, new String[]{"a", "b"}), "toArray(T[]) sized");
        String[] padded = list.toArray(new String[5]);
        check(padded.length == 5 && padded[0].equals("a") && padded[2] == null,
              "toArray(T[]) pads trailing null");
    }

    static void testIterator() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        check(it.hasNext() && it.next().equals("a"), "iterator next");
        check(it.next().equals("b"), "iterator order");
        it.remove();
        check(list.equals(Arrays.asList("a", "c")), "iterator.remove()");
        check(it.next().equals("c") && !it.hasNext(), "iterator exhausts");

        // fail-fast: structural modification invalidates an active iterator.
        Iterator<String> it2 = list.iterator();
        it2.next();
        list.add("d");
        try {
            it2.next();
            throw new AssertionError("expected ConcurrentModificationException");
        } catch (ConcurrentModificationException expected) {
            passed++;
        }
    }

    static void testListIterator() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = list.listIterator(1);
        check(it.hasPrevious() && it.previous().equals("a"), "listIterator previous");
        check(it.nextIndex() == 0, "nextIndex after previous");
        check(it.next().equals("a") && it.next().equals("b"), "listIterator forward");
        it.add("X");
        check(list.equals(Arrays.asList("a", "b", "X", "c")), "listIterator add");
        check(it.previous().equals("X") && it.previous().equals("b"), "listIterator back");
        it.set("B");
        check(list.equals(Arrays.asList("a", "B", "X", "c")), "listIterator set");
    }

    static void testSubList() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = list.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList view content");
        sub.set(0, 20);
        check(list.get(1) == 20, "subList set reflected in backing list");
        list.set(2, 30);
        check(sub.get(1) == 30, "backing list change reflected in subList");
        sub.clear();
        check(list.equals(Arrays.asList(1, 5)), "subList.clear removes range");
    }

    static void testCloneEqualsHashCode() {
        ArrayList<String> a = new ArrayList<>(Arrays.asList("x", "y"));
        @SuppressWarnings("unchecked")
        ArrayList<String> b = (ArrayList<String>) a.clone();
        check(a != b, "clone distinct object");
        check(a.equals(b), "clone equals original");
        check(a.hashCode() == b.hashCode(), "equal lists equal hashCode");

        ArrayList<String> c = new ArrayList<>(Arrays.asList("x", "y"));
        check(a.equals(c) && c.equals(a), "equals symmetric for same elements");
        ArrayList<String> d = new ArrayList<>(Arrays.asList("x"));
        check(!a.equals(d), "!equals different size");
    }

    static void testClearCapacity() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        list.clear();
        check(list.isEmpty() && list.size() == 0, "clear empties");
        list.ensureCapacity(100);
        list.add("x");
        list.trimToSize();
        check(list.size() == 1 && list.get(0).equals("x"), "ensureCapacity/trimToSize usable");
    }

    // ---- deepened contract coverage ----------------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new ArrayList<String>(-1),
                    "ArrayList(-1) IAE");
        checkThrows(NullPointerException.class, () -> new ArrayList<String>((java.util.Collection<String>) null),
                    "ArrayList(null Collection) NPE");
        ArrayList<String> src = new ArrayList<>(Arrays.asList("a", "b"));
        ArrayList<String> copy = new ArrayList<>(src);
        check(copy.equals(src), "ArrayList(Collection) copies");
        copy.add("c");
        check(src.size() == 2, "ArrayList(Collection) independent");
    }

    static void testFailFastChannels() {
        // structural modification via any channel invalidates active iterators
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        it.next();
        list.remove(0);
        checkThrows(ConcurrentModificationException.class, it::next, "CME after remove(int)");

        ArrayList<String> l2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> lit = l2.listIterator();
        lit.next();
        l2.clear();
        checkThrows(ConcurrentModificationException.class, lit::next, "CME after clear");

        ArrayList<String> l3 = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it3 = l3.iterator();
        it3.next();
        l3.add("d");
        checkThrows(ConcurrentModificationException.class, it3::remove, "CME on iterator.remove after add");

        // structural modification through subList also fails fast on backing iterators
        ArrayList<String> l4 = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        Iterator<String> it4 = l4.iterator();
        it4.next();
        l4.subList(0, 2).clear();
        checkThrows(ConcurrentModificationException.class, it4::next, "CME after subList structural change");
    }

    static void testListIteratorEdges() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = list.listIterator(3);
        check(it.hasNext() == false && it.hasPrevious(), "listIterator(size) at end");
        check(it.previous().equals("c"), "previous from end");
        check(it.nextIndex() == 2 && it.previousIndex() == 1, "indices after previous");

        expectIOOBE(() -> list.listIterator(-1), "listIterator(-1) IOBE");
        expectIOOBE(() -> list.listIterator(4), "listIterator(size+1) IOBE");

        ListIterator<String> mid = list.listIterator(1);
        check(mid.nextIndex() == 1 && mid.previousIndex() == 0, "indices at middle");
        check(mid.next().equals("b"), "next at middle returns b");
        mid.remove();
        check(list.equals(Arrays.asList("a", "c")), "listIterator.remove at middle");
    }

    static void testSubListBounds() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        expectIOOBE(() -> list.subList(-1, 2), "subList(-1,2) IOBE");
        expectIOOBE(() -> list.subList(0, 4), "subList(0,4) IOBE");
        checkThrows(IllegalArgumentException.class, () -> list.subList(2, 1), "subList(2,1) IAE");

        // nested subList still reflects backing list
        List<Integer> sub = list.subList(0, 3).subList(1, 3);
        check(sub.equals(Arrays.asList(2, 3)), "nested subList");
        sub.set(0, 20);
        check(list.get(1) == 20, "nested subList set reflects in backing");
    }

    static void testNullArgs() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a"));
        checkThrows(NullPointerException.class, () -> list.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.addAll(0, null), "addAll(index,null) NPE");
        checkThrows(NullPointerException.class, () -> list.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> list.retainAll(null), "retainAll(null) NPE");
    }

    static void testListDefaultMethods() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        list.sort(Integer::compareTo);
        check(list.equals(Arrays.asList(1, 2, 3)), "List.sort");

        ArrayList<Integer> l2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        l2.replaceAll(x -> x * 10);
        check(l2.equals(Arrays.asList(10, 20, 30)), "List.replaceAll");

        ArrayList<Integer> l3 = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        l3.removeIf(x -> x % 2 == 0);
        check(l3.equals(Arrays.asList(1, 3)), "Collection.removeIf");

        final int[] sum = {0};
        l3.forEach(x -> sum[0] += x);
        check(sum[0] == 4, "Iterable.forEach");
    }

    static void testSerialization() throws Exception {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", null));
        ArrayList<String> r = roundTrip(list);
        check(r != list, "round-trip distinct object");
        check(r.equals(list), "round-trip equals original");
        check(r.size() == 3 && r.get(2) == null, "round-trip preserves content incl null");
        r.add("z");
        check(list.size() == 3, "round-trip result independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayList.class);
        check(osc.getSerialVersionUID() == 8683452581122892189L, "ArrayList serialVersionUID stable");
    }

    public static void main(String[] args) throws Exception {
        testConstruction();
        testAddGetSetRemove();
        testContainsIndex();
        testBulk();
        testToArray();
        testIterator();
        testListIterator();
        testSubList();
        testCloneEqualsHashCode();
        testClearCapacity();
        testConstructorMatrix();
        testFailFastChannels();
        testListIteratorEdges();
        testSubListBounds();
        testNullArgs();
        testListDefaultMethods();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
