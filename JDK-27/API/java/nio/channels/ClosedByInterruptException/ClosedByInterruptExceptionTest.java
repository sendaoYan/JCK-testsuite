/*
 * @test
 * @summary java.nio.channels.ClosedByInterruptException conformance: white-box structure + black-box behavior.
 * @run main ClosedByInterruptExceptionTest
 */

import java.nio.channels.ClosedByInterruptException;

public class ClosedByInterruptExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.channels.AsynchronousCloseException.class.isAssignableFrom(ClosedByInterruptException.class), "hierarchy: extends java.nio.channels.AsynchronousCloseException");
        check(new ClosedByInterruptException() != null, "no-arg constructor");
        check(new ClosedByInterruptException().getMessage() == null, "no-arg message is null");
        check(new ClosedByInterruptException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // interrupting a thread blocked on a channel read
        final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        final Exception[] caught = new Exception[1];
        Thread reader = new Thread(() -> {
            try {
                pipe.source().read(java.nio.ByteBuffer.allocate(1));
            } catch (Exception e) {
                caught[0] = e;
            }
        }, "interrupt-reader");
        reader.setDaemon(true);
        reader.start();
        Thread.sleep(300); // let the reader block
        reader.interrupt();
        reader.join(10_000);
        check(!reader.isAlive(), "reader finished after interrupt");
        check(caught[0] instanceof ClosedByInterruptException,
              "interrupted blocked read -> ClosedByInterruptException, got " + caught[0]);
        check(!pipe.source().isOpen(), "channel closed by interrupt");
        pipe.sink().close();
        pipe.source().close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
