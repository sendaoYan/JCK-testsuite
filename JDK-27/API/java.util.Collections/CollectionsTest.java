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
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
