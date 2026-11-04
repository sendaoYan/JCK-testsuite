/*
 * @test
 * @summary java.util.AbstractSet API conformance: equals across set implementations,
 *          hashCode (sum of element hashCodes, order-independent), removeAll size-based
 *          optimization, add/contains/remove/clear via HashSet, null elements.
 * @run main AbstractSetTest
 */

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class AbstractSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Wrap extends AbstractSet<String> {
        final Set<String> backing = new LinkedHashSet<>();
        public Iterator<String> iterator() { return backing.iterator(); }
        public int size() { return backing.size(); }
        public boolean add(String s) { return backing.add(s); }
    }

    static void testEqualsHashCode() {
        Set<Integer> a = new HashSet<>(java.util.Arrays.asList(1, 2, 3));
        Set<Integer> b = new HashSet<>(java.util.Arrays.asList(3, 2, 1));
        check(a.equals(b), "equals ignores iteration order");
        check(a.hashCode() == b.hashCode(), "hashCode order-independent");

        // hashCode == sum of element hashCodes
        check(a.hashCode() == (1 + 2 + 3), "hashCode is sum of element hashCodes");

        check(a.equals(new HashSet<>(java.util.Arrays.asList(1, 2)) ) == false, "!equals different size");
        check(new HashSet<>().equals(new HashSet<>()), "two empty sets equal");
        check(!a.equals(java.util.Arrays.asList(1, 2, 3)), "set !equals list");
    }

    static void testRemoveAll() {
        Wrap a = new Wrap();
        a.add("a"); a.add("b"); a.add("c");
        // this smaller: iterate this
        check(a.removeAll(java.util.Arrays.asList("a", "b", "x", "y")), "removeAll returns true");
        check(a.size() == 1 && a.contains("c"), "removeAll removes common (smaller this)");

        Wrap b = new Wrap();
        b.add("a"); b.add("b"); b.add("c"); b.add("d");
        // argument smaller: iterate argument
        check(b.removeAll(java.util.Arrays.asList("a", "c")), "removeAll returns true (smaller arg)");
        check(b.size() == 2 && b.contains("b") && b.contains("d"), "removeAll removes common (smaller arg)");
    }

    static void testBasicOps() {
        AbstractSet<Integer> s = new HashSet<>();
        check(s.isEmpty(), "empty initially");
        check(s.add(1) && s.add(2) && !s.add(1), "add returns false on duplicate");
        check(s.size() == 2, "size");
        check(s.contains(1) && !s.contains(3), "contains");
        check(s.remove(1), "remove returns true");
        check(s.size() == 1, "size after remove");
        s.add(null);
        check(s.contains(null), "null element allowed");
        s.clear();
        check(s.isEmpty(), "clear empties");
    }


    // ---------- contract deepening (appended; nothing above altered) ----------

    /** Recording probe: counts iterator() creations and remove(Object) delegations. */
    static class RecSet extends AbstractSet<String> {
        final LinkedHashSet<String> data;
        int iteratorCreated, removes;
        RecSet(String... xs) { data = new LinkedHashSet<>(Arrays.asList(xs)); }
        public Iterator<String> iterator() { iteratorCreated++; return data.iterator(); }
        public int size() { return data.size(); }
        public boolean remove(Object o) { removes++; return data.remove(o); }
    }

    static void testCrossImplEquals() {
        RecSet r = new RecSet("a", "b");
        HashSet<String> h = new HashSet<>(Arrays.asList("b", "a"));
        LinkedHashSet<String> lh = new LinkedHashSet<>(Arrays.asList("a", "b"));
        TreeSet<String> t = new TreeSet<>(Arrays.asList("a", "b"));
        check(r.equals(h) && h.equals(r), "AbstractSet probe equals HashSet (symmetric)");
        check(r.equals(t) && t.equals(r), "AbstractSet probe equals TreeSet");
        check(lh.equals(t) && h.equals(lh), "HashSet/LinkedHashSet/TreeSet pairwise equal");
        check(r.hashCode() == h.hashCode() && r.hashCode() == (97 + 98),
              "hashCode equals sum of element hashCodes across impls");
        check(!r.equals(Arrays.asList("a", "b")), "Set never equals List");
        check(!r.equals(null), "equals(null) false");
        check(r.equals(r), "equals self");
        RecSet n = new RecSet("a", null);
        HashSet<String> hn = new HashSet<>(Arrays.asList(null, "a"));
        check(n.equals(hn) && hn.equals(n), "sets with null element equal across impls");
        check(n.hashCode() == 97, "null element contributes 0 to hashCode sum");
        check(new RecSet().hashCode() == 0, "empty set hashCode is 0");
        check(new RecSet().equals(new HashSet<String>()), "empty sets equal across impls");
    }

    static void testRemoveAllOptimization() {
        // this bigger than the argument: skeleton iterates the argument, calling
        // this.remove(e) for every element of it (documented optimization)
        RecSet big = new RecSet("a", "b", "c", "d");
        check(big.removeAll(Arrays.asList("x", "a")), "bigger-this removeAll true");
        check(big.removes == 2, "this bigger: remove(Object) invoked for each argument element");
        check(big.iteratorCreated == 0, "this bigger: this.iterator() never created");
        check(big.data.equals(new LinkedHashSet<>(Arrays.asList("b", "c", "d"))),
              "only the common element removed");
        // this smaller than the argument: skeleton iterates this via iterator(),
        // removing through iterator.remove without calling this.remove
        RecSet small = new RecSet("a", "b");
        check(small.removeAll(Arrays.asList("a", "b", "c", "d")), "smaller-this removeAll true");
        check(small.removes == 0, "this smaller: removals via iterator, this.remove untouched");
        check(small.iteratorCreated >= 1, "this smaller: iterator created");
        check(small.isEmpty(), "all common elements removed");
        // return-value edges
        RecSet d = new RecSet("a", "b");
        check(!d.removeAll(Arrays.asList("x", "y")), "disjoint removeAll false");
        check(d.removes == 0 && d.size() == 2,
              "equal sizes take the iterator path: this.remove untouched");
        check(!d.removeAll(new java.util.ArrayList<String>()), "removeAll(empty) false");
        RecSet e = new RecSet("a", "b", "c");
        check(!e.removeAll(Arrays.asList("x")), "disjoint smaller-arg removeAll false");
        check(e.removes == 1 && e.size() == 3, "single-element argument routed via this.remove");
        check(!new RecSet().removeAll(Arrays.asList("a")), "empty this removeAll false");
    }

    static void testInheritedOps() {
        RecSet s = new RecSet("a", "b", "c");
        check(s.retainAll(Arrays.asList("a", "c")), "retainAll inherited from AbstractCollection");
        check(s.data.equals(new LinkedHashSet<>(Arrays.asList("a", "c"))), "retainAll kept intersection");
        check(s.removes == 0, "retainAll path never calls this.remove(Object)");
        String[] bigArr = new String[5];
        check(s.toArray(bigArr) == bigArr && bigArr[2] == null,
              "toArray reuse and null-pad inherited from AbstractCollection");
        s.clear();
        check(s.data.isEmpty(), "clear inherited drives iterator.remove");
        check(new RecSet("a").contains("a"), "contains inherited scans iterator");
    }

    public static void main(String[] args) {
        testEqualsHashCode();
        testRemoveAll();
        testBasicOps();
        testCrossImplEquals();
        testRemoveAllOptimization();
        testInheritedOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
