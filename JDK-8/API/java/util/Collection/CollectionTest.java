/*
 * @test
 * @summary java.util.Collection API conformance: add/remove/contains/size/isEmpty/clear,
 *          addAll/containsAll/removeAll, toArray overloads, iteration, and the Java 8
 *          default methods forEach and removeIf.
 * @run main CollectionTest
 */

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;

public class CollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        Collection<String> c = new ArrayList<>();
        check(c.isEmpty() && c.size() == 0, "empty");
        check(c.add("a") && c.add("b"), "add returns true");
        check(c.size() == 2, "size");
        check(c.contains("a") && !c.contains("z"), "contains");
        check(c.remove("a"), "remove returns true");
        check(!c.remove("z"), "remove missing returns false");
        check(c.size() == 1, "size after remove");
        c.clear();
        check(c.isEmpty(), "clear");
    }

    static void testBulk() {
        Collection<String> c = new ArrayList<>(Arrays.asList("a"));
        Collection<String> d = new ArrayList<>(Arrays.asList("x", "y"));
        check(c.addAll(d), "addAll returns true");
        check(c.containsAll(d), "containsAll");
        check(c.removeAll(d), "removeAll returns true");
        check(!c.contains("x") && c.contains("a"), "removeAll removed the elements");
    }

    static void testToArray() {
        Collection<String> e = new ArrayList<>(Arrays.asList("p", "q"));
        Object[] arr = e.toArray();
        check(arr.length == 2 && "p".equals(arr[0]), "toArray()");
        String[] typed = e.toArray(new String[0]);
        check(typed.length == 2 && "q".equals(typed[1]), "toArray(T[])");
    }

    static void testIterationAndDefaults() {
        Collection<String> e = new ArrayList<>(Arrays.asList("p", "q"));
        Iterator<String> it = e.iterator();
        check(it.hasNext() && it.next().equals("p") && it.next().equals("q") && !it.hasNext(),
              "iterator");
        final List<String> seen = new ArrayList<>();
        e.forEach(seen::add);
        check(seen.equals(new ArrayList<>(Arrays.asList("p", "q"))), "forEach");
        e.removeIf(s -> s.equals("p"));
        check(e.equals(new ArrayList<>(Arrays.asList("q"))), "removeIf");
    }

    static void testStreamDefaults() {
        Collection<Integer> c = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        check(c.stream().filter(x -> x % 2 == 0).count() == 2, "stream filter");
        check(c.stream().mapToInt(Integer::intValue).sum() == 10, "stream mapToInt sum");
        check(c.parallelStream().sorted().mapToInt(Integer::intValue).sum() == 10,
              "parallelStream sum");
        check(c.spliterator().getExactSizeIfKnown() == 4, "spliterator exact size");
        check(c.stream().findFirst().get() == 1, "stream findFirst order");
    }

    static void testRemoveIfEdge() {
        Collection<Integer> c = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        check(c.removeIf(x -> x % 2 == 1), "removeIf returns true");
        check(c.equals(new ArrayList<>(Arrays.asList(2, 4, 6))), "removeIf removed odds");
        check(!c.removeIf(x -> x > 100), "removeIf returns false when none match");
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

    static void testEqualsContract() {
        List<String> al = new ArrayList<>(Arrays.asList("a", "b"));
        List<String> ll = new LinkedList<>(Arrays.asList("a", "b"));
        List<String> asList = Arrays.asList("a", "b");
        check(al.equals(ll), "ArrayList equals LinkedList with same content/order");
        check(al.hashCode() == ll.hashCode(), "equal collections equal hashCode");
        check(al.equals(asList), "ArrayList equals Arrays.asList view of same elements");
        check(!al.equals(new HashSet<>(Arrays.asList("a", "b"))),
              "List never equals Set even with identical elements");
        check(!al.equals(Arrays.asList("b", "a")), "List equals is order-sensitive");
        check(!al.equals(null), "equals(null) false");
        Collection<String> c = al;
        check(c.equals(c), "equals self");
        check(!al.equals(new ArrayList<>(Arrays.asList("a", "b", "c"))), "different size not equal");
    }

    static void testToArrayContract() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b"));
        String[] big = new String[4];
        String[] retBig = l.toArray(big);
        check(retBig == big, "toArray(T[]) reuses given array when large enough");
        check(big[2] == null && big[3] == null, "toArray(T[]) null-terminates at size position");
        check(big[0].equals("a") && big[1].equals("b"), "toArray(T[]) preserves encounter order");
        String[] exact = l.toArray(new String[2]);
        check(exact.length == 2 && exact[1].equals("b"), "toArray(T[]) exact size fills precisely");
        String[] fresh = l.toArray(new String[0]);
        check(fresh.length == 2 && fresh[0].equals("a"), "toArray(T[]) too small allocates new");
        check(new ArrayList<String>().toArray().length == 0, "empty collection toArray() length 0");
        expectThrows(NullPointerException.class, () -> l.toArray((String[]) null),
                "toArray(null array) NPE");
        @SuppressWarnings("rawtypes")
        Collection raw = new ArrayList<Number>(Arrays.asList(1, 2));
        expectThrows(ArrayStoreException.class, () -> raw.toArray(new String[5]),
                "toArray incompatible component type ASE");
    }

    static void testBulkReturnSemantics() {
        Collection<String> c = new ArrayList<>(Arrays.asList("a", "b"));
        check(!c.addAll(new ArrayList<String>()), "addAll(empty) returns false");
        check(c.size() == 2, "addAll(empty) leaves collection unchanged");
        check(!c.removeAll(Arrays.asList("x", "y")), "removeAll disjoint returns false");
        check(c.size() == 2, "removeAll disjoint leaves elements");
        check(!c.retainAll(Arrays.asList("a", "b", "c")), "retainAll superset returns false");
        check(c.containsAll(Arrays.asList("b", "a")), "containsAll order irrelevant");
        check(c.containsAll(new ArrayList<String>()), "containsAll empty true");
        check(c.retainAll(Arrays.asList("a")), "retainAll returns true when dropping");
        check(c.equals(Arrays.asList("a")), "retainAll kept exactly the intersection");
        Collection<String> d = new ArrayList<>(Arrays.asList("a"));
        check(d.contains(new String("a")), "contains matches by equals not identity");
        check(d.remove(new String("a")) && d.isEmpty(), "remove matches by equals not identity");
    }

    static void testNullAndExceptionSemantics() {
        Collection<String> c = new ArrayList<>(Arrays.asList("a"));
        expectThrows(NullPointerException.class, () -> c.forEach(null), "forEach(null) NPE");
        expectThrows(NullPointerException.class, () -> c.removeIf(null), "removeIf(null) NPE");
        expectThrows(NullPointerException.class, () -> c.addAll(null), "addAll(null) NPE");
        expectThrows(NullPointerException.class, () -> c.containsAll(null), "containsAll(null) NPE");
        final RuntimeException boom = new ArithmeticException("boom");
        Collection<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        try {
            nums.removeIf(x -> { if (x == 3) throw boom; return false; });
            throw new AssertionError("predicate exception must propagate");
        } catch (ArithmeticException e) {
            check(e == boom, "original exception object relayed unchanged");
        }
        // OpenJDK ArrayList computes removals after a complete pass, so a throwing
        // predicate leaves the list structurally unmodified (observed on 8u and 27).
        check(nums.equals(Arrays.asList(1, 2, 3, 4)), "throwing removeIf leaves list unchanged");
    }

    static void testSpliteratorContract() {
        List<Integer> l = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Spliterator<Integer> sp = l.spliterator();
        check(sp.hasCharacteristics(Spliterator.ORDERED), "ArrayList spliterator ORDERED");
        check(sp.hasCharacteristics(Spliterator.SIZED), "ArrayList spliterator SIZED");
        check(sp.hasCharacteristics(Spliterator.SUBSIZED), "ArrayList spliterator SUBSIZED");
        check(sp.getExactSizeIfKnown() == 8, "exact size known before traversal");
        final int[] first = {0};
        check(sp.tryAdvance(n -> first[0] = n) && first[0] == 1,
              "tryAdvance yields first element in encounter order");
        check(sp.getExactSizeIfKnown() == 7, "tryAdvance decrements remaining estimate");
        List<Integer> rest = new ArrayList<>();
        sp.forEachRemaining(rest::add);
        check(rest.equals(Arrays.asList(2, 3, 4, 5, 6, 7, 8)), "forEachRemaining takes the rest in order");
        check(!sp.tryAdvance(n -> first[0] = n), "exhausted tryAdvance returns false");
        Spliterator<Integer> spA = l.spliterator();
        Spliterator<Integer> front = spA.trySplit();
        check(front != null, "trySplit returns a prefix spliterator");
        List<Integer> all = new ArrayList<>();
        front.forEachRemaining(all::add);
        spA.forEachRemaining(all::add);
        check(all.equals(l), "prefix+suffix of split equals original in order");
        Spliterator<Integer> hsp = new HashSet<>(Arrays.asList(1, 2)).spliterator();
        check(hsp.hasCharacteristics(Spliterator.DISTINCT), "HashSet spliterator DISTINCT");
        check(hsp.getExactSizeIfKnown() == 2, "HashSet spliterator exact size before split");
    }

    /**
     * Recording minimal collection: only iterator() and size(); add() keeps the
     * AbstractCollection default (UOE). Proves the default-method dispatch contract.
     */
    static class ProbeCollection extends AbstractCollection<String> {
        final List<String> data = new ArrayList<>();
        int iteratorCreated;
        int iteratorRemoves;
        ProbeCollection(String... xs) { data.addAll(Arrays.asList(xs)); }
        public Iterator<String> iterator() {
            iteratorCreated++;
            return new Iterator<String>() {
                int i; boolean removable;
                public boolean hasNext() { return i < data.size(); }
                public String next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    removable = true;
                    return data.get(i++);
                }
                public void remove() {
                    if (!removable) throw new IllegalStateException();
                    data.remove(--i);
                    removable = false;
                    iteratorRemoves++;
                }
            };
        }
        public int size() { return data.size(); }
    }

    static void testDefaultDispatchProbe() {
        ProbeCollection p = new ProbeCollection("a", "b", "c");
        check(p.contains("b"), "default contains finds element by scanning");
        check(p.iteratorCreated >= 1, "default contains goes through iterator()");
        check(!p.contains("z"), "default contains miss scans whole collection");
        check(p.containsAll(Arrays.asList("a", "c")), "default containsAll true");
        check(!p.containsAll(Arrays.asList("a", "zz")), "default containsAll false");
        check(p.remove("a") && p.data.equals(Arrays.asList("b", "c")),
              "default remove(Object) removes via iterator.remove");
        check(p.iteratorRemoves == 1, "iterator.remove actually invoked once");
        check(!p.remove("zz") && p.iteratorRemoves == 1,
              "remove miss does not invoke iterator.remove");
        check(p.removeIf(x -> x.equals("b")), "default removeIf removes matching");
        check(p.iteratorRemoves == 2, "removeIf removals go through iterator.remove");
        check(p.data.equals(Arrays.asList("c")), "removeIf left exactly non-matching");
        p.clear();
        check(p.data.isEmpty() && p.size() == 0, "default clear empties collection");
        check(p.iteratorRemoves == 3, "clear removed each element via iterator.remove");
        ProbeCollection q = new ProbeCollection("x", null);
        check(q.contains(null), "default contains(null) uses == branch");
        check(q.toString().equals("[x, null]"), "default toString renders null element");
        expectThrows(UnsupportedOperationException.class, () -> q.add("y"),
                "AbstractCollection default add UOE");
        ProbeCollection r1 = new ProbeCollection("a");
        ProbeCollection r2 = new ProbeCollection("a");
        check(!r1.equals(r2), "plain AbstractCollection keeps Object identity equals");
        check(r1.equals(r1), "identity equals holds for self");
    }

    public static void main(String[] args) {
        testBasics();
        testBulk();
        testToArray();
        testIterationAndDefaults();
        testStreamDefaults();
        testRemoveIfEdge();
        testEqualsContract();
        testToArrayContract();
        testBulkReturnSemantics();
        testNullAndExceptionSemantics();
        testSpliteratorContract();
        testDefaultDispatchProbe();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
