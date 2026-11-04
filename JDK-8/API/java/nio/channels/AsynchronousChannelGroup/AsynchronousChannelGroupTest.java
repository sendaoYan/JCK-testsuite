/*
 * @test
 * @summary java.nio.channels.AsynchronousChannelGroup conformance: lifecycle.
 * @run main AsynchronousChannelGroupTest
 */

import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ThreadFactory;

public class AsynchronousChannelGroupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousChannelGroup() throws Exception {
        ThreadFactory daemon = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        };
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(1, daemon);
        check(group != null, "withFixedThreadPool");
        check(group.provider() != null, "provider");
        check(!group.isShutdown(), "isShutdown initially false");
        check(!group.isTerminated(), "isTerminated initially false");
        group.shutdown();
        check(group.isShutdown(), "isShutdown after shutdown");
        group.shutdownNow();
        check(true, "shutdownNow callable");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousChannelGroup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
