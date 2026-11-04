/*
 * @test
 * @summary java.util.AbstractSet API conformance: equals across set implementations,
 *          hashCode (sum of element hashCodes, order-independent), removeAll size-based
 *          optimization, add/contains/remove/clear via HashSet, null elements.
 * @run main AbstractSetTest
 */

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public static void main(String[] args) {
        testEqualsHashCode();
        testRemoveAll();
        testBasicOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
