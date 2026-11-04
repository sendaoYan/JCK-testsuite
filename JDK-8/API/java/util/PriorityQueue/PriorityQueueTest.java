/*
 * @test
 * @summary java.util.PriorityQueue API conformance: natural ordering and comparator
 *          construction, add/offer/peek/poll/remove (heap extraction order), size/isEmpty/
 *          clear, contains, and element ordering under poll.
 * @run main PriorityQueueTest
 */

import java.util.PriorityQueue;
import java.util.Comparator;

public class PriorityQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNaturalOrder() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        check(q.isEmpty(), "empty");
        check(q.offer(5) && q.offer(2) && q.offer(4) && q.offer(1) && q.offer(3),
              "offer returns true");
        check(q.size() == 5, "size");
        check(q.peek() == 1, "peek smallest");
        check(q.contains(3) && !q.contains(99), "contains");
        check(q.poll() == 1, "poll smallest");
        check(q.poll() == 2, "poll next");
        check(q.poll() == 3, "poll next");
        check(q.poll() == 4, "poll next");
        check(q.poll() == 5, "poll last");
        check(q.poll() == null, "poll empty null");
        check(q.isEmpty(), "empty after polls");
    }

    static void testComparator() {
        PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.reverseOrder());
        q.add(1);
        q.add(3);
        q.add(2);
        check(q.peek() == 3, "max-heap peek");
        check(q.poll() == 3 && q.poll() == 2 && q.poll() == 1, "max-heap poll order");
    }

    static void testRemoveClear() {
        PriorityQueue<Integer> q = new PriorityQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        check(q.remove(2), "remove(element)");
        check(q.size() == 2 && !q.contains(2), "removed absent");
        q.clear();
        check(q.isEmpty(), "clear");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testComparator();
        testRemoveClear();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
