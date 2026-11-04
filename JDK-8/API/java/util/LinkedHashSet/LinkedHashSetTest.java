/*
 * @test
 * @summary java.util.LinkedHashSet API conformance: full contract coverage.
 *          no duplicates + insertion-order iteration (re-add rejected without reorder),
 *          constructor matrix (negative capacity IAE, loadFactor NaN/<=0 IAE, null Collection
 *          NPE, copy preserves order), null element allowance, clone shallow, serialization
 *          round-trip + serialVersionUID, equals/hashCode order-independence + cross-
 *          implementation (HashSet), and state invariants.
 * @run main LinkedHashSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class LinkedHashSetTest {
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
    static <E> LinkedHashSet<E> roundTrip(LinkedHashSet<E> s) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(s); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (LinkedHashSet<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testOrderAndDuplicates() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        check(s.add("a") && s.add("b") && s.add("c"), "add");
        check(!s.add("a"), "duplicate rejected");
        check(s.size() == 3, "size");
        StringBuilder sb = new StringBuilder();
        for (String x : s) {
            sb.append(x);
        }
        check("abc".equals(sb.toString()), "insertion order preserved");
        check(s.remove("b") && !s.contains("b"), "remove");
    }

    // ---- constructor matrix + null allowance -------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(-1),
                    "LinkedHashSet(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(1, 0.0f),
                    "LinkedHashSet(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashSet<String>(1, Float.NaN),
                    "LinkedHashSet(1, NaN) IAE");
        checkThrows(NullPointerException.class, () -> new LinkedHashSet<String>((Collection<String>) null),
                    "LinkedHashSet(null Collection) NPE");

        LinkedHashSet<String> copy = new LinkedHashSet<>(Arrays.asList("c", "a", "b"));
        check(new ArrayList<>(copy).equals(Arrays.asList("c", "a", "b")),
              "LinkedHashSet(Collection) preserves order");
    }

    static void testNullAllowance() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        s.add("a");
        s.add(null);
        check(s.size() == 2 && s.contains(null), "null element allowed");
        check(new ArrayList<>(s).equals(Arrays.asList("a", null)), "null keeps insertion position");
        check(!s.add(null), "duplicate null rejected");
    }

    // ---- clone + serialization + cross-impl equals + invariants ------------

    static void testClone() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b"));
        @SuppressWarnings("unchecked")
        LinkedHashSet<String> clone = (LinkedHashSet<String>) s.clone();
        check(clone != s && clone.equals(s), "clone distinct and equal");
        clone.add("c");
        check(clone.size() == 3 && s.size() == 2, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        LinkedHashSet<String> r = roundTrip(s);
        check(r != s && r.equals(s), "round-trip distinct and equal");
        check(new ArrayList<>(r).equals(Arrays.asList("a", "b", "c")), "round-trip preserves order");
        r.add("d");
        check(r.size() == 4 && s.size() == 3, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(LinkedHashSet.class);
        check(osc.getSerialVersionUID() == -2851667679971038690L, "LinkedHashSet serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        LinkedHashSet<String> lhs = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        HashSet<String> hs = new HashSet<>(Arrays.asList("c", "a", "b"));
        check(lhs.equals(hs) && hs.equals(lhs), "LinkedHashSet/HashSet equals order-independent");
        check(lhs.hashCode() == hs.hashCode(), "hashCode equal across implementations");
        hs.add("d");
        check(!lhs.equals(hs), "!equals different content");
    }

    static void testStateInvariants() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        check(!s.add("a"), "failed duplicate add leaves state");
        check(s.size() == 3 && s.contains("a") && s.contains("b") && s.contains("c"),
              "state intact after duplicate add");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        check(!s.contains("a") && !s.contains("b") && !s.contains("c"), "clear removed elements");
        s.add("z");
        check(s.size() == 1 && s.contains("z"), "reusable after clear");
    }

    public static void main(String[] args) throws Exception {
        testOrderAndDuplicates();
        testConstructorMatrix();
        testNullAllowance();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
