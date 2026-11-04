/*
 * @test
 * @summary java.nio.MappedByteBuffer API conformance: memory-map a file and read/write.
 * @run main MappedByteBufferTest
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMappedByteBuffer() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.setLength(8);
            try (FileChannel ch = raf.getChannel()) {
                MappedByteBuffer m = ch.map(FileChannel.MapMode.READ_WRITE, 0, 8);
                check(m.capacity() == 8, "capacity");
                check(!m.isReadOnly(), "not read-only");
                m.put((byte) 'A');
                check(m.get(0) == 'A', "get reflects put");
                m.force();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testMappedByteBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
