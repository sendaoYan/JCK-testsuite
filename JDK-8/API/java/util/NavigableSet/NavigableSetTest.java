/*
 * @test
 * @summary java.util.NavigableSet API conformance: lower/floor/ceiling/higher (with out-of-range
 *          nulls), pollFirst/pollLast, descendingSet/descendingIterator, boolean-inclusive range
 *          views, subSet(from>to) IllegalArgumentException, and null-element NPE under natural
 *          ordering.
 * @run main NavigableSetTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class NavigableSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testNearest() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.lower(3) == 1, "lower");
        check(s.floor(3) == 3, "floor");
        check(s.ceiling(3) == 3, "ceiling");
        check(s.higher(3) == 5, "higher");
        check(s.lower(1) == null && s.higher(5) == null, "out-of-range nulls");
        expectThrows(NullPointerException.class, () -> s.add(null), "add(null) NPE natural");
    }

    static void testPoll() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        check(s.pollFirst() == 1, "pollFirst");
        check(s.pollLast() == 5, "pollLast");
        check(s.size() == 1 && s.first() == 3, "size after polls");

        NavigableSet<Integer> empty = new TreeSet<>();
        check(empty.pollFirst() == null && empty.pollLast() == null, "poll empty null");
    }

    static void testDescending() {
        NavigableSet<Integer> d = new TreeSet<>(Arrays.asList(1, 2, 3));
        check(d.descendingSet().first() == 3 && d.descendingSet().last() == 1, "descendingSet");
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator");
    }

    static void testRangeViews() {
        NavigableSet<Integer> r = new TreeSet<>();
        for (int i = 1; i <= 6; i++) r.add(i);
        check(r.subSet(2, true, 4, true).equals(new TreeSet<>(Arrays.asList(2, 3, 4))),
                "subSet inclusive");
        check(r.headSet(3, false).equals(new TreeSet<>(Arrays.asList(1, 2))), "headSet exclusive");
        check(r.tailSet(4, false).equals(new TreeSet<>(Arrays.asList(5, 6))), "tailSet exclusive");
        expectThrows(IllegalArgumentException.class, () -> r.subSet(4, true, 2, true),
                "subSet from>to IAE");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void testNearestMatrix() {
        // full probe grid over {1,3,5}: existing keys, gap keys, and out-of-range keys
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 3, 5));
        Integer[] lower  = {null, null, 1, 1, 3, 3, 5};
        Integer[] floor  = {null, 1, 1, 3, 3, 5, 5};
        Integer[] ceil   = {1, 1, 3, 3, 5, 5, null};
        Integer[] higher = {1, 3, 3, 5, 5, null, null};
        for (int probe = 0; probe <= 6; probe++) {
            check(s.lower(probe) == null ? lower[probe] == null : s.lower(probe).equals(lower[probe]),
                  "lower(" + probe + ")==" + lower[probe]);
            check(s.floor(probe) == null ? floor[probe] == null : s.floor(probe).equals(floor[probe]),
                  "floor(" + probe + ")==" + floor[probe]);
            check(s.ceiling(probe) == null ? ceil[probe] == null : s.ceiling(probe).equals(ceil[probe]),
                  "ceiling(" + probe + ")==" + ceil[probe]);
            check(s.higher(probe) == null ? higher[probe] == null : s.higher(probe).equals(higher[probe]),
                  "higher(" + probe + ")==" + higher[probe]);
        }
        // descending view mirrors the nearest-neighbour semantics
        NavigableSet<Integer> d = s.descendingSet();
        check(d.lower(4) == 5 && d.higher(4) == 3, "descending lower/higher follow reversed order");
        check(d.floor(3) == 3 && d.ceiling(5) == 5, "descending floor/ceiling on members");
        // null query under natural ordering -> NPE
        expectThrows(NullPointerException.class, () -> s.lower(null), "lower(null) NPE");
        expectThrows(NullPointerException.class, () -> s.floor(null), "floor(null) NPE");
        expectThrows(NullPointerException.class, () -> s.ceiling(null), "ceiling(null) NPE");
        expectThrows(NullPointerException.class, () -> s.higher(null), "higher(null) NPE");
    }

    static void testPollExhaustion() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3));
        check(s.pollFirst() == 1 && s.pollFirst() == 2 && s.pollFirst() == 3,
              "pollFirst drains in ascending order");
        check(s.isEmpty() && s.pollFirst() == null && s.pollLast() == null,
              "polls on empty set return null");
        check(s.add(9) && s.pollLast() == 9, "set reusable after exhaustion");
        NavigableSet<Integer> t = new TreeSet<>(Arrays.asList(1, 2, 3));
        NavigableSet<Integer> d = t.descendingSet();
        check(d.pollFirst() == 3, "descending pollFirst equals original pollLast");
        check(t.size() == 2 && !t.contains(3), "descending poll mutated the backing set");
    }

    static void testDescendingLiveness() {
        NavigableSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
        NavigableSet<Integer> d = s.descendingSet();
        check(d.size() == 5, "descending view reports same size");
        check(d.add(0) && s.contains(0), "descending add reflects in backing set");
        List<Integer> order = new ArrayList<>();
        for (Integer x : d) order.add(x);
        check(order.equals(Arrays.asList(5, 4, 3, 2, 1, 0)), "descending iteration is reversed");
        d.remove(3);
        check(!s.contains(3), "descending remove hits backing set");
        NavigableSet<Integer> dd = d.descendingSet();
        List<Integer> asc = new ArrayList<>();
        for (Integer x : dd) asc.add(x);
        check(asc.equals(Arrays.asList(0, 1, 2, 4, 5)),
              "descendingSet twice restores ascending order");
        // comparator of a descending view over natural order is a non-null reversed comparator
        check(s.comparator() == null && d.comparator() != null,
              "descending view of natural order has explicit reversed comparator");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        NavigableSet<Integer> cus = new TreeSet<>(cmp);
        cus.addAll(Arrays.asList(1, 2));
        NavigableSet<Integer> cd = cus.descendingSet();
        check(cd.first() == 1 && cd.last() == 2, "descending of custom comparator view flips ends");
        // range views through the descending view follow reversed bounds
        check(d.tailSet(3).equals(new TreeSet<>(Arrays.asList(2, 1, 0))),
              "descending tailSet: <=3 naturally (3 itself was removed), descending direction");
        check(d.headSet(3).equals(new TreeSet<>(Arrays.asList(5, 4))),
              "descending headSet(<3), descending direction");
    }

    static void testBooleanRangeMatrix() {
        NavigableSet<Integer> r = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(r.subSet(2, true, 4, true).equals(new TreeSet<>(Arrays.asList(2, 3, 4))),
              "subSet [2,4] closed-closed");
        check(r.subSet(2, true, 4, false).equals(new TreeSet<>(Arrays.asList(2, 3))),
              "subSet [2,4) closed-open");
        check(r.subSet(2, false, 4, true).equals(new TreeSet<>(Arrays.asList(3, 4))),
              "subSet (2,4] open-closed");
        check(r.subSet(2, false, 4, false).equals(new TreeSet<>(Arrays.asList(3))),
              "subSet (2,4) open-open");
        check(r.headSet(3, true).equals(new TreeSet<>(Arrays.asList(1, 2, 3))),
              "headSet inclusive of 3");
        check(r.headSet(3, false).equals(new TreeSet<>(Arrays.asList(1, 2))),
              "headSet exclusive of 3");
        check(r.tailSet(4, true).equals(new TreeSet<>(Arrays.asList(4, 5, 6))),
              "tailSet inclusive of 4");
        check(r.tailSet(4, false).equals(new TreeSet<>(Arrays.asList(5, 6))),
              "tailSet exclusive of 4");
        check(r.subSet(3, true, 3, true).equals(new TreeSet<>(Arrays.asList(3))),
              "subSet closed single-point [3,3] contains 3");
        check(r.subSet(3, false, 3, true).isEmpty(), "subSet (3,3] excludes from");
        check(r.subSet(3, true, 3, false).isEmpty(), "subSet [3,3) excludes to");
        expectThrows(IllegalArgumentException.class, () -> r.subSet(4, true, 2, true),
                "subSet(from>to) IAE even with inclusive flags");
        expectThrows(IllegalArgumentException.class, () -> r.subSet(4, false, 2, true),
                "subSet(from>to) IAE regardless of flags");
        // descending iterator removes through the backing set
        NavigableSet<Integer> t = new TreeSet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> di = t.descendingIterator();
        di.next();
        di.remove();
        check(t.equals(new TreeSet<>(Arrays.asList(1, 2))),
              "descendingIterator.remove drops greatest (first yielded) element");
        // descendingIterator walks full reversed sequence
        List<Integer> seq = new ArrayList<>();
        for (Iterator<Integer> it = t.descendingIterator(); it.hasNext(); ) seq.add(it.next());
        check(seq.equals(Arrays.asList(2, 1)), "descendingIterator full sequence after removal");
    }

    public static void main(String[] args) {
        testNearest();
        testPoll();
        testDescending();
        testRangeViews();
        testNearestMatrix();
        testPollExhaustion();
        testDescendingLiveness();
        testBooleanRangeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
