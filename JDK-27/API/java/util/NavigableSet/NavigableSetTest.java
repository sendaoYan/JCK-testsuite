/*
 * @test
 * @summary java.util.NavigableSet conformance: white-box structure + black-box behavior.
 * @run main NavigableSetTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class NavigableSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.NavigableSet");
        check(c.isInterface(), "is an interface");
        check(SortedSet.class.isAssignableFrom(c), "extends SortedSet");
        for (String m : new String[] {"lower", "floor", "ceiling", "higher"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        for (String m : new String[] {"pollFirst", "pollLast", "descendingSet",
                "descendingIterator"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
    }

    static void testBlackBox() {
        NavigableSet<Integer> s = new TreeSet<>();
        s.add(1);
        s.add(3);
        s.add(5);
        check(s.lower(3) == 1 && s.floor(3) == 3, "lower/floor");
        check(s.ceiling(3) == 3 && s.higher(3) == 5, "ceiling/higher");
        check(s.lower(1) == null && s.higher(5) == null, "out-of-range nulls");
        check(s.first() == 1 && s.last() == 5, "first/last");
        check(s.pollFirst() == 1 && s.pollLast() == 5, "pollFirst/pollLast");
        check(s.size() == 1 && s.first() == 3, "size after polls");

        NavigableSet<Integer> d = new TreeSet<>();
        d.add(1);
        d.add(2);
        d.add(3);
        check(d.descendingSet().first() == 3 && d.descendingSet().last() == 1,
                "descendingSet");
        Iterator<Integer> it = d.descendingIterator();
        check(it.next() == 3 && it.next() == 2 && it.next() == 1, "descendingIterator");

        NavigableSet<Integer> r = new TreeSet<>();
        for (int i = 1; i <= 6; i++) r.add(i);
        check(r.subSet(2, true, 4, true).equals(java.util.Set.of(2, 3, 4)), "subSet inclusive");
        check(r.headSet(3, false).equals(java.util.Set.of(1, 2)), "headSet exclusive");
        check(r.tailSet(4, false).equals(java.util.Set.of(5, 6)), "tailSet exclusive");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
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

    static void testNearestMatrix() {
        // full probe grid over {1,3,5}: existing keys, gap keys, and out-of-range keys
        NavigableSet<Integer> s = new TreeSet<>(List.of(1, 3, 5));
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
        expectThrows(() -> s.lower(null), NullPointerException.class, "lower(null) NPE");
        expectThrows(() -> s.floor(null), NullPointerException.class, "floor(null) NPE");
        expectThrows(() -> s.ceiling(null), NullPointerException.class, "ceiling(null) NPE");
        expectThrows(() -> s.higher(null), NullPointerException.class, "higher(null) NPE");
    }

    static void testPollExhaustion() {
        NavigableSet<Integer> s = new TreeSet<>(List.of(1, 2, 3));
        check(s.pollFirst() == 1 && s.pollFirst() == 2 && s.pollFirst() == 3,
              "pollFirst drains in ascending order");
        check(s.isEmpty() && s.pollFirst() == null && s.pollLast() == null,
              "polls on empty set return null");
        check(s.add(9) && s.pollLast() == 9, "set reusable after exhaustion");
        NavigableSet<Integer> t = new TreeSet<>(List.of(1, 2, 3));
        NavigableSet<Integer> d = t.descendingSet();
        check(d.pollFirst() == 3, "descending pollFirst equals original pollLast");
        check(t.size() == 2 && !t.contains(3), "descending poll mutated the backing set");
    }

    static void testDescendingLiveness() {
        NavigableSet<Integer> s = new TreeSet<>(List.of(1, 2, 3, 4, 5));
        NavigableSet<Integer> d = s.descendingSet();
        check(d.size() == 5, "descending view reports same size");
        check(d.add(0) && s.contains(0), "descending add reflects in backing set");
        List<Integer> order = new ArrayList<>();
        for (Integer x : d) order.add(x);
        check(order.equals(List.of(5, 4, 3, 2, 1, 0)), "descending iteration is reversed");
        d.remove(3);
        check(!s.contains(3), "descending remove hits backing set");
        NavigableSet<Integer> dd = d.descendingSet();
        List<Integer> asc = new ArrayList<>();
        for (Integer x : dd) asc.add(x);
        check(asc.equals(List.of(0, 1, 2, 4, 5)), "descendingSet twice restores ascending order");
        // comparator of a descending view over natural order is a non-null reversed comparator
        check(s.comparator() == null && d.comparator() != null,
              "descending view of natural order has explicit reversed comparator");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        NavigableSet<Integer> cus = new TreeSet<>(cmp);
        cus.addAll(List.of(1, 2));
        NavigableSet<Integer> cd = cus.descendingSet();
        check(cd.first() == 1 && cd.last() == 2, "descending of custom comparator view flips ends");
        // range views through the descending view follow reversed bounds
        check(d.tailSet(3).equals(Set.of(2, 1, 0)),
              "descending tailSet: <=3 naturally (3 itself was removed), descending direction");
        check(d.headSet(3).equals(Set.of(5, 4)), "descending headSet(<3), descending direction");
    }

    static void testBooleanRangeMatrix() {
        NavigableSet<Integer> r = new TreeSet<>(List.of(1, 2, 3, 4, 5, 6));
        check(r.subSet(2, true, 4, true).equals(Set.of(2, 3, 4)), "subSet [2,4] closed-closed");
        check(r.subSet(2, true, 4, false).equals(Set.of(2, 3)), "subSet [2,4) closed-open");
        check(r.subSet(2, false, 4, true).equals(Set.of(3, 4)), "subSet (2,4] open-closed");
        check(r.subSet(2, false, 4, false).equals(Set.of(3)), "subSet (2,4) open-open");
        check(r.headSet(3, true).equals(Set.of(1, 2, 3)), "headSet inclusive of 3");
        check(r.headSet(3, false).equals(Set.of(1, 2)), "headSet exclusive of 3");
        check(r.tailSet(4, true).equals(Set.of(4, 5, 6)), "tailSet inclusive of 4");
        check(r.tailSet(4, false).equals(Set.of(5, 6)), "tailSet exclusive of 4");
        check(r.subSet(3, true, 3, true).equals(Set.of(3)),
              "subSet closed single-point [3,3] contains 3");
        check(r.subSet(3, false, 3, true).isEmpty(), "subSet (3,3] excludes from");
        check(r.subSet(3, true, 3, false).isEmpty(), "subSet [3,3) excludes to");
        expectThrows(() -> r.subSet(4, true, 2, true), IllegalArgumentException.class,
                "subSet(from>to) IAE even with inclusive flags");
        expectThrows(() -> r.subSet(4, false, 2, true), IllegalArgumentException.class,
                "subSet(from>to) IAE regardless of flags");
        // descending iterator removes through the backing set
        NavigableSet<Integer> t = new TreeSet<>(List.of(1, 2, 3));
        Iterator<Integer> di = t.descendingIterator();
        di.next();
        di.remove();
        check(t.equals(Set.of(1, 2)),
              "descendingIterator.remove drops greatest (first yielded) element");
        List<Integer> seq = new ArrayList<>();
        for (Iterator<Integer> it = t.descendingIterator(); it.hasNext(); ) seq.add(it.next());
        check(seq.equals(List.of(2, 1)), "descendingIterator full sequence after removal");
    }

    // JDK-21+: SequencedSet methods reinterpreted by NavigableSet
    static void testSequencedDefaults() {
        NavigableSet<Integer> s = new TreeSet<>(List.of(1, 2, 3));
        check(s.reversed().size() == 3 && s.reversed().containsAll(Set.of(1, 2, 3)),
              "reversed view holds same elements");
        check(new ArrayList<>(s.reversed()).equals(List.of(3, 2, 1)), "reversed iteration descending");
        check(s.removeFirst() == 1 && s.removeLast() == 3, "removeFirst/removeLast via sequenced API");
        check(s.equals(Set.of(2)), "sequenced removals hit the backing set");
        NavigableSet<Integer> t = new TreeSet<>(List.of(1, 2, 3));
        t.reversed().remove(1);
        check(!t.contains(1) && t.size() == 2, "removal through reversed view hits backing set");
        check(t.reversed().first() == 3 && t.reversed().last() == 2, "reversed view ends flipped");
        expectThrows(() -> new TreeSet<Integer>().removeFirst(), java.util.NoSuchElementException.class,
                "empty removeFirst NSEE");
        expectThrows(() -> new TreeSet<Integer>().removeLast(), java.util.NoSuchElementException.class,
                "empty removeLast NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNearestMatrix();
        testPollExhaustion();
        testDescendingLiveness();
        testBooleanRangeMatrix();
        testSequencedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
