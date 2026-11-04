/*
 * @test
 * @summary java.util.PriorityQueue API conformance: full contract coverage.
 *          natural/comparator ordering, add/offer/peek/poll heap extraction order,
 *          duplicates, null rejection on add/offer/collection ctor, contains(null)/
 *          remove(null) return false, empty element/remove NSEE vs poll/peek null,
 *          constructor matrix (negative capacity/NaN-free, all six ctors), toArray
 *          contract (reuse/null terminator/multiset), iterator fail-fast, identity
 *          equals semantics (AbstractQueue), comparator accessor, serialization
 *          round-trip + serialVersionUID, and state invariants.
 * @run main PriorityQueueTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
    static <E> PriorityQueue<E> roundTrip(PriorityQueue<E> q) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(q); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (PriorityQueue<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testNaturalOrder() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        check(q.isEmpty(), "empty");
        check(q.offer(5) && q.offer(2) && q.offer(4) && q.offer(1) && q.offer(3),
              "offer returns true");
        check(q.size() == 5, "size");
        check(q.peek() == 1, "peek smallest");
        check(q.contains(3) && !q.contains(99), "contains");
        check(q.poll() == 1, "poll smallest");
        check(q.poll() == 2, "poll next");
        check(q.poll() == 3, "poll next");
        check(q.poll() == 4, "poll next");
        check(q.poll() == 5, "poll last");
        check(q.poll() == null, "poll empty null");
        check(q.isEmpty(), "empty after polls");
    }

    static void testComparator() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.reverseOrder());
        q.add(1);
        q.add(3);
        q.add(2);
        check(q.peek() == 3, "max-heap peek");
        check(q.poll() == 3 && q.poll() == 2 && q.poll() == 1, "max-heap poll order");
    }

    static void testRemoveClear() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.remove(2), "remove(element)");
        check(q.size() == 2 && !q.contains(2), "removed absent");
        q.clear();
        check(q.isEmpty(), "clear");
    }

    // ---- constructor matrix + null semantics --------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new PriorityQueue<Integer>(-1), "PriorityQueue(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new PriorityQueue<Integer>(-1, Comparator.naturalOrder()),
                    "PriorityQueue(-1,cmp) IAE");
        checkThrows(NullPointerException.class, () -> new PriorityQueue<Integer>((Collection<Integer>) null),
                    "PriorityQueue(null Collection) NPE");
        checkThrows(NullPointerException.class, () -> new PriorityQueue<Integer>((SortedSet<Integer>) null),
                    "PriorityQueue(null SortedSet) NPE");
        checkThrows(NullPointerException.class, () -> new PriorityQueue<Integer>(Arrays.asList(1, null, 2)),
                    "PriorityQueue(Collection with null) NPE");

        checkThrows(IllegalArgumentException.class, () -> new PriorityQueue<Integer>(0), "PriorityQueue(0) IAE");
        PriorityQueue<Integer> sized = new PriorityQueue<>(8);
        sized.add(1);
        check(sized.peek() == 1, "PriorityQueue(int) usable");

        SortedSet<Integer> ss = new TreeSet<>(Arrays.asList(3, 1, 2));
        PriorityQueue<Integer> fromSs = new PriorityQueue<>(ss);
        check(fromSs.poll() == 1 && fromSs.poll() == 2 && fromSs.poll() == 3, "PriorityQueue(SortedSet) order");

        PriorityQueue<Integer> src = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        PriorityQueue<Integer> copy = new PriorityQueue<>(src);
        check(copy.poll() == 1 && copy.poll() == 2 && copy.poll() == 3, "PriorityQueue(PriorityQueue) multiset");

        check(new PriorityQueue<Integer>(Comparator.reverseOrder()).comparator() == Comparator.<Integer>reverseOrder(),
              "PriorityQueue(Comparator) keeps comparator");
        check(new PriorityQueue<Integer>().comparator() == null, "PriorityQueue() natural comparator null");
    }

    static void testNullSemantics() {
        PriorityQueue<String> q = new PriorityQueue<>();
        checkThrows(NullPointerException.class, () -> q.add(null), "add(null) NPE");
        checkThrows(NullPointerException.class, () -> q.offer(null), "offer(null) NPE");
        q.add("a");
        check(!q.contains(null), "contains(null) returns false (not NPE)");
        check(!q.remove(null), "remove(null) returns false (not NPE)");
    }

    // ---- empty semantics + duplicates ---------------------------------------

    static void testEmptySemantics() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        checkThrows(NoSuchElementException.class, q::element, "element empty NSEE");
        checkThrows(NoSuchElementException.class, q::remove, "remove() empty NSEE");
        check(q.poll() == null && q.peek() == null, "empty poll/peek null");
    }

    static void testDuplicates() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.addAll(Arrays.asList(3, 1, 3, 2, 1));
        check(q.size() == 5, "size with duplicates");
        int[] expected = {1, 1, 2, 3, 3};
        for (int i = 0; i < expected.length; i++) check(q.poll() == expected[i], "duplicate poll order " + expected[i]);
        check(q.isEmpty(), "duplicates drained");
    }

    // ---- toArray contract + fail-fast + equals identity ---------------------

    static void testToArrayContract() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        Object[] oa = q.toArray();
        check(oa.length == 3, "toArray() length");
        List<Integer> sorted = new ArrayList<>();
        for (Object o : oa) sorted.add((Integer) o);
        Collections.sort(sorted);
        check(sorted.equals(Arrays.asList(1, 2, 3)), "toArray() multiset");

        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = q.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        List<Integer> sortedReuse = new ArrayList<>(Arrays.asList(reusable[0], reusable[1], reusable[2]));
        Collections.sort(sortedReuse);
        check(sortedReuse.equals(Arrays.asList(1, 2, 3)), "toArray(T[]) multiset");
    }

    static void testFailFast() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = q.iterator();
        it.next();
        q.add(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        Iterator<Integer> it2 = q.iterator();
        it2.next();
        q.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "iterator CME after remove");
    }

    static void testIdentityEquals() {
        // AbstractQueue does not override equals: PriorityQueue equality is identity.
        PriorityQueue<Integer> a = new PriorityQueue<>(Arrays.asList(1, 2, 3));
        PriorityQueue<Integer> b = new PriorityQueue<>(Arrays.asList(1, 2, 3));
        check(a != b, "two distinct instances");
        check(!a.equals(b), "PriorityQueue does not override equals (identity semantics)");
        check(a.equals(a), "a equals itself");
    }

    // ---- serialization + state invariants -----------------------------------

    static void testSerialization() throws Exception {
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(3, 1, 2));
        PriorityQueue<Integer> r = roundTrip(q);
        check(r != q, "round-trip distinct object");
        check(r.size() == 3, "round-trip size preserved");
        check(r.poll() == 1 && r.poll() == 2 && r.poll() == 3, "round-trip heap order preserved");
        check(r.isEmpty(), "round-trip result drained");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(PriorityQueue.class);
        check(osc.getSerialVersionUID() == -7720805057305804111L, "PriorityQueue serialVersionUID stable");
    }

    static void testStateInvariants() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Arrays.asList(5, 4, 3, 2, 1));
        check(q.remove(3), "remove(element) updates");
        check(q.size() == 4 && !q.contains(3), "size after remove");
        check(q.contains(1) && q.contains(2) && q.contains(4) && q.contains(5), "others survive remove");
        checkThrows(NullPointerException.class, () -> q.add(null), "failed add leaves state intact");
        check(q.size() == 4, "state intact after failed op");
        q.clear();
        check(q.isEmpty() && q.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!q.contains(i), "clear removed " + i);
    }

    public static void main(String[] args) throws Exception {
        testNaturalOrder();
        testComparator();
        testRemoveClear();
        testConstructorMatrix();
        testNullSemantics();
        testEmptySemantics();
        testDuplicates();
        testToArrayContract();
        testFailFast();
        testIdentityEquals();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
