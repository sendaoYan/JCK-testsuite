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
import java.util.Set;

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

    public static void main(String[] args) {
        testBasics();
        testBulk();
        testIteration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
