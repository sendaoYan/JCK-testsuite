/*
 * @test
 * @summary java.nio.channels.AsynchronousFileChannel conformance: open/size.
 * @run main AsynchronousFileChannelTest
 */

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;

public class AsynchronousFileChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousFileChannel() throws Exception {
        File f = File.createTempFile("jck-afc", ".tmp");
        f.deleteOnExit();
        AsynchronousFileChannel ch = AsynchronousFileChannel.open(f.toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        check(ch.isOpen(), "isOpen");
        check(ch.size() == 0, "size 0");
        ch.force(true);
        check(true, "force callable");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousFileChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
