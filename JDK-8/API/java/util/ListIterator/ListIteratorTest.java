/*
 * @test
 * @summary java.util.ListIterator API conformance: hasNext/hasPrevious, next/previous,
 *          nextIndex/previousIndex, and set/add/remove on the last-returned element.
 * @run main ListIteratorTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class ListIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIndexes() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        check(!it.hasPrevious() && it.hasNext(), "initial position");
        check(it.nextIndex() == 0 && it.previousIndex() == -1, "indexes at start");
        check(it.next().equals("a"), "next");
        check(it.nextIndex() == 1, "nextIndex after next");
        check(it.previous().equals("a"), "previous");
        check(it.previousIndex() == -1, "previousIndex after previous");
    }

    static void testSetAddRemove() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        ListIterator<String> it = l.listIterator();
        it.next();
        it.next();
        it.set("B");
        check(l.get(1).equals("B"), "set replaces last-returned");
        it.add("x");
        check(l.equals(Arrays.asList("a", "B", "x", "c")), "add inserts");
        check(it.previous().equals("x"), "previous to inserted");
        it.remove();
        check(l.equals(Arrays.asList("a", "B", "c")), "remove removes last-returned");
    }

    public static void main(String[] args) {
        testIndexes();
        testSetAddRemove();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
