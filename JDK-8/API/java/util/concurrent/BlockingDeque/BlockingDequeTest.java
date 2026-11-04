/*
 * @test
 * @summary java.util.concurrent.BlockingDeque API conformance: addFirst/offerLast/take.
 * @run main BlockingDequeTest
 */

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class BlockingDequeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlockingDeque() throws Exception {
        BlockingDeque<String> d = new LinkedBlockingDeque<String>(2);
        d.addFirst("a");
        d.addLast("b");
        check("b".equals(d.peekLast()), "peekLast");
        check("a".equals(d.takeFirst()), "takeFirst");
        check("b".equals(d.pollFirst(1, TimeUnit.SECONDS)), "pollFirst with timeout");
        check(d.peek() == null, "empty after draining");
    }

    public static void main(String[] args) throws Exception {
        testBlockingDeque();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
