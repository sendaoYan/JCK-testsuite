/*
 * @test
 * @summary java.util.LinkedList API conformance: full contract coverage.
 *          deque ops (addFirst/addLast/removeFirst/removeLast/getFirst/getLast/offer/
 *          poll/peek/push/pop), list ops (add/get/set/remove/indexOf/lastIndexOf),
 *          null tolerance, Queue empty semantics (poll/peek null vs remove/element NSEE),
 *          Deque offer/peek/poll variants, removeFirstOccurrence/removeLastOccurrence,
 *          descendingIterator, listIterator (add/set/remove/previous/nextIndex),
 *          iterator/listIterator fail-fast (CME/ISE/NSEE), toArray contract,
 *          subList live view, cross-implementation equals (vs ArrayList), clone
 *          (shallow + structural independence), serialization round-trip +
 *          serialVersionUID, and state invariants.
 * @run main LinkedListTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    // ---- original coverage (kept) ------------------------------------------

    static void testDeque() {
        LinkedList<String> list = new LinkedList<>();
        list.addFirst("b");
        list.addFirst("a");
        list.addLast("c");
        check(list.equals(Arrays.asList("a", "b", "c")), "addFirst/addLast order");
        check(list.getFirst().equals("a") && list.getLast().equals("c"), "getFirst/getLast");
        check(list.removeFirst().equals("a"), "removeFirst");
        check(list.removeLast().equals("c"), "removeLast");
        check(list.equals(Arrays.asList("b")), "after removeFirst/Last");

        LinkedList<String> q = new LinkedList<>();
        check(q.offer("x") && q.offer("y"), "offer returns true");
        check(q.peek().equals("x"), "peek returns head without removing");
        check(q.size() == 2, "peek non-destructive");
        check(q.poll().equals("x"), "poll removes head");
        check(q.poll().equals("y") && q.poll() == null, "poll empty returns null");

        LinkedList<String> stack = new LinkedList<>();
        stack.push("a");
        stack.push("b");
        check(stack.pop().equals("b"), "stack pop LIFO");
        check(stack.pop().equals("a"), "stack pop LIFO");
        checkThrows(NoSuchElementException.class, stack::pop, "pop empty throws NSEE");
    }

    static void testListOps() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3));
        check(list.size() == 3 && list.get(1) == 2, "get by index");
        check(list.set(1, 20) == 2, "set returns old value");
        list.add(1, 15);
        check(list.equals(Arrays.asList(1, 15, 20, 3)), "add at index");
        check(list.remove(1) == 15, "remove(int) returns element");
        check(list.indexOf(20) == 1 && list.lastIndexOf(20) == 1, "indexOf/lastIndexOf");
        check(list.contains(3) && !list.contains(99), "contains");
        list.clear();
        check(list.isEmpty(), "clear empties");
    }

    static void testIteratorOrder() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        check(it.next().equals("a") && it.next().equals("b") && it.next().equals("c"),
              "iterator order");
        check(!it.hasNext(), "iterator exhausted");
    }

    // ---- constructor matrix + null tolerance --------------------------------

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new LinkedList<String>((Collection<String>) null),
                    "LinkedList(null Collection) NPE");
        LinkedList<String> copy = new LinkedList<>(Arrays.asList("a", "b"));
        check(copy.equals(Arrays.asList("a", "b")), "LinkedList(Collection) copies");
        check(new LinkedList<>().isEmpty(), "LinkedList() empty");
    }

    static void testNullTolerance() {
        LinkedList<String> list = new LinkedList<>();
        check(list.add(null), "add null accepted");
        check(list.offer(null), "offer null accepted");
        list.addFirst(null);
        list.addLast("a");
        check(list.size() == 4, "size with nulls");
        check(list.contains(null) && list.indexOf(null) == 0, "contains/indexOf null");
        check(list.get(0) == null && list.peek() == null, "null element retrievable");
        check(list.remove(null), "remove null returns true");
        check(list.size() == 3 && list.indexOf(null) == 0, "one null removed");
        check(list.poll() == null, "poll null element returns null (ambiguous with empty)");
        check(list.size() == 2, "size after poll null element");
    }

    // ---- Queue / Deque empty vs exception semantics -------------------------

    static void testQueueDequeEmptySemantics() {
        LinkedList<String> empty = new LinkedList<>();
        check(empty.poll() == null && empty.peek() == null, "empty poll/peek null");
        checkThrows(NoSuchElementException.class, empty::remove, "remove empty NSEE");
        checkThrows(NoSuchElementException.class, empty::element, "element empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getFirst, "getFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::getLast, "getLast empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeFirst, "removeFirst empty NSEE");
        checkThrows(NoSuchElementException.class, empty::removeLast, "removeLast empty NSEE");
        check(empty.pollFirst() == null && empty.pollLast() == null, "empty pollFirst/pollLast null");
        check(empty.peekFirst() == null && empty.peekLast() == null, "empty peekFirst/peekLast null");

        LinkedList<String> q = new LinkedList<>();
        q.offer("a");
        check(q.element().equals("a") && q.remove().equals("a"), "element/remove on non-empty");
    }

    static void testDequeVariants() {
        LinkedList<String> list = new LinkedList<>();
        check(list.offerFirst("0") && list.offerLast("9"), "offerFirst/offerLast return true");
        check(list.peekFirst().equals("0") && list.peekLast().equals("9"), "peekFirst/peekLast");
        check(list.pollFirst().equals("0") && list.pollLast().equals("9"), "pollFirst/pollLast");
        check(list.isEmpty(), "empty after pollFirst/pollLast");

        LinkedList<Integer> occ = new LinkedList<>(Arrays.asList(1, 2, 1, 3, 1));
        check(occ.removeFirstOccurrence(1), "removeFirstOccurrence returns true");
        check(occ.equals(Arrays.asList(2, 1, 3, 1)), "removeFirstOccurrence removes first");
        check(occ.removeLastOccurrence(1), "removeLastOccurrence returns true");
        check(occ.equals(Arrays.asList(2, 1, 3)), "removeLastOccurrence removes last");
        check(!occ.removeFirstOccurrence(99) && !occ.removeLastOccurrence(99), "occurrence absent false");
    }

    // ---- descendingIterator / listIterator ----------------------------------

    static void testDescendingIterator() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> desc = list.descendingIterator();
        check(desc.next().equals("c") && desc.next().equals("b") && desc.next().equals("a"),
              "descendingIterator order");
        check(!desc.hasNext(), "descendingIterator exhausted");
    }

    static void testListIteratorContract() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> li = list.listIterator(1);
        check(li.hasPrevious() && li.previous().equals("a"), "listIterator previous");
        check(li.nextIndex() == 0, "nextIndex after previous");
        check(li.next().equals("a") && li.next().equals("b"), "listIterator forward");
        check(li.previousIndex() == 1, "previousIndex");
        li.add("X");
        check(list.equals(Arrays.asList("a", "b", "X", "c")), "listIterator add");
        check(li.previous().equals("X") && li.previous().equals("b"), "listIterator back");
        li.set("B");
        check(list.equals(Arrays.asList("a", "B", "X", "c")), "listIterator set");
        checkThrows(IllegalStateException.class, () -> { ListIterator<String> li2 = new LinkedList<>(Arrays.asList("x")).listIterator(); li2.set("y"); },
                    "set before next ISE");
        checkThrows(IllegalStateException.class, () -> { ListIterator<String> li2 = new LinkedList<>(Arrays.asList("x")).listIterator(); li2.remove(); },
                    "remove before next ISE");
    }

    // ---- fail-fast + toArray + subList --------------------------------------

    static void testFailFast() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        it.next();
        list.add("d");
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        ListIterator<String> li = list.listIterator();
        li.next();
        list.remove(0);
        checkThrows(java.util.ConcurrentModificationException.class, li::next, "listIterator CME after remove");

        Iterator<String> desc = list.descendingIterator();
        list.add("e");
        checkThrows(java.util.ConcurrentModificationException.class, desc::next, "descendingIterator CME after add");
    }

    static void testToArrayContract() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3));
        Object[] oa = list.toArray();
        check(Arrays.equals(oa, new Object[]{1, 2, 3}), "toArray() order");
        Integer[] exact = list.toArray(new Integer[0]);
        check(exact.length == 3 && Arrays.equals(exact, new Integer[]{1, 2, 3}), "toArray(new T[0]) exact");
        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = list.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        check(Arrays.equals(Arrays.copyOf(reusable, 3), new Integer[]{1, 2, 3}), "toArray(T[]) content");
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
        checkThrows(IndexOutOfBoundsException.class, () -> list.subList(1, 3), "subList(1,3) beyond size IOOBE");
    }

    // ---- cross-impl equals / clone / serialization / state invariants -------

    static void testCrossImplEquals() {
        LinkedList<Integer> ll = new LinkedList<>(Arrays.asList(1, 2, 3));
        ArrayList<Integer> al = new ArrayList<>(Arrays.asList(1, 2, 3));
        check(ll.equals(al) && al.equals(ll), "LinkedList equals ArrayList symmetric");
        check(ll.hashCode() == al.hashCode(), "equal lists equal hashCode");
        al.add(4);
        check(!ll.equals(al), "unequal after adding element");
    }

    static void testClone() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b"));
        @SuppressWarnings("unchecked")
        LinkedList<String> clone = (LinkedList<String>) list.clone();
        check(clone != list, "clone distinct object");
        check(clone.equals(list) && clone.hashCode() == list.hashCode(), "clone equals original");
        clone.add("c");
        check(list.size() == 2 && !list.contains("c"), "clone structural independence");
        check(clone.get(0) == list.get(0), "clone is shallow (same element refs)");
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

    public static void main(String[] args) throws Exception {
        testDeque();
        testListOps();
        testIteratorOrder();
        testConstructorMatrix();
        testNullTolerance();
        testQueueDequeEmptySemantics();
        testDequeVariants();
        testDescendingIterator();
        testListIteratorContract();
        testFailFast();
        testToArrayContract();
        testSubListView();
        testCrossImplEquals();
        testClone();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
