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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

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

    // ---- contract deep-dive II: reinsert semantics / write channels / fail-fast /
    //      toArray order / equals+hashCode locks / batch returns / stream+spliterator /
    //      serialization with null ------------------------------------------------

    static void testReinsertMovesToEnd() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        s.remove("b");
        s.add("b");
        check(new ArrayList<>(s).equals(Arrays.asList("a", "c", "b")),
              "remove+reinsert moves element to the end");
        check(!s.add("a") && new ArrayList<>(s).equals(Arrays.asList("a", "c", "b")),
              "failed duplicate add does not reorder");
    }

    static void testIteratorWriteChannels() {
        LinkedHashSet<Integer> s = new LinkedHashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Iterator<Integer> it = s.iterator();
        check(it.next() == 1, "iteration starts at first inserted");
        it.remove();
        check(!s.contains(1), "iterator.remove removes element");
        check(new ArrayList<>(s).equals(Arrays.asList(2, 3, 4, 5)),
              "iterator.remove preserves order of remaining elements");
        checkThrows(IllegalStateException.class, it::remove, "remove before next ISE");
        checkThrows(IllegalStateException.class, it::remove, "remove twice ISE");

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.clear();
        checkThrows(java.util.ConcurrentModificationException.class, it2::next,
                    "iterator next on externally cleared set throws CME");

        Iterator<Integer> it3 = new LinkedHashSet<Integer>().iterator();
        check(!it3.hasNext(), "empty set iterator hasNext false");
        checkThrows(NoSuchElementException.class, it3::next, "empty set iterator next NSEE");
    }

    static void testFailFast() {
        LinkedHashSet<Integer> s = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = s.iterator();
        it.next();
        s.add(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "CME after add");

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "CME after remove");

        Iterator<Integer> it3 = s.iterator();
        it3.next();
        s.clear();
        checkThrows(java.util.ConcurrentModificationException.class, it3::next, "CME after clear");

        LinkedHashSet<Integer> s2 = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it4 = s2.iterator();
        it4.next();
        it4.remove(); // iterator.remove is the only legal removal during iteration
        it4.next();
        check(s2.size() == 2, "iterator.remove does not fail the iterator");
    }

    static void testToArrayOrder() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("c", "a", "b"));
        Object[] oa = s.toArray();
        check(java.util.Arrays.equals(oa, new Object[]{"c", "a", "b"}),
              "toArray() preserves insertion order");
        String[] exact = s.toArray(new String[0]);
        check(exact.length == 3 && exact[0].equals("c") && exact[2].equals("b"),
              "toArray(new T[0]) exact length and order");
        String[] reusable = new String[3];
        String[] returned = s.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[0].equals("c") && reusable[1].equals("a") && reusable[2].equals("b"),
              "toArray(T[]) content in insertion order");
        String[] oversized = new String[5];
        s.toArray(oversized);
        check(oversized[3] == null, "toArray(T[]) pads with null just past the end");
        check(oversized[4] == null, "toArray(T[]) leaves remaining slots null");
    }

    static void testEqualsHashCodeDeep() {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
        check(lhs.hashCode() == 6, "hashCode formula sum: {1,2,3} == 6");
        check(lhs.hashCode() == new HashSet<>(Arrays.asList(3, 2, 1)).hashCode(),
              "hashCode equal to HashSet with same content");
        check(lhs.equals(new java.util.TreeSet<>(Arrays.asList(3, 2, 1))),
              "equals TreeSet with same content");
        check(new LinkedHashSet<String>().hashCode() == 0, "empty set hashCode 0");
        check(!lhs.equals(java.util.Arrays.asList(1, 2, 3)), "Set.equals(List) false");
        LinkedHashSet<String> withNull = new LinkedHashSet<>(Arrays.asList("a", null));
        check(withNull.hashCode() == 97, "hashCode counts null element as 0 ({'a',null} == 97)");
        check(withNull.equals(new HashSet<>(Arrays.asList(null, "a"))),
              "equals order-independent with null element");
    }

    static void testBatchOpReturnContracts() {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", "b"));
        check(!s.addAll(new ArrayList<String>()), "addAll(empty) false");
        check(!s.removeAll(Arrays.asList("z")), "removeAll(no match) false");
        check(!s.retainAll(Arrays.asList("a", "b")), "retainAll(superset) false");
        check(!s.removeIf(x -> x.equals("zz")), "removeIf(no match) false");
        check(s.containsAll(new ArrayList<String>()), "containsAll(empty) true");
        check(new ArrayList<>(s).equals(Arrays.asList("a", "b")),
              "no-op batch operations preserve order");
        checkThrows(NullPointerException.class, () -> s.addAll(null), "addAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.containsAll(null), "containsAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.removeAll(null), "removeAll(null) NPE");
        checkThrows(NullPointerException.class, () -> s.retainAll(null), "retainAll(null) NPE");
    }

    static void testStreamAndSpliterator() {
        LinkedHashSet<Integer> s = new LinkedHashSet<>();
        for (int i = 1; i <= 5; i++) s.add(i);
        List<Integer> seq = s.stream().collect(java.util.stream.Collectors.toList());
        check(seq.equals(Arrays.asList(1, 2, 3, 4, 5)), "stream preserves encounter order");

        java.util.Spliterator<Integer> sp = s.spliterator();
        check(sp.characteristics() == (java.util.Spliterator.ORDERED
                | java.util.Spliterator.DISTINCT | java.util.Spliterator.SIZED
                | java.util.Spliterator.SUBSIZED),
              "LinkedHashSet spliterator exact characteristics ORDERED|DISTINCT|SIZED|SUBSIZED (0x4051)");
        check(sp.getExactSizeIfKnown() == 5, "getExactSizeIfKnown == size");

        java.util.Spliterator<Integer> front = sp.trySplit();
        check(front != null, "trySplit non-null");
        List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
        front.forEachRemaining(a::add);
        sp.forEachRemaining(b::add);
        a.addAll(b);
        check(a.equals(Arrays.asList(1, 2, 3, 4, 5)),
              "split halves concatenated in order reproduce original sequence");
    }

    static void testSerializationWithNull() throws Exception {
        LinkedHashSet<String> s = new LinkedHashSet<>(Arrays.asList("a", null, "b"));
        LinkedHashSet<String> r = roundTrip(s);
        check(r.equals(s), "round-trip equal with null element");
        check(new ArrayList<>(r).equals(Arrays.asList("a", null, "b")),
              "round-trip preserves order including null position");
        check(r.contains(null), "round-trip preserves null element");
        LinkedHashSet<String> empty = roundTrip(new LinkedHashSet<String>());
        check(empty.isEmpty() && empty.add("x"), "empty set round-trip usable");
    }

    public static void main(String[] args) throws Exception {
        testOrderAndDuplicates();
        testConstructorMatrix();
        testNullAllowance();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        testReinsertMovesToEnd();
        testIteratorWriteChannels();
        testFailFast();
        testToArrayOrder();
        testEqualsHashCodeDeep();
        testBatchOpReturnContracts();
        testStreamAndSpliterator();
        testSerializationWithNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
