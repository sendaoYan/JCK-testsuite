/*
 * @test
 * @summary java.util.ArrayDeque conformance. White-box: Class.forName + superclass/
 *          interface/member shape. Black-box: Queue (offer/poll/peek vs add/remove/element
 *          empty semantics), Deque (first/last add/offer/peek/poll/get/remove), push/pop
 *          LIFO, removeFirstOccurrence/removeLastOccurrence, null rejection on every mutating/
 *          querying entry point, iterator/descendingIterator order, fail-fast, toArray order,
 *          and growth across the ring boundary.
 * @run main ArrayDequeTest
 *
 * 设计案：
 * 1. 契约：ArrayDeque 是基于环形数组的 Deque，禁止 null；既可作 FIFO 队列也可作 LIFO 栈；
 *    无容量限制（自动扩容）；peek/poll 族在空时返回 null，get/remove/element/remove 族抛 NSEE。
 * 2. 用户场景：new ArrayDeque → addLast/addFirst 两端入队 → peekFirst/peekLast 双端查看 →
 *    pollFirst/pollLast 双端消费 → push/pop 当栈 → removeFirstOccurrence 定向删除 →
 *    iterator/descendingIterator 双向遍历。
 * 3. 组合流程：见各 test*，均为「构造 → 多步组合 → 断言」。
 * 4. 不变量：迭代顺序 == 从头到尾的队列顺序；descendingIterator 顺序与之相反；
 *    addFirst/addLast 后 peekFirst 为最新头、peekLast 为最新尾。
 * 5. 边界/异常：任何 add/offer/push/addFirst/addLast 传 null 抛 NullPointerException；
 *    contains(null)/remove(null) 返回 false；空时 remove/element/getFirst/
 *    getLast/removeFirst/removeLast/pop 抛 NoSuchElementException。
 */

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDequeTest {
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
        Class<?> c = Class.forName("java.util.ArrayDeque");
        check(!c.isInterface(), "ArrayDeque is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "ArrayDeque public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ArrayDeque concrete");
        check(c.getSuperclass() == java.util.AbstractCollection.class,
              "extends AbstractCollection");
        check(java.util.Deque.class.isAssignableFrom(c), "implements Deque");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        c.getMethod("addFirst", Object.class);
        c.getMethod("removeFirst");
        c.getMethod("offerLast", Object.class);
        c.getMethod("descendingIterator");
        check(true, "key public methods present");
    }

    static void testQueue() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        check(d.isEmpty(), "empty initially");
        check(d.offer(1) && d.offer(2) && d.offer(3), "offer returns true");
        check(d.peek() == 1, "peek head");
        check(d.poll() == 1, "poll removes head");
        check(d.peek() == 2, "peek after poll");
        check(d.remove() == 2, "remove() returns head");
        check(d.element() == 3, "element() returns head without removing");
        check(d.size() == 1, "element non-destructive");

        d.poll();
        check(d.poll() == null && d.peek() == null, "empty poll/peek return null");
        expect(NoSuchElementException.class, d::remove, "remove empty throws NSEE");
        expect(NoSuchElementException.class, d::element, "element empty throws NSEE");
    }

    static void testDeque() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addFirst(1);
        d.addLast(2);
        d.addFirst(0);
        check(d.peekFirst() == 0 && d.peekLast() == 2, "addFirst/addLast + peek");
        check(d.size() == 3, "size");

        check(d.offerFirst(-1) && d.offerLast(3), "offerFirst/offerLast return true");
        check(d.getFirst() == -1 && d.getLast() == 3, "getFirst/getLast");
        check(d.removeFirst() == -1 && d.removeLast() == 3, "removeFirst/removeLast");
        check(d.pollFirst() == 0 && d.pollLast() == 2, "pollFirst/pollLast");
        check(d.pollFirst() == 1 && d.isEmpty(), "drained");

        ArrayDeque<Integer> empty = new ArrayDeque<>();
        check(empty.peekFirst() == null && empty.pollLast() == null, "empty poll/peek null");
        expect(NoSuchElementException.class, empty::getFirst, "getFirst empty NSEE");
        expect(NoSuchElementException.class, empty::removeLast, "removeLast empty NSEE");
    }

    static void testPushPop() {
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        check(stack.peek().equals("c"), "push puts on head");
        check(stack.pop().equals("c") && stack.pop().equals("b"), "pop LIFO");
        check(stack.pop().equals("a"), "pop last");
        expect(NoSuchElementException.class, stack::pop, "pop empty NSEE");
    }

    static void testRemoveOccurrences() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addAll(Arrays.asList(1, 2, 1, 3, 1));
        check(d.removeFirstOccurrence(1), "removeFirstOccurrence returns true");
        check(Arrays.equals(d.toArray(), new Object[]{2, 1, 3, 1}),
              "removeFirstOccurrence removes first");
        check(d.removeLastOccurrence(1), "removeLastOccurrence returns true");
        check(Arrays.equals(d.toArray(), new Object[]{2, 1, 3}),
              "removeLastOccurrence removes last");
        check(!d.removeFirstOccurrence(99), "removeFirstOccurrence absent false");
        check(!d.removeLastOccurrence(99), "removeLastOccurrence absent false");
    }

    static void testNullRejection() {
        ArrayDeque<String> d = new ArrayDeque<>();
        expect(NullPointerException.class, () -> d.add(null), "add(null)");
        expect(NullPointerException.class, () -> d.offer(null), "offer(null)");
        expect(NullPointerException.class, () -> d.addFirst(null), "addFirst(null)");
        expect(NullPointerException.class, () -> d.addLast(null), "addLast(null)");
        expect(NullPointerException.class, () -> d.offerFirst(null), "offerFirst(null)");
        expect(NullPointerException.class, () -> d.offerLast(null), "offerLast(null)");
        expect(NullPointerException.class, () -> d.push(null), "push(null)");

        d.add("a");
        check(!d.contains(null), "contains(null) returns false");
        check(!d.remove(null), "remove(null) returns false");
    }

    static void testIterators() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addFirst(2);
        d.addFirst(1);
        d.addLast(3);
        check(Arrays.equals(d.toArray(), new Object[]{1, 2, 3}), "toArray order");

        Iterator<Integer> it = d.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3 && !it.hasNext(),
              "iterator head-to-tail");

        Iterator<Integer> desc = d.descendingIterator();
        check(desc.next() == 3 && desc.next() == 2 && desc.next() == 1 && !desc.hasNext(),
              "descendingIterator tail-to-head");
    }

    static void testGrowth() {
        ArrayDeque<Integer> d = new ArrayDeque<>(8);
        for (int i = 0; i < 100; i++) {
            d.addLast(i);
        }
        check(d.size() == 100, "size after growth");
        check(d.peekFirst() == 0 && d.peekLast() == 99, "growth preserves order");
        for (int i = 0; i < 100; i++) {
            check(d.pollFirst() == i, "poll order after growth");
        }
        check(d.isEmpty(), "drained after growth");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testQueue();
        testDeque();
        testPushPop();
        testRemoveOccurrences();
        testNullRejection();
        testIterators();
        testGrowth();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
