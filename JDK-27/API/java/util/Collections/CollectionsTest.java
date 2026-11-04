/*
 * @test
 * @summary java.util.Collections conformance. White-box: Class.forName + class modifiers/
 *          constant fields/member shape. Black-box: sort/binarySearch (natural + Comparator),
 *          reverse/shuffle (Random seed determinism)/rotate/swap/fill/copy/replaceAll, min/max
 *          (natural + Comparator + empty), frequency/disjoint/nCopies/addAll/indexOfSubList/
 *          lastIndexOfSubList, reverseOrder, checkedList type-check, unmodifiable/singleton/
 *          empty wrappers (UnsupportedOperationException on mutation), emptyIterator/
 *          ListIterator/Enumeration, enumeration/list round-trip, newSetFromMap, and asLifoQueue.
 * @run main CollectionsTest
 *
 * 设计案：
 * 1. 契约：Collections 是集合算法与包装器工具；算法（sort/binarySearch/rotate/…）原地或
 *    返回计算结果；包装器（unmodifiable/synchronized/checked/empty/singleton）提供不同语义。
 * 2. 用户场景：构造 List → sort 排序 → binarySearch 查找 → reverse/rotate/shuffle/swap/fill
 *    重排 → min/max/frequency/disjoint 统计 → checkedList/unmodifiableList 包装 →
 *    singleton/empty 工厂 → asLifoQueue/newSetFromMap 视图。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：shuffle(Random 固定种子) 可复现；sort/rotate/swap/fill 保持元素多重集；
 *    unmodifiable 包装不改变读语义、拒绝写；singleton 恰含一个元素。
 * 5. 边界/异常：copy 目标过小抛 IndexOutOfBoundsException；min/max 空集合抛
 *    NoSuchElementException；checkedList 插入错类型抛 ClassCastException；unmodifiable/
 *    singleton/empty 的写操作抛 UnsupportedOperationException。
 */

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                passed++;
            } else {
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
        }
    }

    // ---- white-box: 结构/反射断言 ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Collections");
        check(!c.isInterface(), "Collections is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Collections public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Collections final");
        java.lang.reflect.Field emptyList = c.getField("EMPTY_LIST");
        check(java.lang.reflect.Modifier.isStatic(emptyList.getModifiers())
              && java.lang.reflect.Modifier.isFinal(emptyList.getModifiers()),
              "EMPTY_LIST is static final");
        c.getField("EMPTY_SET");
        c.getField("EMPTY_MAP");
        c.getMethod("sort", java.util.List.class);
        c.getMethod("binarySearch", java.util.List.class, Object.class);
        c.getMethod("reverse", java.util.List.class);
        check(true, "key public static members present");
    }

    static void testSortBinarySearch() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        Collections.sort(list);
        check(list.equals(Arrays.asList(1, 2, 3)), "sort natural");
        Collections.sort(list, Comparator.reverseOrder());
        check(list.equals(Arrays.asList(3, 2, 1)), "sort with reverse comparator");

        List<Integer> sorted = Arrays.asList(1, 3, 5, 7, 9);
        check(Collections.binarySearch(sorted, 5) == 2, "binarySearch found");
        check(Collections.binarySearch(sorted, 2) == -2, "binarySearch insertion point");
        check(Collections.binarySearch(sorted, 5, Comparator.naturalOrder()) == 2,
              "binarySearch with comparator");
    }

    static void testReverseShuffleRotateSwapFillCopyReplace() {
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
        Collections.reverse(list);
        check(list.equals(Arrays.asList(3, 2, 1)), "reverse");

        List<Integer> a = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> b = new ArrayList<>(a);
        Collections.shuffle(a, new Random(42));
        Collections.shuffle(b, new Random(42));
        check(a.equals(b), "shuffle same seed identical");
        List<Integer> sortedCopy = new ArrayList<>(a);
        Collections.sort(sortedCopy);
        check(sortedCopy.equals(Arrays.asList(1, 2, 3, 4, 5)), "shuffle preserves elements");

        List<Integer> r = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Collections.rotate(r, 2);
        check(r.equals(Arrays.asList(4, 5, 1, 2, 3)), "rotate +2");
        Collections.rotate(r, -2);
        check(r.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate -2 reverses");
        Collections.rotate(r, 5);
        check(r.equals(Arrays.asList(1, 2, 3, 4, 5)), "rotate by size no-op");

        Collections.swap(r, 0, 4);
        check(r.equals(Arrays.asList(5, 2, 3, 4, 1)), "swap");

        Collections.fill(r, 0);
        check(r.equals(Arrays.asList(0, 0, 0, 0, 0)), "fill");

        List<Integer> src = Arrays.asList(1, 2, 3);
        List<Integer> dst = new ArrayList<>(Arrays.asList(null, null, null));
        Collections.copy(dst, src);
        check(dst.equals(Arrays.asList(1, 2, 3)), "copy");
        List<Integer> tooSmall = new ArrayList<>(Arrays.asList(0, 0));
        expect(IndexOutOfBoundsException.class, () -> Collections.copy(tooSmall, src),
              "copy dest too small IOBE");

        List<String> repl = new ArrayList<>(Arrays.asList("a", "b", "a"));
        check(Collections.replaceAll(repl, "a", "X"), "replaceAll returns true");
        check(repl.equals(Arrays.asList("X", "b", "X")), "replaceAll replaces all");
        check(!Collections.replaceAll(repl, "z", "q"), "replaceAll absent returns false");
    }

    static void testMinMaxFrequency() {
        List<Integer> list = Arrays.asList(3, 1, 2);
        check(Collections.min(list) == 1 && Collections.max(list) == 3, "min/max natural");
        check(Collections.min(list, Comparator.reverseOrder()) == 3, "min reverse");
        check(Collections.max(list, Comparator.reverseOrder()) == 1, "max reverse");
        expect(NoSuchElementException.class, () -> Collections.min(Collections.emptyList()),
              "min empty NSEE");
        expect(NoSuchElementException.class, () -> Collections.max(Collections.emptyList()),
              "max empty NSEE");

        List<String> f = Arrays.asList("a", "b", "a", "c", "a");
        check(Collections.frequency(f, "a") == 3, "frequency");
        check(Collections.frequency(f, "z") == 0, "frequency missing");
        check(Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(3, 4)), "disjoint true");
        check(!Collections.disjoint(Arrays.asList(1, 2), Arrays.asList(2, 3)), "disjoint false");
        check(Collections.disjoint(Collections.emptyList(), Arrays.asList(1)), "disjoint empty");
        check(Collections.nCopies(3, "x").equals(Arrays.asList("x", "x", "x")), "nCopies");
    }

    static void testAddAllSubListReverseOrder() {
        List<Integer> list = new ArrayList<>();
        check(Collections.addAll(list, 1, 2, 3), "addAll returns true");
        check(list.equals(Arrays.asList(1, 2, 3)), "addAll adds varargs");

        List<Integer> source = Arrays.asList(1, 2, 3, 2, 3, 4);
        List<Integer> target = Arrays.asList(2, 3);
        check(Collections.indexOfSubList(source, target) == 1, "indexOfSubList");
        check(Collections.lastIndexOfSubList(source, target) == 3, "lastIndexOfSubList");

        Comparator<Integer> rev = Collections.reverseOrder();
        check(rev.compare(1, 2) > 0, "reverseOrder descending");
        check(Collections.reverseOrder(Comparator.<Integer>naturalOrder()).compare(1, 2) > 0,
              "reverseOrder(Comparator)");
    }

    static void testChecked() {
        List<String> checked = Collections.checkedList(new ArrayList<>(), String.class);
        checked.add("x");
        check(checked.get(0).equals("x"), "checkedList accepts correct type");
        @SuppressWarnings({"unchecked", "rawtypes"})
        List rawList = checked;
        expect(ClassCastException.class, () -> rawList.add(1), "checkedList rejects wrong type");
    }

    static void testUnmodifiable() {
        List<String> base = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> unmod = Collections.unmodifiableList(base);
        check(unmod.equals(Arrays.asList("a", "b")), "unmodifiableList content");
        expect(UnsupportedOperationException.class, () -> unmod.add("c"), "unmodifiableList.add");
        expect(UnsupportedOperationException.class, () -> unmod.remove(0), "unmodifiableList.remove");
        expect(UnsupportedOperationException.class, () -> unmod.set(0, "z"), "unmodifiableList.set");

        Set<String> uset = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("a", "b")));
        expect(UnsupportedOperationException.class, () -> uset.add("c"), "unmodifiableSet.add");
        expect(UnsupportedOperationException.class, () -> uset.remove("a"), "unmodifiableSet.remove");

        Map<String, Integer> m = new HashMap<>();
        m.put("k", 1);
        Map<String, Integer> umap = Collections.unmodifiableMap(m);
        check(umap.get("k") == 1, "unmodifiableMap get");
        expect(UnsupportedOperationException.class, () -> umap.put("x", 2), "unmodifiableMap.put");
    }

    static void testEmptySingleton() {
        check(Collections.emptyList().isEmpty(), "emptyList empty");
        check(Collections.emptySet().isEmpty(), "emptySet empty");
        check(Collections.emptyMap().isEmpty(), "emptyMap empty");
        expect(UnsupportedOperationException.class, () -> Collections.emptyList().add("x"),
              "emptyList.add UOE");
        expect(UnsupportedOperationException.class, () -> Collections.emptySet().add("x"),
              "emptySet.add UOE");

        check(!Collections.emptyIterator().hasNext(), "emptyIterator no next");
        check(!Collections.emptyListIterator().hasNext()
              && !Collections.emptyListIterator().hasPrevious(), "emptyListIterator");
        check(!Collections.emptyEnumeration().hasMoreElements(), "emptyEnumeration");

        List<String> sing = Collections.singletonList("x");
        check(sing.size() == 1 && sing.get(0).equals("x"), "singletonList");
        expect(UnsupportedOperationException.class, () -> sing.add("y"), "singletonList.add UOE");
        expect(UnsupportedOperationException.class, () -> sing.set(0, "y"), "singletonList.set UOE");
        expect(UnsupportedOperationException.class, () -> sing.remove("x"), "singletonList.remove UOE");

        Set<String> sset = Collections.singleton("x");
        check(sset.contains("x") && sset.size() == 1, "singleton set");
        expect(UnsupportedOperationException.class, () -> sset.add("y"), "singleton.add UOE");

        check(Collections.singletonMap("k", 1).get("k") == 1, "singletonMap");
        expect(UnsupportedOperationException.class,
              () -> Collections.singletonMap("k", 1).put("z", 2), "singletonMap.put UOE");
    }

    static void testEnumerationListNewSetAsLifo() {
        Enumeration<String> e = Collections.enumeration(Arrays.asList("a", "b"));
        List<String> l = Collections.list(e);
        check(l.equals(Arrays.asList("a", "b")), "enumeration/list round-trip");

        Set<String> s = Collections.newSetFromMap(new HashMap<>());
        check(s.add("a") && s.contains("a") && s.size() == 1, "newSetFromMap");

        Deque<Integer> deque = new ArrayDeque<>();
        Queue<Integer> lifo = Collections.asLifoQueue(deque);
        lifo.add(1);
        lifo.add(2);
        check(lifo.size() == 2, "asLifoQueue size");
        check(lifo.remove() == 2, "asLifoQueue remove returns last added");
        check(lifo.peek() == 1, "asLifoQueue peek returns next");
    }

    static void testSortSearchBoundaries() {
        // empty list
        List<Integer> empty = new ArrayList<>();
        Collections.sort(empty);
        check(empty.isEmpty(), "sort empty list no-op");
        check(Collections.binarySearch(empty, 1) == -1, "binarySearch empty -1");
        // single element
        List<Integer> single = new ArrayList<>(Arrays.asList(5));
        Collections.sort(single);
        check(Collections.binarySearch(single, 5) == 0, "binarySearch single found");
        check(Collections.binarySearch(single, 3) == -1, "binarySearch single miss");
        // already sorted is stable; reverse sorted is sorted
        List<Integer> sorted = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        List<Integer> sortedCopy = new ArrayList<>(sorted);
        Collections.sort(sortedCopy);
        check(sortedCopy.equals(sorted), "sort already sorted is stable");
        List<Integer> reversed = new ArrayList<>(Arrays.asList(4, 3, 2, 1));
        Collections.sort(reversed);
        check(reversed.equals(Arrays.asList(1, 2, 3, 4)), "sort reverse-sorted");
        // duplicates
        List<Integer> dups = new ArrayList<>(Arrays.asList(2, 1, 2, 1, 2));
        Collections.sort(dups);
        check(dups.equals(Arrays.asList(1, 1, 2, 2, 2)), "sort with duplicates");
        int dupIdx = Collections.binarySearch(dups, 2);
        check(dupIdx >= 0 && dups.get(dupIdx) == 2, "binarySearch duplicate returns a hit");
        // null element: natural sort throws NPE, nullsFirst sorts deterministically
        List<String> withNull = new ArrayList<>(Arrays.asList("b", null, "a"));
        expect(NullPointerException.class, () -> Collections.sort(withNull), "sort with null NPE");
        Collections.sort(withNull, Comparator.nullsFirst(Comparator.naturalOrder()));
        check(withNull.equals(Arrays.asList(null, "a", "b")), "sort nullsFirst");
        // min/max over single and duplicates
        check(Collections.min(single) == 5 && Collections.max(single) == 5, "min/max single");
        check(Collections.min(dups) == 1 && Collections.max(dups) == 2, "min/max duplicates");
        // reverse/rotate/swap/fill/copy boundary sizes
        List<Integer> one = new ArrayList<>(Arrays.asList(7));
        Collections.reverse(one);
        check(one.equals(Arrays.asList(7)), "reverse single no-op");
        Collections.rotate(one, 0);
        check(one.equals(Arrays.asList(7)), "rotate by 0 no-op");
        Collections.rotate(one, 1);
        check(one.equals(Arrays.asList(7)), "rotate single by 1 no-op");
        Collections.swap(one, 0, 0);
        check(one.equals(Arrays.asList(7)), "swap same index no-op");
        List<Integer> toFill = new ArrayList<>(Arrays.asList(1, 2));
        Collections.fill(toFill, 9);
        check(toFill.equals(Arrays.asList(9, 9)), "fill overwrites");
        // nCopies(0) / frequency / disjoint on empty
        check(Collections.nCopies(0, "x").isEmpty(), "nCopies(0) empty");
        check(Collections.frequency(empty, 1) == 0, "frequency on empty 0");
        check(Collections.disjoint(empty, Arrays.asList(1)), "disjoint with empty");
        // indexOfSubList / lastIndexOfSubList boundaries
        List<Integer> src = Arrays.asList(1, 2, 3, 2, 3, 4);
        check(Collections.indexOfSubList(src, src) == 0, "indexOfSubList source==target");
        check(Collections.lastIndexOfSubList(src, src) == 0, "lastIndexOfSubList source==target");
        check(Collections.indexOfSubList(src, Arrays.asList(5)) == -1, "indexOfSubList absent -1");
        check(Collections.indexOfSubList(src, Arrays.asList(1, 2, 3, 2, 3, 4, 5)) == -1,
              "indexOfSubList target longer -1");
    }

    static void testBlackBox() {
        // ---- checked* wrappers enforce runtime type checks ----
        Collection<String> cc = Collections.checkedCollection(new ArrayList<>(), String.class);
        cc.add("x");
        check(cc.size() == 1 && cc.contains("x"), "checkedCollection accepts correct type");
        @SuppressWarnings({"unchecked", "rawtypes"})
        Collection rawCc = cc;
        expect(ClassCastException.class, () -> rawCc.add(1), "checkedCollection rejects wrong type");

        Set<String> cs = Collections.checkedSet(new HashSet<>(), String.class);
        cs.add("x");
        @SuppressWarnings({"unchecked", "rawtypes"})
        Set rawCs = cs;
        expect(ClassCastException.class, () -> rawCs.add(2), "checkedSet rejects wrong type");

        Queue<String> cq = Collections.checkedQueue(new ArrayDeque<>(), String.class);
        cq.offer("x");
        @SuppressWarnings({"unchecked", "rawtypes"})
        Queue rawCq = cq;
        expect(ClassCastException.class, () -> rawCq.offer(3), "checkedQueue rejects wrong type");

        SortedSet<String> css = Collections.checkedSortedSet(new TreeSet<>(), String.class);
        css.add("a");
        check(css.first().equals("a"), "checkedSortedSet accepts correct type");
        @SuppressWarnings({"unchecked", "rawtypes"})
        SortedSet rawCss = css;
        expect(ClassCastException.class, () -> rawCss.add(4), "checkedSortedSet rejects wrong type");

        NavigableSet<String> cns = Collections.checkedNavigableSet(new TreeSet<>(), String.class);
        cns.add("a");
        @SuppressWarnings({"unchecked", "rawtypes"})
        NavigableSet rawCns = cns;
        expect(ClassCastException.class, () -> rawCns.add(5), "checkedNavigableSet rejects wrong type");

        Map<String, Integer> cm = Collections.checkedMap(new HashMap<>(), String.class, Integer.class);
        cm.put("k", 1);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map rawCm = cm;
        expect(ClassCastException.class, () -> rawCm.put(6, 1), "checkedMap rejects wrong key type");
        expect(ClassCastException.class, () -> rawCm.put("k", "v"), "checkedMap rejects wrong value type");

        SortedMap<String, Integer> csm = Collections.checkedSortedMap(new TreeMap<>(), String.class, Integer.class);
        csm.put("a", 1);
        check(csm.get("a") == 1, "checkedSortedMap accepts correct type");
        @SuppressWarnings({"unchecked", "rawtypes"})
        SortedMap rawCsm = csm;
        expect(ClassCastException.class, () -> rawCsm.put("b", "v"), "checkedSortedMap rejects wrong value type");

        NavigableMap<String, Integer> cnm = Collections.checkedNavigableMap(new TreeMap<>(), String.class, Integer.class);
        cnm.put("a", 1);
        @SuppressWarnings({"unchecked", "rawtypes"})
        NavigableMap rawCnm = cnm;
        expect(ClassCastException.class, () -> rawCnm.put(7, 1), "checkedNavigableMap rejects wrong key type");

        // ---- empty sorted/navigable wrappers: empty + unmodifiable ----
        SortedMap<Object, Object> esm = Collections.emptySortedMap();
        check(esm.isEmpty() && esm.size() == 0, "emptySortedMap empty");
        check(esm.comparator() == null, "emptySortedMap natural comparator null");
        expect(UnsupportedOperationException.class, () -> esm.put("k", 1), "emptySortedMap.put UOE");

        NavigableMap<Object, Object> enm = Collections.emptyNavigableMap();
        check(enm.isEmpty() && enm.firstEntry() == null && enm.lastEntry() == null,
              "emptyNavigableMap empty first/last null");
        expect(UnsupportedOperationException.class, () -> enm.put("k", 1), "emptyNavigableMap.put UOE");

        SortedSet<Object> ess = Collections.emptySortedSet();
        check(ess.isEmpty() && ess.size() == 0, "emptySortedSet empty");
        expect(UnsupportedOperationException.class, () -> ess.add("x"), "emptySortedSet.add UOE");

        NavigableSet<Object> ens = Collections.emptyNavigableSet();
        check(ens.isEmpty() && ens.size() == 0, "emptyNavigableSet empty");
        check(ens.ceiling("a") == null && ens.floor("a") == null,
              "emptyNavigableSet ceiling/floor null");
        expect(NoSuchElementException.class, ens::first, "emptyNavigableSet.first NSEE");
        expect(UnsupportedOperationException.class, () -> ens.add("x"), "emptyNavigableSet.add UOE");

        // ---- newSequencedSetFromMap: SequencedSet view over a SequencedMap ----
        SequencedMap<String, Boolean> backing = new LinkedHashMap<>();
        SequencedSet<String> seqSet = Collections.newSequencedSetFromMap(backing);
        check(seqSet.add("a") && seqSet.add("b"), "newSequencedSetFromMap add");
        check(!seqSet.add("a"), "newSequencedSetFromMap duplicate rejected");
        check(seqSet.size() == 2 && seqSet.contains("a"), "newSequencedSetFromMap content");
        check(seqSet.remove("a") && !seqSet.contains("a"), "newSequencedSetFromMap remove");
        seqSet.addFirst("z");
        seqSet.addLast("w");
        check(seqSet.getFirst().equals("z") && seqSet.getLast().equals("w"),
              "newSequencedSetFromMap sequenced order");

        // ---- synchronized* wrappers delegate read/write (mutable) ----
        Collection<Integer> sc = Collections.synchronizedCollection(new ArrayList<>());
        sc.add(1);
        check(sc.contains(1) && sc.size() == 1, "synchronizedCollection delegates");

        Set<Integer> ssync = Collections.synchronizedSet(new HashSet<>());
        ssync.add(1);
        check(ssync.contains(1), "synchronizedSet delegates");

        Map<String, Integer> smsync = Collections.synchronizedMap(new HashMap<>());
        smsync.put("k", 1);
        check(smsync.get("k") == 1, "synchronizedMap delegates");

        SortedMap<String, Integer> ssmsync = Collections.synchronizedSortedMap(new TreeMap<>());
        ssmsync.put("b", 2);
        ssmsync.put("a", 1);
        check(ssmsync.firstKey().equals("a") && ssmsync.get("b") == 2, "synchronizedSortedMap delegates");

        NavigableMap<String, Integer> snmsync = Collections.synchronizedNavigableMap(new TreeMap<>());
        snmsync.put("a", 1);
        check(snmsync.firstKey().equals("a"), "synchronizedNavigableMap delegates");

        SortedSet<Integer> sssync = Collections.synchronizedSortedSet(new TreeSet<>());
        sssync.add(2);
        sssync.add(1);
        check(sssync.first() == 1, "synchronizedSortedSet delegates");

        NavigableSet<Integer> snssync = Collections.synchronizedNavigableSet(new TreeSet<>());
        snssync.add(5);
        check(snssync.first() == 5, "synchronizedNavigableSet delegates");

        // ---- unmodifiable* wrappers: read works, write throws UOE ----
        Collection<Integer> uc = Collections.unmodifiableCollection(new ArrayList<>(Arrays.asList(1, 2)));
        check(uc.size() == 2 && uc.contains(1), "unmodifiableCollection read");
        expect(UnsupportedOperationException.class, () -> uc.add(3), "unmodifiableCollection.add UOE");
        expect(UnsupportedOperationException.class, uc::clear, "unmodifiableCollection.clear UOE");

        Set<Integer> us = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2)));
        expect(UnsupportedOperationException.class, () -> us.add(3), "unmodifiableSet.add UOE");

        SortedMap<String, Integer> treeBase = new TreeMap<>();
        treeBase.put("a", 1);
        SortedMap<String, Integer> usm = Collections.unmodifiableSortedMap(treeBase);
        check(usm.get("a") == 1 && usm.firstKey().equals("a"), "unmodifiableSortedMap read");
        expect(UnsupportedOperationException.class, () -> usm.put("b", 2), "unmodifiableSortedMap.put UOE");

        NavigableMap<String, Integer> navBase = new TreeMap<>();
        navBase.put("a", 1);
        NavigableMap<String, Integer> unm = Collections.unmodifiableNavigableMap(navBase);
        check(unm.firstKey().equals("a"), "unmodifiableNavigableMap read");
        expect(UnsupportedOperationException.class, () -> unm.put("b", 2), "unmodifiableNavigableMap.put UOE");

        SortedSet<Integer> sortedBase = new TreeSet<>(Arrays.asList(1, 2));
        SortedSet<Integer> uss = Collections.unmodifiableSortedSet(sortedBase);
        check(uss.first() == 1, "unmodifiableSortedSet read");
        expect(UnsupportedOperationException.class, () -> uss.add(3), "unmodifiableSortedSet.add UOE");

        NavigableSet<Integer> navSetBase = new TreeSet<>(Arrays.asList(1, 2));
        NavigableSet<Integer> uns = Collections.unmodifiableNavigableSet(navSetBase);
        check(uns.first() == 1, "unmodifiableNavigableSet read");
        expect(UnsupportedOperationException.class, () -> uns.add(3), "unmodifiableNavigableSet.add UOE");

        SequencedCollection<Integer> seqCollBase = new ArrayList<>(Arrays.asList(1, 2, 3));
        SequencedCollection<Integer> usqc = Collections.unmodifiableSequencedCollection(seqCollBase);
        check(usqc.getFirst() == 1 && usqc.getLast() == 3, "unmodifiableSequencedCollection read");
        expect(UnsupportedOperationException.class, () -> usqc.add(4), "unmodifiableSequencedCollection.add UOE");

        SequencedMap<String, Integer> seqMapBase = new LinkedHashMap<>();
        seqMapBase.put("a", 1);
        SequencedMap<String, Integer> usqm = Collections.unmodifiableSequencedMap(seqMapBase);
        check(usqm.firstEntry().getKey().equals("a"), "unmodifiableSequencedMap read");
        expect(UnsupportedOperationException.class, () -> usqm.put("b", 2), "unmodifiableSequencedMap.put UOE");

        SequencedSet<Integer> seqSetBase = new LinkedHashSet<>(Arrays.asList(1, 2));
        SequencedSet<Integer> usqs = Collections.unmodifiableSequencedSet(seqSetBase);
        check(usqs.getFirst() == 1, "unmodifiableSequencedSet read");
        expect(UnsupportedOperationException.class, () -> usqs.add(3), "unmodifiableSequencedSet.add UOE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSortBinarySearch();
        testReverseShuffleRotateSwapFillCopyReplace();
        testMinMaxFrequency();
        testAddAllSubListReverseOrder();
        testChecked();
        testUnmodifiable();
        testEmptySingleton();
        testEnumerationListNewSetAsLifo();
        testSortSearchBoundaries();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
