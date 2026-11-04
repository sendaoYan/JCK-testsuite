/*
 * @test
 * @summary java.util.SortedSet conformance: white-box structure + black-box behavior.
 * @run main SortedSetTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;

public class SortedSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SortedSet");
        check(c.isInterface(), "is an interface");
        check(java.util.Set.class.isAssignableFrom(c), "extends Set");
        for (String m : new String[] {"comparator", "first", "last"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("subSet", Object.class, Object.class)
                .getModifiers()), "subSet abstract");
        for (String m : new String[] {"headSet", "tailSet"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("addFirst", Object.class).getModifiers()),
                "addFirst default");
    }

    static void testBlackBox() {
        SortedSet<Integer> s = new TreeSet<>();
        s.add(1);
        s.add(2);
        s.add(3);
        check(s.comparator() == null, "natural comparator null");
        check(s.first() == 1 && s.last() == 3, "first/last");
        check(s.subSet(1, 3).equals(java.util.Set.of(1, 2)), "subSet");
        check(s.headSet(2).equals(java.util.Set.of(1)), "headSet");
        check(s.tailSet(2).equals(java.util.Set.of(2, 3)), "tailSet");
        check(s.getFirst() == 1 && s.getLast() == 3, "getFirst/getLast");
        expectThrows(() -> s.addFirst(0), UnsupportedOperationException.class, "addFirst UOE");
        check(s.reversed().first() == 3, "reversed");
        check(s.spliterator().estimateSize() == 3, "spliterator");

        SortedSet<Integer> rev = new TreeSet<>(Comparator.<Integer>reverseOrder());
        rev.add(1);
        rev.add(2);
        check(rev.first() == 2, "reverse comparator first");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void testRangeMatrix() {
        SortedSet<Integer> s = new TreeSet<>(List.of(1, 2, 3, 4, 5));
        check(s.headSet(3).equals(Set.of(1, 2)), "headSet(to) exclusive of to");
        check(s.headSet(1).isEmpty(), "headSet(smallest) empty");
        check(s.headSet(0).isEmpty(), "headSet(below all) empty");
        check(s.headSet(6).size() == 5, "headSet(above all) is whole set");
        check(s.headSet(3).size() == 2, "headSet size");
        check(s.tailSet(3).equals(Set.of(3, 4, 5)), "tailSet(from) inclusive of from");
        check(s.tailSet(6).isEmpty(), "tailSet(above all) empty");
        check(s.tailSet(0).size() == 5, "tailSet(below all) is whole set");
        check(s.subSet(2, 4).equals(Set.of(2, 3)), "subSet [from,to)");
        check(s.subSet(3, 3).isEmpty(), "subSet(from==to) is a valid empty range");
        check(s.subSet(2, 99).size() == 4, "subSet bounds may lie outside the set");
        check(s.subSet(1, 5).equals(Set.of(1, 2, 3, 4)), "subSet exact bounds");
        expectThrows(() -> s.headSet(3).tailSet(4), IllegalArgumentException.class,
                "tailSet bound above headSet bound IAE");
        expectThrows(() -> s.tailSet(3).headSet(2), IllegalArgumentException.class,
                "headSet bound below tailSet bound IAE");
        expectThrows(() -> s.subSet(3, 2), IllegalArgumentException.class, "subSet(from>to) IAE");
    }

    static void testViewMutationRules() {
        TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 4, 5));
        SortedSet<Integer> sub = s.subSet(1, 5);
        check(sub.add(3), "in-range add through view returns true");
        check(s.contains(3) && s.size() == 5, "view add reflects in backing set");
        expectThrows(() -> sub.add(5), IllegalArgumentException.class,
                "view add at exclusive upper bound rejected");
        expectThrows(() -> sub.add(0), IllegalArgumentException.class, "view add below lower bound rejected");
        expectThrows(() -> sub.add(99), IllegalArgumentException.class, "view add above upper bound rejected");
        check(sub.remove(3) && !s.contains(3), "view remove reflects in backing set");
        check(sub.size() == 3 && s.size() == 4, "view covers range only (5 excluded), backing full set");
        s.add(3);
        check(sub.contains(3) && sub.size() == 4, "view is live for backing mutations in range");
        SortedSet<Integer> head = s.headSet(3);
        expectThrows(() -> head.add(3), IllegalArgumentException.class,
                "headSet add(to) rejected: to is exclusive");
        SortedSet<Integer> tail = s.tailSet(3);
        expectThrows(() -> tail.add(2), IllegalArgumentException.class, "tailSet add below from rejected");
        check(!tail.add(3) && s.contains(3),
              "tailSet add(from) of existing element returns false: from is inclusive");
        head.remove(1);
        check(!s.contains(1), "headSet remove reflects in backing set");
        SortedSet<Integer> range = s.subSet(2, 5);
        range.clear();
        check(range.isEmpty() && !s.contains(2) && !s.contains(3) && !s.contains(4),
              "view clear removes only the range from backing set");
        check(s.size() == 1 && s.contains(5), "elements outside range survive view clear");
    }

    static void testIterationOrder() {
        SortedSet<String> s = new TreeSet<>(List.of("pear", "apple", "fig"));
        List<String> order = new ArrayList<>();
        for (String x : s) order.add(x);
        check(order.equals(List.of("apple", "fig", "pear")),
              "iteration follows ascending natural order");
        check(s.toArray()[0].equals("apple"), "toArray follows sort order");
        Iterator<String> it = s.iterator();
        check(it.next().equals("apple") && it.next().equals("fig"), "iterator ascending");
        SortedSet<Integer> rev = new TreeSet<>(Comparator.<Integer>reverseOrder());
        rev.addAll(List.of(1, 2, 3));
        check(new ArrayList<>(rev).equals(List.of(3, 2, 1)), "reverse comparator iterates descending");
        check(rev.first() == 3 && rev.last() == 1, "reverse comparator first/last swapped");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        check(new TreeSet<>(cmp).comparator() == cmp, "comparator() returns the same instance");
        // empty-set edge cases for the extremes
        TreeSet<Integer> empty = new TreeSet<>();
        expectThrows(empty::first, NoSuchElementException.class, "empty first NSEE");
        expectThrows(empty::last, NoSuchElementException.class, "empty last NSEE");
    }

    static void testSpliteratorContract() {
        SortedSet<Integer> s = new TreeSet<>(List.of(3, 1, 2));
        Spliterator<Integer> sp = s.spliterator();
        check(sp.hasCharacteristics(Spliterator.ORDERED), "TreeSet spliterator ORDERED");
        check(sp.hasCharacteristics(Spliterator.SORTED), "TreeSet spliterator SORTED");
        check(sp.hasCharacteristics(Spliterator.DISTINCT), "TreeSet spliterator DISTINCT");
        check(sp.getExactSizeIfKnown() == 3, "TreeSet spliterator sized");
        check(sp.getComparator() == null && s.comparator() == null,
              "SORTED spliterator reports natural order as null comparator");
        Comparator<Integer> cmp = Comparator.<Integer>reverseOrder();
        SortedSet<Integer> t = new TreeSet<>(cmp);
        t.addAll(List.of(1, 2));
        Spliterator<Integer> tsp = t.spliterator();
        check(tsp.getComparator() == cmp, "spliterator comparator equals set comparator");
    }

    // JDK-21+: SequencedSet default methods carried by SortedSet
    static void testSequencedDefaults() {
        TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3));
        check(s.getFirst() == 1 && s.getLast() == 3, "getFirst/getLast mirror first/last");
        check(s.removeFirst() == 1 && !s.contains(1), "removeFirst removes and returns least");
        check(s.removeLast() == 3 && !s.contains(3), "removeLast removes and returns greatest");
        check(s.size() == 1 && s.first() == 2, "remaining middle element");
        expectThrows(() -> s.addFirst(0), UnsupportedOperationException.class,
                "addFirst would violate sort order: UOE");
        expectThrows(() -> s.addLast(9), UnsupportedOperationException.class,
                "addLast would violate sort order: UOE");
        SortedSet<Integer> t = new TreeSet<>(List.of(1, 2, 3));
        check(new ArrayList<>(t.reversed()).equals(List.of(3, 2, 1)), "reversed view iterates descending");
        t.add(9);
        check(new ArrayList<>(t.reversed()).equals(List.of(9, 3, 2, 1)),
              "reversed view is live for parent additions");
        t.reversed().remove(9);
        check(!t.contains(9) && t.size() == 3, "removal through reversed view hits backing set");
        check(new ArrayList<>(t.reversed().reversed()).equals(List.of(1, 2, 3)),
              "reversed twice restores ascending order");
        TreeSet<Integer> empty = new TreeSet<>();
        expectThrows(empty::getFirst, NoSuchElementException.class, "empty getFirst NSEE");
        expectThrows(empty::getLast, NoSuchElementException.class, "empty getLast NSEE");
        expectThrows(empty::removeFirst, NoSuchElementException.class, "empty removeFirst NSEE");
        expectThrows(empty::removeLast, NoSuchElementException.class, "empty removeLast NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRangeMatrix();
        testViewMutationRules();
        testIterationOrder();
        testSpliteratorContract();
        testSequencedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
