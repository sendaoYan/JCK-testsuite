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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
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

    static void testBlackBox() {
        ArrayDeque<Integer> d = new ArrayDeque<>();
        d.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // forEach iterates head-to-tail in encounter order
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        d.forEach(seen::add);
        check(seen.equals(Arrays.asList(1, 2, 3, 4, 5)), "forEach encounter order");

        // clone produces an independent, content-equal deque
        ArrayDeque<Integer> c = d.clone();
        check(c != d, "clone returns a distinct instance");
        check(Arrays.equals(c.toArray(), d.toArray()), "clone content equal");
        c.addLast(99);
        check(d.size() == 5 && c.size() == 6, "clone structural independence");

        // removeAll removes every matching element
        check(d.removeAll(Arrays.asList(2, 4)), "removeAll returns true");
        check(Arrays.equals(d.toArray(), new Object[]{1, 3, 5}), "removeAll removed matching");
        check(!d.removeAll(Arrays.asList(9, 10)), "removeAll absent returns false");

        // retainAll keeps only matching elements
        ArrayDeque<Integer> r = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        check(r.retainAll(Arrays.asList(1, 3, 5)), "retainAll returns true");
        check(Arrays.equals(r.toArray(), new Object[]{1, 3, 5}), "retainAll kept matching");
        check(!r.retainAll(Arrays.asList(1, 3, 5)), "retainAll no-op returns false");

        // removeIf removes matching predicate
        ArrayDeque<Integer> f = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(f.removeIf(x -> x % 2 == 0), "removeIf returns true");
        check(Arrays.equals(f.toArray(), new Object[]{1, 3, 5}), "removeIf removed evens");

        // spliterator reports ORDERED|NONNULL|SIZED|SUBSIZED and covers all in order
        java.util.Spliterator<Integer> sp = d.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(java.util.Spliterator.ORDERED
                | java.util.Spliterator.NONNULL
                | java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED),
              "spliterator characteristics");
        java.util.List<Integer> gathered = new java.util.ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(1, 3, 5)), "spliterator order");

        // clear empties the deque
        d.clear();
        check(d.isEmpty() && d.size() == 0, "clear empties");
        d.addLast(7);
        check(!d.isEmpty(), "reusable after clear");

        // ---- boundary/equivalence additions (M2) ----
        ArrayDeque<Integer> e0 = new ArrayDeque<>();
        check(e0.isEmpty() && e0.size() == 0, "empty size/isEmpty");
        check(e0.peek() == null && e0.poll() == null, "empty poll/peek null");
        expect(NoSuchElementException.class, e0::getFirst, "empty getFirst NSEE");
        check(!e0.removeFirstOccurrence(1) && !e0.removeLastOccurrence(1),
              "remove occurrences on empty returns false");

        ArrayDeque<Integer> cap0 = new ArrayDeque<>(0);
        cap0.addLast(1);
        check(cap0.size() == 1 && cap0.peekFirst() == 1, "ctor(0) usable");

        ArrayDeque<Integer> single = new ArrayDeque<>();
        single.addFirst(1);
        check(single.getFirst() == 1 && single.getLast() == 1, "single first==last");
        check(single.removeLast() == 1 && single.isEmpty(), "drain single");

        ArrayDeque<Integer> d2 = new ArrayDeque<>();
        d2.addLast(1);
        d2.addLast(2);
        d2.addLast(3);
        check(d2.peekFirst() == 1 && d2.peekLast() == 3, "peek both ends");
        d2.addFirst(0);
        check(d2.peekFirst() == 0 && d2.size() == 4, "addFirst grows front");

        ArrayDeque<Integer> fifo = new ArrayDeque<>();
        for (int i = 0; i < 5; i++) fifo.offerLast(i);
        for (int i = 0; i < 5; i++) check(fifo.pollFirst() == i, "FIFO drain " + i);
        check(fifo.isEmpty(), "FIFO drained");
    }

    // ---- contract deep-dive (constructor matrix / empty semantics / fail-fast /
    //      toArray contract / serialization / state invariants) ---------------

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

    @SuppressWarnings("unchecked")
    static <E> ArrayDeque<E> roundTrip(ArrayDeque<E> d) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(d); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ArrayDeque<E>) ois.readObject();
        }
    }

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new ArrayDeque<Integer>((Collection<Integer>) null),
                    "ArrayDeque(null Collection) NPE");
        checkThrows(NullPointerException.class, () -> new ArrayDeque<Integer>(Arrays.asList(1, null)),
                    "ArrayDeque(Collection with null) NPE");

        ArrayDeque<Integer> neg = new ArrayDeque<>(-1);
        neg.addLast(1);
        check(neg.size() == 1 && neg.peekFirst() == 1, "ArrayDeque(-1) usable (min capacity)");
        ArrayDeque<Integer> zero = new ArrayDeque<>(0);
        zero.addLast(1);
        check(zero.size() == 1 && zero.peekFirst() == 1, "ArrayDeque(0) usable");

        ArrayDeque<Integer> copy = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        check(copy.size() == 3 && copy.peekFirst() == 1 && copy.peekLast() == 3, "ArrayDeque(Collection) copies");
    }

    static void testEmptySemantics() {
        ArrayDeque<Integer> empty = new ArrayDeque<>();
        check(empty.poll() == null && empty.peek() == null, "empty poll/peek null");
        check(empty.pollFirst() == null && empty.pollLast() == null, "empty pollFirst/pollLast null");
        check(empty.peekFirst() == null && empty.peekLast() == null, "empty peekFirst/peekLast null");
        checkThrows(NoSuchElementException.class, empty::remove, "remove empty NSEE");
        checkThrows(NoSuchElementException.class, empty::element, "element empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getFirst, "getFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getLast, "getLast empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeFirst, "removeFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeLast, "removeLast empty NSEE");
        checkThrows(NoSuchElementException.class, empty::pop, "pop empty NSEE");
        check(!empty.removeFirstOccurrence(1) && !empty.removeLastOccurrence(1), "occurrence on empty returns false");
    }

    static void testToArrayContract() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        Object[] oa = d.toArray();
        check(Arrays.equals(oa, new Object[]{1, 2, 3}), "toArray() order");

        Integer[] exact = d.toArray(new Integer[0]);
        check(exact.length == 3 && Arrays.equals(exact, new Integer[]{1, 2, 3}), "toArray(new T[0]) exact");

        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = d.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        check(Arrays.equals(Arrays.copyOf(reusable, 3), new Integer[]{1, 2, 3}), "toArray(T[]) content");
    }

    static void testSerialization() throws Exception {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3));
        ArrayDeque<Integer> r = roundTrip(d);
        check(r != d, "round-trip distinct object");
        check(Arrays.equals(r.toArray(), new Object[]{1, 2, 3}), "round-trip order preserved");
        r.addLast(4);
        check(r.size() == 4 && d.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ArrayDeque.class);
        check(osc.getSerialVersionUID() == 2340985798034038923L, "ArrayDeque serialVersionUID stable");
    }

    static void testStateInvariants() {
        ArrayDeque<Integer> d = new ArrayDeque<>(Arrays.asList(1, 2, 3, 4, 5));
        d.removeFirstOccurrence(3);
        check(d.size() == 4 && !d.contains(3), "remove updates size");
        check(d.contains(1) && d.contains(2) && d.contains(4) && d.contains(5), "others survive remove");
        checkThrows(NullPointerException.class, () -> d.add(null), "failed add leaves state intact");
        check(d.size() == 4, "state intact after failed op");
        d.clear();
        check(d.isEmpty() && d.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!d.contains(i), "clear removed " + i);
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
        testBlackBox();
        testConstructorMatrix();
        testEmptySemantics();
        testToArrayContract();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
