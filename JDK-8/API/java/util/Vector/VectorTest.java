/*
 * @test
 * @summary java.util.Vector API conformance: default capacity, add/get/set/remove,
 *          indexOf/lastIndexOf/contains, size/isEmpty/clear, iteration order, and the
 *          legacy addElement/removeElement/elementAt/firstElement/lastElement methods.
 * @run main VectorTest
 */

import java.util.Arrays;
import java.util.Vector;

public class VectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        Vector<String> v = new Vector<>();
        check(v.isEmpty() && v.size() == 0, "empty");
        check(v.capacity() == 10, "default capacity 10");
        check(v.add("a") && v.add("b") && v.add("c"), "add");
        check(v.size() == 3, "size");
        check(v.get(1).equals("b"), "get");
        check(v.set(1, "B").equals("b"), "set returns old");
        check(v.indexOf("c") == 2 && v.lastIndexOf("c") == 2, "indexOf/lastIndexOf");
        check(v.contains("B") && !v.contains("z"), "contains");
        check(v.remove("B") && !v.contains("B"), "remove");
        check(v.remove(0).equals("a"), "remove(index)");
        check(v.size() == 1 && v.get(0).equals("c"), "after removes");
    }

    static void testLegacy() {
        Vector<String> v = new Vector<>();
        v.addElement("a");
        v.addElement("b");
        check(v.elementAt(0).equals("a"), "elementAt");
        check(v.firstElement().equals("a") && v.lastElement().equals("b"),
              "firstElement/lastElement");
        v.insertElementAt("x", 1);
        check(v.equals(Arrays.asList("a", "x", "b")), "insertElementAt");
        v.removeElement("x");
        check(v.equals(Arrays.asList("a", "b")), "removeElement");
        v.removeElementAt(0);
        check(v.equals(Arrays.asList("b")), "removeElementAt");
    }

    static void testClearIteration() {
        Vector<Integer> v = new Vector<>(Arrays.asList(1, 2, 3));
        java.util.Iterator<Integer> it = v.iterator();
        check(it.next() == 1 && it.next() == 2 && it.next() == 3 && !it.hasNext(),
              "iteration order");
        v.clear();
        check(v.isEmpty(), "clear");
    }

    public static void main(String[] args) {
        testBasics();
        testLegacy();
        testClearIteration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
