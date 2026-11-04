/*
 * @test
 * @summary java.io.InterruptedIOException conformance (pure black-box):
 *          constructors and the public bytesTransferred field; a real trigger
 *          interrupting a thread blocked in PipedInputStream.read; interrupt status
 *          of the reading thread after the throw.
 * @run main InterruptedIOExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class InterruptedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructors and field default
        InterruptedIOException e0 = new InterruptedIOException();
        check(e0.getMessage() == null && e0.bytesTransferred == 0,
              "no-arg: null message, bytesTransferred 0");
        InterruptedIOException e1 = new InterruptedIOException("read timed out");
        check("read timed out".equals(e1.getMessage()) && e1.bytesTransferred == 0,
              "(String): message set, bytesTransferred still 0");

        // 2. bytesTransferred is a writable data field (documents partial transfer)
        e1.bytesTransferred = 512;
        check(e1.bytesTransferred == 512, "bytesTransferred assignable");
        e1.bytesTransferred = 0;

        // 3. real trigger: interrupt a thread blocked reading an empty pipe
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        final InterruptedIOException[] hit = new InterruptedIOException[1];
        final int[] partial = new int[]{-1};
        Thread reader = new Thread(null, () -> {
            try {
                in.read();
            } catch (InterruptedIOException iio) {
                hit[0] = iio;
                partial[0] = iio.bytesTransferred;
            } catch (IOException io) {
                // unexpected
            }
        }, "iio-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (int i = 0; i < 400; i++) {
            Thread.State st = reader.getState();
            if (st == Thread.State.WAITING || st == Thread.State.TIMED_WAITING) break;
            Thread.sleep(5);
        }
        check(reader.getState() == Thread.State.WAITING
              || reader.getState() == Thread.State.TIMED_WAITING,
              "reader parked on the empty pipe: " + reader.getState());
        reader.interrupt();
        reader.join(5000);
        check(hit[0] != null, "blocked read threw InterruptedIOException");
        check(partial[0] == 0, "bytesTransferred 0 (no byte had arrived)");
        check(!reader.isInterrupted(),
              "interrupt is consumed by the throw (PipedInputStream does not re-set it)");
        out.close();
        in.close();

        // 4. contrast: interrupting is not required for ordinary reads
        check(new DataInputStream(new ByteArrayInputStream(new byte[]{5})).read() == 5,
              "normal read unaffected");

        // 5. caught as IOException by API callers
        try {
            throw new InterruptedIOException("x");
        } catch (IOException io) {
            check(io instanceof InterruptedIOException, "catchable as IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
