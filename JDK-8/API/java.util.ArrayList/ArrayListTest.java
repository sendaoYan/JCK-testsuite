/*
 * @test
 * @summary java.util.ArrayList API conformance: construction/size/isEmpty, add (tail
 *          and indexed, incl. null), get/set/remove, contains/indexOf/lastIndexOf,
 *          addAll/removeAll/retainAll/containsAll, toArray variants, iterator/listIterator
 *          order and fail-fast (ConcurrentModificationException), subList view semantics,
 *          clone (shallow), equals/hashCode, clear, and capacity methods.
 * @run main ArrayListTest
 */

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

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
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

    public static void main(String[] args) {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
