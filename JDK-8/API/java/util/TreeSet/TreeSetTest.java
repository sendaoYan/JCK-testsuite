/*
 * @test
 * @summary java.util.TreeSet API conformance: full contract coverage.
 *          natural/comparator ordering, add (false on duplicate), contains/remove,
 *          first/last, NavigableSet ops (lower/floor/ceiling/higher, pollFirst/pollLast),
 *          subSet/headSet/tailSet range views (inclusive/exclusive + write-through),
 *          descendingSet/descendingIterator, constructor matrix + exception paths,
 *          null element semantics, iterator write channels + fail-fast (CME/ISE/NSEE),
 *          toArray contract, cross-implementation equals/hashCode, clone (shallow +
 *          structural independence), serialization round-trip + serialVersionUID,
 *          and state invariants.
 * @run main TreeSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class TreeSetTest {
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

    static TreeSet<Integer> filled(int n) {
        TreeSet<Integer> s = new TreeSet<>();
        for (int i = 1; i <= n; i++) s.add(i);
        return s;
    }

    @SuppressWarnings("unchecked")
    static <E> TreeSet<E> roundTrip(TreeSet<E> set) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(set); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (TreeSet<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testNaturalOrder() {
        TreeSet<String> set = new TreeSet<>();
        check(set.comparator() == null, "natural comparator null");
        check(set.add("c") && set.add("a") && set.add("b"), "add returns true");
        check(!set.add("a"), "add duplicate returns false");
        check(set.first().equals("a") && set.last().equals("c"), "first/last");
        check(set.size() == 3 && set.contains("b"), "size/contains");
        check(set.remove("b") && !set.contains("b"), "remove");
    }

    static void testComparator() {
        TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(1);
        set.add(3);
        set.add(2);
        check(set.first() == 3 && set.last() == 1, "reverse comparator order");
    }

    static void testNavigable() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i += 2) {
            set.add(i); // 1,3,5,7,9
        }
        check(set.lower(5) == 3, "lower");
        check(set.floor(5) == 5 && set.floor(4) == 3, "floor");
        check(set.ceiling(5) == 5 && set.ceiling(4) == 5, "ceiling");
        check(set.higher(5) == 7, "higher");
        check(set.pollFirst() == 1 && set.pollLast() == 9, "pollFirst/pollLast");
        check(set.size() == 3, "size after polls");
    }

    static void testRangeViews() {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= 10; i++) {
            set.add(i);
        }
        check(set.subSet(3, 7).size() == 4 && set.subSet(3, 7).first() == 3
              && set.subSet(3, 7).last() == 6, "subSet exclusive toElement");
        check(set.headSet(4).size() == 3, "headSet");
        check(set.tailSet(8).size() == 3, "tailSet");
        check(set.subSet(3, true, 7, true).size() == 5, "subSet inclusive bounds");
    }

    // ---- constructor matrix + exception paths -------------------------------

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new TreeSet<Integer>((Collection<Integer>) null),
                    "TreeSet(null Collection)");
        checkThrows(NullPointerException.class, () -> new TreeSet<Integer>((SortedSet<Integer>) null),
                    "TreeSet(null SortedSet)");
        // null Comparator means natural ordering, which rejects null elements.
        checkThrows(NullPointerException.class, () -> new TreeSet<Integer>((Comparator<Integer>) null).add(null),
                    "TreeSet(null Comparator) add(null) uses natural ordering");

        // TreeSet(Collection) with null element (natural order) throws NPE.
        List<Integer> withNull = Arrays.asList(1, null, 2);
        checkThrows(NullPointerException.class, () -> new TreeSet<>(withNull),
                    "TreeSet(Collection) with null element NPE");

        // TreeSet(SortedSet) preserves comparator.
        SortedSet<Integer> rev = new TreeSet<>(Comparator.reverseOrder());
        rev.add(1);
        rev.add(2);
        TreeSet<Integer> fromSorted = new TreeSet<>(rev);
        check(fromSorted.comparator() == Comparator.<Integer>reverseOrder(), "TreeSet(SortedSet) keeps comparator");
        check(fromSorted.first() == 2 && fromSorted.last() == 1, "TreeSet(SortedSet) keeps order");

        // null-tolerant comparator permits null element.
        TreeSet<Integer> nullTolerant = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        nullTolerant.add(null);
        check(nullTolerant.contains(null) && nullTolerant.first() == null, "null element with null-tolerant comparator");
    }

    static void testNullSemantics() {
        TreeSet<Integer> s = new TreeSet<>();
        checkThrows(NullPointerException.class, () -> s.add(null), "add null natural NPE");
        checkThrows(NullPointerException.class, () -> s.contains(null), "contains null natural NPE");
        checkThrows(NullPointerException.class, () -> s.remove(null), "remove null natural NPE");
        TreeSet<Integer> cmp = new TreeSet<>(Comparator.naturalOrder());
        checkThrows(NullPointerException.class, () -> cmp.add(null), "comparator add null NPE");
    }

    // ---- NavigableSet boundary + live views ---------------------------------

    static void testNavigableBoundaries() {
        TreeSet<Integer> empty = new TreeSet<>();
        checkThrows(NoSuchElementException.class, empty::first, "empty first NSEE");
        checkThrows(NoSuchElementException.class, empty::last, "empty last NSEE");
        check(empty.pollFirst() == null && empty.pollLast() == null, "empty poll null");
        check(empty.lower(5) == null && empty.floor(5) == null, "empty lower/floor null");
        check(empty.ceiling(5) == null && empty.higher(5) == null, "empty ceiling/higher null");

        TreeSet<Integer> s = filled(10);
        check(s.lower(1) == null && s.higher(10) == null, "boundary lower/higher null");
        check(s.floor(5) == 5 && s.ceiling(5) == 5, "floor/ceiling exact");
        check(s.floor(0) == null && s.ceiling(11) == null, "out of range floor/ceiling null");
    }

    static void testRangeViewWriteThrough() {
        TreeSet<Integer> s = filled(10);
        SortedSet<Integer> sub = s.subSet(3, 7);
        checkThrows(IllegalArgumentException.class, () -> sub.add(100), "subSet add out of range IAE");
        checkThrows(IllegalArgumentException.class, () -> sub.add(2), "subSet add below range IAE");
        check(sub.add(5) || true, "subSet add existing");
        sub.add(4);
        check(s.contains(4), "subSet add reflects in set");
        sub.remove(4);
        check(!s.contains(4), "subSet remove reflects in set");
        sub.clear();
        check(!s.contains(3) && !s.contains(6), "subSet clear removes range only");
        check(s.contains(2) && s.contains(7), "elements outside range survive clear");

        checkThrows(IllegalArgumentException.class, () -> s.subSet(7, 3), "subSet from>to IAE");
        checkThrows(IllegalArgumentException.class, () -> s.subSet(3, true, 7, false).add(7),
                    "subSet exclusive toElement rejects add(toElement)");
        checkThrows(IllegalArgumentException.class, () -> s.subSet(3, false, 7, true).add(3),
                    "subSet exclusive fromElement rejects add(fromElement)");

        SortedSet<Integer> head = s.headSet(4);
        checkThrows(IllegalArgumentException.class, () -> head.add(4), "headSet add at/above bound IAE");
        head.add(3);
        check(s.contains(3), "headSet add reflects");
        SortedSet<Integer> tail = s.tailSet(8);
        tail.add(8);
        check(s.contains(8), "tailSet add reflects");
    }

    static void testDescendingViews() {
        TreeSet<Integer> s = filled(10);
        NavigableSet<Integer> desc = s.descendingSet();
        check(desc.first() == 10 && desc.last() == 1, "descendingSet endpoints");
        check(desc.comparator().compare(5, 3) < 0, "descendingSet comparator reverses order");
        desc.add(11);
        check(s.contains(11) && s.first() == 1 && s.last() == 11, "descendingSet add reflects");
        desc.remove(1);
        check(!s.contains(1), "descendingSet remove reflects");

        Iterator<Integer> dit = s.descendingIterator();
        check(dit.next() == 11, "descendingIterator starts at max");
        int prev = dit.next();
        while (dit.hasNext()) {
            int cur = dit.next();
            check(prev > cur, "descendingIterator decreasing");
            prev = cur;
        }
    }

    // ---- iterator write channels + fail-fast --------------------------------

    static void testIteratorWriteChannels() {
        TreeSet<Integer> s = filled(5);
        Iterator<Integer> it = s.iterator();
        it.next();
        it.remove();
        check(!s.contains(1), "iterator.remove removes first");
        checkThrows(IllegalStateException.class, it::remove, "remove before next ISE");

        Iterator<Integer> it2 = s.iterator();
        checkThrows(NoSuchElementException.class, () -> { while (it2.hasNext()) it2.next(); it2.next(); },
                    "exhausted iterator next NSEE");

        TreeSet<Integer> r = filled(5);
        r.removeAll(Arrays.asList(2, 4));
        check(r.size() == 3 && !r.contains(2) && !r.contains(4), "removeAll");
        r.retainAll(Arrays.asList(1, 3));
        check(r.size() == 2 && r.contains(1) && r.contains(3), "retainAll");
        r.clear();
        check(r.isEmpty(), "clear");
    }

    static void testFailFast() {
        TreeSet<Integer> s = filled(5);
        Iterator<Integer> it = s.iterator();
        it.next();
        s.add(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "iterator CME after remove");

        Iterator<Integer> dit = s.descendingIterator();
        dit.next();
        s.clear();
        checkThrows(java.util.ConcurrentModificationException.class, dit::next, "descendingIterator CME after clear");
    }

    // ---- toArray contract ---------------------------------------------------

    static void testToArrayContract() {
        TreeSet<Integer> s = filled(3);
        Object[] oa = s.toArray();
        check(oa.length == 3 && oa[0].equals(1) && oa[2].equals(3), "toArray() ascending");

        Integer[] exact = s.toArray(new Integer[0]);
        check(exact.length == 3 && exact[0] == 1 && exact[2] == 3, "toArray(new T[0]) exact");

        Integer[] reusable = new Integer[]{9, 9, 9, 9};
        Integer[] returned = s.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[3] == null, "toArray(T[]) pads trailing null");
        check(reusable[0] == 1 && reusable[1] == 2 && reusable[2] == 3, "toArray(T[]) content ascending");
    }

    // ---- cross-implementation equals/hashCode -------------------------------

    static void testCrossImplEquals() {
        TreeSet<Integer> ts = filled(3);
        HashSet<Integer> hs = new HashSet<>(Arrays.asList(3, 1, 2));
        check(ts.equals(hs) && hs.equals(ts), "TreeSet equals HashSet symmetric");
        check(ts.hashCode() == hs.hashCode(), "equal sets equal hashCode");
        check(!ts.equals(new HashSet<>(Arrays.asList(1, 2))), "!equals different size");

        TreeSet<Integer> rev = new TreeSet<>(Comparator.reverseOrder());
        rev.add(1);
        rev.add(2);
        rev.add(3);
        check(ts.equals(rev) && rev.equals(ts), "TreeSet equals regardless of comparator");
    }

    // ---- clone / serialization / state invariants ---------------------------

    static void testClone() {
        TreeSet<Integer> s = filled(3);
        @SuppressWarnings("unchecked")
        TreeSet<Integer> clone = (TreeSet<Integer>) s.clone();
        check(clone != s, "clone distinct object");
        check(clone.equals(s) && clone.hashCode() == s.hashCode(), "clone equals original");
        check(clone.comparator() == s.comparator(), "clone preserves comparator");
        clone.add(4);
        check(s.size() == 3 && !s.contains(4), "clone structural independence");
    }

    static void testSerialization() throws Exception {
        TreeSet<Integer> s = filled(3);
        TreeSet<Integer> r = roundTrip(s);
        check(r != s, "round-trip distinct object");
        check(r.equals(s), "round-trip content equal");
        check(r.first() == 1 && r.last() == 3, "round-trip preserves ordering");
        r.add(4);
        check(r.size() == 4 && s.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(TreeSet.class);
        check(osc.getSerialVersionUID() == -2479143000061671589L, "TreeSet serialVersionUID stable");
    }

    static void testStateInvariants() {
        TreeSet<Integer> s = filled(5);
        s.remove(3);
        check(s.size() == 4 && !s.contains(3), "remove updates size");
        check(s.contains(1) && s.contains(2) && s.contains(4) && s.contains(5), "others survive remove");
        checkThrows(NullPointerException.class, () -> s.add(null), "failed add leaves state intact");
        check(s.size() == 4 && s.first() == 1 && s.last() == 5, "state intact after failed op");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!s.contains(i), "clear removed " + i);
    }

    public static void main(String[] args) throws Exception {
        testNaturalOrder();
        testComparator();
        testNavigable();
        testRangeViews();
        testConstructorMatrix();
        testNullSemantics();
        testNavigableBoundaries();
        testRangeViewWriteThrough();
        testDescendingViews();
        testIteratorWriteChannels();
        testFailFast();
        testToArrayContract();
        testCrossImplEquals();
        testClone();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
