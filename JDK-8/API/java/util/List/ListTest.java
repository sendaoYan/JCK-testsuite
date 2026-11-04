/*
 * @test
 * @summary java.util.List API conformance: get/set/add(index)/remove(index), indexOf/
 *          lastIndexOf, subList, listIterator, and the Java 8 default sort.
 * @run main ListTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class ListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIndexedOps() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "b", "c"));
        check("a".equals(l.get(0)) && "c".equals(l.get(2)), "get");
        check(l.indexOf("b") == 1 && l.lastIndexOf("b") == 1, "indexOf/lastIndexOf");
        check("b".equals(l.set(1, "B")), "set returns old");
        check("B".equals(l.get(1)), "set updates");
        l.add(0, "first");
        check("first".equals(l.get(0)) && l.size() == 4, "add(index)");
        check("first".equals(l.remove(0)), "remove(index) returns removed");
    }

    static void testSubList() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "B", "c"));
        List<String> sub = l.subList(0, 2);
        check(sub.size() == 2 && "a".equals(sub.get(0)), "subList view");
    }

    static void testListIterator() {
        List<String> l = new ArrayList<>(Arrays.asList("a", "B", "c"));
        ListIterator<String> li = l.listIterator();
        check(li.next().equals("a") && li.next().equals("B"), "next");
        check(li.previous().equals("B"), "previous");
        check(li.nextIndex() == 1 && li.previousIndex() == 0, "nextIndex/previousIndex");
    }

    static void testSort() {
        List<Integer> nums = new ArrayList<>(Arrays.asList(3, 1, 2));
        nums.sort(Comparator.naturalOrder());
        check(nums.equals(Arrays.asList(1, 2, 3)), "sort default method");
    }

    public static void main(String[] args) {
        testIndexedOps();
        testSubList();
        testListIterator();
        testSort();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
