/*
 * @test
 * @summary java.util.Collections API conformance: sort/reverse/shuffle/rotate/swap/fill/
 *          copy/replaceAll, binarySearch, min/max (natural and comparator), frequency/
 *          disjoint/nCopies, reverseOrder, and the empty/singleton/unmodifiable/synchronized
 *          wrappers (including UnsupportedOperationException).
 * @run main CollectionsTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    // ---- deepened contract coverage, round 2 (empirically locked on jdk8u372) --

    static void testBinarySearchInsertionFormula() {
        List<Integer> list = Arrays.asList(2, 4, 6, 8, 10);
        for (int i = 0; i < list.size(); i++)
            check(Collections.binarySearch(list, list.get(i)) == i,
                  "binarySearch hit at own index " + i);
        check(Collections.binarySearch(list, 1) == -1, "miss before first -> -1");
        check(Collections.binarySearch(list, 3) == -2, "miss gap after idx0 -> -2");
        check(Collections.binarySearch(list, 5) == -3, "miss gap after idx1 -> -3");
        check(Collections.binarySearch(list, 7) == -4, "miss gap after idx2 -> -4");
        check(Collections.binarySearch(list, 9) == -5, "miss gap after idx3 -> -5");
        check(Collections.binarySearch(list, 11) == -6, "miss after last -> -(size+1)");
        check(Collections.binarySearch(Collections.<Integer>emptyList(), 1) == -1,
              "binarySearch empty -> -1");
        // descending list searched with a reverse comparator
        List<Integer> desc = Arrays.asList(5, 4, 2, 1);
        check(Collections.binarySearch(desc, 4, Comparator.reverseOrder()) == 1,
              "comparator binarySearch hit");
        check(Collections.binarySearch(desc, 3, Comparator.reverseOrder()) == -3,
              "comparator binarySearch gap -> -3");
        check(Collections.binarySearch(desc, 6, Comparator.reverseOrder()) == -1,
              "comparator binarySearch before first -> -1");
        check(Collections.binarySearch(desc, 0, Comparator.reverseOrder()) == -5,
              "comparator binarySearch after last -> -(size+1)");
        // null key with natural ordering -> NPE; null comparator means natural order
        checkThrows(NullPointerException.class,
                    () -> Collections.binarySearch(Arrays.asList("a", "b"), null),
                    "binarySearch(null key) NPE");
        List<Integer> nat = Arrays.asList(1, 2, 3);
        check(Collections.binarySearch(nat, 2, null) == 1,
              "binarySearch(list, key, null comparator) natural ordering");
        check(Collections.binarySearch(nat, 2, Comparator.<Integer>naturalOrder()) == 1,
              "binarySearch explicit comparator hit");
        check(Collections.binarySearch(nat, 0, Comparator.<Integer>naturalOrder()) == -1,
              "binarySearch explicit comparator miss");
    }

    static void testRotateBoundaries() {
        List<Integer> r = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.rotate(r, 7);                    // 7 mod 5 == 2
        check(r.equals(Arrays.asList(4, 5, 1, 2, 3)), "rotate k>size folds mod size");
        Collections.rotate(r, -7);
        check(r.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate -k>size restores");
        Collections.rotate(r, 5);
        check(r.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate by size no-op");
        Collections.rotate(r, 0);
        check(r.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate by 0 no-op");
        List<Integer> empty = new ArrayList<>();
        Collections.rotate(empty, 3);
        check(empty.isEmpty(), "rotate empty list no-op");
        List<Integer> one = new ArrayList<>(Arrays.asList(9));
        Collections.rotate(one, 4);
        check(one.equals(Arrays.asList(9)), "rotate singleton no-op");
    }

    static void testNCopiesContract() {
        List<String> three = Collections.nCopies(3, "x");
        check(three.size() == 3 && !three.isEmpty(), "nCopies size/isEmpty");
        check(three.get(0).equals("x") && three.get(2).equals("x"), "nCopies get");
        check(three.contains("x") && !three.contains("y"), "nCopies contains");
        check(three.indexOf("x") == 0 && three.lastIndexOf("x") == 2, "nCopies indexOf/lastIndexOf");
        check(three.containsAll(Arrays.asList("x", "x")), "nCopies containsAll");
        check(three.equals(Arrays.asList("x", "x", "x")), "nCopies equals same content");
        check(!three.equals(Arrays.asList("x", "x")), "nCopies !equals smaller list");
        check(three.hashCode() == 31 * (31 * (31 * 1 + "x".hashCode()) + "x".hashCode()) + "x".hashCode(),
              "nCopies hashCode follows List.hashCode formula");
        check(Collections.nCopies(2, "a").hashCode() == 4065, "nCopies hashCode hand-computed 4065");
        check(Collections.nCopies(0, "x").isEmpty(), "nCopies(0) empty");
        check(Collections.nCopies(0, "x").equals(Collections.emptyList()), "nCopies(0) equals emptyList");
        check(Collections.nCopies(2, null).get(0) == null, "nCopies null element allowed");
        final List<String> neg = Collections.nCopies(3, "x");
        checkThrows(IndexOutOfBoundsException.class, () -> neg.get(3), "nCopies.get(size) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> neg.get(-1), "nCopies.get(-1) IOOBE");
        try {
            Collections.nCopies(-1, "x");
            throw new AssertionError("nCopies(-1): expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("List length = -1".equals(e.getMessage()), "nCopies(-1) message exact");
        }
    }

    static void testEmptyIteratorContract() {
        Iterator<Object> it = Collections.emptyIterator();
        check(!it.hasNext(), "emptyIterator hasNext false");
        checkThrows(NoSuchElementException.class, () -> it.next(), "emptyIterator.next NSEE");
        checkThrows(IllegalStateException.class, () -> it.remove(),
                    "emptyIterator.remove ISE (no preceding next)");
        check(!it.hasNext(), "emptyIterator hasNext still false after NSEE");

        ListIterator<Object> li = Collections.emptyListIterator();
        check(!li.hasNext() && !li.hasPrevious(), "emptyListIterator no next/previous");
        check(li.nextIndex() == 0 && li.previousIndex() == -1, "emptyListIterator indices");
        checkThrows(NoSuchElementException.class, () -> li.next(), "emptyListIterator.next NSEE");
        checkThrows(NoSuchElementException.class, () -> li.previous(), "emptyListIterator.previous NSEE");
        expectUOE(() -> li.add(new Object()), "emptyListIterator.add UOE");
        checkThrows(IllegalStateException.class, () -> li.set(new Object()),
                    "emptyListIterator.set ISE");
        checkThrows(IllegalStateException.class, () -> li.remove(),
                    "emptyListIterator.remove ISE");

        Enumeration<Object> en = Collections.emptyEnumeration();
        check(!en.hasMoreElements(), "emptyEnumeration hasMoreElements false");
        checkThrows(NoSuchElementException.class, () -> en.nextElement(),
                    "emptyEnumeration.nextElement NSEE");
    }

    static void testEmptySortedNavigable() {
        SortedSet<Object> ess = Collections.emptySortedSet();
        check(ess.isEmpty() && ess.size() == 0, "emptySortedSet empty");
        check(ess.comparator() == null, "emptySortedSet natural comparator null");
        expectUOE(() -> ess.add("x"), "emptySortedSet.add UOE");
        check(!ess.contains("x"), "emptySortedSet.contains false");
        checkThrows(NoSuchElementException.class, () -> ess.first(), "emptySortedSet.first NSEE");
        checkThrows(NoSuchElementException.class, () -> ess.last(), "emptySortedSet.last NSEE");

        NavigableSet<Object> ens = Collections.emptyNavigableSet();
        check(ens.isEmpty() && ens.floor("a") == null && ens.ceiling("a") == null,
              "emptyNavigableSet floor/ceiling null");
        check(ens.lower("a") == null && ens.higher("a") == null,
              "emptyNavigableSet lower/higher null");
        expectUOE(() -> ens.pollFirst(), "emptyNavigableSet.pollFirst UOE (documented quirk)");
        expectUOE(() -> ens.pollLast(), "emptyNavigableSet.pollLast UOE (documented quirk)");
        expectUOE(() -> ens.add("x"), "emptyNavigableSet.add UOE");
        checkThrows(NoSuchElementException.class, () -> ens.first(), "emptyNavigableSet.first NSEE");
        check(ens.headSet("z").isEmpty() && ens.tailSet("a").isEmpty(),
              "emptyNavigableSet sub-views empty");
        check(Collections.emptySet().equals(ens) && ens.equals(Collections.emptySet()),
              "emptyNavigableSet equals emptySet symmetric");

        NavigableMap<Object, Object> enm = Collections.emptyNavigableMap();
        check(enm.isEmpty() && enm.size() == 0, "emptyNavigableMap empty");
        check(enm.comparator() == null, "emptyNavigableMap comparator null");
        check(enm.firstEntry() == null && enm.lastEntry() == null,
              "emptyNavigableMap first/lastEntry null");
        check(enm.floorKey("x") == null && enm.ceilingKey("x") == null,
              "emptyNavigableMap floor/ceilingKey null");
        check(enm.descendingMap().isEmpty(), "emptyNavigableMap descendingMap empty");
        expectUOE(() -> enm.put("k", "v"), "emptyNavigableMap.put UOE");
        expectUOE(() -> enm.pollFirstEntry(), "emptyNavigableMap.pollFirstEntry UOE (quirk)");
        expectUOE(() -> enm.pollLastEntry(), "emptyNavigableMap.pollLastEntry UOE (quirk)");
        SortedMap<Object, Object> esm = Collections.emptySortedMap();
        checkThrows(NoSuchElementException.class, () -> esm.firstKey(),
                    "emptySortedMap.firstKey NSEE");
        checkThrows(NoSuchElementException.class, () -> esm.lastKey(),
                    "emptySortedMap.lastKey NSEE");
    }

    static void testSingletonContracts() {
        Set<String> ss = Collections.singleton("x");
        check(ss.hashCode() == "x".hashCode(), "singleton hashCode = element hash");
        check(ss.equals(new HashSet<>(Arrays.asList("x"))), "singleton equals HashSet");
        check(new HashSet<>(Arrays.asList("x")).equals(ss), "HashSet equals singleton (symmetric)");
        expectUOE(() -> ss.remove("x"), "singleton.remove UOE");
        expectUOE(() -> ss.clear(), "singleton.clear UOE");

        Map<String, Integer> sm = Collections.singletonMap("k", 1);
        check(sm.hashCode() == ("k".hashCode() ^ Integer.valueOf(1).hashCode()),
              "singletonMap hashCode = key ^ value hash");
        check(sm.equals(new HashMap<>(Collections.singletonMap("k", 1))),
              "singletonMap equals HashMap single entry");
        check(sm.containsKey("k") && sm.containsValue(1) && sm.getOrDefault("z", -1) == -1,
              "singletonMap query methods");
        expectUOE(() -> sm.remove("k"), "singletonMap.remove UOE");
        expectUOE(() -> sm.entrySet().iterator().next().setValue(9),
                  "singletonMap entry.setValue UOE");

        List<String> sl = Collections.singletonList("x");
        check(sl.hashCode() == 31 * 1 + "x".hashCode(), "singletonList hashCode formula");
        check(sl.equals(Arrays.asList("x")) && sl.indexOf("x") == 0, "singletonList content");
        check(sl.toString().equals("[x]"), "singletonList toString");
        expectUOE(() -> sl.iterator().remove(), "singletonList.iterator.remove UOE");
    }

    static void testUnmodifiableSortedNavigable() {
        TreeSet<Integer> ts = new TreeSet<>(Arrays.asList(1, 2, 3));
        SortedSet<Integer> uss = Collections.unmodifiableSortedSet(ts);
        expectUOE(() -> uss.headSet(3).add(9), "unmodSortedSet.headSet.add UOE");
        expectUOE(() -> uss.tailSet(1).remove(1), "unmodSortedSet.tailSet.remove UOE");
        expectUOE(() -> uss.remove(1), "unmodSortedSet.remove UOE");
        ts.add(5);
        check(uss.contains(5) && uss.size() == 4, "unmodSortedSet live view");
        NavigableSet<Integer> uns = Collections.unmodifiableNavigableSet(ts);
        check(uns.first() == 1 && uns.floor(3) == 3, "unmodNavigableSet read");
        expectUOE(() -> uns.pollFirst(), "unmodNavigableSet.pollFirst UOE");
        expectUOE(() -> uns.descendingSet().add(9), "unmodNavigableSet.descendingSet.add UOE");

        TreeMap<String, Integer> tm = new TreeMap<>();
        tm.put("a", 1);
        SortedMap<String, Integer> usm = Collections.unmodifiableSortedMap(tm);
        expectUOE(() -> usm.subMap("a", "z").put("b", 2), "unmodSortedMap.subMap.put UOE");
        tm.put("c", 3);
        check(usm.size() == 2 && usm.lastKey().equals("c"), "unmodSortedMap live view");
        NavigableMap<String, Integer> unm = Collections.unmodifiableNavigableMap(tm);
        check(unm.floorKey("b").equals("a"), "unmodNavigableMap read");
        expectUOE(() -> unm.descendingMap().put("z", 9), "unmodNavigableMap.descendingMap.put UOE");

        // unmodifiable Map default methods (Java 8) all route to put/remove -> UOE
        Map<String, Integer> um = Collections.unmodifiableMap(
                new HashMap<String, Integer>(Collections.singletonMap("k", 1)));
        expectUOE(() -> um.putIfAbsent("a", 1), "unmodMap.putIfAbsent UOE");
        expectUOE(() -> um.compute("a", (k, v) -> 1), "unmodMap.compute UOE");
        expectUOE(() -> um.computeIfAbsent("a", k -> 1), "unmodMap.computeIfAbsent UOE");
        expectUOE(() -> um.computeIfPresent("k", (k, v) -> 2), "unmodMap.computeIfPresent UOE");
        expectUOE(() -> um.merge("a", 1, Integer::sum), "unmodMap.merge UOE");
        expectUOE(() -> um.remove("k", 1), "unmodMap.remove(k,v) UOE");
        expectUOE(() -> um.replace("k", 2), "unmodMap.replace(k,v) UOE");
        expectUOE(() -> um.replace("k", 1, 2), "unmodMap.replace(k,old,new) UOE");
        expectUOE(() -> um.replaceAll((k, v) -> v), "unmodMap.replaceAll UOE");
        expectUOE(() -> um.entrySet().iterator().next().setValue(9), "unmodMap entry.setValue UOE");

        // unmodifiable List write channels through iterators/subList
        List<String> ul = Collections.unmodifiableList(new ArrayList<>(Arrays.asList("a")));
        expectUOE(() -> ul.iterator().remove(), "unmodList.iterator.remove UOE");
        expectUOE(() -> ul.listIterator().add("b"), "unmodList.listIterator.add UOE");
        expectUOE(() -> ul.listIterator().set("b"), "unmodList.listIterator.set UOE");
        expectUOE(() -> ul.subList(0, 1).add("b"), "unmodList.subList.add UOE");

        checkThrows(NullPointerException.class, () -> Collections.unmodifiableList(null),
                    "unmodifiableList(null) NPE");
        checkThrows(NullPointerException.class, () -> Collections.unmodifiableMap(null),
                    "unmodifiableMap(null) NPE");
        checkThrows(NullPointerException.class, () -> Collections.unmodifiableSet(null),
                    "unmodifiableSet(null) NPE");
    }

    static void testSynchronizedFamilies() {
        Collection<Integer> sc = Collections.synchronizedCollection(new ArrayList<>());
        check(sc.add(1) && sc.contains(1) && sc.size() == 1, "synchronizedCollection delegates");
        Set<Integer> sset = Collections.synchronizedSet(new HashSet<>());
        check(sset.add(1) && sset.contains(1), "synchronizedSet delegates");
        Map<String, Integer> smap = Collections.synchronizedMap(new HashMap<>());
        check(smap.put("k", 1) == null && smap.get("k") == 1, "synchronizedMap delegates");
        SortedMap<String, Integer> ssm = Collections.synchronizedSortedMap(new TreeMap<>());
        ssm.put("b", 2); ssm.put("a", 1);
        check(ssm.firstKey().equals("a") && ssm.get("b") == 2, "synchronizedSortedMap delegates");
        NavigableMap<String, Integer> snm = Collections.synchronizedNavigableMap(new TreeMap<>());
        snm.put("a", 1);
        check(snm.firstKey().equals("a"), "synchronizedNavigableMap delegates");
        SortedSet<Integer> sss = Collections.synchronizedSortedSet(new TreeSet<>());
        sss.add(2); sss.add(1);
        check(sss.first() == 1, "synchronizedSortedSet delegates");
        NavigableSet<Integer> sns = Collections.synchronizedNavigableSet(new TreeSet<>());
        sns.add(5);
        check(sns.first() == 5, "synchronizedNavigableSet delegates");

        // concurrent smoke: two threads x 1000 adds -> 2000 (bounded wait)
        final List<Integer> sync = Collections.synchronizedList(new ArrayList<Integer>());
        final CountDownLatch done = new CountDownLatch(2);
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) synchronized (sync) { sync.add(i); }
            done.countDown();
        };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.setDaemon(true); t2.setDaemon(true);
        t1.start(); t2.start();
        try {
            check(done.await(5, TimeUnit.SECONDS), "synchronizedList 2x1000 within 5s");
        } catch (InterruptedException e) {
            throw new AssertionError("interrupted waiting for workers", e);
        }
        check(sync.size() == 2000, "synchronizedList concurrent adds all land");
        try { t1.join(2000); t2.join(2000); } catch (InterruptedException e) { /* daemon */ }
    }

    static void testReverseOrderContract() {
        Comparator<Integer> rev = Collections.reverseOrder();
        Comparator<Integer> nat = Comparator.naturalOrder();
        check(rev.compare(1, 2) > 0 && rev.compare(2, 1) < 0 && rev.compare(3, 3) == 0,
              "reverseOrder sign matrix");
        check(rev.compare(1, 2) == -nat.compare(1, 2), "reverseOrder inverts naturalOrder");
        Comparator<Integer> revNat = Collections.reverseOrder(Comparator.<Integer>naturalOrder());
        check(revNat.compare(1, 2) > 0 && revNat.compare(2, 1) < 0,
              "reverseOrder(naturalOrder) descending");
        // documented as NPE for null cmp, but the implementation accepts null as natural order
        Comparator<Integer> revNull = Collections.reverseOrder(null);
        check(revNull.compare(1, 2) > 0 && revNull.compare(2, 1) < 0 && revNull.compare(5, 5) == 0,
              "reverseOrder(null) behaves as reverse natural order (no throw, empirical)");
        TreeSet<Integer> tr = new TreeSet<>(Collections.reverseOrder());
        tr.add(1); tr.add(3); tr.add(2);
        check(tr.equals(new TreeSet<>(Arrays.asList(3, 2, 1))), "TreeSet with reverseOrder content");
        check(new ArrayList<>(tr).equals(Arrays.asList(3, 2, 1)), "TreeSet reverseOrder iteration desc");
        check(tr.first() == 3 && tr.last() == 1, "TreeSet reverseOrder first/last swapped");
        // sgn contract: c(a,b) == -c(b,a) over a matrix
        Integer[] vals = {-2, 0, 2};
        for (Integer x : vals) for (Integer y : vals)
            check(Integer.signum(rev.compare(x, y)) == -Integer.signum(rev.compare(y, x)),
                  "reverseOrder sgn antisymmetry " + x + "," + y);
        // nullsFirst with reversed inner comparator: null first, then descending
        TreeSet<String> tn = new TreeSet<>(Comparator.nullsFirst(Comparator.reverseOrder()));
        tn.add("b"); tn.add(null); tn.add("a");
        check(tn.toString().equals("[null, b, a]"), "nullsFirst(reverseOrder) ordering");
    }

    static void testShuffleDeterminism() {
        List<Integer> a = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        List<Integer> b = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Collections.shuffle(a, new Random(42));
        Collections.shuffle(b, new Random(42));
        check(a.equals(b), "shuffle same seed -> identical sequence");
        List<Integer> sorted = new ArrayList<>(a);
        Collections.sort(sorted);
        check(sorted.equals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)),
              "shuffle preserves element multiset");
        checkThrows(NullPointerException.class,
                    () -> Collections.shuffle(new ArrayList<Integer>(Arrays.asList(1, 2)), null),
                    "shuffle(size>=2, null rnd) NPE (rnd consulted)");
        // empty list with null rnd: no element is touched, no throw needed by spec
        Collections.shuffle(new ArrayList<Integer>(), null);
        check(true, "shuffle(empty, null rnd) tolerated");
    }

    static void testMinMaxNullArgs() {
        checkThrows(NullPointerException.class, () -> Collections.min(null),
                    "min(null list) NPE");
        checkThrows(NullPointerException.class, () -> Collections.max(null),
                    "max(null list) NPE");
        // null comparator means natural ordering (no NPE; implementation substitutes naturalOrder)
        check(Collections.min(Arrays.asList(3, 1, 2), null) == 1,
              "min(list, null comparator) natural ordering");
        check(Collections.max(Arrays.asList(3, 1, 2), null) == 3,
              "max(list, null comparator) natural ordering");
        List<String> withNull = Arrays.asList("b", null, "a");
        check(Collections.min(withNull, Comparator.nullsLast(Comparator.naturalOrder())).equals("a"),
              "min with nullsLast ignores trailing null");
        check(Collections.max(withNull, Comparator.nullsFirst(Comparator.naturalOrder())).equals("b"),
              "max with nullsFirst ignores leading null");
    }

    static void testDisjointNullArgs() {
        checkThrows(NullPointerException.class,
                    () -> Collections.disjoint(null, Collections.emptyList()),
                    "disjoint(null, empty) NPE (even empty arg pair)");
        checkThrows(NullPointerException.class,
                    () -> Collections.disjoint(Collections.emptyList(), null),
                    "disjoint(empty, null) NPE");
        checkThrows(NullPointerException.class,
                    () -> Collections.disjoint(Arrays.asList(1), null),
                    "disjoint(non-empty, null) NPE");
        checkThrows(NullPointerException.class,
                    () -> Collections.disjoint(null, Arrays.asList(1)),
                    "disjoint(null, non-empty) NPE");
        check(Collections.disjoint(Collections.emptyList(), Collections.emptyList()),
              "disjoint(empty, empty) true");
    }

    static void testNewSetFromMapContract() {
        Set<String> s = Collections.newSetFromMap(new HashMap<String, Boolean>());
        check(s.add("a") && s.add("b"), "newSetFromMap add new elements true");
        check(!s.add("a"), "newSetFromMap duplicate add false");
        check(s.contains("a") && s.size() == 2, "newSetFromMap content");
        check(s.remove("a") && !s.contains("a"), "newSetFromMap remove");
        check(s.removeAll(Arrays.asList("b")) && s.isEmpty(), "newSetFromMap removeAll");
        HashMap<String, Boolean> backing = new HashMap<>();
        Set<String> view = Collections.newSetFromMap(backing);
        backing.put("z", Boolean.TRUE);           // direct backing-map mutation shows in view
        check(view.contains("z") && view.size() == 1, "newSetFromMap backing map writes visible");
        check(view.add("w") && Boolean.TRUE.equals(backing.get("w")),
              "newSetFromMap view add writes backing map");
        checkThrows(IllegalArgumentException.class,
                    () -> Collections.newSetFromMap(new HashMap<String, Boolean>(
                            Collections.singletonMap("x", Boolean.TRUE))),
                    "newSetFromMap(non-empty map) IAE");
        checkThrows(NullPointerException.class,
                    () -> Collections.newSetFromMap(null),
                    "newSetFromMap(null) NPE");
        // set semantics: equals/hashCode like an ordinary set
        check(view.equals(new HashSet<>(Arrays.asList("z", "w"))), "newSetFromMap equals HashSet");
        check(view.hashCode() == new HashSet<>(Arrays.asList("z", "w")).hashCode(),
              "newSetFromMap hashCode = sum of element hashes");
    }

    static void testAsLifoQueueContract() {
        Deque<Integer> deque = new ArrayDeque<>();
        Queue<Integer> lifo = Collections.asLifoQueue(deque);
        check(lifo.add(1) && lifo.add(2) && lifo.add(3), "asLifoQueue add");
        check(lifo.size() == 3, "asLifoQueue size");
        check(lifo.peek() == 3, "asLifoQueue peek returns last added");
        check(lifo.remove() == 3, "asLifoQueue remove pops last added");
        check(lifo.poll() == 2, "asLifoQueue poll LIFO");
        check(lifo.element() == 1, "asLifoQueue element is oldest");
        check(lifo.remove() == 1 && lifo.isEmpty(), "asLifoQueue drained");
        check(lifo.poll() == null, "asLifoQueue poll empty null");
        checkThrows(NoSuchElementException.class, () -> lifo.element(),
                    "asLifoQueue element empty NSEE");
        checkThrows(NoSuchElementException.class, () -> lifo.remove(),
                    "asLifoQueue remove empty NSEE");
        check(lifo.offer(9) && lifo.peek() == 9, "asLifoQueue offer then peek");
        check(lifo.addAll(Arrays.asList(7, 8)) && lifo.size() == 3,
              "asLifoQueue addAll appends all");
        check(lifo.contains(7) && lifo.remove(Integer.valueOf(7)),
              "asLifoQueue contains/remove(Object)");
    }

    static void testEnumerationListEdges() {
        Enumeration<String> empty = Collections.enumeration(Collections.<String>emptyList());
        check(!empty.hasMoreElements(), "enumeration over empty collection");
        checkThrows(NoSuchElementException.class, () -> empty.nextElement(),
                    "enumeration over empty nextElement NSEE");
        List<String> src = Arrays.asList("p", "q", "r");
        Enumeration<String> en = Collections.enumeration(src);
        List<String> out = new ArrayList<>();
        while (en.hasMoreElements()) out.add(en.nextElement());
        check(out.equals(src), "enumeration preserves order");
        check(Collections.list(Collections.enumeration(src)).equals(src),
              "list(enumeration(...)) round-trip");
        checkThrows(NullPointerException.class, () -> Collections.list(null),
                    "list(null) NPE");
        check(Collections.addAll(new ArrayList<String>()) == false,
              "addAll with no varargs returns false");
    }

    static void testSerializationRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        List<String> empty = Collections.emptyList();
        List<String> single = Collections.singletonList("x");
        List<String> copies = Collections.nCopies(2, "y");
        List<String> unmod = Collections.unmodifiableList(new ArrayList<>(Arrays.asList("u", "v")));
        Map<String, Integer> unmodM = Collections.unmodifiableMap(
                new HashMap<String, Integer>(Collections.singletonMap("k", 1)));
        oos.writeObject(empty); oos.writeObject(single); oos.writeObject(copies);
        oos.writeObject(unmod); oos.writeObject(unmodM);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject().equals(empty), "serialized emptyList round-trip");
        check(ois.readObject().equals(single), "serialized singletonList round-trip");
        check(ois.readObject().equals(copies), "serialized nCopies round-trip");
        @SuppressWarnings("unchecked")
        List<String> unmodBack = (List<String>) ois.readObject();
        check(unmodBack.equals(unmod), "serialized unmodifiableList round-trip");
        expectUOE(() -> unmodBack.add("w"), "serialized unmodifiableList still unmodifiable");
        @SuppressWarnings("unchecked")
        Map<String, Integer> unmodMBack = (Map<String, Integer>) ois.readObject();
        check(unmodMBack.equals(unmodM), "serialized unmodifiableMap round-trip");
        expectUOE(() -> unmodMBack.put("z", 2), "serialized unmodifiableMap still unmodifiable");
        ois.close();
    }

    static void testCheckedMoreWrappers() {
        SortedSet<String> css = Collections.checkedSortedSet(new TreeSet<String>(), String.class);
        css.add("a");
        check(css.first().equals("a") && css.comparator() == null, "checkedSortedSet delegates");
        @SuppressWarnings({"unchecked", "rawtypes"})
        SortedSet rawCss = css;
        checkThrows(ClassCastException.class, () -> rawCss.add(4),
                    "checkedSortedSet.add wrong type CCE");
        SortedMap<String, Integer> csm =
                Collections.checkedSortedMap(new TreeMap<String, Integer>(), String.class, Integer.class);
        csm.put("a", 1);
        check(csm.subMap("a", "z").size() == 1, "checkedSortedMap.subMap view");
        @SuppressWarnings({"unchecked", "rawtypes"})
        SortedMap rawCsm = csm;
        checkThrows(ClassCastException.class, () -> rawCsm.put("b", "v"),
                    "checkedSortedMap.put wrong value type CCE");
        NavigableSet<String> cns =
                Collections.checkedNavigableSet(new TreeSet<String>(), String.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        NavigableSet rawCns = cns;
        checkThrows(ClassCastException.class, () -> rawCns.add(5),
                    "checkedNavigableSet.add wrong type CCE");
        NavigableMap<String, Integer> cnm =
                Collections.checkedNavigableMap(new TreeMap<String, Integer>(), String.class, Integer.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        NavigableMap rawCnm = cnm;
        checkThrows(ClassCastException.class, () -> rawCnm.put(7, 1),
                    "checkedNavigableMap.put wrong key type CCE");
        List<String> cl = Collections.checkedList(new ArrayList<String>(), String.class);
        cl.add("ok");
        checkThrows(ClassCastException.class,
                    () -> cl.listIterator().add((String) (Object) Integer.valueOf(5)),
                    "checkedList.listIterator.add wrong type CCE");
        checkThrows(ClassCastException.class,
                    () -> cl.set(0, (String) (Object) Integer.valueOf(5)),
                    "checkedList.set wrong type CCE");
    }

    static void testMutatorNullArgs() {
        checkThrows(NullPointerException.class, () -> Collections.reverse(null),
                    "reverse(null) NPE");
        checkThrows(NullPointerException.class, () -> Collections.fill(null, "x"),
                    "fill(null, x) NPE");
        checkThrows(NullPointerException.class,
                    () -> Collections.binarySearch(null, 1),
                    "binarySearch(null list) NPE");
        checkThrows(NullPointerException.class,
                    () -> Collections.copy(null, Arrays.asList(1)),
                    "copy(null dest, src) NPE");
        checkThrows(NullPointerException.class,
                    () -> Collections.copy(new ArrayList<Integer>(), null),
                    "copy(dest, null src) NPE");
        List<String> replaceNull = new ArrayList<>(Arrays.asList("a", null, "a"));
        check(Collections.replaceAll(replaceNull, null, "x"),
              "replaceAll with null old value matches nulls");
        check(replaceNull.equals(Arrays.asList("a", "x", "a")), "replaceAll null replaced");
        List<Integer> same = new ArrayList<>(Arrays.asList(1, 2));
        Collections.swap(same, 1, 1);
        check(same.equals(Arrays.asList(1, 2)), "swap(i, i) no-op");
    }

    public static void main(String[] args) throws Exception {
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
        testBinarySearchInsertionFormula();
        testRotateBoundaries();
        testNCopiesContract();
        testEmptyIteratorContract();
        testEmptySortedNavigable();
        testSingletonContracts();
        testUnmodifiableSortedNavigable();
        testSynchronizedFamilies();
        testReverseOrderContract();
        testShuffleDeterminism();
        testMinMaxNullArgs();
        testDisjointNullArgs();
        testNewSetFromMapContract();
        testAsLifoQueueContract();
        testEnumerationListEdges();
        testSerializationRoundTrip();
        testCheckedMoreWrappers();
        testMutatorNullArgs();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
