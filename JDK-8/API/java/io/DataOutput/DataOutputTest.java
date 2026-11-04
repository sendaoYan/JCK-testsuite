/*
 * @test
 * @summary java.io.DataOutput conformance (pure black-box): machine-independent
 *          primitive output contract exercised polymorphically through
 *          DataOutputStream and RandomAccessFile; big-endian framing asserted
 *          byte-for-byte; writeBytes vs writeChars vs writeUTF encoding
 *          differences; null and bounds checks.
 *          Deepening: cross-implementation boundary images (MIN/MAX/-0.0/NaN/
 *          Inf written through both implementations and compared byte-for-
 *          byte), writeXxx(int) truncation parity, writeUTF 65535-byte and
 *          empty-string parity, null/bounds and write-after-close parity on
 *          RAF, write(int) low-8-bits parity, and the pure-contract shape of
 *          the interface (14 abstract methods, no static/default members).
 * @run main DataOutputTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
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
        d2.writeBytes("é");        // low 8 bits of 'e' (0xE9)
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String msg, String substr) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(substr),
                      msg + ": message must contain \"" + substr + "\", got: " + t.getMessage());
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** The boundary-value write sequence shared by both implementations. */
    static void boundaryThrough(DataOutput out) throws IOException {
        out.writeByte(Byte.MIN_VALUE);
        out.writeByte(Byte.MAX_VALUE);
        out.writeShort(Short.MIN_VALUE);
        out.writeShort(Short.MAX_VALUE);
        out.writeChar('\u0000');
        out.writeChar('￿');
        out.writeInt(Integer.MIN_VALUE);
        out.writeInt(Integer.MAX_VALUE);
        out.writeLong(Long.MIN_VALUE);
        out.writeLong(Long.MAX_VALUE);
        out.writeFloat(Float.NaN);
        out.writeFloat(-0.0f);
        out.writeDouble(Double.NaN);
        out.writeDouble(-0.0);
        out.writeUTF("\u0000中");
        out.writeBytes("中");
        out.writeChars("中");
    }

    // ---- deepening: cross-implementation boundary image equality ----
    static void testCrossImplBoundaryImages() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        boundaryThrough(new DataOutputStream(bos));
        byte[] want = bos.toByteArray();
        check(want.length == 68, "boundary image width 2+4+4+8+16+8+16+7+1+2 = 68 bytes");
        check((want[34] & 0xFF) == 0x7F && (want[35] & 0xFF) == 0xC0,
              "writeFloat NaN bits at offset 34");
        check((want[38] & 0xFF) == 0x80 && want[41] == 0, "writeFloat -0.0 bits");
        check((want[42] & 0xFF) == 0x7F && (want[43] & 0xFF) == 0xF8,
              "writeDouble NaN bits at offset 42");
        check((want[50] & 0xFF) == 0x80 && want[57] == 0, "writeDouble -0.0 bits");
        check(want[58] == 0 && (want[59] & 0xFF) == 5, "writeUTF length prefix 5 (2+3 body)");
        check((want[60] & 0xFF) == 0xC0 && (want[61] & 0xFF) == 0x80,
              "writeUTF encodes NUL as {C0 80}, never a raw 0x00");
        check((want[62] & 0xFF) == 0xE4 && (want[64] & 0xFF) == 0xAD,
              "writeUTF encodes 中 as 3 bytes {E4 B8 AD}");
        check((want[65] & 0xFF) == 0x2D,
              "writeBytes truncates 中 (U+4E2D) to its low byte 0x2D");
        check((want[66] & 0xFF) == 0x4E && (want[67] & 0xFF) == 0x2D,
              "writeChars keeps the full 16 bits {4E 2D}");

        File tmp = File.createTempFile("dataoutbound", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            boundaryThrough(raf);
            check(raf.length() == want.length,
                  "RAF wrote exactly as many bytes as DataOutputStream");
            raf.seek(0);
            byte[] got = new byte[want.length];
            raf.readFully(got);
            check(Arrays.equals(got, want),
                  "RAF boundary image byte-identical to DataOutputStream");
            // read back through DataInput as end-to-end framing proof
            java.io.DataInputStream in = new java.io.DataInputStream(
                    new java.io.ByteArrayInputStream(got));
            check(in.readByte() == Byte.MIN_VALUE && in.readByte() == Byte.MAX_VALUE,
                  "RAF image parses back: byte MIN/MAX");
            check(in.readShort() == Short.MIN_VALUE && in.readShort() == Short.MAX_VALUE,
                  "RAF image parses back: short MIN/MAX");
            check(in.readChar() == '\u0000' && in.readChar() == '￿',
                  "RAF image parses back: char \\u0000 then \\uFFFF");
            check(in.readInt() == Integer.MIN_VALUE && in.readInt() == Integer.MAX_VALUE,
                  "RAF image parses back: int MIN/MAX");
            check(in.readLong() == Long.MIN_VALUE && in.readLong() == Long.MAX_VALUE,
                  "RAF image parses back: long MIN/MAX");
            check(Float.isNaN(in.readFloat()) && Float.floatToRawIntBits(in.readFloat()) == 0x80000000,
                  "RAF image parses back: float NaN then -0.0");
            check(Double.isNaN(in.readDouble()) && Double.doubleToRawLongBits(in.readDouble()) == 0x8000000000000000L,
                  "RAF image parses back: double NaN then -0.0");
            check("\u0000中".equals(in.readUTF()), "RAF image parses back: mixed UTF");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: writeXxx(int) truncation parity ----
    static void testTruncationParity() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutput d = new DataOutputStream(bos);
        d.writeByte(0x1234);
        d.writeShort(0x12345678);
        d.writeChar(0x12345678);
        check(Arrays.equals(bos.toByteArray(), new byte[]{0x34, 0x56, 0x78, 0x56, 0x78}),
              "writeByte/Short/Char keep the low 8/16/16 bits");

        File tmp = File.createTempFile("dataouttrunc", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.writeByte(0x1234);
            raf.writeShort(0x12345678);
            raf.writeChar(0x12345678);
            raf.seek(0);
            byte[] got = new byte[5];
            raf.readFully(got);
            check(Arrays.equals(got, new byte[]{0x34, 0x56, 0x78, 0x56, 0x78}),
                  "RandomAccessFile truncates identically through the DataOutput contract");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: writeUTF boundary parity ----
    static void testWriteUTFBoundaryParity() throws Exception {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 65535; i++) a.append('a');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new DataOutputStream(bos).writeUTF(a.toString());
        byte[] b = bos.toByteArray();
        check(b.length == 65537 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xFF,
              "max-length writeUTF: 65535 body bytes with prefix 0xFFFF");

        File tmp = File.createTempFile("dataoututf", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.writeUTF(a.toString());
            check(raf.length() == 65537, "RAF writeUTF of the same string: identical length");
            raf.seek(0);
            check(raf.readUnsignedShort() == 0xFFFF, "RAF length prefix identical");
            checkThrows(UTFDataFormatException.class,
                  () -> raf.writeUTF(a.toString() + "x"),
                  "RAF writeUTF 65536 bytes -> UTFDataFormatException");
            raf.setLength(0);
            raf.seek(0);
            raf.writeUTF("");
            raf.seek(0);
            check(raf.read() == 0 && raf.read() == 0, "RAF writeUTF(\"\") emits 00 00");
        } finally {
            tmp.delete();
        }

        checkThrows(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(a.toString() + "x"),
              "DOS writeUTF 65536 bytes -> UTFDataFormatException");
        ByteArrayOutputStream e = new ByteArrayOutputStream();
        new DataOutputStream(e).writeUTF("");
        check(Arrays.equals(e.toByteArray(), new byte[]{0, 0}), "writeUTF(\"\") emits 00 00");
    }

    // ---- deepening: null/bounds checks and write-after-close parity ----
    static void testNullBoundsParity() throws Exception {
        final DataOutput d = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(NullPointerException.class, () -> d.writeUTF(null), "DOS writeUTF(null) NPE");
        checkThrows(NullPointerException.class, () -> d.writeBytes(null), "DOS writeBytes(null) NPE");
        checkThrows(NullPointerException.class, () -> d.writeChars(null), "DOS writeChars(null) NPE");

        File tmp = File.createTempFile("dataoutnull", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            checkThrows(NullPointerException.class, () -> raf.writeUTF(null), "RAF writeUTF(null) NPE");
            checkThrows(NullPointerException.class, () -> raf.writeBytes(null), "RAF writeBytes(null) NPE");
            checkThrows(NullPointerException.class, () -> raf.writeChars(null), "RAF writeChars(null) NPE");
        } finally {
            tmp.delete();
        }

        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            // write a byte so the file exists; it is closed by the try block
            raf.writeByte(1);
        }
        RandomAccessFile raf2 = new RandomAccessFile(tmp, "rw");
        raf2.writeByte(1);
        raf2.close();
        checkThrowsMsg(IOException.class, () -> raf2.writeInt(1),
              "RAF writeInt after close", "Stream Closed");
        checkThrowsMsg(IOException.class, () -> raf2.read(),
              "RAF read after close", "Stream Closed");
        tmp.delete();
    }

    // ---- deepening: write(int) low-8-bits parity ----
    static void testWriteIntLowBitsParity() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new DataOutputStream(bos).write(0x1FF);
        check((bos.toByteArray()[0] & 0xFF) == 0xFF, "DOS write(0x1FF) keeps 0xFF");

        File tmp = File.createTempFile("dataoutlow", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(0x1FF);
            raf.seek(0);
            check(raf.readUnsignedByte() == 0xFF, "RAF write(0x1FF) keeps 0xFF");
            check(raf.read() == -1, "exactly one byte was written");
        } finally {
            tmp.delete();
        }
    }

    // ---- deepening: the interface itself is a pure contract ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.io.DataOutput");
        check(c.isInterface(), "DataOutput is an interface");
        Method[] ms = c.getMethods();
        int abstractCount = 0;
        int staticCount = 0;
        for (Method m : ms) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            if (Modifier.isStatic(m.getModifiers())) staticCount++;
        }
        check(staticCount == 0, "no static methods: the interface defines no factory/contract");
        check(abstractCount == ms.length, "every interface method is abstract (no defaults)");
        check(ms.length == 14, "exactly 14 methods (2 write overloads + 12 typed writes)");
        check(Arrays.asList(DataOutputStream.class.getInterfaces()).contains(c),
              "DataOutputStream implements DataOutput");
        check(Arrays.asList(RandomAccessFile.class.getInterfaces()).contains(c),
              "RandomAccessFile implements DataOutput");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testCrossImplBoundaryImages();
        testTruncationParity();
        testWriteUTFBoundaryParity();
        testNullBoundsParity();
        testWriteIntLowBitsParity();
        testStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
