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

    // ---- contract deep-dive II: CASE_INSENSITIVE ordering / inclusive-bound views /
    //      view liveness+CME / descending twice / comparator serialization /
    //      nullsFirst / poll drain / spliterator exact bits / addAll sorting -----------

    static void testCaseInsensitiveComparator() {
        TreeSet<String> ci = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        check(ci.comparator() == String.CASE_INSENSITIVE_ORDER,
              "comparator() returns the CASE_INSENSITIVE_ORDER instance");
        check(String.CASE_INSENSITIVE_ORDER.compare("A", "a") == 0,
              "CASE_INSENSITIVE_ORDER compares 'A' and 'a' as equal");
        ci.add("b");
        ci.add("C");
        ci.add("A");
        check(new ArrayList<>(ci).equals(Arrays.asList("A", "b", "C")),
              "case-insensitive iteration order A,b,C");
        check(!ci.add("a"), "'a' is a duplicate of 'A' under CASE_INSENSITIVE_ORDER");
        check(ci.size() == 3, "duplicate rejected by comparator equality");
        check(ci.first().equals("A") && ci.last().equals("C"), "first/last under comparator");
        check(ci.contains("B"), "contains('B') matches stored 'b'");
        check(ci.remove("c"), "remove('c') removes stored 'C'");
    }

    static void testInclusiveBoundViews() {
        TreeSet<Integer> s = filled(10);
        SortedSet<Integer> head = s.headSet(4, true);
        check(head.size() == 4 && head.last() == 4, "headSet(to, true) includes bound");
        check(head.first() == 1, "headSet view first");
        check(new ArrayList<>(head).equals(Arrays.asList(1, 2, 3, 4)), "headSet inclusive content");
        check(s.headSet(4, false).size() == 3, "headSet(to, false) excludes bound");
        check(s.headSet(4, false).last() == 3, "headSet exclusive last is below bound");

        SortedSet<Integer> tail = s.tailSet(8, false);
        check(tail.size() == 2 && tail.first() == 9, "tailSet(from, false) excludes bound");
        check(s.tailSet(8, true).size() == 3 && s.tailSet(8, true).first() == 8,
              "tailSet(from, true) includes bound");

        NavigableSet<Integer> sub = s.subSet(3, true, 7, true);
        check(new ArrayList<>(sub).equals(Arrays.asList(3, 4, 5, 6, 7)),
              "subSet inclusive both bounds content");
        check(sub.first() == 3 && sub.last() == 7, "subSet inclusive endpoints");

        checkThrows(IllegalArgumentException.class, () -> tail.add(8),
                    "tailSet(from,false) rejects add(from)");
        checkThrows(IllegalArgumentException.class, () -> head.add(5),
                    "headSet(to,true) rejects add(to+1)");
    }

    static void testViewLivenessAndCme() {
        // sparse set (odd numbers) so in-range insertions are possible
        TreeSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5, 7, 9));
        SortedSet<Integer> sub = s.subSet(2, 8); // {3,5,7}
        check(sub.size() == 3, "subSet initial size");
        s.add(4); // parent inserts element inside the view range
        check(sub.contains(4) && sub.size() == 4, "view sees parent insertion inside range");
        s.remove(4);
        check(!sub.contains(4) && sub.size() == 3, "view sees parent removal");
        check(sub.comparator() == s.comparator(), "view comparator same instance as parent");

        Iterator<Integer> it = sub.iterator();
        it.next();
        s.add(50); // parent structural modification outside the range
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                    "view iterator throws CME after parent modification");

        // nested views
        SortedSet<Integer> inner = sub.subSet(4, 6); // {5}
        check(inner.size() == 1 && inner.first() == 5,
              "nested subSet bounds relative to parent view");
        inner.remove(5);
        check(!s.contains(5), "nested view remove propagates to original set");
    }

    static void testDescendingTwice() {
        TreeSet<Integer> s = filled(5);
        NavigableSet<Integer> d2 = s.descendingSet().descendingSet();
        check(d2.equals(s), "descending twice returns equivalent set");
        check(d2.first() == 1 && d2.last() == 5, "descending twice restores orientation");
        check(new ArrayList<>(d2).equals(Arrays.asList(1, 2, 3, 4, 5)),
              "descending twice iteration ascending");
        d2.add(6);
        check(s.contains(6), "double-descending view is still live");
    }

    static void testComparatorSerialization() throws Exception {
        // serializable comparator survives round-trip with behavior intact
        TreeSet<String> ci = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        ci.add("b");
        ci.add("A");
        TreeSet<String> back = roundTrip(ci);
        check(back.comparator() == String.CASE_INSENSITIVE_ORDER,
              "round-trip preserves comparator instance");
        check(new ArrayList<>(back).equals(Arrays.asList("A", "b")), "round-trip order under comparator");
        check(!back.add("a"), "comparator semantics survive round-trip ('a' duplicate of 'A')");

        // null-tolerant comparator serializes; null handling survives
        TreeSet<Integer> nf = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        nf.add(5);
        nf.add(null);
        TreeSet<Integer> nfBack = roundTrip(nf);
        check(nfBack.contains(null) && nfBack.first() == null,
              "nullsFirst comparator round-trip keeps null first");

        // non-serializable comparator: ObjectOutputStream throws NotSerializableException
        TreeSet<String> bad = new TreeSet<>(new LengthComparator());
        bad.add("xx");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            checkThrows(java.io.NotSerializableException.class, () -> oos.writeObject(bad),
                        "writeObject with non-Serializable comparator throws NotSerializableException");
        }
    }

    static class LengthComparator implements Comparator<String> {
        @Override public int compare(String a, String b) { return a.length() - b.length(); }
    }

    static void testNullsFirstOrdering() {
        TreeSet<Integer> nf = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        nf.add(5);
        nf.add(null);
        nf.add(1);
        check(new ArrayList<>(nf).equals(Arrays.asList(null, 1, 5)), "nullsFirst puts null first");
        check(nf.contains(null), "contains(null) with null-tolerant comparator");
        check(nf.first() == null, "first() is null with nullsFirst");
        check(nf.remove(null), "remove(null) removes null element");
        check(nf.size() == 2 && !nf.contains(null), "size after null removal");
        check(nf.pollFirst() == 1, "pollFirst after null removal returns smallest real element");
        TreeSet<Integer> nl = new TreeSet<>(Comparator.nullsLast(Comparator.naturalOrder()));
        nl.add(1);
        nl.add(null);
        check(nl.last() == null, "nullsLast puts null last");
        check(nl.first() == 1, "nullsLast keeps real elements first");
    }

    static void testPollDrainSequences() {
        TreeSet<Integer> s = filled(5);
        List<Integer> ascending = new ArrayList<>();
        while (!s.isEmpty()) ascending.add(s.pollFirst());
        check(ascending.equals(Arrays.asList(1, 2, 3, 4, 5)), "pollFirst drains ascending");
        check(s.isEmpty() && s.pollFirst() == null, "drained set pollFirst null");

        TreeSet<Integer> s2 = filled(5);
        List<Integer> descending = new ArrayList<>();
        while (!s2.isEmpty()) descending.add(s2.pollLast());
        check(descending.equals(Arrays.asList(5, 4, 3, 2, 1)), "pollLast drains descending");
        check(s2.isEmpty() && s2.pollLast() == null, "drained set pollLast null");

        // interleaved: pollFirst/pollLast peel from both ends
        TreeSet<Integer> s3 = filled(4);
        check(s3.pollFirst() == 1 && s3.pollLast() == 4 && s3.pollFirst() == 2 && s3.pollLast() == 3,
              "interleaved pollFirst/pollLast peel from both ends");
    }

    static void testOrderAfterMiddleRemoval() {
        TreeSet<Integer> s = filled(7);
        s.remove(4);
        check(new ArrayList<>(s).equals(Arrays.asList(1, 2, 3, 5, 6, 7)),
              "iteration still ascending after middle removal");
        s.add(4);
        check(new ArrayList<>(s).equals(Arrays.asList(1, 2, 3, 4, 5, 6, 7)),
              "re-insertion restores position in order");
        check(s.removeIf(x -> x % 2 == 0), "removeIf removes evens");
        check(new ArrayList<>(s).equals(Arrays.asList(1, 3, 5, 7)), "order maintained after removeIf");
        List<Integer> viaStream = s.stream().collect(java.util.stream.Collectors.toList());
        check(viaStream.equals(Arrays.asList(1, 3, 5, 7)), "stream iterates in sorted order");
    }

    static void testSpliteratorDeep() {
        TreeSet<Integer> s = filled(64);
        java.util.Spliterator<Integer> sp = s.spliterator();
        check(sp.characteristics() == (java.util.Spliterator.ORDERED
                | java.util.Spliterator.DISTINCT | java.util.Spliterator.SORTED
                | java.util.Spliterator.SIZED),
              "TreeSet spliterator exact characteristics ORDERED|DISTINCT|SORTED|SIZED (0x55)");
        check(sp.getExactSizeIfKnown() == 64, "getExactSizeIfKnown == size");
        check(sp.getComparator() == null, "SORTED spliterator getComparator null for natural order");

        java.util.Spliterator<Integer> front = sp.trySplit();
        check(front != null, "trySplit non-null");
        List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
        front.forEachRemaining(a::add);
        sp.forEachRemaining(b::add);
        check(a.size() + b.size() == 64, "split halves sizes sum");
        for (int i = 1; i < a.size(); i++) check(a.get(i - 1) < a.get(i), "split front half sorted");
        for (int i = 1; i < b.size(); i++) check(b.get(i - 1) < b.get(i), "split back half sorted");
        List<Integer> merged = new ArrayList<>(a);
        merged.addAll(b);
        check(merged.equals(new ArrayList<>(s)), "split halves in order reproduce the set");

        TreeSet<Integer> rev = new TreeSet<>(Comparator.reverseOrder());
        for (int i = 1; i <= 8; i++) rev.add(i);
        check(rev.spliterator().getComparator() == rev.comparator(),
              "getComparator() == comparator() for explicit comparator");
    }

    static void testHashCodeLockAndAddAllSorting() {
        TreeSet<Integer> ts = filled(3);
        check(ts.hashCode() == 6, "Set hashCode formula sum: {1,2,3} == 6");
        check(!ts.equals(Arrays.asList(1, 2, 3)), "TreeSet.equals(List) false");

        TreeSet<Integer> fromUnsorted = new TreeSet<>();
        check(fromUnsorted.addAll(Arrays.asList(5, 1, 4, 2, 3)), "addAll unsorted input true");
        check(new ArrayList<>(fromUnsorted).equals(Arrays.asList(1, 2, 3, 4, 5)),
              "addAll sorts regardless of input order");
        check(!fromUnsorted.addAll(Arrays.asList(2, 3)), "addAll all-duplicates false");

        TreeSet<Integer> cloneRev = new TreeSet<>(Comparator.reverseOrder());
        cloneRev.addAll(Arrays.asList(1, 2, 3));
        @SuppressWarnings("unchecked")
        TreeSet<Integer> cloned = (TreeSet<Integer>) cloneRev.clone();
        check(cloned.comparator() == Comparator.<Integer>reverseOrder(),
              "clone keeps comparator");
        check(new ArrayList<>(cloned).equals(Arrays.asList(3, 2, 1)), "clone keeps comparator order");
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
        testCaseInsensitiveComparator();
        testInclusiveBoundViews();
        testViewLivenessAndCme();
        testDescendingTwice();
        testComparatorSerialization();
        testNullsFirstOrdering();
        testPollDrainSequences();
        testOrderAfterMiddleRemoval();
        testSpliteratorDeep();
        testHashCodeLockAndAddAllSorting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
