/*
 * @test
 * @summary java.util.AbstractQueue API conformance: add/element/remove/clear delegating to offer/peek/poll.
 * @run main AbstractQueueTest
 */

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class AbstractQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Q extends AbstractQueue<String> {
        private final LinkedList<String> list = new LinkedList<String>();

        public boolean offer(String e) {
            return list.offer(e);
        }
        public String poll() {
            return list.poll();
        }
        public String peek() {
            return list.peek();
        }
        public Iterator<String> iterator() {
            return list.iterator();
        }
        public int size() {
            return list.size();
        }
    }

    static void testAbstractQueue() {
        Q q = new Q();
        check(q.add("a"), "add");
        check(q.add("b"), "add second");
        check("a".equals(q.element()), "element returns head without removing");
        check("a".equals(q.remove()), "remove returns and removes head");
        check("b".equals(q.poll()), "poll returns and removes head");
        check(q.poll() == null, "poll on empty returns null");
        q.add("c");
        q.clear();
        check(q.size() == 0, "clear empties the queue");
    }

    public static void main(String[] args) {
        testAbstractQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
