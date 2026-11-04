/*
 * @test
 * @summary java.util.PriorityQueue conformance. White-box: Class.forName + superclass/
 *          interface/member shape. Black-box: natural/Comparator ordering, add/offer/peek/
 *          poll heap-extraction order, duplicates, null rejection, element/remove empty
 *          (NoSuchElementException), remove(Object)/contains/removeIf, comparator() accessor,
 *          toArray multiset, and construction from Collection/SortedSet.
 * @run main PriorityQueueTest
 *
 * 设计案：
 * 1. 契约：PriorityQueue 是无界优先队列，按自然序或 Comparator 出队；offer/add 永不失败；
 *    poll/peek 作用于最小（或 Comparator 最优先）元素；禁止 null；相等元素顺序不保证。
 * 2. 用户场景：new PriorityQueue(Comparator) → add/offer 建堆 → peek 看最小 → poll 依序取出 →
 *    remove/removeIf 定向删除 → contains 查询 → comparator() 查看比较器。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：poll 序列单调（按比较器）；size 与 poll 次数一致；重复元素全部出队；
 *    迭代顺序不保证有序（只保证能遍历全量）。
 * 5. 边界/异常：add/offer(null) 及含 null 集合构造抛 NullPointerException；
 *    空队列 element()/remove() 抛 NoSuchElementException、poll()/peek() 返回 null；
 *    comparator() 在自然序时返回 null。
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

public class PriorityQueueTest {
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
        Class<?> c = Class.forName("java.util.PriorityQueue");
        check(!c.isInterface(), "PriorityQueue is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "PriorityQueue public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "PriorityQueue concrete");
        check(c.getSuperclass() == java.util.AbstractQueue.class, "extends AbstractQueue");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        c.getMethod("add", Object.class);
        c.getMethod("offer", Object.class);
        c.getMethod("peek");
        c.getMethod("poll");
        c.getMethod("comparator");
        check(true, "key public methods present");
    }

    static void testNaturalOrder() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        check(q.isEmpty(), "empty initially");
        check(q.offer(5) && q.offer(2) && q.offer(4) && q.offer(1) && q.offer(3),
              "offer returns true");
        check(q.size() == 5, "size");
        check(q.peek() == 1, "peek smallest");
        check(q.contains(3) && !q.contains(99), "contains");
        int[] expected = {1, 2, 3, 4, 5};
        for (int i = 0; i < expected.length; i++) {
            check(q.poll() == expected[i], "poll order ascending " + expected[i]);
        }
        check(q.poll() == null && q.peek() == null, "empty poll/peek null");
        check(q.isEmpty(), "empty after polls");
    }

    static void testComparatorOrder() {
        Comparator<Integer> cmp = Comparator.reverseOrder();
        PriorityQueue<Integer> q = new PriorityQueue<>(cmp);
        check(q.comparator() == cmp, "comparator() returns supplied comparator");
        q.add(1);
        q.add(3);
        q.add(2);
        check(q.peek() == 3, "max-heap peek");
        check(q.poll() == 3 && q.poll() == 2 && q.poll() == 1, "max-heap poll order");

        PriorityQueue<Integer> natural = new PriorityQueue<>();
        check(natural.comparator() == null, "natural order comparator() is null");
    }

    static void testDuplicates() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.addAll(Arrays.asList(3, 1, 3, 2, 1));
        check(q.size() == 5, "size with duplicates");
        int[] expected = {1, 1, 2, 3, 3};
        for (int i = 0; i < expected.length; i++) {
            check(q.poll() == expected[i], "duplicate poll order " + expected[i]);
        }
        check(q.isEmpty(), "duplicates drained");
    }

    static void testNullRejection() {
        PriorityQueue<String> q = new PriorityQueue<>();
        expect(NullPointerException.class, () -> q.add(null), "add(null)");
        expect(NullPointerException.class, () -> q.offer(null), "offer(null)");
        expect(NullPointerException.class,
              () -> new PriorityQueue<>(Arrays.asList("a", null, "b")),
              "ctor with null element");
    }

    static void testElementRemoveEmpty() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        expect(NoSuchElementException.class, q::element, "element empty NSEE");
        expect(NoSuchElementException.class, q::remove, "remove() empty NSEE");
        check(q.poll() == null && q.peek() == null, "empty poll/peek null");
    }

    static void testRemoveContainsRemoveIf() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(q.remove(2), "remove(Object) returns true");
        check(q.size() == 5 && !q.contains(2), "removed absent");
        check(q.removeIf(x -> x % 2 == 0), "removeIf returns true");
        int[] expected = {1, 3, 5};
        for (int i = 0; i < expected.length; i++) {
            check(q.poll() == expected[i], "removeIf leftover poll " + expected[i]);
        }
        check(q.isEmpty(), "removeIf drained");
    }

    static void testToArray() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        Object[] arr = q.toArray();
        check(arr.length == 3, "toArray length");
        int[] sorted = Arrays.stream(arr).mapToInt(o -> (Integer) o).sorted().toArray();
        check(Arrays.equals(sorted, new int[]{1, 2, 3}), "toArray multiset (sorted)");

        Integer[] tarr = q.toArray(new Integer[0]);
        check(tarr.length == 3, "toArray(T[]) length");
        int[] tsorted = Arrays.stream(tarr).mapToInt(Integer::intValue).sorted().toArray();
        check(Arrays.equals(tsorted, new int[]{1, 2, 3}), "toArray(T[]) multiset (sorted)");
    }

    static void testConstructorFromCollectionAndSortedSet() {
        SortedSet<Integer> ss = new TreeSet<>(Arrays.asList(3, 1, 2));
        PriorityQueue<Integer> fromSs = new PriorityQueue<>(ss);
        check(fromSs.poll() == 1 && fromSs.poll() == 2 && fromSs.poll() == 3,
              "ctor from SortedSet preserves order");

        PriorityQueue<Integer> fromCol = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        check(fromCol.peek() == 1, "ctor from Collection heapifies");
    }

    static void testBlackBox() {
        PriorityQueue<Integer> sized = new PriorityQueue<>(8);
        sized.add(1);
        check(sized.peek() == 1, "ctor (int) works");
        PriorityQueue<Integer> cmp = new PriorityQueue<>(8, Comparator.reverseOrder());
        cmp.add(1);
        cmp.add(2);
        check(cmp.peek() == 2, "ctor (int, Comparator) works");

        // copy constructor from another PriorityQueue preserves the multiset
        PriorityQueue<Integer> src = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        PriorityQueue<Integer> copy = new PriorityQueue<>(src);
        int[] sorted = {1, 2, 3};
        for (int i = 0; i < 3; i++) check(copy.poll() == sorted[i], "ctor (PriorityQueue) poll order");

        // iterator covers all elements (order unspecified), forEach as well
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(5, 1, 4, 2, 3));
        int count = 0;
        for (java.util.Iterator<Integer> it = q.iterator(); it.hasNext(); it.next()) count++;
        check(count == 5, "iterator covers all elements");
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        q.forEach(seen::add);
        java.util.Collections.sort(seen);
        check(seen.equals(Arrays.asList(1, 2, 3, 4, 5)), "forEach visits all (sorted)");

        // removeAll / retainAll operate by element equality
        PriorityQueue<Integer> r = new PriorityQueue<>(Arrays.asList(1, 2, 3, 4, 5));
        check(r.removeAll(Arrays.asList(2, 4)), "removeAll returns true");
        int[] afterRemove = {1, 3, 5};
        for (int i = 0; i < 3; i++) check(r.poll() == afterRemove[i], "removeAll leftover");
        PriorityQueue<Integer> t = new PriorityQueue<>(Arrays.asList(1, 2, 3, 4, 5));
        check(t.retainAll(Arrays.asList(1, 3, 5)), "retainAll returns true");
        int[] afterRetain = {1, 3, 5};
        for (int i = 0; i < 3; i++) check(t.poll() == afterRetain[i], "retainAll kept");

        // spliterator reports SIZED|SUBSIZED|NONNULL and covers all
        PriorityQueue<Integer> sp = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        java.util.Spliterator<Integer> spl = sp.spliterator();
        check(spl.estimateSize() == 3, "spliterator estimateSize");
        check(spl.hasCharacteristics(java.util.Spliterator.SIZED | java.util.Spliterator.SUBSIZED
                | java.util.Spliterator.NONNULL), "spliterator characteristics");
        java.util.List<Integer> splGathered = new java.util.ArrayList<>();
        spl.forEachRemaining(splGathered::add);
        java.util.Collections.sort(splGathered);
        check(splGathered.equals(Arrays.asList(1, 2, 3)), "spliterator covers all (sorted)");

        // clear empties
        q.clear();
        check(q.isEmpty() && q.size() == 0, "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testNaturalOrder();
        testComparatorOrder();
        testDuplicates();
        testNullRejection();
        testElementRemoveEmpty();
        testRemoveContainsRemoveIf();
        testToArray();
        testConstructorFromCollectionAndSortedSet();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
