/*
 * @test
 * @summary java.util.EnumSet API conformance: allOf/noneOf/of/range/copyOf/complementOf,
 *          add/remove/contains, ordinal-order iteration, size/isEmpty, and null rejection.
 * @run main EnumSetTest
 */

import java.util.EnumSet;

public class EnumSetTest {
    enum Color { RED, GREEN, BLUE, YELLOW }

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFactories() {
        EnumSet<Color> all = EnumSet.allOf(Color.class);
        check(all.size() == 4 && all.contains(Color.RED) && all.contains(Color.YELLOW),
              "allOf");

        EnumSet<Color> none = EnumSet.noneOf(Color.class);
        check(none.isEmpty(), "noneOf empty");

        EnumSet<Color> of = EnumSet.of(Color.RED, Color.BLUE);
        check(of.size() == 2 && of.contains(Color.RED) && of.contains(Color.BLUE), "of");

        EnumSet<Color> range = EnumSet.range(Color.GREEN, Color.BLUE);
        check(range.size() == 2 && range.contains(Color.GREEN) && range.contains(Color.BLUE),
              "range inclusive");

        EnumSet<Color> comp = EnumSet.complementOf(EnumSet.of(Color.RED));
        check(comp.size() == 3 && !comp.contains(Color.RED), "complementOf");

        EnumSet<Color> copied = EnumSet.copyOf(of);
        check(copied.equals(of) && copied != of, "copyOf");
    }

    static void testAddRemoveIteration() {
        EnumSet<Color> set = EnumSet.noneOf(Color.class);
        check(set.add(Color.BLUE) && set.add(Color.RED), "add returns true");
        check(!set.add(Color.RED), "add duplicate returns false");
        check(set.contains(Color.RED), "contains");
        check(set.remove(Color.RED), "remove");
        check(!set.contains(Color.RED), "removed absent");

        // Iteration follows natural (ordinal) order.
        set.clear();
        set.add(Color.YELLOW);
        set.add(Color.RED);
        set.add(Color.GREEN);
        java.util.Iterator<Color> it = set.iterator();
        check(it.next() == Color.RED && it.next() == Color.GREEN && it.next() == Color.YELLOW,
              "iteration in ordinal order");
    }

    static void testNullRejection() {
        EnumSet<Color> set = EnumSet.noneOf(Color.class);
        try {
            set.add(null);
            throw new AssertionError("add(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testFactories();
        testAddRemoveIteration();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
