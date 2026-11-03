/*
 * @test
 * @summary java.nio.channels.AsynchronousCloseException conformance: white-box structure + black-box behavior.
 * @run main AsynchronousCloseExceptionTest
 */

import java.nio.channels.AsynchronousCloseException;

public class AsynchronousCloseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.channels.ClosedChannelException.class.isAssignableFrom(AsynchronousCloseException.class), "hierarchy: extends java.nio.channels.ClosedChannelException");
        check(new AsynchronousCloseException() != null, "no-arg constructor");
        check(new AsynchronousCloseException().getMessage() == null, "no-arg message is null");
        check(new AsynchronousCloseException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // closing a pipe source while another thread is blocked reading it
        final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        final Exception[] caught = new Exception[1];
        Thread reader = new Thread(() -> {
            try {
                pipe.source().read(java.nio.ByteBuffer.allocate(1));
            } catch (Exception e) {
                caught[0] = e;
            }
        }, "async-close-reader");
        reader.setDaemon(true);
        reader.start();
        Thread.sleep(300); // let the reader block
        pipe.source().close();
        reader.join(10_000);
        check(!reader.isAlive(), "reader finished");
        check(caught[0] instanceof AsynchronousCloseException,
              "blocked read closed by another thread -> AsynchronousCloseException, got " + caught[0]);
        check(caught[0] instanceof java.nio.channels.ClosedChannelException,
              "AsynchronousCloseException is a ClosedChannelException");
        pipe.sink().close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
