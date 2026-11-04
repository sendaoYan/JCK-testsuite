/*
 * @test
 * @summary java.util.zip.CRC32 conformance: known checksum and reset.
 * @run main CRC32Test
 */

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CRC32Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCRC32() throws Exception {
        byte[] data = "123456789".getBytes("UTF-8");
        CRC32 c = new CRC32();
        check(c.getValue() == 0L, "initial value 0");
        c.update(data, 0, data.length);
        check(c.getValue() == 0xCBF43926L, "crc32 of '123456789'");
        c.reset();
        check(c.getValue() == 0L, "reset to 0");

        CRC32 d = new CRC32();
        for (byte b : data) d.update(b);
        check(d.getValue() == 0xCBF43926L, "update(int) byte-wise");

        CRC32 e = new CRC32();
        e.update(ByteBuffer.wrap(data));
        check(e.getValue() == 0xCBF43926L, "update(ByteBuffer)");
    }

    public static void main(String[] args) throws Exception {
        testCRC32();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
