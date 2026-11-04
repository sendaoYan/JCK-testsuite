/*
 * @test
 * @summary java.util.List API conformance: get/set/add(index)/remove(index), indexOf/
 *          lastIndexOf, subList, listIterator, and the Java 8 default sort.
 * @run main ListTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class ListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIndexedOps() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        check("a".equals(l.get(0)) && "c".equals(l.get(2)), "get");
        check(l.indexOf("b") == 1 && l.lastIndexOf("b") == 1, "indexOf/lastIndexOf");
        check("b".equals(l.set(1, "B")), "set returns old");
        check("B".equals(l.get(1)), "set updates");
        l.add(0, "first");
        check("first".equals(l.get(0)) && l.size() == 4, "add(index)");
        check("first".equals(l.remove(0)), "remove(index) returns removed");
    }

    static void testSubList() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "B", "c"));
        List<String> sub = l.subList(0, 2);
        check(sub.size() == 2 && "a".equals(sub.get(0)), "subList view");
    }

    static void testListIterator() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "B", "c"));
        ListIterator<String> li = l.listIterator();
        check(li.next().equals("a") && li.next().equals("B"), "next");
        check(li.previous().equals("B"), "previous");
        check(li.nextIndex() == 1 && li.previousIndex() == 0, "nextIndex/previousIndex");
    }

    static void testSort() {
        List<Integer> nums = new ArrayList<>(Arrays.asList(3, 1, 2));
        nums.sort(Comparator.naturalOrder());
        check(nums.equals(Arrays.asList(1, 2, 3)), "sort default method");
        // sort(null) uses natural ordering
        List<Integer> nat = new ArrayList<>(Arrays.asList(3, 1, 2));
        nat.sort(null);
        check(nat.equals(Arrays.asList(1, 2, 3)), "sort(null) natural ordering");
    }

    static void testReplaceAllRemoveIf() {
        List<Integer> l = new ArrayList<>(Arrays.asList(1, 2, 3));
        l.replaceAll(x -> x * 10);
        check(l.equals(Arrays.asList(10, 20, 30)), "replaceAll");

        List<Integer> l2 = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        l2.removeIf(x -> x % 2 == 0);
        check(l2.equals(Arrays.asList(1, 3)), "removeIf");

        checkThrows(java.lang.NullPointerException.class, () -> l.replaceAll(null), "replaceAll(null) NPE");
    }

    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepened contract coverage II ---------------------------------------

    static void testHashCodeEqualsContract() {
        // hashCode formula: 1; 31*r + hash(e) -- identical for every List impl
        check(Arrays.asList().hashCode() == 1, "empty list hashCode 1");
        check(Arrays.asList("a").hashCode() == 128, "[a] hashCode 128");
        check(Arrays.asList("a", "b").hashCode() == 4066, "[a,b] hashCode 4066");
        check(Arrays.asList("a", "b", null).hashCode() == 126046, "[a,b,null] hashCode 126046");
        check(new ArrayList<>(Arrays.asList("a", "b")).hashCode() == 4066,
              "ArrayList same formula");
        check(new java.util.LinkedList<>(Arrays.asList("a", "b")).hashCode() == 4066,
              "LinkedList same formula");
        check(new java.util.Vector<>(Arrays.asList("a", "b")).hashCode() == 4066,
              "Vector same formula");
        // equals is element-wise, order-sensitive, across implementations
        List<String> al = new ArrayList<>(Arrays.asList("a", "b"));
        check(al.equals(new java.util.LinkedList<>(Arrays.asList("a", "b")))
              && al.equals(new java.util.Vector<>(Arrays.asList("a", "b")))
              && al.equals(Arrays.asList("a", "b")), "cross-impl equals");
        check(!al.equals(Arrays.asList("b", "a")), "order significant");
        check(!al.equals(Arrays.asList("a")), "size significant");
        check(!al.equals(Arrays.asList("a", "b", "c")), "prefix not equal");
        // null element equality
        check(Arrays.asList("a", null).equals(new ArrayList<>(Arrays.asList("a", null))),
              "null element equals");
        check(!Arrays.asList("a", null).equals(Arrays.asList("a", "b")), "null vs value");
        // equals must return false for non-List (even Iterable with same content)
        check(!al.equals(java.util.Arrays.asList("a", "b").toString()), "non-List false");
    }

    static void testSortDefaultDispatch() {
        // default sort() with a Comparator chain: nullsFirst + reversed
        List<Integer> l = new ArrayList<>(Arrays.asList(3, null, 1));
        l.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        check(l.equals(Arrays.asList(null, 1, 3)), "sort nullsFirst puts null first");
        List<String> s = new ArrayList<>(Arrays.asList("bb", "a", "ccc"));
        s.sort(Comparator.comparing(String::length));
        check(s.equals(Arrays.asList("a", "bb", "ccc")), "sort by key extractor");
        s.sort(Comparator.comparing(String::length).reversed());
        check(s.equals(Arrays.asList("ccc", "bb", "a")), "sort reversed comparator");
        // stability: equal keys preserve encounter order
        List<String> st = new ArrayList<>(Arrays.asList("b2", "a1", "b1", "a2"));
        st.sort(Comparator.comparing(x -> x.substring(0, 1)));
        check(st.equals(Arrays.asList("a1", "a2", "b2", "b1")), "sort stable for equal keys");
    }

    static void testReplaceAllDefaultDispatch() {
        // replaceAll on Arrays.asList (set-through) works; on unmodifiable view UOE
        List<Integer> asList = Arrays.asList(1, 2, 3);
        asList.replaceAll(x -> x + 1);
        check(asList.equals(Arrays.asList(2, 3, 4)), "replaceAll writes through Arrays.asList");
        List<Integer> ro = java.util.Collections.unmodifiableList(new ArrayList<>(Arrays.asList(1)));
        checkThrows(UnsupportedOperationException.class, () -> ro.replaceAll(x -> x),
                    "replaceAll on unmodifiable UOE");
        // identity operator leaves content
        List<String> l = new ArrayList<>(Arrays.asList("a"));
        l.replaceAll(x -> x);
        check(l.equals(Arrays.asList("a")), "replaceAll identity");
        // replaceAll with null elements
        List<String> ln = new ArrayList<>(Arrays.asList("a", null));
        ln.replaceAll(x -> x == null ? "n" : x.toUpperCase(java.util.Locale.ROOT));
        check(ln.equals(Arrays.asList("A", "n")), "replaceAll maps null");
    }

    static void testDefaultMethodOnProbe() {
        // A List whose iterator supports no remove: removeIf must fall back (no crash)
        List<String> fixed = Arrays.asList("a", "b", "c");
        checkThrows(UnsupportedOperationException.class, () -> fixed.removeIf(x -> true),
                    "removeIf on Arrays.asList UOE");
        check(fixed.size() == 3, "failed removeIf leaves list intact");
        // forEach works on fixed-size list
        StringBuilder sb = new StringBuilder();
        fixed.forEach(sb::append);
        check(sb.toString().equals("abc"), "forEach on Arrays.asList");
        // containsAll/spliterator default available
        check(fixed.containsAll(Arrays.asList("a", "c")), "containsAll");
        check(fixed.spliterator().estimateSize() == 3, "spliterator default SIZED");
    }

    static void testSubListViewContract() {
        List<Integer> l = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = l.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList content");
        check(sub.hashCode() == Arrays.asList(2, 3, 4).hashCode(), "subList hashCode");
        check(sub.indexOf(3) == 1, "subList indexOf relative");
        sub.set(0, 20);
        check(l.get(1) == 20, "subList set passes through");
        sub.add(0, 99);
        check(l.equals(Arrays.asList(1, 99, 20, 3, 4, 5)), "subList add passes through");
        sub.clear();
        check(l.equals(Arrays.asList(1, 5)), "subList clear removes range");
        // subList boundaries
        check(l.subList(0, 0).isEmpty() && l.subList(2, 2).isEmpty(), "empty subLists");
        check(l.subList(0, l.size()).equals(l), "full-range subList equals whole");
    }

    static void testIndexedBoundContract() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        checkThrows(IndexOutOfBoundsException.class, () -> l.get(-1), "get(-1) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.get(2), "get(size) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.set(2, "x"), "set(size) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.add(3, "x"), "add(size+1) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.remove(2), "remove(size) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.addAll(3, Arrays.asList("c")),
                    "addAll(size+1) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> l.listIterator(3), "listIterator(size+1) IOOBE");
        check(l.equals(Arrays.asList("a", "b")), "failed ops leave list intact");
        l.add(2, "c"); // add(size) legal append
        check(l.equals(Arrays.asList("a", "b", "c")), "add(size,e) appends");
    }

    static void testArraysAsListView() {
        // Arrays.asList: fixed size, set passes through, add/remove UOE
        List<String> v = Arrays.asList("a", "b");
        check(v.set(0, "z").equals("a"), "asList set returns old");
        check(v.get(0).equals("z"), "asList set stored");
        check(v.size() == 2, "asList size fixed");
        checkThrows(UnsupportedOperationException.class, () -> v.add("x"), "asList add UOE");
        checkThrows(UnsupportedOperationException.class, () -> v.remove(0), "asList remove UOE");
        checkThrows(UnsupportedOperationException.class, () -> v.clear(), "asList clear UOE");
        List<String> sub = v.subList(0, 1);
        checkThrows(UnsupportedOperationException.class, () -> sub.add("x"), "asList subList add UOE");
        check(sub.set(0, "y").equals("z"), "asList subList set passes through");
        // asList equals ArrayList
        check(v.equals(new ArrayList<>(Arrays.asList("y", "b"))), "asList equals ArrayList");
    }

    public static void main(String[] args) {
        testIndexedOps();
        testSubList();
        testListIterator();
        testSort();
        testReplaceAllRemoveIf();
        testHashCodeEqualsContract();
        testSortDefaultDispatch();
        testReplaceAllDefaultDispatch();
        testDefaultMethodOnProbe();
        testSubListViewContract();
        testIndexedBoundContract();
        testArraysAsListView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
