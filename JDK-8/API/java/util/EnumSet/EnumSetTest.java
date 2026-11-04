/*
 * @test
 * @summary java.util.EnumSet API conformance: full contract coverage.
 *          allOf/noneOf/of/range/copyOf/complementOf factories (incl. edge cases: range single,
 *          complementOf noneOf==allOf and allOf==noneOf, of duplicate dedup, copyOf empty IAE,
 *          range from>to IAE), add/remove/contains, ordinal-order iteration regardless of insert
 *          order, null rejection (add NPE, contains/remove null graceful false), collection ops
 *          (addAll/removeAll/retainAll/containsAll), equals/hashCode order-independence +
 *          cross-implementation (HashSet), clone deep, serialization round-trip + serialVersionUID,
 *          and state invariants.
 * @run main EnumSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

public class EnumSetTest {
    enum Color { RED, GREEN, BLUE, YELLOW }

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
    static <E extends Enum<E>> EnumSet<E> roundTrip(EnumSet<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (EnumSet<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testFactories() {
        EnumSet<Color> all = EnumSet.allOf(Color.class);
        check(all.size() == 4 && all.contains(Color.RED) && all.contains(Color.YELLOW), "allOf");

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
        checkThrows(NullPointerException.class, () -> set.add(null), "add(null) NPE");
        check(!set.contains(null), "contains(null) returns false");
        check(!set.remove(null), "remove(null) returns false");
    }

    // ---- factory edge cases + null rejection (extended) --------------------

    static void testFactoryEdgeCases() {
        // range single element (from == to) is valid
        check(EnumSet.range(Color.RED, Color.RED).equals(EnumSet.of(Color.RED)),
              "range(from==to) single element");
        checkThrows(IllegalArgumentException.class, () -> EnumSet.range(Color.BLUE, Color.RED),
                    "range(from>to) IAE");
        checkThrows(NullPointerException.class, () -> EnumSet.range(null, Color.RED),
                    "range(null from) NPE");
        checkThrows(NullPointerException.class, () -> EnumSet.range(Color.RED, null),
                    "range(null to) NPE");

        // complementOf(noneOf) == allOf; complementOf(allOf) == noneOf
        check(EnumSet.complementOf(EnumSet.noneOf(Color.class)).equals(EnumSet.allOf(Color.class)),
              "complementOf(noneOf) == allOf");
        check(EnumSet.complementOf(EnumSet.allOf(Color.class)).isEmpty(),
              "complementOf(allOf) == noneOf");

        // of varargs silently dedups duplicates
        check(EnumSet.of(Color.RED, Color.GREEN, Color.RED).equals(EnumSet.of(Color.RED, Color.GREEN)),
              "of varargs dedups duplicates");

        // copyOf(Collection) and copyOf(EnumSet)
        check(EnumSet.copyOf(Arrays.asList(Color.YELLOW, Color.RED)).equals(EnumSet.of(Color.RED, Color.YELLOW)),
              "copyOf(Collection)");
        checkThrows(IllegalArgumentException.class, () -> EnumSet.copyOf(new ArrayList<Color>()),
                    "copyOf(empty) IAE");
        checkThrows(NullPointerException.class, () -> EnumSet.copyOf((Collection<Color>) null),
                    "copyOf(null Collection) NPE");

        // allOf/noneOf null Class NPE
        checkThrows(NullPointerException.class, () -> EnumSet.allOf(null), "allOf(null) NPE");
        checkThrows(NullPointerException.class, () -> EnumSet.noneOf(null), "noneOf(null) NPE");
    }

    static void testCollectionOps() {
        EnumSet<Color> a = EnumSet.of(Color.RED, Color.GREEN);
        EnumSet<Color> b = EnumSet.of(Color.GREEN, Color.BLUE);
        check(a.containsAll(EnumSet.of(Color.RED)), "containsAll true");
        check(!a.containsAll(EnumSet.of(Color.RED, Color.BLUE)), "containsAll false");
        a.addAll(b);
        check(a.equals(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE)), "addAll union");
        a.retainAll(b);
        check(a.equals(EnumSet.of(Color.GREEN, Color.BLUE)), "retainAll intersection");
        a.removeAll(b);
        check(a.isEmpty(), "removeAll");
    }

    // ---- clone + serialization + cross-impl equals + invariants ------------

    static void testClone() {
        EnumSet<Color> orig = EnumSet.of(Color.RED, Color.YELLOW);
        EnumSet<Color> cloned = orig.clone();
        check(cloned != orig && cloned.equals(orig), "clone distinct and equal");
        cloned.add(Color.GREEN);
        check(cloned.size() == 3 && orig.size() == 2, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        EnumSet<Color> r = roundTrip(s);
        check(r != s && r.equals(s), "round-trip distinct and equal");
        r.add(Color.YELLOW);
        check(r.size() == 4 && s.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(EnumSet.class);
        check(osc.getSerialVersionUID() == 1009687484059888093L, "EnumSet serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        EnumSet<Color> es = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        HashSet<Color> hs = new HashSet<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.RED));
        check(es.equals(hs) && hs.equals(es), "EnumSet/HashSet equals order-independent");
        check(es.hashCode() == hs.hashCode(), "hashCode equal across implementations");
        hs.add(Color.YELLOW);
        check(!es.equals(hs), "!equals different content");
    }

    static void testStateInvariants() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        checkThrows(NullPointerException.class, () -> s.add(null), "failed add leaves state");
        check(s.size() == 3 && s.contains(Color.RED) && s.contains(Color.GREEN) && s.contains(Color.BLUE),
              "state intact after failed add");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        check(!s.contains(Color.RED) && !s.contains(Color.GREEN) && !s.contains(Color.BLUE),
              "clear removed elements");
        s.add(Color.YELLOW);
        check(s.size() == 1 && s.contains(Color.YELLOW), "reusable after clear");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testAddRemoveIteration();
        testNullRejection();
        testFactoryEdgeCases();
        testCollectionOps();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
