/*
 * @test
 * @summary java.util.Collection conformance: white-box structure + black-box behavior.
 * @run main CollectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CollectionTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Collection");
        check(c.isInterface(), "is an interface");
        check(Iterable.class.isAssignableFrom(c), "extends Iterable");
        c.getDeclaredMethod("size");
        c.getDeclaredMethod("isEmpty");
        c.getDeclaredMethod("contains", Object.class);
        c.getDeclaredMethod("iterator");
        c.getDeclaredMethod("toArray");
        c.getDeclaredMethod("toArray", Object[].class);
        c.getDeclaredMethod("add", Object.class);
        c.getDeclaredMethod("remove", Object.class);
        c.getDeclaredMethod("containsAll", Collection.class);
        c.getDeclaredMethod("addAll", Collection.class);
        c.getDeclaredMethod("removeAll", Collection.class);
        c.getDeclaredMethod("retainAll", Collection.class);
        c.getDeclaredMethod("clear");
        c.getDeclaredMethod("equals", Object.class);
        c.getDeclaredMethod("hashCode");
        check(true, "declares core members");
        check(Modifier.isAbstract(c.getDeclaredMethod("size").getModifiers()), "size abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("iterator").getModifiers()), "iterator abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("add", Object.class).getModifiers()), "add abstract");
        // default methods
        check(!Modifier.isAbstract(c.getDeclaredMethod("removeIf", Predicate.class).getModifiers()),
                "removeIf default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("spliterator").getModifiers()), "spliterator default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("stream").getModifiers()), "stream default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("parallelStream").getModifiers()), "parallelStream default");
        check(!Modifier.isAbstract(c.getDeclaredMethod("toArray",
                java.util.function.IntFunction.class).getModifiers()), "toArray(IntFunction) default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. basics
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

        // 2. bulk
        Collection<String> c2 = new ArrayList<>(Arrays.asList("a"));
        Collection<String> d = new ArrayList<>(Arrays.asList("x", "y"));
        check(c2.addAll(d), "addAll returns true");
        check(c2.containsAll(d), "containsAll");
        check(c2.removeAll(d), "removeAll returns true");
        check(!c2.contains("x") && c2.contains("a"), "removeAll removed the elements");

        // 3. toArray overloads
        Collection<String> e = new ArrayList<>(Arrays.asList("p", "q"));
        Object[] arr = e.toArray();
        check(arr.length == 2 && "p".equals(arr[0]), "toArray()");
        String[] typed = e.toArray(new String[0]);
        check(typed.length == 2 && "q".equals(typed[1]), "toArray(T[])");
        String[] viaIntFn = e.toArray(String[]::new);
        check(viaIntFn.length == 2 && "p".equals(viaIntFn[0]), "toArray(IntFunction)");

        // 4. iteration + defaults
        Collection<String> e2 = new ArrayList<>(Arrays.asList("p", "q"));
        Iterator<String> it = e2.iterator();
        check(it.hasNext() && it.next().equals("p") && it.next().equals("q") && !it.hasNext(),
                "iterator");
        final List<String> seen = new ArrayList<>();
        e2.forEach(seen::add);
        check(seen.equals(new ArrayList<>(Arrays.asList("p", "q"))), "forEach");
        e2.removeIf(s -> s.equals("p"));
        check(e2.equals(new ArrayList<>(Arrays.asList("q"))), "removeIf");

        // 5. stream/parallelStream
        Collection<Integer> nums = new ArrayList<>(Arrays.asList(1, 2, 3));
        Stream<Integer> st = nums.stream();
        check(st.mapToInt(Integer::intValue).sum() == 6, "stream");
        check(nums.parallelStream().mapToInt(Integer::intValue).sum() == 6, "parallelStream");

        // 6. spliterator
        Spliterator<Integer> sp = nums.spliterator();
        check(sp.estimateSize() == 3 && sp.getExactSizeIfKnown() == 3, "spliterator sized");
        final int[] sum = {0};
        sp.forEachRemaining(n -> sum[0] += n);
        check(sum[0] == 6, "spliterator forEachRemaining");
    }

    // ---------- contract deepening (appended; nothing above altered) ----------

    static void testEqualsContract() {
        List<String> al = new ArrayList<>(List.of("a", "b"));
        List<String> ll = new LinkedList<>(List.of("a", "b"));
        check(al.equals(ll), "ArrayList equals LinkedList with same content/order");
        check(al.hashCode() == ll.hashCode(), "equal collections equal hashCode");
        check(al.equals(Arrays.asList("a", "b")), "ArrayList equals Arrays.asList view");
        check(!al.equals(new HashSet<>(List.of("a", "b"))), "List never equals Set");
        check(!al.equals(List.of("b", "a")), "List equals is order-sensitive");
        check(!al.equals(null), "equals(null) false");
        Collection<String> c = al;
        check(c.equals(c), "equals self");
        check(!al.equals(List.of("a", "b", "c")), "different size not equal");
    }

    static void testToArrayContract() {
        List<String> l = new ArrayList<>(List.of("a", "b"));
        String[] big = new String[4];
        String[] retBig = l.toArray(big);
        check(retBig == big, "toArray(T[]) reuses given array when large enough");
        check(big[2] == null && big[3] == null, "toArray(T[]) null-terminates at size position");
        check(big[0].equals("a") && big[1].equals("b"), "toArray(T[]) preserves encounter order");
        String[] exact = l.toArray(new String[2]);
        check(exact.length == 2 && exact[1].equals("b"), "toArray(T[]) exact size fills precisely");
        String[] fresh = l.toArray(new String[0]);
        check(fresh.length == 2 && fresh[0].equals("a"), "toArray(T[]) too small allocates new");
        String[] viaFn = l.toArray(String[]::new);
        check(viaFn.length == 2 && viaFn[1].equals("b"), "toArray(IntFunction) sized to fit");
        expectThrows(() -> l.toArray((String[]) null), NullPointerException.class, "toArray(null array) NPE");
        expectThrows(() -> l.toArray((java.util.function.IntFunction<String[]>) null),
                NullPointerException.class, "toArray(IntFunction null) NPE");
        @SuppressWarnings("rawtypes")
        Collection raw = new ArrayList<Number>(Arrays.asList(1, 2));
        expectThrows(() -> raw.toArray(new String[5]), ArrayStoreException.class,
                "toArray incompatible component type ASE");
        check(new ArrayList<String>().toArray().length == 0, "empty collection toArray() length 0");
    }

    static void testBulkReturnSemantics() {
        Collection<String> c = new ArrayList<>(List.of("a", "b"));
        check(!c.addAll(new ArrayList<String>()), "addAll(empty) returns false");
        check(c.size() == 2, "addAll(empty) leaves collection unchanged");
        check(!c.removeAll(List.of("x", "y")), "removeAll disjoint returns false");
        check(c.size() == 2, "removeAll disjoint leaves elements");
        check(!c.retainAll(List.of("a", "b", "c")), "retainAll superset returns false");
        check(c.containsAll(List.of("b", "a")), "containsAll order irrelevant");
        check(c.containsAll(List.of()), "containsAll empty true");
        check(c.retainAll(List.of("a")), "retainAll returns true when dropping");
        check(c.equals(List.of("a")), "retainAll kept exactly the intersection");
        Collection<String> d = new ArrayList<>(List.of("a"));
        check(d.contains(new String("a")), "contains matches by equals not identity");
        check(d.remove(new String("a")) && d.isEmpty(), "remove matches by equals not identity");
    }

    static void testNullAndExceptionSemantics() {
        Collection<String> c = new ArrayList<>(List.of("a"));
        expectThrows(() -> c.forEach(null), NullPointerException.class, "forEach(null) NPE");
        expectThrows(() -> c.removeIf(null), NullPointerException.class, "removeIf(null) NPE");
        expectThrows(() -> c.addAll(null), NullPointerException.class, "addAll(null) NPE");
        expectThrows(() -> c.containsAll(null), NullPointerException.class, "containsAll(null) NPE");
        final RuntimeException boom = new ArithmeticException("boom");
        Collection<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            nums.removeIf(x -> { if (x == 3) throw boom; return false; });
            throw new AssertionError("predicate exception must propagate");
        } catch (ArithmeticException e) {
            check(e == boom, "original exception object relayed unchanged");
        }
        // OpenJDK ArrayList applies removals only after a complete pass, so a
        // throwing predicate leaves the list structurally unmodified (observed on 8u and 27).
        check(nums.equals(List.of(1, 2, 3, 4)), "throwing removeIf leaves list unchanged");
    }

    static void testSpliteratorContract() {
        List<Integer> l = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
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
        check(rest.equals(List.of(2, 3, 4, 5, 6, 7, 8)), "forEachRemaining takes the rest in order");
        check(!sp.tryAdvance(n -> first[0] = n), "exhausted tryAdvance returns false");
        Spliterator<Integer> spA = l.spliterator();
        Spliterator<Integer> front = spA.trySplit();
        check(front != null, "trySplit returns a prefix spliterator");
        List<Integer> all = new ArrayList<>();
        front.forEachRemaining(all::add);
        spA.forEachRemaining(all::add);
        check(all.equals(l), "prefix+suffix of split equals original in order");
        Spliterator<Integer> hsp = new HashSet<>(List.of(1, 2)).spliterator();
        check(hsp.hasCharacteristics(Spliterator.DISTINCT), "HashSet spliterator DISTINCT");
        check(hsp.getExactSizeIfKnown() == 2, "HashSet spliterator exact size before split");
    }

    /** Recording minimal collection: only iterator()/size(); add() keeps the default UOE. */
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
        check(p.containsAll(List.of("a", "c")), "default containsAll true");
        check(!p.containsAll(List.of("a", "zz")), "default containsAll false");
        check(p.remove("a") && p.data.equals(List.of("b", "c")),
              "default remove(Object) removes via iterator.remove");
        check(p.iteratorRemoves == 1, "iterator.remove actually invoked once");
        check(!p.remove("zz") && p.iteratorRemoves == 1, "remove miss does not invoke iterator.remove");
        check(p.removeIf(x -> x.equals("b")), "default removeIf removes matching");
        check(p.iteratorRemoves == 2, "removeIf removals go through iterator.remove");
        check(p.data.equals(List.of("c")), "removeIf left exactly non-matching");
        p.clear();
        check(p.data.isEmpty() && p.size() == 0, "default clear empties collection");
        check(p.iteratorRemoves == 3, "clear removed each element via iterator.remove");
        ProbeCollection q = new ProbeCollection("x", null);
        check(q.contains(null), "default contains(null) uses == branch");
        check(q.toString().equals("[x, null]"), "default toString renders null element");
        expectThrows(() -> q.add("y"), UnsupportedOperationException.class,
                "AbstractCollection default add UOE");
        ProbeCollection r1 = new ProbeCollection("a");
        ProbeCollection r2 = new ProbeCollection("a");
        check(!r1.equals(r2), "plain AbstractCollection keeps Object identity equals");
        check(r1.equals(r1), "identity equals holds for self");
    }

    // JDK-21+: SequencedCollection default methods observed through its core implementations
    static void testSequencedDefaults() {
        List<String> l = new ArrayList<>(List.of("a", "b"));
        l.addFirst("z");
        l.addLast("q");
        check(l.equals(List.of("z", "a", "b", "q")), "addFirst/addLast push to both ends");
        check(l.getFirst().equals("z") && l.getLast().equals("q"), "getFirst/getLast");
        check(l.removeFirst().equals("z"), "removeFirst returns and drops first");
        check(l.removeLast().equals("q"), "removeLast returns and drops last");
        check(l.equals(List.of("a", "b")), "ends restored");
        check(l.reversed().equals(List.of("b", "a")), "reversed view reverses encounter order");
        check(l.reversed().reversed().equals(l), "reversed twice restores order");
        expectThrows(() -> List.of().getFirst(), NoSuchElementException.class, "empty getFirst NSEE");
        expectThrows(() -> List.of().getLast(), NoSuchElementException.class, "empty getLast NSEE");
        // immutable empty list reports its unmodifiable nature before emptiness
        expectThrows(() -> List.of().removeFirst(), UnsupportedOperationException.class,
                "immutable removeFirst UOE");
        expectThrows(() -> List.of().removeLast(), UnsupportedOperationException.class,
                "immutable removeLast UOE");
        List<String> mutableEmpty = new ArrayList<>();
        expectThrows(() -> mutableEmpty.removeFirst(), NoSuchElementException.class,
                "empty mutable removeFirst NSEE");
        expectThrows(() -> mutableEmpty.removeLast(), NoSuchElementException.class,
                "empty mutable removeLast NSEE");

        LinkedHashSet<String> s = new LinkedHashSet<>(List.of("a", "b", "c"));
        s.addFirst("z");
        check(new ArrayList<>(s).equals(List.of("z", "a", "b", "c")), "LinkedHashSet addFirst new");
        s.addFirst("b");
        check(new ArrayList<>(s).equals(List.of("b", "z", "a", "c")), "addFirst existing relocates to front");
        s.addLast("b");
        check(new ArrayList<>(s).equals(List.of("z", "a", "c", "b")), "addLast existing relocates to end");
        check(s.getFirst().equals("z") && s.getLast().equals("b"), "LinkedHashSet getFirst/getLast");
        check(s.removeFirst().equals("z"), "LinkedHashSet removeFirst");
        check(s.removeLast().equals("b"), "LinkedHashSet removeLast");
        check(new ArrayList<>(s).equals(List.of("a", "c")), "remaining order after end removals");
        check(new ArrayList<>(s.reversed()).equals(List.of("c", "a")), "reversed view iteration");
        s.add("n");
        check(s.reversed().getFirst().equals("n"), "reversed view is live (parent add visible)");
        expectThrows(() -> new LinkedHashSet<String>().getFirst(), NoSuchElementException.class,
                "empty LinkedHashSet getFirst NSEE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEqualsContract();
        testToArrayContract();
        testBulkReturnSemantics();
        testNullAndExceptionSemantics();
        testSpliteratorContract();
        testDefaultDispatchProbe();
        testSequencedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
