/*
 * @test
 * @summary java.util.Set API conformance: add returns false for duplicates, no duplicate
 *          elements, contains/remove, addAll (union), removeAll/retainAll (difference/
 *          intersection), containsAll, iterator, toArray, and clear.
 * @run main SetTest
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        Set<String> s = new HashSet<>();
        check(s.isEmpty(), "new set is empty");
        check(s.add("a"), "add returns true");
        check(!s.add("a"), "add duplicate returns false");
        check(s.size() == 1, "size excludes duplicates");
        check(s.contains("a") && !s.contains("z"), "contains");
        check(s.remove("a") && s.isEmpty(), "remove returns true and empties");
        check(!s.remove("a"), "remove missing returns false");
    }

    static void testBulk() {
        Set<String> s = new HashSet<>(Arrays.asList("a", "b"));
        Set<String> other = new HashSet<>(Arrays.asList("b", "c"));
        check(s.addAll(other), "addAll returns true when adding");
        check(s.equals(new HashSet<>(Arrays.asList("a", "b", "c"))), "addAll union");
        check(!s.addAll(new HashSet<>(Arrays.asList("a"))), "addAll all-present returns false");
        check(s.containsAll(Arrays.asList("a", "c")), "containsAll");
        check(s.removeAll(Arrays.asList("a", "b")), "removeAll returns true");
        check(s.equals(new HashSet<>(Arrays.asList("c"))), "removeAll difference");
        Set<String> r = new HashSet<>(Arrays.asList("a", "b", "c"));
        check(r.retainAll(Arrays.asList("a", "c")), "retainAll returns true");
        check(r.equals(new HashSet<>(Arrays.asList("a", "c"))), "retainAll intersection");
    }

    static void testIteration() {
        Set<String> s = new HashSet<>(Arrays.asList("a", "b"));
        Iterator<String> it = s.iterator();
        int n = 0;
        while (it.hasNext()) {
            it.next();
            n++;
        }
        check(n == 2, "iterator covers all elements");
        Object[] arr = s.toArray();
        check(arr.length == 2, "toArray length");
        String[] typed = s.toArray(new String[0]);
        check(typed.length == 2, "toArray(T[]) length");
        s.clear();
        check(s.isEmpty(), "clear empties");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

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

    static void testEqualsHashCodeContract() {
        // cross-implementation equality: same elements -> equal sets, equal hashCode
        Set<String> hs = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> lhs = new LinkedHashSet<>(Arrays.asList("c", "a", "b"));
        Set<String> ts = new TreeSet<>(Arrays.asList("b", "c", "a"));
        check(hs.equals(lhs) && lhs.equals(hs), "HashSet equals LinkedHashSet");
        check(hs.equals(ts) && ts.equals(hs), "HashSet equals TreeSet");
        check(lhs.equals(ts), "LinkedHashSet equals TreeSet");
        check(hs.hashCode() == lhs.hashCode() && hs.hashCode() == ts.hashCode(),
              "equal sets share hashCode across implementations");
        // locked formula: sum of element hashCodes ("a"=97, "b"=98)
        check(hs.hashCode() == (97 + 98 + 99), "Set.hashCode is sum of element hashCodes");
        check(new HashSet<String>().hashCode() == 0, "empty set hashCode is 0");
        // Integer.hashCode() == the value itself
        check(new HashSet<>(Arrays.asList(1, 2, 3)).hashCode() == 6,
              "Integer set hashCode locks sum formula");
        Set<String> n1 = new HashSet<>(Arrays.asList(null, "a"));
        Set<String> n2 = new LinkedHashSet<>(Arrays.asList("a", null));
        check(n1.equals(n2), "sets with null element equal across impls");
        check(n1.hashCode() == 97, "null element contributes 0 to hashCode sum");
        // Set is never equal to a List of the same elements
        check(!hs.equals(Arrays.asList("a", "b", "c")), "Set !equals List");
        check(!hs.equals(null), "equals(null) false");
        check(hs.equals(hs), "equals self");
        check(!hs.equals(new HashSet<>(Arrays.asList("a", "b"))), "different size not equal");
    }

    static void testNullElementSemantics() {
        Set<String> s = new HashSet<>();
        check(s.add(null), "HashSet accepts null element");
        check(s.contains(null), "contains(null) true");
        check(s.size() == 1, "null counts as one element");
        check(!s.add(null), "second add(null) returns false");
        check(s.remove(null) && s.isEmpty(), "remove(null) works");
        Set<Integer> ts = new TreeSet<>();
        expectThrows(NullPointerException.class, () -> ts.add(null),
                "TreeSet natural order rejects null (NPE)");
    }

    static void testToArrayContract() {
        Set<String> s = new LinkedHashSet<>(Arrays.asList("a", "b"));
        String[] big = new String[4];
        String[] retBig = s.toArray(big);
        check(retBig == big, "toArray(T[]) reuses given array when large enough");
        check(big[2] == null, "toArray(T[]) null-terminates at size position");
        check(big[0].equals("a") && big[1].equals("b"), "toArray(T[]) follows iteration order");
        String[] exact = s.toArray(new String[2]);
        check(exact.length == 2 && exact[1].equals("b"), "toArray(T[]) exact size");
        String[] fresh = s.toArray(new String[0]);
        check(fresh.length == 2, "toArray(T[]) too small allocates new");
        expectThrows(NullPointerException.class, () -> s.toArray((String[]) null),
                "toArray(null) NPE");
        check(new HashSet<String>().toArray().length == 0, "empty set toArray() length 0");
    }

    static void testBulkEdgeSemantics() {
        Set<String> s = new HashSet<>(Arrays.asList("a", "b"));
        check(!s.addAll(new HashSet<String>()), "addAll(empty) false");
        check(!s.removeAll(new HashSet<String>()), "removeAll(empty) false");
        check(!s.removeAll(Arrays.asList("x", "y")), "removeAll disjoint false");
        check(!s.retainAll(Arrays.asList("a", "b")), "retainAll no-change false");
        check(s.removeAll(Arrays.asList("a", "zz")), "removeAll partial overlap true");
        check(s.equals(new HashSet<>(Arrays.asList("b"))), "only overlapping removed");
        Set<String> t = new HashSet<>(Arrays.asList("a", "b"));
        check(t.retainAll(Arrays.asList("b")), "retainAll drops others");
        check(t.equals(new HashSet<>(Arrays.asList("b"))), "intersection kept");
        t.clear();
        check(t.isEmpty() && t.size() == 0, "clear empties");
        // equals-based matching across distinct String instances
        Set<String> u = new HashSet<>(Arrays.asList("a"));
        check(u.contains(new String("a")), "contains uses equals not identity");
        check(u.remove(new String("a")) && u.isEmpty(), "remove uses equals not identity");
    }

    static void testIteratorWriteChannel() {
        Set<String> s = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = s.iterator();
        it.next();
        it.remove();
        check(s.size() == 2 && !s.contains("a"), "iterator.remove removes last returned");
        it.next();
        it.remove();
        try {
            it.remove();
            throw new AssertionError("double remove: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(s.size() == 1 && s.contains("c"), "second remove was rejected without effect");
        // iterate the rest then exhaust
        while (it.hasNext()) it.next();
        check(!it.hasNext(), "iterator exhausted with elements consumed");
        expectThrows(java.util.NoSuchElementException.class, it::next,
                "next after exhaustion NSEE");
    }

    public static void main(String[] args) {
        testBasics();
        testBulk();
        testIteration();
        testEqualsHashCodeContract();
        testNullElementSemantics();
        testToArrayContract();
        testBulkEdgeSemantics();
        testIteratorWriteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
