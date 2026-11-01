/*
 * @test
 * @summary java.util.zip.Adler32 conformance: known checksum and update overloads.
 * @run main Adler32Test
 */

import java.nio.ByteBuffer;
import java.util.zip.Adler32;

public class Adler32Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAdler32() throws Exception {
        byte[] data = "Wikipedia".getBytes("UTF-8");
        Adler32 a = new Adler32();
        check(a.getValue() == 1L, "initial value 1");
        a.update(data, 0, data.length);
        check(a.getValue() == 0x11E60398L, "adler32 of 'Wikipedia'");
        a.reset();
        check(a.getValue() == 1L, "reset to initial");

        Adler32 b = new Adler32();
        for (byte c : data) b.update(c);
        check(b.getValue() == 0x11E60398L, "update(int) byte-wise");

        Adler32 c = new Adler32();
        c.update(ByteBuffer.wrap(data));
        check(c.getValue() == 0x11E60398L, "update(ByteBuffer)");
    }

    public static void main(String[] args) throws Exception {
        testAdler32();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
