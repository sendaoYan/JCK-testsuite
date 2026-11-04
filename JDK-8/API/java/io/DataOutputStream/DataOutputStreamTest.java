/*
 * @test
 * @summary java.io.DataOutputStream API conformance: full contract coverage.
 *          Constructor null semantics, write(int) low 8 bits, write(byte[],off,len)
 *          validation (NPE/IOBE), primitive round-trip against DataInputStream,
 *          size() counter for every write type, writeBytes/writeChars byte layout,
 *          writeUTF modified-UTF-8 length prefix + >65535 -> UTFDataFormatException,
 *          flush delegation.
 * @run main DataOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UTFDataFormatException;

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- 1. constructor null semantics ----
    static void testConstructorNull() throws Exception {
        DataOutputStream dos = new DataOutputStream(null);
        checkThrows(NullPointerException.class, () -> dos.writeByte(1),
                    "writeByte on null-backed -> NPE");
    }

    // ---- 2. write(int) low 8 bits ----
    static void testWriteInt() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(0x1FF);
        dos.write(256);
        dos.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 2 && (b[0] & 0xFF) == 0xFF && b[1] == 0, "write(int) low 8 bits");
        check(dos.size() == 2, "size after write(int)");
    }

    // ---- 3. write(byte[],off,len) validation ----
    static void testWriteArrayValidation() {
        final DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(NullPointerException.class, () -> dos.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> dos.write(new byte[2], 0, 3),
                    "write(b,0,3) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> dos.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
    }

    // ---- 4. primitive round-trip ----
    static void testPrimitivesRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBoolean(true);
        dos.writeBoolean(false);
        dos.writeByte(0x7F);
        dos.writeShort(0x1234);
        dos.writeChar('Z');
        dos.writeInt(0x12345678);
        dos.writeLong(0x1234567890ABCDEFL);
        dos.writeFloat(1.5f);
        dos.writeDouble(-2.25);
        dos.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readBoolean(), "writeBoolean true round-trip");
        check(!dis.readBoolean(), "writeBoolean false round-trip");
        check(dis.readByte() == 0x7F, "writeByte round-trip");
        check(dis.readShort() == 0x1234, "writeShort round-trip");
        check(dis.readChar() == 'Z', "writeChar round-trip");
        check(dis.readInt() == 0x12345678, "writeInt round-trip");
        check(dis.readLong() == 0x1234567890ABCDEFL, "writeLong round-trip");
        check(dis.readFloat() == 1.5f, "writeFloat round-trip");
        check(dis.readDouble() == -2.25, "writeDouble round-trip");
    }

    // ---- 5. size() counter ----
    static void testSize() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        check(dos.size() == 0, "initial size 0");
        dos.writeBoolean(true);         // 1
        dos.writeByte(1);               // 1
        dos.writeShort(1);              // 2
        dos.writeChar('a');             // 2
        dos.writeInt(1);                // 4
        dos.writeLong(1);               // 8
        dos.writeFloat(1);              // 4
        dos.writeDouble(1);             // 8
        check(dos.size() == 30, "size sums primitive widths (1+1+2+2+4+8+4+8=30)");
        dos.writeUTF("hello");          // 2 + 5
        check(dos.size() == 37, "size after writeUTF (2 + utf length)");
    }

    // ---- 6. writeBytes / writeChars byte layout ----
    static void testWriteBytesChars() throws Exception {
        // writeBytes writes low 8 bits of each char
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBytes("AB");
        byte[] b = bos.toByteArray();
        check(b.length == 2 && b[0] == 'A' && b[1] == 'B', "writeBytes low 8 bits");

        // writeChars writes two bytes per char, big-endian
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        DataOutputStream dos2 = new DataOutputStream(bos2);
        dos2.writeChars("AZ");
        byte[] c = bos2.toByteArray();
        check(c.length == 4, "writeChars 2 bytes per char");
        check(c[0] == 0 && c[1] == 'A' && c[2] == 0 && c[3] == 'Z', "writeChars big-endian");
    }

    // ---- 7. writeUTF length prefix + >65535 ----
    static void testWriteUTF() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF("héllo");
        byte[] b = bos.toByteArray();
        check((b[0] & 0xFF) == 0 && (b[1] & 0xFF) == 6, "writeUTF length prefix (6 UTF bytes)");
        check(b.length == 2 + 6, "writeUTF total length");

        // > 65535 encoded bytes -> UTFDataFormatException
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) sb.append('a');
        final String tooLong = sb.toString();
        final DataOutputStream big = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(UTFDataFormatException.class, () -> big.writeUTF(tooLong),
                    "writeUTF > 65535 bytes -> UTFDataFormatException");

        // writeUTF(null) -> NPE
        checkThrows(NullPointerException.class, () -> dos.writeUTF(null),
                    "writeUTF(null) -> NPE");
    }

    // ---- 8. flush delegation ----
    static void testFlush() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(7);
        dos.flush();
        check(bos.size() == 1, "flush flushes to underlying");
    }

    public static void main(String[] args) throws Exception {
        testConstructorNull();
        testWriteInt();
        testWriteArrayValidation();
        testPrimitivesRoundTrip();
        testSize();
        testWriteBytesChars();
        testWriteUTF();
        testFlush();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
