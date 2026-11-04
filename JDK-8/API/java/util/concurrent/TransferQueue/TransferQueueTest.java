/*
 * @test
 * @summary java.util.concurrent.TransferQueue API conformance: hasWaitingConsumer/transfer.
 * @run main TransferQueueTest
 */

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class TransferQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTransferQueue() throws Exception {
        TransferQueue<String> q = new LinkedTransferQueue<String>();
        check(q.getWaitingConsumerCount() == 0, "no waiting consumers initially");
        q.put("a");
        check(q.tryTransfer("b", 100, TimeUnit.MILLISECONDS) || q.size() > 0, "tryTransfer without blocking");
        check("a".equals(q.take()), "take");
    }

    public static void main(String[] args) throws Exception {
        testTransferQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
