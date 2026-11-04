/*
 * @test
 * @summary java.util.Collection API conformance: add/remove/contains/size/isEmpty/clear,
 *          addAll/containsAll/removeAll, toArray overloads, iteration, and the Java 8
 *          default methods forEach and removeIf.
 * @run main CollectionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    public static void main(String[] args) {
        testBasics();
        testBulk();
        testToArray();
        testIterationAndDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
