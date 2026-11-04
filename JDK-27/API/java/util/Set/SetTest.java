/*
 * @test
 * @summary java.util.Set conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main SetTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SetTest {
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
        Class<?> c = Class.forName("java.util.Set");
        check(c.isInterface(), "is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "extends Collection");
        c.getDeclaredMethod("of");
        c.getDeclaredMethod("copyOf", java.util.Collection.class);
        c.getDeclaredMethod("ofLazy", Set.class, java.util.function.Predicate.class);
        check(Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()),
                "add abstract");
    }

    static void testBlackBox() {
        check(Set.of().isEmpty(), "of() empty");
        check(Set.of("a", "b").size() == 2, "of varargs");
        expectThrows(() -> Set.of("a", "a"), IllegalArgumentException.class, "duplicate element");
        expectThrows(() -> Set.of("a", (String) null), NullPointerException.class, "null element");

        Set<String> cp = Set.copyOf(new HashSet<>(java.util.List.of("x", "y")));
        check(cp.equals(Set.of("x", "y")), "copyOf");

        Set<Integer> lazy = Set.ofLazy(Set.of(0, 1, 2, 3), i -> i % 2 == 0);
        check(lazy.equals(Set.of(0, 2)), "ofLazy filter");
    }

    static void testBasicContract() {
        Set<String> s = new HashSet<>();
        check(s.isEmpty(), "new set empty");
        check(s.add("a") && !s.add("a"), "add true then false for duplicate");
        check(s.size() == 1, "size excludes duplicate");
        check(s.contains("a") && !s.contains("z"), "contains");
        check(s.remove("a") && s.isEmpty(), "remove true and empties");

        Set<String> u = new HashSet<>(List.of("a", "b"));
        check(u.addAll(new HashSet<>(List.of("b", "c"))), "addAll returns true");
        check(u.equals(new HashSet<>(List.of("a", "b", "c"))), "addAll union");
        check(!u.addAll(new HashSet<>(List.of("a"))), "addAll all-present returns false");
        check(u.containsAll(List.of("a", "c")), "containsAll");
        check(u.removeAll(List.of("a", "b")), "removeAll returns true");
        check(u.equals(new HashSet<>(List.of("c"))), "removeAll difference");

        Set<String> r = new HashSet<>(List.of("a", "b", "c"));
        check(r.retainAll(List.of("a", "c")), "retainAll returns true");
        check(r.equals(new HashSet<>(List.of("a", "c"))), "retainAll intersection");

        Iterator<String> it = u.iterator();
        check("c".equals(it.next()) && !it.hasNext(), "iterator covers all");
        check(u.toArray().length == 1, "toArray length");
        u.clear();
        check(u.isEmpty(), "clear empties");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void testImmutableFactories() {
        Set<String> empty = Set.of();
        check(empty.isEmpty() && empty.size() == 0, "Set.of() empty");
        check(!empty.iterator().hasNext(), "Set.of() has no elements to iterate");
        check(empty.equals(new HashSet<String>()), "Set.of() equals any empty set");
        Set<String> one = Set.of("a");
        check(one.size() == 1 && one.contains("a"), "Set.of(E) single element");
        check(one.equals(new HashSet<>(List.of("a"))), "Set.of equals HashSet with same element");
        check(one.hashCode() == 97, "Set.of hashCode follows sum formula");
        // immutability: every mutating channel throws UOE
        Set<String> s = Set.of("a", "b");
        expectThrows(() -> s.add("x"), UnsupportedOperationException.class, "of add UOE");
        expectThrows(() -> s.remove("a"), UnsupportedOperationException.class, "of remove UOE");
        expectThrows(() -> s.addAll(List.of("x")), UnsupportedOperationException.class, "of addAll UOE");
        expectThrows(() -> s.removeAll(List.of("a")), UnsupportedOperationException.class, "of removeAll UOE");
        expectThrows(() -> s.retainAll(List.of("a")), UnsupportedOperationException.class, "of retainAll UOE");
        expectThrows(s::clear, UnsupportedOperationException.class, "of clear UOE");
        Iterator<String> it = one.iterator();
        it.next();
        expectThrows(it::remove, UnsupportedOperationException.class, "of iterator.remove UOE");
        // nulls and duplicates rejected at factory time
        expectThrows(() -> Set.of("a", (String) null), NullPointerException.class, "of null element NPE");
        expectThrows(() -> Set.of((Object[]) null), NullPointerException.class, "of null varargs array NPE");
        expectThrows(() -> Set.copyOf(null), NullPointerException.class, "copyOf(null) NPE");
        // copyOf: content equal, insulated from later source mutation, itself immutable
        HashSet<String> src = new HashSet<>(List.of("m", "n"));
        Set<String> cp = Set.copyOf(src);
        check(cp.equals(Set.of("m", "n")), "copyOf copies content");
        src.add("later");
        check(cp.size() == 2 && !cp.contains("later"), "copyOf insulated from later source mutation");
        expectThrows(() -> cp.add("z"), UnsupportedOperationException.class, "copyOf result immutable");
        check(Set.copyOf(cp).equals(cp), "copyOf of a copy equals it");
        check(cp.contains(new String("m")), "copyOf membership via equals");
    }

    static void testEqualsHashCodeContract() {
        Set<String> hs = new HashSet<>(List.of("a", "b", "c"));
        Set<String> lhs = new LinkedHashSet<>(List.of("c", "a", "b"));
        Set<String> ts = new TreeSet<>(List.of("b", "c", "a"));
        check(hs.equals(lhs) && lhs.equals(hs), "HashSet equals LinkedHashSet");
        check(hs.equals(ts) && ts.equals(hs), "HashSet equals TreeSet");
        check(lhs.equals(ts), "LinkedHashSet equals TreeSet");
        check(hs.hashCode() == lhs.hashCode() && hs.hashCode() == ts.hashCode(),
              "equal sets share hashCode across implementations");
        check(hs.hashCode() == (97 + 98 + 99), "Set.hashCode is sum of element hashCodes");
        check(new HashSet<String>().hashCode() == 0, "empty set hashCode is 0");
        check(new HashSet<>(List.of(1, 2, 3)).hashCode() == 6, "Integer set locks sum formula");
        Set<String> n1 = new HashSet<>(Arrays.asList(null, "a"));
        Set<String> n2 = new LinkedHashSet<>(Arrays.asList("a", null));
        check(n1.equals(n2), "sets with null element equal across impls");
        check(n1.hashCode() == 97, "null element contributes 0 to hashCode sum");
        check(!hs.equals(List.of("a", "b", "c")), "Set !equals List");
        check(!hs.equals(null), "equals(null) false");
        check(hs.equals(hs), "equals self");
        check(!hs.equals(new HashSet<>(List.of("a", "b"))), "different size not equal");
        check(Set.of("a", "b").equals(new HashSet<>(List.of("b", "a"))),
              "Set.of participates in Set equality contract");
    }

    static void testNullElementSemantics() {
        Set<String> s = new HashSet<>();
        check(s.add(null), "HashSet accepts null element");
        check(s.contains(null), "contains(null) true");
        check(!s.add(null), "second add(null) returns false");
        check(s.remove(null) && s.isEmpty(), "remove(null) works");
        Set<Integer> ts = new TreeSet<>();
        expectThrows(() -> ts.add(null), NullPointerException.class,
                "TreeSet natural order rejects null (NPE)");
    }

    static void testToArrayContract() {
        Set<String> s = new LinkedHashSet<>(List.of("a", "b"));
        String[] big = new String[4];
        String[] retBig = s.toArray(big);
        check(retBig == big, "toArray(T[]) reuses given array when large enough");
        check(big[2] == null, "toArray(T[]) null-terminates at size position");
        check(big[0].equals("a") && big[1].equals("b"), "toArray(T[]) follows iteration order");
        String[] exact = s.toArray(new String[2]);
        check(exact.length == 2 && exact[1].equals("b"), "toArray(T[]) exact size");
        String[] fresh = s.toArray(new String[0]);
        check(fresh.length == 2, "toArray(T[]) too small allocates new");
        expectThrows(() -> s.toArray((String[]) null), NullPointerException.class, "toArray(null) NPE");
        check(new HashSet<String>().toArray().length == 0, "empty set toArray() length 0");
    }

    static void testBulkEdgeSemantics() {
        Set<String> s = new HashSet<>(List.of("a", "b"));
        check(!s.addAll(new HashSet<String>()), "addAll(empty) false");
        check(!s.removeAll(new HashSet<String>()), "removeAll(empty) false");
        check(!s.removeAll(List.of("x", "y")), "removeAll disjoint false");
        check(!s.retainAll(List.of("a", "b")), "retainAll no-change false");
        check(s.removeAll(List.of("a", "zz")), "removeAll partial overlap true");
        check(s.equals(new HashSet<>(List.of("b"))), "only overlapping removed");
        Set<String> t = new HashSet<>(List.of("a", "b"));
        check(t.retainAll(List.of("b")), "retainAll drops others");
        check(t.equals(new HashSet<>(List.of("b"))), "intersection kept");
        t.clear();
        check(t.isEmpty() && t.size() == 0, "clear empties");
        Set<String> u = new HashSet<>(List.of("a"));
        check(u.contains(new String("a")), "contains uses equals not identity");
        check(u.remove(new String("a")) && u.isEmpty(), "remove uses equals not identity");
    }

    static void testIteratorWriteChannel() {
        Set<String> s = new LinkedHashSet<>(List.of("a", "b", "c"));
        Iterator<String> it = s.iterator();
        it.next();
        it.remove();
        check(s.size() == 2 && !s.contains("a"), "iterator.remove removes last returned");
        it.next();
        it.remove();
        expectThrows(it::remove, IllegalStateException.class, "second consecutive remove ISE");
        check(s.size() == 1 && s.contains("c"), "second remove rejected without effect");
        while (it.hasNext()) it.next();
        check(!it.hasNext(), "iterator exhausted");
        expectThrows(it::next, java.util.NoSuchElementException.class, "next after exhaustion NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBasicContract();
        testImmutableFactories();
        testEqualsHashCodeContract();
        testNullElementSemantics();
        testToArrayContract();
        testBulkEdgeSemantics();
        testIteratorWriteChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
