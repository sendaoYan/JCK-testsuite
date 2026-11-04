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
import java.util.List;

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

    // ---- contract deep-dive II: of() arities+null / copyOf typing / addAll typing /
    //      empty enum / iterator channels / cross-type equals / varargs aliasing /
    //      batch returns / spliterator exact bits / huge enum / serialization deep ----

    enum Size { S, M }
    enum Big {
        B00,B01,B02,B03,B04,B05,B06,B07,B08,B09,B10,B11,B12,B13,B14,B15,B16,B17,B18,B19,
        B20,B21,B22,B23,B24,B25,B26,B27,B28,B29,B30,B31,B32,B33,B34,B35,B36,B37,B38,B39,
        B40,B41,B42,B43,B44,B45,B46,B47,B48,B49,B50,B51,B52,B53,B54,B55,B56,B57,B58,B59,
        B60,B61,B62,B63,B64,B65,B66,B67,B68,B69
    }

    static void testOfAritiesAndNull() {
        check(EnumSet.of(Color.RED).size() == 1, "of(E) single");
        check(EnumSet.of(Color.RED, Color.GREEN).size() == 2, "of(E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE).size() == 3, "of(E,E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).size() == 4, "of(E,E,E,E)");
        check(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED).size() == 4,
              "of(E,E,E,E,E) five-arg overload dedups");
        checkThrows(NullPointerException.class, () -> EnumSet.of(null), "of(null) NPE");
        checkThrows(NullPointerException.class, () -> EnumSet.of(Color.RED, (Color[]) null),
                    "of(e, null rest array) NPE");
        checkThrows(NullPointerException.class, () -> EnumSet.of(Color.RED, new Color[]{Color.BLUE, null}),
                    "of(e, rest containing null) NPE");
    }

    static void testCopyOfTyping() {
        @SuppressWarnings("unchecked")
        Collection<Color> bad = (Collection<Color>) (Collection<?>) Arrays.asList("not-a-color");
        checkThrows(ClassCastException.class, () -> EnumSet.copyOf(bad),
                    "copyOf(Collection of wrong type) CCE");
        checkThrows(NullPointerException.class,
                    () -> EnumSet.copyOf(Arrays.asList(Color.RED, null)),
                    "copyOf(Collection containing null) NPE");
        EnumSet<Color> fromHashSet = EnumSet.copyOf(new HashSet<>(Arrays.asList(Color.BLUE, Color.RED)));
        check(fromHashSet.equals(EnumSet.of(Color.RED, Color.BLUE)),
              "copyOf(HashSet) infers enum type from elements");
        check(new ArrayList<>(fromHashSet).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "copyOf result iterates in declaration order");
        EnumSet<Color> copy = EnumSet.copyOf(EnumSet.range(Color.GREEN, Color.YELLOW));
        check(copy.add(Color.RED) && copy.equals(EnumSet.allOf(Color.class)),
              "copyOf(EnumSet) copy is independent");
    }

    static void testAddAllTyping() {
        EnumSet<Color> s = EnumSet.noneOf(Color.class);
        check(s.addAll(Arrays.asList(Color.RED, Color.BLUE)), "addAll(plain List) true");
        check(s.equals(EnumSet.of(Color.RED, Color.BLUE)), "addAll(plain List) adds elements");
        @SuppressWarnings("unchecked")
        Collection<Color> bad = (Collection<Color>) (Collection<?>) Arrays.asList("x");
        checkThrows(ClassCastException.class, () -> EnumSet.noneOf(Color.class).addAll(bad),
                    "addAll(wrong element type) CCE");
        checkThrows(NullPointerException.class,
                    () -> EnumSet.noneOf(Color.class).addAll(Arrays.asList(Color.RED, null)),
                    "addAll(collection containing null) NPE");
    }

    enum Empty {}

    static void testEmptyEnumUniverse() {
        EnumSet<Empty> none = EnumSet.noneOf(Empty.class);
        check(none.isEmpty() && none.size() == 0, "noneOf(empty enum) empty");
        check(!none.iterator().hasNext(), "empty-universe iterator hasNext false");
        check(EnumSet.allOf(Empty.class).isEmpty(), "allOf(empty enum) empty");
        check(none.clone().isEmpty(), "clone of empty-universe set empty");
        checkThrows(IllegalArgumentException.class,
                    () -> EnumSet.copyOf(new ArrayList<Empty>()), "copyOf(empty) IAE");
    }

    static void testIteratorChannels() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        java.util.Iterator<Color> it = s.iterator();
        check(it.next() == Color.RED, "iterator starts at lowest ordinal");
        it.remove();
        check(!s.contains(Color.RED) && s.size() == 3, "iterator.remove removes");
        check(it.next() == Color.GREEN, "iteration continues in ordinal order after remove");
        checkThrows(IllegalStateException.class,
                    () -> s.iterator().remove(), "fresh iterator remove before next ISE");
        List<Color> rest = new ArrayList<>();
        it.forEachRemaining(rest::add);
        check(rest.equals(Arrays.asList(Color.BLUE, Color.YELLOW)),
              "forEachRemaining visits remaining in ordinal order");

        // EnumSet iterators are NOT fail-fast: they snapshot the membership mask at
        // creation, so a structural add during iteration neither throws CME nor is
        // observed by the ongoing iterator (verified identical on JDK-8 and JDK-27).
        EnumSet<Color> mod = EnumSet.of(Color.RED, Color.GREEN);
        java.util.Iterator<Color> it2 = mod.iterator();
        it2.next();
        mod.add(Color.BLUE);
        List<Color> remaining = new ArrayList<>();
        it2.forEachRemaining(remaining::add);
        check(remaining.equals(Arrays.asList(Color.GREEN)),
              "iterator unaffected by concurrent add (no CME, new element not visible)");
        List<Color> fresh = new ArrayList<>();
        for (Color c : mod) fresh.add(c);
        check(fresh.equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "fresh iterator sees the element added during previous iteration");
    }

    static void testCrossTypeEquals() {
        check(!EnumSet.of(Color.RED).equals(EnumSet.of(Size.S)),
              "EnumSets of different enum types are not equal (same size)");
        check(EnumSet.of(Color.RED, Color.GREEN).equals(EnumSet.of(Color.GREEN, Color.RED)),
              "equals independent of iteration/insert order");
    }

    static void testVarargsNotAliased() {
        Color[] rest = {Color.GREEN, Color.BLUE};
        EnumSet<Color> s = EnumSet.of(Color.RED, rest);
        rest[0] = Color.YELLOW;
        check(s.equals(EnumSet.of(Color.RED, Color.GREEN, Color.BLUE)),
              "of() does not alias the caller's varargs array");
    }

    static void testBatchReturnValues() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN);
        check(!s.addAll(Arrays.asList(Color.RED)), "addAll(all duplicates) false");
        check(!s.removeAll(Arrays.asList(Color.BLUE)), "removeAll(no match) false");
        check(!s.retainAll(EnumSet.allOf(Color.class)), "retainAll(superset) false");
        check(!s.removeIf(c -> c == Color.BLUE), "removeIf(no match) false");
        check(s.containsAll(new ArrayList<Color>()), "containsAll(empty) true");
        check(!s.containsAll(EnumSet.allOf(Color.class)), "containsAll(bigger) false");
        checkThrows(NullPointerException.class, () -> s.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.retainAll(null), "retainAll(null) NPE");
    }

    static void testSpliteratorExact() {
        EnumSet<Color> s = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        java.util.Spliterator<Color> sp = s.spliterator();
        // JDK-8 EnumSet spliterator: SIZED|SUBSIZED|DISTINCT only (0x4041).
        // NOTE: JDK 9+ widened this to also report ORDERED|SORTED|NONNULL (0x4155);
        // the JDK-27 variant of this test locks the widened mask instead.
        check(sp.characteristics() == (java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED | java.util.Spliterator.DISTINCT),
              "JDK-8 EnumSet spliterator exact characteristics SIZED|SUBSIZED|DISTINCT (0x4041)");
        check(sp.getExactSizeIfKnown() == 3, "getExactSizeIfKnown == size");
        check(!sp.hasCharacteristics(java.util.Spliterator.SORTED),
              "JDK-8 EnumSet spliterator does not report SORTED");
        checkThrows(IllegalStateException.class, sp::getComparator,
                    "getComparator without SORTED throws IllegalStateException");
        List<Color> gathered = new ArrayList<>();
        sp.forEachRemaining(gathered::add);
        check(gathered.equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "spliterator visits elements in declaration order");
    }

    static void testHugeEnumUniverse() {
        // > 64 constants exercises the non-Regular EnumSet implementation
        EnumSet<Big> range = EnumSet.range(Big.B05, Big.B66);
        check(range.size() == 62, "huge-universe range size inclusive both ends");
        check(range.contains(Big.B05) && range.contains(Big.B66), "range endpoints present");
        check(!range.contains(Big.B04) && !range.contains(Big.B67), "range excludes neighbors");
        List<Big> seq = new ArrayList<>();
        java.util.Iterator<Big> it = range.iterator();
        while (it.hasNext()) seq.add(it.next());
        check(seq.size() == 62 && seq.get(0) == Big.B05 && seq.get(61) == Big.B66,
              "huge-universe iteration follows declaration order endpoints");
        it = range.iterator();
        it.next();
        it.remove();
        check(!range.contains(Big.B05) && range.size() == 61, "huge-universe iterator.remove");
        EnumSet<Big> comp = EnumSet.complementOf(range);
        check(comp.size() == 9, "huge-universe complementOf size");
        check(comp.contains(Big.B00) && comp.contains(Big.B69), "complement contains outside elements");
        check(EnumSet.complementOf(comp).equals(range), "double complement restores");
    }

    static void testSerializationDeep() throws Exception {
        EnumSet<Color> s = EnumSet.of(Color.YELLOW, Color.RED);
        EnumSet<Color> r = roundTrip(s);
        check(new ArrayList<>(r).equals(Arrays.asList(Color.RED, Color.YELLOW)),
              "round-trip iterates in declaration order regardless of fill order");
        r.add(Color.GREEN);
        check(r.equals(EnumSet.of(Color.RED, Color.GREEN, Color.YELLOW)),
              "round-trip result still ordinal-ordered after add");
        EnumSet<Color> empty = roundTrip(EnumSet.noneOf(Color.class));
        check(empty.isEmpty() && empty.add(Color.BLUE), "round-trip of empty set usable");
        EnumSet<Big> big = EnumSet.range(Big.B00, Big.B69);
        EnumSet<Big> bigBack = roundTrip(big);
        check(bigBack.equals(big) && bigBack.size() == 70, "huge-universe set round-trip");
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
        testOfAritiesAndNull();
        testCopyOfTyping();
        testAddAllTyping();
        testEmptyEnumUniverse();
        testIteratorChannels();
        testCrossTypeEquals();
        testVarargsNotAliased();
        testBatchReturnValues();
        testSpliteratorExact();
        testHugeEnumUniverse();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
