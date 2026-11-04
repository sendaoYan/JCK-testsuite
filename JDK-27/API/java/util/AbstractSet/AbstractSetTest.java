/*
 * @test
 * @summary java.util.AbstractSet conformance: white-box structure + black-box behavior.
 * @run main AbstractSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AbstractSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete subclass: iterator + size + add. */
    static class S extends AbstractSet<String> {
        final Set<String> backing = new LinkedHashSet<>();
        @Override public Iterator<String> iterator() { return backing.iterator(); }
        @Override public int size() { return backing.size(); }
        @Override public boolean add(String e) { return backing.add(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractSet");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractCollection.class.isAssignableFrom(c), "extends AbstractCollection");
        check(Set.class.isAssignableFrom(c), "implements Set");
        Method eq = c.getDeclaredMethod("equals", Object.class);
        check(!Modifier.isAbstract(eq.getModifiers()), "equals concrete");
        Method hc = c.getDeclaredMethod("hashCode");
        check(!Modifier.isAbstract(hc.getModifiers()), "hashCode concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("removeAll", java.util.Collection.class).getModifiers()),
                "removeAll concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. add/contains/size/isEmpty/remove inherited
        S s = new S();
        check(s.add("a") && s.add("b") && !s.add("a"), "add returns false on duplicate");
        check(s.size() == 2 && s.contains("a") && !s.contains("z"), "size/contains");
        check(!s.isEmpty(), "not empty");
        check(s.remove("a") && !s.contains("a") && s.size() == 1, "remove");

        // 2. equals: size + containsAll (order-independent)
        S s1 = new S(); s1.add("x"); s1.add("y");
        S s2 = new S(); s2.add("y"); s2.add("x");
        check(s1.equals(s2) && s2.equals(s1), "AbstractSet.equals is order-independent");
        check(s1.hashCode() == s2.hashCode(), "hashCode equals for equal sets");
        S s3 = new S(); s3.add("x"); s3.add("z");
        check(!s1.equals(s3), "different elements -> not equal");

        // 3. hashCode is the sum of element hashCodes
        S nums = new S(); nums.add("1"); nums.add("2"); nums.add("3");
        check(nums.hashCode() == ("1".hashCode() + "2".hashCode() + "3".hashCode()),
                "hashCode is sum of element hashCodes");

        // 4. equals against a non-Set / null / self
        check(!s1.equals(null), "equals(null) false");
        check(!s1.equals("xy"), "equals(other type) false");
        check(s1.equals(s1), "equals(self) true");

        // 5. removeAll removes common elements
        S base = new S(); base.add("a"); base.add("b"); base.add("c");
        List<String> other = new ArrayList<>(Arrays.asList("b", "c", "d"));
        check(base.removeAll(other), "removeAll returns true");
        check(base.equals(new LinkedHashSet<>(Arrays.asList("a"))), "removeAll removes intersection");

        // 6. toArray / iteration inherited from AbstractCollection
        S ord = new S(); ord.add("p"); ord.add("q");
        check(Arrays.equals(ord.toArray(), new Object[] {"p", "q"}), "toArray via iterator order");
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
        HashSet<String> h = new HashSet<>(List.of("b", "a"));
        LinkedHashSet<String> lh = new LinkedHashSet<>(List.of("a", "b"));
        TreeSet<String> t = new TreeSet<>(List.of("a", "b"));
        check(r.equals(h) && h.equals(r), "AbstractSet probe equals HashSet (symmetric)");
        check(r.equals(t) && t.equals(r), "AbstractSet probe equals TreeSet");
        check(lh.equals(t) && h.equals(lh), "HashSet/LinkedHashSet/TreeSet pairwise equal");
        check(r.hashCode() == h.hashCode() && r.hashCode() == (97 + 98),
              "hashCode equals sum of element hashCodes across impls");
        check(r.equals(Set.of("a", "b")) && Set.of("b", "a").equals(r),
              "Set.of participates in Set equality");
        check(!r.equals(List.of("a", "b")), "Set never equals List");
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
        check(big.removeAll(List.of("x", "a")), "bigger-this removeAll true");
        check(big.removes == 2, "this bigger: remove(Object) invoked for each argument element");
        check(big.iteratorCreated == 0, "this bigger: this.iterator() never created");
        check(big.data.equals(new LinkedHashSet<>(List.of("b", "c", "d"))),
              "only the common element removed");
        // this smaller than the argument: skeleton iterates this via iterator(),
        // removing through iterator.remove without calling this.remove
        RecSet small = new RecSet("a", "b");
        check(small.removeAll(List.of("a", "b", "c", "d")), "smaller-this removeAll true");
        check(small.removes == 0, "this smaller: removals via iterator, this.remove untouched");
        check(small.iteratorCreated >= 1, "this smaller: iterator created");
        check(small.isEmpty(), "all common elements removed");
        // return-value edges
        RecSet d = new RecSet("a", "b");
        check(!d.removeAll(List.of("x", "y")), "disjoint removeAll false");
        check(d.removes == 0 && d.size() == 2,
              "equal sizes take the iterator path: this.remove untouched");
        check(!d.removeAll(List.of()), "removeAll(empty) false");
        RecSet e = new RecSet("a", "b", "c");
        check(!e.removeAll(List.of("x")), "disjoint smaller-arg removeAll false");
        check(e.removes == 1 && e.size() == 3, "single-element argument routed via this.remove");
        check(!new RecSet().removeAll(List.of("a")), "empty this removeAll false");
    }

    static void testInheritedOps() {
        RecSet s = new RecSet("a", "b", "c");
        check(s.retainAll(List.of("a", "c")), "retainAll inherited from AbstractCollection");
        check(s.data.equals(new LinkedHashSet<>(List.of("a", "c"))), "retainAll kept intersection");
        check(s.removes == 0, "retainAll path never calls this.remove(Object)");
        String[] bigArr = new String[5];
        check(s.toArray(bigArr) == bigArr && bigArr[2] == null,
              "toArray reuse and null-pad inherited from AbstractCollection");
        s.clear();
        check(s.data.isEmpty(), "clear inherited drives iterator.remove");
        check(new RecSet("a").contains("a"), "contains inherited scans iterator");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCrossImplEquals();
        testRemoveAllOptimization();
        testInheritedOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
