/*
 * @test
 * @summary java.util.SortedSet API conformance: comparator (natural null vs custom), first/last
 *          with empty NoSuchElementException, headSet/tailSet/subSet range views (exclusive/
 *          inclusive), subSet(from>to) IllegalArgumentException, null-element NPE under natural
 *          ordering, and backed view liveness.
 * @run main SortedSetTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;

public class SortedSetTest {
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

    static void testNaturalOrder() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(3, 1, 2));
        check(s.comparator() == null, "natural ordering comparator is null");
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.size() == 3, "size");
        expectThrows(NullPointerException.class, () -> s.add(null), "add(null) NPE natural");
    }

    static void testEmpty() {
        SortedSet<Integer> s = new TreeSet<>();
        expectThrows(NoSuchElementException.class, s::first, "first empty NSEE");
        expectThrows(NoSuchElementException.class, s::last, "last empty NSEE");
    }

    static void testCustomComparator() {
        SortedSet<Integer> s = new TreeSet<>(Comparator.<Integer>reverseOrder());
        s.add(1);
        s.add(2);
        check(s.comparator() != null, "custom comparator is non-null");
        check(s.first() == 2, "reverse comparator first");
    }

    static void testRangeViews() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
        check(s.headSet(3).equals(new TreeSet<>(Arrays.asList(1, 2))), "headSet exclusive");
        check(s.tailSet(3).equals(new TreeSet<>(Arrays.asList(3, 4, 5))), "tailSet inclusive");
        check(s.subSet(2, 4).equals(new TreeSet<>(Arrays.asList(2, 3))), "subSet [from,to)");
        expectThrows(IllegalArgumentException.class, () -> s.subSet(4, 2), "subSet from>to IAE");
    }

    static void testBackedViews() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2));
        SortedSet<Integer> sub = s.subSet(1, 4);
        check(sub.add(3), "subSet add in range returns true");
        check(s.contains(3), "subSet is live (add reflects)");
        sub.remove(2);
        check(!s.contains(2), "subSet remove reflects in backing set");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void testRangeMatrix() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5));
        check(s.headSet(3).equals(new TreeSet<>(Arrays.asList(1, 2))), "headSet(to) exclusive of to");
        check(s.headSet(1).isEmpty(), "headSet(smallest) empty");
        check(s.headSet(0).isEmpty(), "headSet(below all) empty");
        check(s.headSet(6).size() == 5, "headSet(above all) is whole set");
        check(s.headSet(3).size() == 2, "headSet size");
        check(s.tailSet(3).equals(new TreeSet<>(Arrays.asList(3, 4, 5))), "tailSet(from) inclusive of from");
        check(s.tailSet(6).isEmpty(), "tailSet(above all) empty");
        check(s.tailSet(0).size() == 5, "tailSet(below all) is whole set");
        check(s.subSet(2, 4).equals(new TreeSet<>(Arrays.asList(2, 3))), "subSet [from,to)");
        check(s.subSet(3, 3).isEmpty(), "subSet(from==to) is a valid empty range");
        check(s.subSet(2, 99).size() == 4, "subSet bounds may lie outside the set");
        check(s.subSet(1, 5).equals(new TreeSet<>(Arrays.asList(1, 2, 3, 4))), "subSet exact bounds");
        expectThrows(IllegalArgumentException.class, () -> s.headSet(3).tailSet(4),
                "tailSet bound above headSet bound IAE");
        expectThrows(IllegalArgumentException.class, () -> s.tailSet(3).headSet(2),
                "headSet bound below tailSet bound IAE");
        expectThrows(IllegalArgumentException.class, () -> s.subSet(3, 2),
                "subSet(from>to) IAE");
    }

    static void testViewMutationRules() {
        TreeSet<Integer> s = new TreeSet<>(Arrays.asList(1, 2, 4, 5));
        SortedSet<Integer> sub = s.subSet(1, 5);
        check(sub.add(3), "in-range add through view returns true");
        check(s.contains(3) && s.size() == 5, "view add reflects in backing set");
        expectThrows(IllegalArgumentException.class, () -> sub.add(5),
                "view add at exclusive upper bound rejected");
        expectThrows(IllegalArgumentException.class, () -> sub.add(0),
                "view add below lower bound rejected");
        expectThrows(IllegalArgumentException.class, () -> sub.add(99),
                "view add above upper bound rejected");
        check(sub.remove(3) && !s.contains(3), "view remove reflects in backing set");
        check(sub.size() == 3 && s.size() == 4, "view covers range only (5 excluded), backing full set");
        s.add(3); // backing-set add inside the range is visible through the view
        check(sub.contains(3) && sub.size() == 4, "view is live for backing mutations in range");
        SortedSet<Integer> head = s.headSet(3);
        expectThrows(IllegalArgumentException.class, () -> head.add(3),
                "headSet add(to) rejected: to is exclusive");
        SortedSet<Integer> tail = s.tailSet(3);
        expectThrows(IllegalArgumentException.class, () -> tail.add(2),
                "tailSet add below from rejected");
        check(!tail.add(3) && s.contains(3),
              "tailSet add(from) of existing element returns false: from is inclusive");
        head.remove(1);
        check(!s.contains(1), "headSet remove reflects in backing set");
        // clear through the view empties exactly the range
        SortedSet<Integer> range = s.subSet(2, 5);
        range.clear();
        check(range.isEmpty() && !s.contains(2) && !s.contains(3) && !s.contains(4),
              "view clear removes only the range from backing set");
        check(s.size() == 1 && s.contains(5), "elements outside range survive view clear");
    }

    static void testIterationOrder() {
        SortedSet<String> s = new TreeSet<>(Arrays.asList("pear", "apple", "fig"));
        List<String> order = new ArrayList<>();
        for (String x : s) order.add(x);
        check(order.equals(Arrays.asList("apple", "fig", "pear")),
              "iteration follows ascending natural order");
        check(s.toArray().length == 3 && s.toArray()[0].equals("apple"), "toArray follows sort order");
        Iterator<String> it = s.iterator();
        check(it.next().equals("apple") && it.next().equals("fig"), "iterator ascending");
        SortedSet<Integer> rev = new TreeSet<>(Comparator.<Integer>reverseOrder());
        rev.addAll(Arrays.asList(1, 2, 3));
        List<Integer> revOrder = new ArrayList<>(rev);
        check(revOrder.equals(Arrays.asList(3, 2, 1)), "reverse comparator iterates descending");
        check(rev.first() == 3 && rev.last() == 1, "reverse comparator first/last swapped");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        check(new TreeSet<>(cmp).comparator() == cmp, "comparator() returns the same instance");
    }

    static void testSpliteratorContract() {
        SortedSet<Integer> s = new TreeSet<>(Arrays.asList(3, 1, 2));
        Spliterator<Integer> sp = s.spliterator();
        check(sp.hasCharacteristics(Spliterator.ORDERED), "TreeSet spliterator ORDERED");
        check(sp.hasCharacteristics(Spliterator.SORTED), "TreeSet spliterator SORTED");
        check(sp.hasCharacteristics(Spliterator.DISTINCT), "TreeSet spliterator DISTINCT");
        check(sp.getExactSizeIfKnown() == 3, "TreeSet spliterator sized");
        check(sp.getComparator() == null && s.comparator() == null,
              "SORTED spliterator reports natural order as null comparator");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        SortedSet<Integer> t = new TreeSet<>(cmp);
        t.addAll(Arrays.asList(1, 2));
        Spliterator<Integer> tsp = t.spliterator();
        check(tsp.getComparator() == cmp, "spliterator comparator equals set comparator");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testEmpty();
        testCustomComparator();
        testRangeViews();
        testBackedViews();
        testRangeMatrix();
        testViewMutationRules();
        testIterationOrder();
        testSpliteratorContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
