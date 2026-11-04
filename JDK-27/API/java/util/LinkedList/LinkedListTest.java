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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    // ---- contract deep-dive (constructor matrix / listIterator contract /
    //      listIterator fail-fast / subList view / cross-impl equals /
    //      serialization / state invariants) ----------------------------------

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
    static <E> LinkedList<E> roundTrip(LinkedList<E> list) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(list); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (LinkedList<E>) ois.readObject();
        }
    }

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new LinkedList<String>((Collection<String>) null),
                    "LinkedList(null Collection) NPE");
        LinkedList<String> copy = new LinkedList<>(Arrays.asList("a", "b"));
        check(copy.equals(Arrays.asList("a", "b")), "LinkedList(Collection) copies");
        check(new LinkedList<>().isEmpty(), "LinkedList() empty");
    }

    static void testListIteratorContract() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> li = list.listIterator(1);
        check(li.hasPrevious() && li.previous().equals("a"), "listIterator previous");
        check(li.nextIndex() == 0, "nextIndex after previous");
        check(li.next().equals("a") && li.next().equals("b"), "listIterator forward");
        check(li.previousIndex() == 1, "previousIndex");
        checkThrows(IllegalStateException.class, () -> { ListIterator<String> li2 = new LinkedList<>(Arrays.asList("x")).listIterator(); li2.set("y"); },
                    "set before next ISE");
        checkThrows(IllegalStateException.class, () -> { ListIterator<String> li2 = new LinkedList<>(Arrays.asList("x")).listIterator(); li2.remove(); },
                    "remove before next ISE");
    }

    static void testListIteratorFailFast() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> li = list.listIterator();
        li.next();
        list.remove(0);
        checkThrows(ConcurrentModificationException.class, li::next, "listIterator CME after remove");
    }

    static void testSubListView() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = list.subList(1, 4);
        check(sub.equals(Arrays.asList(2, 3, 4)), "subList view content");
        sub.set(0, 20);
        check(list.get(1) == 20, "subList set reflected in backing list");
        list.set(2, 30);
        check(sub.get(1) == 30, "backing list change reflected in subList");
        sub.clear();
        check(list.equals(Arrays.asList(1, 5)), "subList.clear removes range");
        checkThrows(IndexOutOfBoundsException.class, () -> list.subList(-1, 2), "subList(-1,2) IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> list.subList(1, 3), "subList beyond size IOOBE");
    }

    static void testCrossImplEquals() {
        LinkedList<Integer> ll = new LinkedList<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(ll.equals(al) && al.equals(ll), "LinkedList equals ArrayList symmetric");
        check(ll.hashCode() == al.hashCode(), "equal lists equal hashCode");
        al.add(4);
        check(!ll.equals(al), "unequal after adding element");
    }

    static void testSerialization() throws Exception {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", null, "b"));
        LinkedList<String> r = roundTrip(list);
        check(r != list, "round-trip distinct object");
        check(r.equals(list), "round-trip content equal");
        check(r.get(1) == null, "round-trip preserves null element");
        r.add("c");
        check(r.size() == 4 && list.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(LinkedList.class);
        check(osc.getSerialVersionUID() == 876323262645176354L, "LinkedList serialVersionUID stable");
    }

    static void testStateInvariants() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5));
        list.remove(2);
        check(list.equals(Arrays.asList(1, 2, 4, 5)), "remove(int) shifts and updates");
        checkThrows(IndexOutOfBoundsException.class, () -> list.get(99), "failed get leaves state intact");
        check(list.size() == 4 && list.getFirst() == 1 && list.getLast() == 5, "state intact after failed op");
        list.clear();
        check(list.isEmpty() && list.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!list.contains(i), "clear removed " + i);
    }

    // ---- deepened II: hashCode 公式 / Deque 12 方法矩阵 / 别名等价 /
    //      ListIterator 状态机 / descendingIterator / subList CME / 中插 addAll --

    static void testHashCodeFormula() {
        check(new LinkedList<String>().hashCode() == 1, "hashCode of empty list is 1");
        check(new LinkedList<>(Arrays.asList("a")).hashCode() == 128, "hashCode [a] = 128");
        check(new LinkedList<>(Arrays.asList("a", "b")).hashCode() == 4066, "hashCode [a,b] = 4066");
        check(new LinkedList<>(Arrays.asList("a", "b", null)).hashCode() == 126046,
              "hashCode [a,b,null] = 126046");
        check(new LinkedList<>(Arrays.asList((String) null)).hashCode() == 31, "hashCode [null] = 31");
        check(new LinkedList<>(Arrays.asList("x", "y")).hashCode()
              == new java.util.ArrayList<>(Arrays.asList("x", "y")).hashCode(),
              "hashCode matches ArrayList for equal content");
        check(new LinkedList<>(Arrays.asList("a", "b")).hashCode() == java.util.List.of("a", "b").hashCode(),
              "hashCode matches List.of for equal content");
    }

    static void testDequeTwelveMethodMatrix() {
        LinkedList<String> e = new LinkedList<>();
        check(e.peekFirst() == null && e.peekLast() == null, "empty peekFirst/peekLast null");
        check(e.pollFirst() == null && e.pollLast() == null, "empty pollFirst/pollLast null");
        expect(NoSuchElementException.class, e::getFirst, "empty getFirst NSEE");
        expect(NoSuchElementException.class, e::getLast, "empty getLast NSEE");
        expect(NoSuchElementException.class, e::removeFirst, "empty removeFirst NSEE");
        expect(NoSuchElementException.class, e::removeLast, "empty removeLast NSEE");
        LinkedList<String> d = new LinkedList<>();
        d.addFirst("m");
        d.addFirst("f");
        d.addLast("l");
        check(d.peekFirst().equals("f") && d.peekLast().equals("l"), "peekFirst/peekLast non-empty");
        check(d.getFirst().equals("f") && d.getLast().equals("l"), "getFirst/getLast non-empty");
        check(d.removeFirst().equals("f"), "removeFirst takes front");
        check(d.getLast().equals("l"), "removeFirst does not touch back");
        check(d.removeLast().equals("l"), "removeLast takes back");
        check(d.equals(Arrays.asList("m")), "only middle remains");
        check(d.offerFirst("F") && d.offerLast("L"), "offerFirst/offerLast return true");
        check(d.equals(Arrays.asList("F", "m", "L")), "offerFirst/offerLast place ends");
        check(d.peek().equals("F"), "Queue peek is head (=first)");
        check(d.poll().equals("F"), "Queue poll is head (=first)");
    }

    static void testQueueDequeAliases() {
        LinkedList<String> a = new LinkedList<>(Arrays.asList("1", "2", "3"));
        LinkedList<String> b = new LinkedList<>(Arrays.asList("1", "2", "3"));
        check(a.add("4"), "add returns true");
        b.addLast("4");
        check(a.equals(b), "add(e) equivalent to addLast(e)");
        check(a.remove().equals(b.removeFirst()) && a.equals(b), "remove() equivalent to removeFirst()");
        check(a.element().equals(b.getFirst()), "element() equivalent to getFirst()");
        check(a.peek().equals(b.peekFirst()), "peek() equivalent to peekFirst()");
        check(a.poll().equals(b.pollFirst()) && a.equals(b), "poll() equivalent to pollFirst()");
        a.push("p");
        b.addFirst("p");
        check(a.equals(b), "push(e) equivalent to addFirst(e)");
        check(a.pop().equals(b.removeFirst()) && a.equals(b), "pop() equivalent to removeFirst()");
    }

    static void testListIteratorStateMachine() {
        LinkedList<String> l = new LinkedList<>(Arrays.asList("a", "b"));
        ListIterator<String> li = l.listIterator();
        li.next();
        li.add("X");
        expect(IllegalStateException.class, () -> li.set("Y"), "set after add ISE");
        expect(IllegalStateException.class, () -> li.remove(), "remove after add ISE");
        li.previous(); // "X"
        li.set("Z");
        check(l.equals(Arrays.asList("a", "Z", "b")), "set after previous replaces element");
        ListIterator<String> li2 = l.listIterator();
        li2.next();
        li2.next(); // "Z"
        li2.previous();
        li2.remove();
        check(l.equals(Arrays.asList("a", "b")), "remove after previous");
        ListIterator<String> li3 = l.listIterator();
        check(li3.nextIndex() == 0 && li3.previousIndex() == -1, "fresh iterator indices");
        li3.next();
        check(li3.nextIndex() == 1 && li3.previousIndex() == 0, "indices after next");
        li3.previous();
        check(li3.nextIndex() == 0 && li3.previousIndex() == -1, "indices back at start");
        li3.add("w");
        check(li3.nextIndex() == 1 && li3.previousIndex() == 0, "add shifts cursor: nextIndex+1");
        check(l.equals(Arrays.asList("w", "a", "b")), "add at cursor 0 inserts at front");
        StringBuilder sb = new StringBuilder();
        l.listIterator(1).forEachRemaining(sb::append);
        check(sb.toString().equals("ab"), "listIterator forEachRemaining from index 1");
    }

    static void testDescendingIteratorContract() {
        java.util.Iterator<String> d = new LinkedList<>(Arrays.asList("a", "b", "c")).descendingIterator();
        StringBuilder sb = new StringBuilder();
        d.forEachRemaining(sb::append);
        check(sb.toString().equals("cba"), "descendingIterator order via forEachRemaining");
        LinkedList<String> l = new LinkedList<>(Arrays.asList("a", "b", "c"));
        java.util.Iterator<String> d2 = l.descendingIterator();
        d2.next(); // c
        d2.remove();
        check(l.equals(Arrays.asList("a", "b")), "descendingIterator.remove removes visited tail");
        expect(IllegalStateException.class, d2::remove, "descending remove twice ISE");
        expect(NoSuchElementException.class,
                new LinkedList<String>().descendingIterator()::next, "empty descendingIterator NSEE");
    }

    static void testSubListContract() {
        LinkedList<Integer> parent = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = parent.subList(1, 4);
        List<Integer> seg = new java.util.ArrayList<>(Arrays.asList(2, 3, 4));
        check(sub.equals(seg) && sub.hashCode() == seg.hashCode(), "subList equals/hashCode on own content");
        sub.add(0, 99);
        check(parent.equals(Arrays.asList(1, 99, 2, 3, 4, 5)), "subList.add(0,e) inserts in parent");
        sub.remove(sub.size() - 1);
        check(parent.equals(Arrays.asList(1, 99, 2, 3, 5)), "subList.remove(index) shrinks parent");
        LinkedList<Integer> p2 = new LinkedList<>(Arrays.asList(1, 2, 3));
        List<Integer> s2 = p2.subList(0, 2);
        p2.removeFirst();
        expect(java.util.ConcurrentModificationException.class, () -> s2.get(0),
               "subList.get after parent removeFirst CME");
        expect(java.util.ConcurrentModificationException.class, () -> s2.iterator().next(),
               "subList iterator after parent mod CME");
        LinkedList<Integer> mid = new LinkedList<>(Arrays.asList(1, 5));
        mid.addAll(1, Arrays.asList(2, 3, 4));
        check(mid.equals(Arrays.asList(1, 2, 3, 4, 5)), "addAll(index,c) inserts in middle");
        mid.addAll(0, Arrays.asList(0));
        check(mid.get(0) == 0 && mid.size() == 6, "addAll at head");
        mid.addAll(mid.size(), Arrays.asList(6));
        check(mid.getLast() == 6, "addAll at tail");
    }

    static void testIteratorRemoveOnlyLegalModification() {
        LinkedList<Integer> l = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        java.util.Iterator<Integer> it = l.iterator();
        int kept = 0;
        while (it.hasNext()) {
            int v = it.next();
            if (v % 3 != 0) it.remove(); else kept++;
        }
        check(kept == 2 && l.equals(Arrays.asList(3, 6)), "filter via iterator.remove keeps multiples of 3");
        check(l.getFirst() == 3 && l.getLast() == 6, "deque ends after filtering");
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
        testConstructorMatrix();
        testListIteratorContract();
        testListIteratorFailFast();
        testSubListView();
        testCrossImplEquals();
        testSerialization();
        testStateInvariants();
        testHashCodeFormula();
        testDequeTwelveMethodMatrix();
        testQueueDequeAliases();
        testListIteratorStateMachine();
        testDescendingIteratorContract();
        testSubListContract();
        testIteratorRemoveOnlyLegalModification();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
