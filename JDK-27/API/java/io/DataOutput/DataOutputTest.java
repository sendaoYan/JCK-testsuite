/*
 * @test
 * @summary java.io.DataOutput conformance: white-box structure (machine-independent
 *          primitive output contract) + black-box behavior (every method exercised
 *          polymorphically through DataOutputStream and RandomAccessFile; big-endian
 *          framing asserted byte-for-byte; writeBytes vs writeChars vs writeUTF
 *          encoding differences; null and bounds checks).
 * @run main DataOutputTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataOutputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Writes the canonical value set through any DataOutput. */
    static void exercise(DataOutput out) throws IOException {
        out.writeBoolean(true);
        out.writeByte(-2);
        out.writeShort(-2);
        out.writeChar('Q');
        out.writeInt(0xDEADBEEF);
        out.writeLong(9L);
        out.writeFloat(2.5f);
        out.writeDouble(-2.5);
        out.writeUTF("utf");
        out.writeBytes("b");       // 1 byte
        out.writeChars("c");       // 2 bytes
        out.write(0x0A);
        out.write(new byte[]{1, 2});
        out.write(new byte[]{9, 8, 7}, 1, 1);
    }

    /** Expected image produced by exercise() on any implementation. */
    static byte[] expected() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new DataOutputStream(bos).flush();
        bos.reset();
        DataOutputStream d = new DataOutputStream(bos);
        exercise(d);
        return bos.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataOutput");
        check(c.isInterface(), "is an interface");
        for (Method m : c.getMethods())
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        for (String m : new String[]{"write", "writeBoolean", "writeByte", "writeShort",
                                     "writeChar", "writeInt", "writeLong", "writeFloat",
                                     "writeDouble", "writeBytes", "writeChars", "writeUTF"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "write", byte[].class), "write(byte[]) overload");
        check(hasOverload(c, "write", byte[].class, int.class, int.class),
              "write(byte[],int,int) overload");
        check(Arrays.asList(java.io.DataOutputStream.class.getInterfaces()).contains(c),
              "DataOutputStream implements DataOutput");
        check(Arrays.asList(RandomAccessFile.class.getInterfaces()).contains(c),
              "RandomAccessFile implements DataOutput");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static boolean hasOverload(Class<?> c, String m, Class<?>... ps) {
        try { c.getMethod(m, ps); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] want = expected();

        // 1. implementation A: DataOutputStream through the interface reference
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutput a = new DataOutputStream(bos);
        exercise(a);
        check(Arrays.equals(bos.toByteArray(), want), "DataOutputStream image byte-identical");

        // 2. implementation B: RandomAccessFile through the interface reference
        File tmp = File.createTempFile("dataout", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            exercise(raf);
            raf.seek(0);
            byte[] got = new byte[(int) raf.length()];
            raf.readFully(got);
            check(Arrays.equals(got, want), "RandomAccessFile image byte-identical");
        } finally {
            tmp.delete();
        }

        // 3. fixed big-endian layout spot checks on the canonical image
        check(want[0] == 0x01, "boolean byte");
        check(want[1] == (byte) 0xFE && want[2] == (byte) 0xFF && want[3] == (byte) 0xFE,
              "byte then short big-endian");
        check(want[4] == 0 && want[5] == 'Q', "char as 2 bytes");
        check(want[6] == (byte) 0xDE && want[9] == (byte) 0xEF, "int big-endian");
        check(want[10] == 0 && want[17] == 9, "long big-endian");
        check(want[18] == 0x40 && want[19] == 0x20, "float bits");
        check(want[22] == (byte) 0xC0 && want[23] == 0x04, "double bits");
        check(want[30] == 0 && want[31] == 3, "writeUTF length prefix");
        check(want[32] == 'u' && want[34] == 'f', "writeUTF body");
        check(want[35] == 'b', "writeBytes: 1 byte per char");
        check(want[36] == 0 && want[37] == 'c', "writeChars: 2 bytes per char");
        check(want[38] == 0x0A, "write(int) truncates to low byte");

        // 4. writeBytes vs writeChars vs writeUTF for non-ASCII
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        DataOutput d2 = new DataOutputStream(b2);
        d2.writeBytes("é");        // low 8 bits of 'é' (0xE9)
        d2.writeChars("é");        // 2 bytes 0x00 0xE9
        d2.writeUTF("é");          // 0x00 0x02 0xC3 0xA9 (modified UTF-8)
        byte[] img = b2.toByteArray();
        check(img[0] == (byte) 0xE9, "writeBytes truncates");
        check(img[1] == 0 && img[2] == (byte) 0xE9, "writeChars 2-byte form");
        check(img[3] == 0 && img[4] == 2 && img[5] == (byte) 0xC3 && img[6] == (byte) 0xA9,
              "writeUTF modified-UTF-8 form");

        // 5. write(int) only stores the low 8 bits
        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        new DataOutputStream(b3).write(0x1234);
        check(b3.toByteArray().length == 1 && b3.toByteArray()[0] == 0x34, "low byte kept");

        // 6. null and bounds checks through the interface
        DataOutput d3 = new DataOutputStream(new ByteArrayOutputStream());
        try {
            d3.write((byte[]) null);
            check(false, "write(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            d3.write(new byte[2], 2, 1);
            check(false, "out-of-bounds write must throw");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }

        // 7. read back through DataInput as an end-to-end framing check
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(b4)) {
            out.writeUTF("frame");
            out.writeInt(-1);
        }
        try (java.io.DataInputStream in = new java.io.DataInputStream(
                new java.io.ByteArrayInputStream(b4.toByteArray()))) {
            check("frame".equals(in.readUTF()) && in.readInt() == -1,
                  "DataOutput writes are DataInput-readable");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
