/*
 * @test
 * @summary java.util.LinkedList conformance. White-box: Class.forName + superclass/
 *          interface/member shape. Black-box: full List contract chain (addAll/get/set/
 *          remove/indexOf/lastIndexOf/contains), Queue (offer/poll/peek vs add/remove/
 *          element empty semantics), Deque (first/last accessors + offer/poll/peek variants +
 *          removeFirstOccurrence/removeLastOccurrence), push/pop LIFO, null tolerance,
 *          descendingIterator, listIterator, and iterator fail-fast.
 * @run main LinkedListTest
 *
 * 设计案：
 * 1. 契约：LinkedList 是双向链表实现的 List+Deque；支持索引访问（O(n)）、双端操作、
 *    允许 null 元素；既可按队列（FIFO）也按栈（LIFO）使用。
 * 2. 用户场景：new LinkedList → addFirst/addLast/offer 建表 → get/set/remove 改表 →
 *    indexOf/lastIndexOf/contains 查询 → 队列 poll/peek 消费 → push/pop 当栈 →
 *    descendingIterator 反向遍历 → removeFirstOccurrence 定向删元素。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：插入顺序 == 迭代顺序；peek/poll 作用于队头、peekFirst/pollFirst 同队头、
 *    peekLast/pollLast 作用于队尾；removeFirstOccurrence 删首个、removeLastOccurrence 删末个。
 * 5. 边界/异常：空队列 poll/peek 返回 null、remove/element 抛 NoSuchElementException；
 *    空表 removeFirst/removeLast/getFirst/getLast/pop 抛 NoSuchElementException；
 *    结构修改后 iterator/listIterator/descendingIterator 抛 ConcurrentModificationException。
 */

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LinkedListTest {
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
        Class<?> c = Class.forName("java.util.LinkedList");
        check(!c.isInterface(), "LinkedList is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "LinkedList public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "LinkedList concrete");
        check(c.getSuperclass() == java.util.AbstractSequentialList.class,
              "extends AbstractSequentialList");
        check(java.util.List.class.isAssignableFrom(c), "implements List");
        check(java.util.Deque.class.isAssignableFrom(c), "implements Deque");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        c.getMethod("addFirst", Object.class);
        c.getMethod("removeFirst");
        c.getMethod("descendingIterator");
        c.getMethod("offer", Object.class);
        check(true, "key public methods present");
    }

    static void testContractChain() {
        LinkedList<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.addAll(Arrays.asList(3, 4));
        check(list.equals(Arrays.asList(1, 2, 3, 4)), "add/addAll order");

        list.add(1, 15);
        check(list.equals(Arrays.asList(1, 15, 2, 3, 4)), "add at index");
        check(list.get(1) == 15 && list.set(1, 20) == 15, "get/set by index");
        check(list.remove(1) == 20, "remove(int) returns element");
        check(list.indexOf(3) == 2 && list.lastIndexOf(3) == 2, "indexOf/lastIndexOf");
        check(list.contains(4) && !list.contains(99), "contains");

        list.removeAll(Arrays.asList(1, 4));
        check(list.equals(Arrays.asList(2, 3)), "removeAll");
        list.addAll(Arrays.asList(2, 5));
        check(list.retainAll(Arrays.asList(2, 3)), "retainAll returns true");
        check(list.equals(Arrays.asList(2, 3, 2)), "retainAll keeps matching");
    }

    static void testQueueSemantics() {
        LinkedList<String> q = new LinkedList<>();
        check(q.offer("x") && q.offer("y"), "offer returns true");
        check(q.peek().equals("x"), "peek returns head without removing");
        check(q.size() == 2, "peek non-destructive");
        check(q.poll().equals("x") && q.poll().equals("y"), "poll removes head");
        check(q.poll() == null && q.peek() == null, "empty poll/peek return null");

        LinkedList<String> empty = new LinkedList<>();
        expect(NoSuchElementException.class, empty::remove, "remove empty throws NSEE");
        expect(NoSuchElementException.class, empty::element, "element empty throws NSEE");
        check(empty.add("a"), "add returns true");
        check(empty.element().equals("a") && empty.remove().equals("a"), "element/remove head");
    }

    static void testDeque() {
        LinkedList<String> list = new LinkedList<>();
        list.addFirst("b");
        list.addFirst("a");
        list.addLast("c");
        check(list.equals(Arrays.asList("a", "b", "c")), "addFirst/addLast order");
        check(list.getFirst().equals("a") && list.getLast().equals("c"), "getFirst/getLast");
        check(list.removeFirst().equals("a") && list.removeLast().equals("c"), "removeFirst/Last");
        check(list.equals(Arrays.asList("b")), "after removeFirst/Last");

        check(list.offerFirst("0") && list.offerLast("9"), "offerFirst/offerLast return true");
        check(list.equals(Arrays.asList("0", "b", "9")), "offerFirst/offerLast order");
        check(list.peekFirst().equals("0") && list.peekLast().equals("9"), "peekFirst/peekLast");
        check(list.pollFirst().equals("0") && list.pollLast().equals("9"), "pollFirst/pollLast");
        check(list.equals(Arrays.asList("b")), "after pollFirst/pollLast");

        LinkedList<String> empty = new LinkedList<>();
        expect(NoSuchElementException.class, empty::getFirst, "getFirst empty");
        expect(NoSuchElementException.class, empty::removeLast, "removeLast empty");
        check(empty.pollFirst() == null && empty.peekLast() == null, "empty poll/peek null");
    }

    static void testRemoveOccurrences() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 1, 3, 1));
        check(list.removeFirstOccurrence(1), "removeFirstOccurrence returns true");
        check(list.equals(Arrays.asList(2, 1, 3, 1)), "removeFirstOccurrence removes first");
        check(list.removeLastOccurrence(1), "removeLastOccurrence returns true");
        check(list.equals(Arrays.asList(2, 1, 3)), "removeLastOccurrence removes last");
        check(!list.removeFirstOccurrence(99), "removeFirstOccurrence absent false");
    }

    static void testNullTolerance() {
        LinkedList<String> list = new LinkedList<>();
        check(list.add(null) && list.offer(null), "add/offer null accepted");
        list.addFirst("a");
        check(list.contains(null) && list.indexOf(null) == 1, "contains/indexOf null");
        check(list.remove(null), "remove null returns true");
    }

    static void testStackPushPop() {
        LinkedList<String> stack = new LinkedList<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        check(stack.pop().equals("c") && stack.pop().equals("b"), "pop LIFO");
        check(stack.peek().equals("a"), "peek after pops");
        expect(NoSuchElementException.class, () -> { stack.pop(); stack.pop(); },
              "pop empty throws NSEE");
    }

    static void testDescendingListIteratorFailFast() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));

        Iterator<String> desc = list.descendingIterator();
        check(desc.next().equals("c") && desc.next().equals("b") && desc.next().equals("a"),
              "descendingIterator order");
        check(!desc.hasNext(), "descendingIterator exhausted");

        ListIterator<String> li = list.listIterator();
        li.add("X");
        check(list.equals(Arrays.asList("X", "a", "b", "c")), "listIterator add at start");
        check(li.next().equals("a"), "listIterator next after add");
        li.set("A");
        check(list.equals(Arrays.asList("X", "A", "b", "c")), "listIterator set");
        check(li.next().equals("b"), "listIterator forward next");
        li.remove();
        check(list.equals(Arrays.asList("X", "A", "c")), "listIterator remove");

        ListIterator<String> pi = list.listIterator(list.size());
        check(pi.previous().equals("c") && pi.previous().equals("A"),
              "listIterator previous traversal");

        Iterator<String> it = list.iterator();
        it.next();
        list.add("d");
        expect(ConcurrentModificationException.class, it::next, "iterator fail-fast");
        Iterator<String> d2 = list.descendingIterator();
        list.add("e");
        expect(ConcurrentModificationException.class, d2::next, "descendingIterator fail-fast");
    }

    static void testBlackBox() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3));

        // toArray() and toArray(T[])
        check(Arrays.equals(list.toArray(), new Object[]{1, 2, 3}), "toArray()");
        check(Arrays.equals(list.toArray(new Integer[0]), new Integer[]{1, 2, 3}), "toArray(T[])");
        Integer[] padded = list.toArray(new Integer[5]);
        check(padded.length == 5 && padded[0] == 1 && padded[3] == null, "toArray(T[]) pads null");

        // reversed() is a reversed-ordered, live view
        java.util.Deque<Integer> rev = list.reversed();
        check(new java.util.ArrayList<>(rev).equals(Arrays.asList(3, 2, 1)), "reversed() order");
        check(rev.getFirst() == 3 && rev.getLast() == 1, "reversed() Deque first/last");
        rev.addFirst(9);
        check(list.getLast() == 9, "reversed() view reflects to backing list");

        // spliterator reports ORDERED|SIZED|SUBSIZED and iterates in order
        java.util.Spliterator<Integer> sp = list.spliterator();
        check(sp.estimateSize() == 4, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED | java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED), "spliterator characteristics");
        java.util.List<Integer> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(1, 2, 3, 9)), "spliterator order");

        // clone independent + equal
        @SuppressWarnings("unchecked")
        LinkedList<Integer> cloned = (LinkedList<Integer>) list.clone();
        check(cloned != list && cloned.equals(list), "clone independent and equal");
        cloned.add(100);
        check(list.size() == 4 && !list.contains(100), "clone structural independence");

        // clear empties
        list.clear();
        check(list.isEmpty() && list.size() == 0, "clear empties");

        // ---- boundary/equivalence additions (M2) ----
        LinkedList<Integer> e0 = new LinkedList<>();
        check(e0.isEmpty() && e0.size() == 0, "empty size/isEmpty");
        check(e0.peek() == null && e0.poll() == null, "empty queue peek/poll null");
        check(e0.pollFirst() == null && e0.peekLast() == null, "empty deque poll/peek null");
        expect(NoSuchElementException.class, e0::getFirst, "empty getFirst throws NSEE");

        LinkedList<Integer> l2 = new LinkedList<>(Arrays.asList(1, 2, 3));
        expect(IndexOutOfBoundsException.class, () -> l2.get(l2.size()), "get(size) throws IOOBE");
        expect(IndexOutOfBoundsException.class, () -> l2.get(-1), "get(-1) throws IOOBE");
        expect(IndexOutOfBoundsException.class, () -> l2.add(l2.size() + 1, 9), "add at size+1 throws IOOBE");
        l2.add(0, 0);
        check(l2.getFirst() == 0 && l2.size() == 4, "add at index 0");
        l2.add(l2.size(), 9);
        check(l2.getLast() == 9, "add at index==size appends");
        check(l2.subList(0, 0).isEmpty(), "subList(0,0) empty view");

        LinkedList<String> single = new LinkedList<>();
        single.add("only");
        check(single.getFirst().equals("only") && single.getLast().equals("only"),
              "single element first==last");
        check(single.removeFirst().equals("only") && single.isEmpty(), "drain single element");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testContractChain();
        testQueueSemantics();
        testDeque();
        testRemoveOccurrences();
        testNullTolerance();
        testStackPushPop();
        testDescendingListIteratorFailFast();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
