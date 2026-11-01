/*
 * @test
 * @summary java.nio.channels.FileChannel conformance: read/write/position/size.
 * @run main FileChannelTest
 */

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class FileChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileChannel() throws Exception {
        File f = File.createTempFile("jck-fc", ".tmp");
        f.deleteOnExit();
        try (FileChannel ch = FileChannel.open(f.toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            check(ch.isOpen(), "isOpen");
            ByteBuffer buf = ByteBuffer.wrap(new byte[] {'h', 'e', 'l', 'l', 'o'});
            check(ch.write(buf) == 5, "write 5 bytes");
            check(ch.size() == 5, "size");
            check(ch.position() == 5, "position after write");
            ch.position(0);
            check(ch.position() == 0, "position set to 0");
            ByteBuffer r = ByteBuffer.allocate(5);
            check(ch.read(r) == 5, "read 5 bytes");
            check(new String(r.array(), "UTF-8").equals("hello"), "read back");
            ch.force(true);
            check(true, "force callable");
            ch.truncate(0);
            check(ch.size() == 0, "truncate");
        }
    }

    public static void main(String[] args) throws Exception {
        testFileChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
