/*
 * @test
 * @summary java.util.TreeSet 一致性：白盒（反射结构）+ 黑盒（自然序/Comparator/NavigableSet）。
 * @run main TreeSetTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractSet、实现 NavigableSet/Cloneable/Serializable、
 *   first/last/ceiling/floor/higher/lower/范围视图/descendingSet 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：TreeSet 是有序 NavigableSet——自然序或 Comparator 序；默认拒绝 null 元素（NPE）；
 *    提供 first/last/ceiling/floor/higher/lower 与 subSet/headSet/tailSet。
 * 2. 场景：new TreeSet() / new TreeSet(Comparator.reverseOrder()) -> add 乱序 -> 首尾查询 ->
 *    ceiling/floor/higher/lower -> 范围视图 -> 视图 add/remove 联动 -> descendingSet。
 * 3. 组合链路：插入 1..10 -> 上下界查询 -> subSet/headSet/tailSet -> 视图写入联动 ->
 *    descendingSet 倒序 -> pollFirst/pollLast。
 * 4. 不变量：迭代有序；范围视图与 set 联动；comparator() 反映构造。
 * 5. 边界/异常：add(null) 抛 NPE；subSet(from,to) from>to 抛 IAE；视图 add 越界抛 IAE。
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class TreeSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.TreeSet");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractSet"), "extends AbstractSet");
        check(NavigableSet.class.isAssignableFrom(c), "implements NavigableSet");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "comparator"), "comparator");
        check(hasMethod(c, "first") && hasMethod(c, "last"), "first/last");
        check(hasMethod(c, "ceiling", Object.class) && hasMethod(c, "floor", Object.class), "ceiling/floor");
        check(hasMethod(c, "higher", Object.class) && hasMethod(c, "lower", Object.class), "higher/lower");
        check(hasMethod(c, "subSet", Object.class, Object.class), "subSet");
        check(hasMethod(c, "headSet", Object.class) && hasMethod(c, "tailSet", Object.class), "headSet/tailSet");
        check(hasMethod(c, "descendingSet") && hasMethod(c, "descendingIterator"), "descendingSet/Iterator");
        check(hasMethod(c, "pollFirst") && hasMethod(c, "pollLast"), "pollFirst/pollLast");
    }

    static void testNaturalOrder() {
        TreeSet<String> set = new TreeSet<>();
        check(set.comparator() == null, "natural comparator null");
        check(set.add("c") && set.add("a") && set.add("b"), "add returns true");
        check(!set.add("a"), "add duplicate returns false");
        check(set.first().equals("a") && set.last().equals("c"), "first/last");
        check(new ArrayList<>(set).equals(Arrays.asList("a", "b", "c")), "natural iteration order");
        check(set.contains("b") && !set.contains("z"), "contains");
        check(set.remove("b") && !set.contains("b"), "remove");
        check(set.size() == 2, "size");
    }

    static void testComparatorOrder() {
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(1);
        set.add(3);
        set.add(2);
        check(set.first() == 3 && set.last() == 1, "reverse first/last");
        check(set.comparator() == Comparator.<Integer>reverseOrder(), "comparator() reflects ctor");
        check(new ArrayList<>(set).equals(Arrays.asList(3, 2, 1)), "reverse iteration order");
    }

    static void testNavigable() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i += 2) {
            set.add(i);
        }
        check(set.lower(5) == 3, "lower");
        check(set.floor(5) == 5 && set.floor(4) == 3, "floor");
        check(set.ceiling(5) == 5 && set.ceiling(4) == 5, "ceiling");
        check(set.higher(5) == 7, "higher");
        check(set.higher(9) == null && set.lower(1) == null, "out-of-range returns null");
        check(set.pollFirst() == 1 && set.pollLast() == 9, "pollFirst/pollLast");
        check(set.first() == 3 && set.last() == 7, "first/last after polls");
        check(set.size() == 3, "size after polls");
    }

    static void testRangeViewsAndDescending() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i++) {
            set.add(i);
        }
        check(set.subSet(3, 7).size() == 4, "subSet from inclusive, to exclusive");
        check(set.subSet(3, 7).first() == 3 && set.subSet(3, 7).last() == 6, "subSet endpoints");
        check(set.subSet(3, true, 7, true).size() == 5, "subSet inclusive both");
        check(set.headSet(4).size() == 3, "headSet exclusive");
        check(set.headSet(4, true).size() == 4, "headSet inclusive");
        check(set.tailSet(8).size() == 3, "tailSet inclusive");
        check(set.tailSet(8, false).size() == 2, "tailSet exclusive");

        // 视图与 set 联动（subSet(E,E) 返回 SortedSet 视图）
        java.util.SortedSet<Integer> sub = set.subSet(3, 7);
        check(sub.remove(4) && !set.contains(4), "subSet remove reflects in set");
        check(sub.add(4) && set.contains(4), "subSet add (new) reflects in set");
        check(!sub.add(4), "subSet add existing returns false");
        expectThrows(() -> sub.add(100), IllegalArgumentException.class,
                     "subSet add outside range throws IAE");

        // descendingSet / descendingIterator
        check(set.descendingSet().first() == 10 && set.descendingSet().last() == 1,
              "descendingSet first/last");
        java.util.Iterator<Integer> dit = set.descendingIterator();
        int prev = dit.next();
        while (dit.hasNext()) {
            int cur = dit.next();
            check(prev > cur, "descendingIterator decreasing");
            prev = cur;
        }
        check(new ArrayList<>(set.reversed()).equals(new ArrayList<>(set.descendingSet())),
              "reversed() equals descendingSet order");

        expectThrows(() -> set.subSet(7, 3), IllegalArgumentException.class,
                     "subSet from>to throws IAE");
    }

    static void testNullRejection() {
        TreeSet<String> natural = new TreeSet<>();
        expectThrows(() -> natural.add(null), NullPointerException.class,
                     "natural-order add null");
        TreeSet<Integer> cmp = new TreeSet<>(Comparator.naturalOrder());
        expectThrows(() -> cmp.add(null), NullPointerException.class,
                     "comparator-order add null");
    }

    static void testBlackBox() {
        // addAll union
        TreeSet<Integer> set = new TreeSet<>(Arrays.asList(1, 2, 3));
        check(set.addAll(Arrays.asList(3, 4, 5)), "addAll returns true");
        check(set.size() == 5 && set.first() == 1 && set.last() == 5, "addAll union");
        check(!set.addAll(Arrays.asList(1, 2)), "addAll no-op returns false");

        // isEmpty / clear / clone
        check(!set.isEmpty(), "isEmpty non-empty");
        @SuppressWarnings("unchecked")
        TreeSet<Integer> cloned = (TreeSet<Integer>) set.clone();
        check(cloned != set && cloned.equals(set), "clone independent and equal");
        cloned.add(6);
        check(set.size() == 5, "clone structural independence");
        TreeSet<Integer> toClear = new TreeSet<>(set);
        toClear.clear();
        check(toClear.isEmpty() && toClear.size() == 0, "clear empties");

        // ctor from SortedSet preserves comparator
        java.util.SortedSet<Integer> rev = new TreeSet<>(Comparator.reverseOrder());
        rev.add(1);
        rev.add(2);
        rev.add(3);
        TreeSet<Integer> fromSorted = new TreeSet<>(rev);
        check(fromSorted.first() == 3 && fromSorted.last() == 1, "ctor (SortedSet) preserves comparator");

        // iterator iterates in sorted order
        TreeSet<Integer> iter = new TreeSet<>(Arrays.asList(3, 1, 2));
        java.util.Iterator<Integer> it = iter.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3 && !it.hasNext(),
              "iterator sorted order");

        // spliterator reports ORDERED|DISTINCT and iterates in sorted order
        java.util.Spliterator<Integer> sp = iter.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED | java.util.Spliterator.DISTINCT),
              "spliterator characteristics");
        java.util.List<Integer> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(1, 2, 3)), "spliterator sorted order");

        // addFirst / addLast always throw UnsupportedOperationException (sorted set)
        expectThrows(() -> set.addFirst(99), UnsupportedOperationException.class, "addFirst UOE");
        expectThrows(() -> set.addLast(99), UnsupportedOperationException.class, "addLast UOE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrder();
        testComparatorOrder();
        testNavigable();
        testRangeViewsAndDescending();
        testNullRejection();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
