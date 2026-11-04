/*
 * @test
 * @summary java.util.Collections API conformance: sort/reverse/shuffle/rotate/swap/fill/
 *          copy/replaceAll, binarySearch, min/max (natural and comparator), frequency/
 *          disjoint/nCopies, reverseOrder, and the empty/singleton/unmodifiable/synchronized
 *          wrappers (including UnsupportedOperationException).
 * @run main CollectionsTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class CollectionsTest {
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

    static void expectUOE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
    }

    static void testSortReverse() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        Collections.sort(list);
        check(list.equals(Arrays.asList(1, 2, 3)), "sort natural");
        Collections.sort(list, Comparator.reverseOrder());
        check(list.equals(Arrays.asList(3, 2, 1)), "sort with reverse comparator");
        Collections.reverse(list);
        check(list.equals(Arrays.asList(1, 2, 3)), "reverse");

        List<Integer> shuffled = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.shuffle(shuffled);
        List<Integer> sorted = new ArrayList<>(shuffled);
        Collections.sort(sorted);
        check(sorted.equals(Arrays.asList(1, 2, 3, 4, 5)), "shuffle preserves elements");
    }

    static void testBinarySearch() {
        List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
        check(Collections.binarySearch(list, 5) == 2, "binarySearch found");
        check(Collections.binarySearch(list, 2) == -2, "binarySearch insertion point");
        check(Collections.binarySearch(list, 10) == -6, "binarySearch after last");
    }

    static void testMinMax() {
        List<Integer> list = Arrays.asList(3, 1, 2);
        check(Collections.min(list) == 1, "min");
        check(Collections.max(list) == 3, "max");
        check(Collections.min(list, Comparator.reverseOrder()) == 3, "min reverse");
        check(Collections.max(list, Comparator.reverseOrder()) == 1, "max reverse");
    }

    static void testFrequencyDisjointNCopies() {
        List<String> list = Arrays.asList("a", "b", "a", "c", "a");
        check(Collections.frequency(list, "a") == 3, "frequency");
        check(Collections.frequency(list, "z") == 0, "frequency missing");
        check(Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(3, 4)), "disjoint true");
        check(!Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(2, 3)), "disjoint false");
        check(Collections.nCopies(3, "x").equals(Arrays.asList("x", "x", "x")), "nCopies");
    }

    static void testReverseOrder() {
        Comparator<Integer> rev = Collections.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder compares descending");
        check(Collections.reverseOrder(Comparator.<Integer>naturalOrder()).compare(1, 2) > 0,
              "reverseOrder(Comparator)");
    }

    static void testUnmodifiable() {
        List<String> base = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> unmod = Collections.unmodifiableList(base);
        check(unmod.equals(Arrays.asList("a", "b")), "unmodifiableList content");
        expectUOE(() -> unmod.add("c"), "unmodifiableList.add");
        expectUOE(() -> unmod.remove(0), "unmodifiableList.remove");
        expectUOE(() -> unmod.set(0, "z"), "unmodifiableList.set");

        check(Collections.unmodifiableMap(Collections.singletonMap("k", "v")).get("k").equals("v"),
              "unmodifiableMap get");
    }

    static void testEmptySingleton() {
        check(Collections.emptyList().isEmpty(), "emptyList empty");
        check(Collections.emptySet().isEmpty(), "emptySet empty");
        check(Collections.emptyMap().isEmpty(), "emptyMap empty");
        expectUOE(() -> Collections.emptyList().add("x"), "emptyList.add");

        check(Collections.singletonList("x").get(0).equals("x"), "singletonList");
        check(Collections.singleton("x").contains("x"), "singleton set");
        check(Collections.singletonMap("k", 1).get("k") == 1, "singletonMap");
    }

    static void testSynchronized() {
        List<String> sync = Collections.synchronizedList(new ArrayList<>(Arrays.asList("a", "b")));
        check(sync.size() == 2 && sync.get(0).equals("a"), "synchronizedList content");
        sync.add("c");
        check(sync.size() == 3, "synchronizedList add");
    }

    // ---- deepened contract coverage ----------------------------------------

    static void testSortEdgeCases() {
        checkThrows(NullPointerException.class, () -> Collections.sort(null), "sort(null) NPE");
        // sort(list, null comparator) uses natural ordering (List.sort contract).
        List<Integer> nat = new ArrayList<>(Arrays.asList(3, 1, 2));
        Collections.sort(nat, null);
        check(nat.equals(Arrays.asList(1, 2, 3)), "sort(list, null) natural ordering");

        List<Integer> empty = new ArrayList<>();
        checkThrows(NoSuchElementException.class, () -> Collections.min(empty), "min empty NSEE");
        checkThrows(NoSuchElementException.class, () -> Collections.max(empty), "max empty NSEE");

        List<String> withNull = new ArrayList<>(Arrays.asList("b", null, "a"));
        Collections.sort(withNull, Comparator.nullsFirst(Comparator.<String>naturalOrder()));
        check(withNull.get(0) == null, "sort with nullsFirst puts null first");
    }

    static void testCopyFillReplaceAllRotateSwap() {
        List<Integer> src = Arrays.asList(1, 2, 3);
        List<Integer> dest = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        Collections.copy(dest, src);
        check(dest.equals(Arrays.asList(1, 2, 3, 0)), "copy overwrites prefix");
        List<Integer> tooSmall = new ArrayList<>(Arrays.asList(0));
        checkThrows(IndexOutOfBoundsException.class, () -> Collections.copy(tooSmall, src),
                    "copy(dest smaller than src) IOBE");

        List<String> filled = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Collections.fill(filled, "z");
        check(filled.equals(Arrays.asList("z", "z", "z")), "fill");

        List<String> rep = new ArrayList<>(Arrays.asList("a", "b", "a"));
        check(Collections.replaceAll(rep, "a", "x"), "replaceAll returns true");
        check(rep.equals(Arrays.asList("x", "b", "x")), "replaceAll replaces all occurrences");
        check(!Collections.replaceAll(rep, "missing", "y"), "replaceAll returns false when absent");

        List<Integer> rot = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.rotate(rot, 2);
        check(rot.equals(Arrays.asList(4, 5, 1, 2, 3)), "rotate right by 2");
        Collections.rotate(rot, -2);
        check(rot.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate back by -2");

        List<Integer> sw = new ArrayList<>(Arrays.asList(1, 2, 3));
        Collections.swap(sw, 0, 2);
        check(sw.equals(Arrays.asList(3, 2, 1)), "swap");
        checkThrows(IndexOutOfBoundsException.class, () -> Collections.swap(sw, 0, 5), "swap OOB IOBE");
    }

    static void testAddAllEnumerationList() {
        List<Integer> target = new ArrayList<>(Arrays.asList(1));
        check(Collections.addAll(target, 2, 3, 4), "addAll(T...) returns true");
        check(target.equals(Arrays.asList(1, 2, 3, 4)), "addAll(T...) appends");

        Enumeration<String> en = Collections.enumeration(Arrays.asList("a", "b"));
        check(en.hasMoreElements() && en.nextElement().equals("a"), "enumeration next");
        List<String> fromEnum = Collections.list(Collections.enumeration(Arrays.asList("x", "y")));
        check(fromEnum.equals(Arrays.asList("x", "y")), "list(enumeration)");

        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
        Enumeration<Integer> sen = Collections.enumeration(set);
        check(sen.hasMoreElements() && sen.nextElement() != null, "enumeration over set");
    }

    static void testCheckedCollections() {
        List<String> checked = Collections.checkedList(new ArrayList<String>(), String.class);
        checked.add("ok");
        checkThrows(ClassCastException.class,
                    () -> checked.add((String) (Object) Integer.valueOf(5)),
                    "checkedList.add wrong type CCE");

        Set<String> checkedSet = Collections.checkedSet(new HashSet<String>(), String.class);
        checkedSet.add("ok");
        checkThrows(ClassCastException.class,
                    () -> checkedSet.add((String) (Object) Integer.valueOf(5)),
                    "checkedSet.add wrong type CCE");

        Map<String, Integer> checkedMap = Collections.checkedMap(new HashMap<String, Integer>(), String.class, Integer.class);
        checkedMap.put("k", 1);
        checkThrows(ClassCastException.class,
                    () -> checkedMap.put("k2", (Integer) (Object) "wrong"),
                    "checkedMap.put wrong value type CCE");
        checkThrows(ClassCastException.class,
                    () -> checkedMap.put((String) (Object) Integer.valueOf(1), 2),
                    "checkedMap.put wrong key type CCE");

        Collection<String> checkedColl = Collections.checkedCollection(new ArrayList<String>(), String.class);
        checkedColl.add("ok");
        checkThrows(ClassCastException.class,
                    () -> checkedColl.add((String) (Object) Integer.valueOf(5)),
                    "checkedCollection.add wrong type CCE");
    }

    static void testUnmodifiableIsLiveView() {
        List<String> base = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> unmod = Collections.unmodifiableList(base);
        base.set(0, "A");
        check(unmod.get(0).equals("A"), "unmodifiableList is live view");
        base.add("c");
        check(unmod.size() == 3, "unmodifiableList reflects add to backing");
        expectUOE(() -> unmod.clear(), "unmodifiableList.clear");

        Map<String, Integer> m = new HashMap<>();
        m.put("k", 1);
        Map<String, Integer> um = Collections.unmodifiableMap(m);
        m.put("k2", 2);
        check(um.size() == 2 && um.get("k2") == 2, "unmodifiableMap live view");
        expectUOE(() -> um.put("k3", 3), "unmodifiableMap.put");

        Set<String> s = new HashSet<>(Arrays.asList("a"));
        Set<String> us = Collections.unmodifiableSet(s);
        s.add("b");
        check(us.size() == 2, "unmodifiableSet live view");
        expectUOE(() -> us.add("c"), "unmodifiableSet.add");
    }

    static void testEmptySingletonIdentity() {
        check(Collections.emptyList() == Collections.emptyList(), "emptyList singleton");
        check(Collections.emptySet() == Collections.emptySet(), "emptySet singleton");
        check(Collections.emptyMap() == Collections.emptyMap(), "emptyMap singleton");
        check(Collections.emptyList().size() == 0, "emptyList size 0");
        expectUOE(() -> Collections.emptyList().add("x"), "emptyList.add UOE");
        expectUOE(() -> Collections.emptySet().add("x"), "emptySet.add UOE");
        expectUOE(() -> Collections.emptyMap().put("k", "v"), "emptyMap.put UOE");
    }

    static void testSingletonEdge() {
        List<String> sl = Collections.singletonList("x");
        check(sl.size() == 1 && sl.get(0).equals("x"), "singletonList");
        expectUOE(() -> sl.add("y"), "singletonList.add UOE");
        expectUOE(() -> sl.set(0, "y"), "singletonList.set UOE");
        expectUOE(() -> sl.remove(0), "singletonList.remove UOE");

        List<String> slNull = Collections.singletonList(null);
        check(slNull.size() == 1 && slNull.get(0) == null, "singletonList(null) allowed");

        Set<String> ss = Collections.singleton("x");
        check(ss.contains("x") && ss.size() == 1, "singleton set");
        expectUOE(() -> ss.add("y"), "singleton add UOE");

        Map<String, Integer> sm = Collections.singletonMap("k", 1);
        check(sm.get("k") == 1 && sm.size() == 1, "singletonMap");
        expectUOE(() -> sm.put("k2", 2), "singletonMap.put UOE");

        check(Collections.nCopies(3, null).size() == 3, "nCopies with null");
        expectUOE(() -> Collections.nCopies(3, "x").set(0, "y"), "nCopies.set UOE");
    }

    static void testFrequencyDisjointNull() {
        List<String> list = Arrays.asList("a", null, "a", null);
        check(Collections.frequency(list, null) == 2, "frequency(null)");
        check(Collections.disjoint(Collections.singletonList((String) null), Arrays.asList("x")),
              "disjoint with null");
        check(!Collections.disjoint(Collections.singletonList((String) null),
                                    Collections.singletonList((String) null)),
              "!disjoint when both share null");
    }

    static void testIndexOfSubList() {
        List<Integer> list = Arrays.asList(1, 2, 3, 2, 3);
        check(Collections.indexOfSubList(list, Arrays.asList(2, 3)) == 1, "indexOfSubList");
        check(Collections.lastIndexOfSubList(list, Arrays.asList(2, 3)) == 3, "lastIndexOfSubList");
        check(Collections.indexOfSubList(list, Arrays.asList(9)) == -1, "indexOfSubList absent");
    }

    public static void main(String[] args) {
        testSortReverse();
        testBinarySearch();
        testMinMax();
        testFrequencyDisjointNCopies();
        testReverseOrder();
        testUnmodifiable();
        testEmptySingleton();
        testSynchronized();
        testSortEdgeCases();
        testCopyFillReplaceAllRotateSwap();
        testAddAllEnumerationList();
        testCheckedCollections();
        testUnmodifiableIsLiveView();
        testSingletonEdge();
        testEmptySingletonIdentity();
        testFrequencyDisjointNull();
        testIndexOfSubList();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
