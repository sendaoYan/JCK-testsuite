/*
 * @test
 * @summary java.util.HashSet API conformance: full contract coverage.
 *          add (false on duplicate, incl. null), contains/remove/size/isEmpty/clear,
 *          constructor matrix (negative capacity/NaN load factor/null collection),
 *          null element semantics, iterator write channels (iterator.remove/removeAll/
 *          retainAll/clear/removeIf), fail-fast (CME/ISE/NSEE), toArray contract,
 *          cross-implementation equals/hashCode (vs TreeSet), clone (shallow +
 *          structural independence), serialization round-trip + serialVersionUID,
 *          spliterator characteristics, and state invariants.
 * @run main HashSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeSet;

public class HashSetTest {
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
    static <E> HashSet<E> roundTrip(HashSet<E> set) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(set); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (HashSet<E>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testAddContains() {
        HashSet<String> set = new HashSet<>();
        check(set.isEmpty() && set.size() == 0, "empty");
        check(set.add("a"), "add new returns true");
        check(set.add("b"), "add another");
        check(set.add(null), "add null");
        check(!set.add("a"), "add duplicate returns false");
        check(set.size() == 3, "size counts unique + null");
        check(set.contains("a") && set.contains("b") && set.contains(null), "contains");
        check(!set.contains("z"), "!contains");
        check(set.remove("a"), "remove returns true");
        check(!set.remove("a"), "remove absent returns false");
        check(set.remove(null), "remove null");
        check(!set.contains("a") && !set.contains(null), "removed absent");
        set.clear();
        check(set.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        HashSet<String> a = new HashSet<>(Arrays.asList("x", "y"));
        HashSet<String> b = new HashSet<>(Arrays.asList("y", "x"));
        check(a.equals(b) && b.equals(a), "equals order-independent");
        check(a.hashCode() == b.hashCode(), "equal sets equal hashCode");
        b.add("z");
        check(!a.equals(b), "!equals different content");
    }

    // ---- constructor matrix + exception paths -------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(-1), "HashSet(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(-1, 0.75f), "HashSet(-1,lf) IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, 0f), "HashSet loadFactor 0 IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, -0.5f), "HashSet negative loadFactor IAE");
        checkThrows(IllegalArgumentException.class, () -> new HashSet<>(1, Float.NaN), "HashSet NaN loadFactor IAE");
        checkThrows(NullPointerException.class, () -> new HashSet<>((Collection<String>) null), "HashSet(null Collection) NPE");

        HashSet<String> zero = new HashSet<>(0);
        zero.add("a");
        check(zero.contains("a"), "HashSet(0) usable");
        HashSet<String> sized = new HashSet<>(16, 0.5f);
        sized.add("x");
        check(sized.contains("x"), "HashSet(int,float) usable");
    }

    static void testNullSemantics() {
        HashSet<String> s = new HashSet<>();
        check(s.add(null), "add null returns true");
        check(!s.add(null), "add null duplicate returns false");
        check(s.contains(null), "contains null");
        check(s.remove(null), "remove null returns true");
        check(!s.remove(null), "remove absent null returns false");
    }

    // ---- iterator write channels + fail-fast --------------------------------

    static void testIteratorWriteChannels() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Iterator<Integer> it = s.iterator();
        check(it.hasNext(), "iterator has next");
        Integer first = it.next();
        it.remove();
        check(!s.contains(first), "iterator.remove removes element");
        checkThrows(IllegalStateException.class, it::remove, "remove before next ISE");

        Iterator<Integer> it2 = s.iterator();
        checkThrows(NoSuchElementException.class, () -> { while (it2.hasNext()) it2.next(); it2.next(); },
                    "exhausted iterator next NSEE");

        HashSet<Integer> r = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        r.removeAll(Arrays.asList(2, 4));
        check(r.size() == 3 && !r.contains(2) && !r.contains(4), "removeAll");
        r.retainAll(Arrays.asList(1, 3));
        check(r.size() == 2 && r.contains(1) && r.contains(3), "retainAll");
        check(r.removeIf(x -> x == 3), "removeIf returns true");
        check(!r.contains(3), "removeIf removed");
        r.clear();
        check(r.isEmpty(), "clear");
    }

    static void testFailFast() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = s.iterator();
        it.next();
        s.add(99);
        checkThrows(java.util.ConcurrentModificationException.class, it::next, "iterator CME after add");

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.remove(99);
        checkThrows(java.util.ConcurrentModificationException.class, it2::next, "iterator CME after remove");

        Iterator<Integer> it3 = s.iterator();
        it3.next();
        s.clear();
        checkThrows(java.util.ConcurrentModificationException.class, it3::next, "iterator CME after clear");
    }

    // ---- toArray contract ---------------------------------------------------

    static void testToArrayContract() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b"));
        Object[] oa = s.toArray();
        check(oa.length == 2, "toArray() length");
        List<String> sortedOa = new ArrayList<>();
        for (Object o : oa) sortedOa.add((String) o);
        Collections.sort(sortedOa);
        check(sortedOa.equals(Arrays.asList("a", "b")), "toArray() multiset");

        String[] exact = s.toArray(new String[0]);
        check(exact.length == 2, "toArray(new T[0]) exact length");

        String[] reusable = new String[]{"X", "X", "X"};
        String[] returned = s.toArray(reusable);
        check(returned == reusable, "toArray(T[]) reuses array when it fits");
        check(reusable[2] == null, "toArray(T[]) pads trailing null");
        List<String> sortedReuse = new ArrayList<>(Arrays.asList(reusable[0], reusable[1]));
        Collections.sort(sortedReuse);
        check(sortedReuse.equals(Arrays.asList("a", "b")), "toArray(T[]) content multiset");
    }

    // ---- cross-implementation equals/hashCode -------------------------------

    static void testCrossImplEquals() {
        HashSet<Integer> hs = new HashSet<>(Arrays.asList(1, 2, 3));
        TreeSet<Integer> ts = new TreeSet<>(Arrays.asList(3, 1, 2));
        check(hs.equals(ts) && ts.equals(hs), "HashSet equals TreeSet symmetric");
        check(hs.hashCode() == ts.hashCode(), "equal sets equal hashCode");
        check(!hs.equals(new HashSet<>(Arrays.asList(1, 2))), "!equals different size");
    }

    // ---- clone / serialization / spliterator / state invariants -------------

    static void testClone() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b"));
        @SuppressWarnings("unchecked")
        HashSet<String> clone = (HashSet<String>) s.clone();
        check(clone != s, "clone distinct object");
        check(clone.equals(s) && clone.hashCode() == s.hashCode(), "clone equals original");
        clone.add("c");
        check(s.size() == 2 && !s.contains("c"), "clone structural independence");
    }

    static void testSerialization() throws Exception {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", null, "b"));
        HashSet<String> r = roundTrip(s);
        check(r != s, "round-trip distinct object");
        check(r.equals(s), "round-trip content equal");
        check(r.contains(null), "round-trip preserves null element");
        r.add("c");
        check(r.size() == 4 && s.size() == 3, "round-trip result usable and independent");

        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(HashSet.class);
        check(osc.getSerialVersionUID() == -5024744406713321676L, "HashSet serialVersionUID stable");
    }

    static void testSpliterator() {
        HashSet<String> s = new HashSet<>(Arrays.asList("a", "b", "c"));
        Spliterator<String> sp = s.spliterator();
        check(sp.estimateSize() == 3, "spliterator estimateSize");
        check(sp.hasCharacteristics(Spliterator.SIZED | Spliterator.DISTINCT), "spliterator SIZED|DISTINCT");
        List<String> gathered = new ArrayList<>();
        sp.forEachRemaining(gathered::add);
        Collections.sort(gathered);
        check(gathered.equals(Arrays.asList("a", "b", "c")), "spliterator covers all (sorted)");
    }

    static void testStateInvariants() {
        HashSet<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        s.remove(3);
        check(s.size() == 4 && !s.contains(3), "remove updates size");
        check(s.contains(1) && s.contains(2) && s.contains(4) && s.contains(5), "others survive remove");
        check(!s.remove(99), "failed remove leaves state intact");
        check(s.size() == 4, "state intact after failed op");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        for (int i = 1; i <= 5; i++) check(!s.contains(i), "clear removed " + i);
    }

    public static void main(String[] args) throws Exception {
        testAddContains();
        testEqualsHashCode();
        testConstructorMatrix();
        testNullSemantics();
        testIteratorWriteChannels();
        testFailFast();
        testToArrayContract();
        testCrossImplEquals();
        testClone();
        testSerialization();
        testSpliterator();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
