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

    // ---- deepened contract coverage II ---------------------------------------

    static void testHashCodeFormula() {
        check(new LinkedList<String>().hashCode() == 1, "hashCode of empty list is 1");
        check(new LinkedList<>(Arrays.asList("a")).hashCode() == 128, "hashCode [a] = 31*1+97 = 128");
        check(new LinkedList<>(Arrays.asList("a", "b")).hashCode() == 4066,
              "hashCode [a,b] = 31*128+98 = 4066");
        check(new LinkedList<>(Arrays.asList("a", "b", null)).hashCode() == 126046,
              "hashCode [a,b,null] = 31*4066+0 = 126046");
        check(new LinkedList<>(Arrays.asList((String) null)).hashCode() == 31, "hashCode [null] = 31");
        // identical to ArrayList's hashCode for equal content
        check(new LinkedList<>(Arrays.asList("x", "y")).hashCode()
              == new ArrayList<>(Arrays.asList("x", "y")).hashCode(), "hashCode matches ArrayList");
    }

    static void testDequeTwelveMethodMatrix() {
        // empty state: peek/poll forms return null, get/remove forms throw NSEE
        LinkedList<String> e = new LinkedList<>();
        check(e.peekFirst() == null && e.peekLast() == null, "empty peekFirst/peekLast null");
        check(e.pollFirst() == null && e.pollLast() == null, "empty pollFirst/pollLast null");
        checkThrows(NoSuchElementException.class, e::getFirst, "empty getFirst NSEE");
        checkThrows(NoSuchElementException.class, e::getLast, "empty getLast NSEE");
        checkThrows(NoSuchElementException.class, e::removeFirst, "empty removeFirst NSEE");
        checkThrows(NoSuchElementException.class, e::removeLast, "empty removeLast NSEE");
        // non-empty state: every method pair operates on its own end
        LinkedList<String> d = new LinkedList<>();
        // rebuild a known state
        d.clear();
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
        // Queue/Deque method equivalence on identical states
        LinkedList<String> a = new LinkedList<>(Arrays.asList("1", "2", "3"));
        LinkedList<String> b = new LinkedList<>(Arrays.asList("1", "2", "3"));
        check(a.add("4") && b.size() == 3, "add returns true before alias check");
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
        // add() clears the last-returned marker: set/remove right after add -> ISE
        LinkedList<String> l = new LinkedList<>(Arrays.asList("a", "b"));
        ListIterator<String> li = l.listIterator();
        li.next();
        li.add("X");
        checkThrows(IllegalStateException.class, () -> li.set("Y"), "set after add ISE");
        checkThrows(IllegalStateException.class, () -> li.remove(), "remove after add ISE");
        // after previous(), set replaces what previous() returned
        li.previous(); // "X"
        li.set("Z");
        check(l.equals(Arrays.asList("a", "Z", "b")), "set after previous replaces element");
        // remove after previous removes that element
        ListIterator<String> li2 = l.listIterator();
        li2.next();
        li2.next(); // "Z"
        li2.previous(); // "Z"
        li2.remove();
        check(l.equals(Arrays.asList("a", "b")), "remove after previous");
        // nextIndex/previousIndex full matrix
        ListIterator<String> li3 = l.listIterator();
        check(li3.nextIndex() == 0 && li3.previousIndex() == -1, "fresh iterator indices");
        li3.next();
        check(li3.nextIndex() == 1 && li3.previousIndex() == 0, "indices after next");
        li3.previous();
        check(li3.nextIndex() == 0 && li3.previousIndex() == -1, "indices back at start");
        li3.add("w");
        check(li3.nextIndex() == 1 && li3.previousIndex() == 0, "add shifts cursor: nextIndex+1");
        check(l.equals(Arrays.asList("w", "a", "b")), "add at cursor 0 inserts at front");
        // forEachRemaining from the middle
        StringBuilder sb = new StringBuilder();
        l.listIterator(1).forEachRemaining(sb::append);
        check(sb.toString().equals("ab"), "listIterator forEachRemaining from index 1");
    }

    static void testDescendingIteratorContract() {
        LinkedList<String> l = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> d = l.descendingIterator();
        d.next(); // c
        d.remove();
        check(l.equals(Arrays.asList("a", "b")), "descendingIterator.remove removes visited tail");
        checkThrows(IllegalStateException.class, d::remove, "descending remove twice ISE");
        StringBuilder sb = new StringBuilder();
        l.descendingIterator().forEachRemaining(sb::append);
        check(sb.toString().equals("ba"), "descendingIterator forEachRemaining order");
        checkThrows(NoSuchElementException.class,
                    new LinkedList<String>().descendingIterator()::next,
                    "empty descendingIterator next NSEE");
    }

    static void testSubListContract() {
        LinkedList<Integer> parent = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> sub = parent.subList(1, 4);
        List<Integer> seg = new ArrayList<>(Arrays.asList(2, 3, 4));
        check(sub.equals(seg) && sub.hashCode() == seg.hashCode(), "subList equals/hashCode on own content");
        sub.add(0, 99);
        check(parent.equals(Arrays.asList(1, 99, 2, 3, 4, 5)), "subList.add(0,e) inserts in parent");
        sub.remove(sub.size() - 1);
        check(parent.equals(Arrays.asList(1, 99, 2, 3, 5)), "subList.remove(index) shrinks parent");
        // parent structural change invalidates the subList
        LinkedList<Integer> p2 = new LinkedList<>(Arrays.asList(1, 2, 3));
        List<Integer> s2 = p2.subList(0, 2);
        p2.removeFirst();
        checkThrows(java.util.ConcurrentModificationException.class, () -> s2.get(0),
                    "subList.get after parent removeFirst CME");
        checkThrows(java.util.ConcurrentModificationException.class, () -> s2.iterator().next(),
                    "subList iterator after parent mod CME");
        // addAll in the middle
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
        Iterator<Integer> it = l.iterator();
        int kept = 0;
        while (it.hasNext()) {
            int v = it.next();
            if (v % 3 != 0) it.remove(); else kept++;
        }
        check(kept == 2 && l.equals(Arrays.asList(3, 6)), "filter via iterator.remove keeps multiples of 3");
        check(l.getFirst() == 3 && l.getLast() == 6, "deque ends after filtering");
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
