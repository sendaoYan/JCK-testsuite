/*
 * @test
 * @summary java.util.LinkedList API conformance: deque ops (addFirst/addLast/
 *          removeFirst/removeLast/getFirst/getLast/offer/poll/peek/push/pop),
 *          list ops (add/get/set/remove/indexOf), contains/size/clear, and
 *          iterator order.
 * @run main LinkedListTest
 */

import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNSEE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    static void testDeque() {
        LinkedList<String> list = new LinkedList<>();
        list.addFirst("b");
        list.addFirst("a");
        list.addLast("c");
        check(list.equals(Arrays.asList("a", "b", "c")), "addFirst/addLast order");
        check(list.getFirst().equals("a") && list.getLast().equals("c"), "getFirst/getLast");
        check(list.removeFirst().equals("a"), "removeFirst");
        check(list.removeLast().equals("c"), "removeLast");
        check(list.equals(Arrays.asList("b")), "after removeFirst/Last");

        LinkedList<String> q = new LinkedList<>();
        check(q.offer("x") && q.offer("y"), "offer returns true");
        check(q.peek().equals("x"), "peek returns head without removing");
        check(q.size() == 2, "peek non-destructive");
        check(q.poll().equals("x"), "poll removes head");
        check(q.poll().equals("y") && q.poll() == null, "poll empty returns null");

        LinkedList<String> stack = new LinkedList<>();
        stack.push("a");
        stack.push("b");
        check(stack.pop().equals("b"), "stack pop LIFO");
        check(stack.pop().equals("a"), "stack pop LIFO");
        expectNSEE(() -> stack.pop(), "pop empty throws NSEE");
    }

    static void testListOps() {
        LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3));
        check(list.size() == 3 && list.get(1) == 2, "get by index");
        check(list.set(1, 20) == 2, "set returns old value");
        list.add(1, 15);
        check(list.equals(Arrays.asList(1, 15, 20, 3)), "add at index");
        check(list.remove(1) == 15, "remove(int) returns element");
        check(list.indexOf(20) == 1 && list.lastIndexOf(20) == 1, "indexOf/lastIndexOf");
        check(list.contains(3) && !list.contains(99), "contains");
        list.clear();
        check(list.isEmpty(), "clear empties");
    }

    static void testIteratorOrder() {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Iterator<String> it = list.iterator();
        check(it.next().equals("a") && it.next().equals("b") && it.next().equals("c"),
              "iterator order");
        check(!it.hasNext(), "iterator exhausted");
    }

    public static void main(String[] args) {
        testDeque();
        testListOps();
        testIteratorOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
