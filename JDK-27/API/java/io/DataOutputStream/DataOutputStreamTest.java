/*
 * @test
 * @summary java.io.DataOutputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructor null semantics, write(int) low 8 bits,
 *          write(byte[],off,len) validation (NPE/IOBE), primitive round-trip, size()
 *          counter, writeBytes/writeChars layout, writeUTF length prefix + >65535 -> UTFDataFormatException
 *          + null -> NPE, flush delegation.
 *          Deepening: byte-exact layout of every writeXxx on boundary values
 *          (MIN/MAX/-0.0/NaN/Inf), writeXxx(int) low-bit truncation, size()
 *          across every operation and across flush/close, IEEE 754 bit
 *          passthrough, writeUTF 65535-byte boundary (ascii/CJK/NUL forms),
 *          counted flush propagation, write-after-close on a file sink,
 *          writeUTF framing shared with ObjectOutputStream block data, and
 *          JDK-11+ OutputStream.nullOutputStream as a sink.
 * @run main DataOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        check(hasInterface(c, "java.io.DataOutput"), "implements DataOutput");
        hasMethod(c, "writeInt", int.class);
        hasMethod(c, "writeLong", long.class);
        hasMethod(c, "writeUTF", String.class);
        hasMethod(c, "size");
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
        dos.writeBoolean(true);
        dos.writeByte(1);
        dos.writeShort(1);
        dos.writeChar('a');
        dos.writeInt(1);
        dos.writeLong(1);
        dos.writeFloat(1);
        dos.writeDouble(1);
        check(dos.size() == 30, "size sums primitive widths (1+1+2+2+4+8+4+8=30)");
        dos.writeUTF("hello");
        check(dos.size() == 37, "size after writeUTF (2 + utf length)");
    }

    // ---- 6. writeBytes / writeChars byte layout ----
    static void testWriteBytesChars() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBytes("AB");
        dos.writeChars("CD");
        byte[] b = bos.toByteArray();
        check(Arrays.equals(b, new byte[]{0x41, 0x42, 0x00, 0x43, 0x00, 0x44}),
              "writeBytes/writeChars layout");
    }

    // ---- 7. writeUTF length prefix + >65535 + null ----
    static void testWriteUTF() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF("héllo");
        byte[] b = bos.toByteArray();
        check((b[0] & 0xFF) == 0 && (b[1] & 0xFF) == 6, "writeUTF length prefix (6 UTF bytes)");
        check(b.length == 2 + 6, "writeUTF total length");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) sb.append('a');
        final String tooLong = sb.toString();
        final DataOutputStream big = new DataOutputStream(new ByteArrayOutputStream());
        checkThrows(UTFDataFormatException.class, () -> big.writeUTF(tooLong),
                    "writeUTF > 65535 bytes -> UTFDataFormatException");

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

    static final class CountingSink extends OutputStream {
        int flushes;
        int closes;
        long bytes;

        public void write(int b) { bytes++; }
        public void write(byte[] b, int off, int len) { bytes += len; }
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    // ---- 9. byte-exact layout of every writeXxx on boundary values ----
    static void testExactLayouts() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(Byte.MIN_VALUE);
        dos.writeByte(Byte.MAX_VALUE);
        dos.writeShort(Short.MIN_VALUE);
        dos.writeShort(Short.MAX_VALUE);
        dos.writeChar('\u0000');
        dos.writeChar('￿');
        dos.writeInt(Integer.MIN_VALUE);
        dos.writeInt(Integer.MAX_VALUE);
        dos.writeLong(Long.MIN_VALUE);
        dos.writeLong(Long.MAX_VALUE);
        dos.writeFloat(0.0f);
        dos.writeFloat(-0.0f);
        dos.writeFloat(Float.NaN);
        dos.writeFloat(Float.NEGATIVE_INFINITY);
        dos.writeDouble(0.0);
        dos.writeDouble(-0.0);
        dos.writeDouble(Double.NaN);
        dos.writeDouble(Double.POSITIVE_INFINITY);
        dos.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 82, "boundary image width 2+4+4+8+16+16+32 = 82 bytes");
        check((b[0] & 0xFF) == 0x80 && (b[1] & 0xFF) == 0x7F, "writeByte MIN/MAX -> 80 7F");
        check((b[2] & 0xFF) == 0x80 && b[3] == 0, "writeShort MIN -> 80 00");
        check((b[4] & 0xFF) == 0x7F && (b[5] & 0xFF) == 0xFF, "writeShort MAX -> 7F FF");
        check(b[6] == 0 && b[7] == 0, "writeChar \\u0000 -> 00 00");
        check((b[8] & 0xFF) == 0xFF && (b[9] & 0xFF) == 0xFF, "writeChar \\uFFFF -> FF FF");
        check((b[10] & 0xFF) == 0x80 && b[11] == 0 && b[12] == 0 && b[13] == 0,
              "writeInt MIN -> 80 00 00 00");
        check((b[14] & 0xFF) == 0x7F && (b[15] & 0xFF) == 0xFF && (b[16] & 0xFF) == 0xFF
                  && (b[17] & 0xFF) == 0xFF,
              "writeInt MAX -> 7F FF FF FF");
        check((b[18] & 0xFF) == 0x80 && (b[25] & 0xFF) == 0x00,
              "writeLong MIN -> 80 00*7");
        check((b[26] & 0xFF) == 0x7F && (b[33] & 0xFF) == 0xFF,
              "writeLong MAX -> 7F FF*7");
        check(b[34] == 0 && b[37] == 0, "writeFloat 0.0 -> 00 00 00 00");
        check((b[38] & 0xFF) == 0x80 && b[39] == 0 && b[41] == 0, "writeFloat -0.0 -> 80 00 00 00");
        check((b[42] & 0xFF) == 0x7F && (b[43] & 0xFF) == 0xC0 && b[44] == 0 && b[45] == 0,
              "writeFloat NaN -> 7F C0 00 00");
        check((b[46] & 0xFF) == 0xFF && (b[47] & 0xFF) == 0x80 && b[48] == 0 && b[49] == 0,
              "writeFloat -Inf -> FF 80 00 00");
        check(b[50] == 0 && b[57] == 0, "writeDouble 0.0 -> all zero bytes");
        check((b[58] & 0xFF) == 0x80 && b[59] == 0, "writeDouble -0.0 -> 80 00*7");
        check((b[66] & 0xFF) == 0x7F && (b[67] & 0xFF) == 0xF8 && b[73] == 0,
              "writeDouble NaN -> 7F F8 00*6");
        check((b[74] & 0xFF) == 0x7F && (b[75] & 0xFF) == 0xF0 && b[81] == 0,
              "writeDouble +Inf -> 7F F0 00*6");
        check(dos.size() == 82, "size() mirrors the byte-exact image");
    }

    // ---- 10. writeByte/Short/Char(int) truncation to the low bits ----
    static void testTruncation() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(0x1234);
        dos.writeShort(0x12345678);
        dos.writeChar(0x12345678);
        dos.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 5, "1 + 2 + 2 bytes emitted");
        check((b[0] & 0xFF) == 0x34, "writeByte(int) keeps only the low 8 bits");
        check((b[1] & 0xFF) == 0x56 && (b[2] & 0xFF) == 0x78,
              "writeShort(int) keeps only the low 16 bits");
        check((b[3] & 0xFF) == 0x56 && (b[4] & 0xFF) == 0x78,
              "writeChar(int) keeps only the low 16 bits");
    }

    // ---- 11. size() for every operation; flush/close never reset it ----
    static void testSizeContract() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        check(dos.size() == 0, "size starts at 0");
        dos.write(new byte[3], 0, 2);
        check(dos.size() == 2, "write(b,off,len) adds len");
        dos.write(new byte[1]);
        check(dos.size() == 3, "write(byte[]) adds its length");
        dos.writeUTF("");
        check(dos.size() == 5, "writeUTF(\"\") adds the 2 header bytes only");
        dos.writeUTF("中");
        check(dos.size() == 10, "writeUTF CJK adds 2 + 3 bytes");
        dos.writeBytes("ab");
        check(dos.size() == 12, "writeBytes adds 1 byte per char");
        dos.writeChars("ab");
        check(dos.size() == 16, "writeChars adds 2 bytes per char");
        dos.flush();
        check(dos.size() == 16, "flush does not reset size");
        dos.writeBoolean(true);
        check(dos.size() == 17, "size keeps accumulating across flush");
        check(bos.size() == 17, "counter is write-through (sink holds the same bytes)");
        dos.close();
        check(dos.size() == 17, "size readable after close, not reset");
    }

    // ---- 12. IEEE 754 bit passthrough ----
    static void testFloatDoubleBits() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeFloat(Float.intBitsToFloat(0x12345678));
        dos.writeDouble(Double.longBitsToDouble(0x123456789ABCDEFL));
        dos.flush();
        byte[] b = bos.toByteArray();
        check((b[0] & 0xFF) == 0x12 && (b[1] & 0xFF) == 0x34 && (b[2] & 0xFF) == 0x56
                  && (b[3] & 0xFF) == 0x78,
              "writeFloat emits Float.floatToIntBits verbatim, BIG-ENDIAN");
        check((b[4] & 0xFF) == 0x01 && (b[5] & 0xFF) == 0x23 && (b[6] & 0xFF) == 0x45
                  && (b[7] & 0xFF) == 0x67 && (b[8] & 0xFF) == 0x89 && (b[9] & 0xFF) == 0xAB
                  && (b[10] & 0xFF) == 0xCD && (b[11] & 0xFF) == 0xEF,
              "writeDouble emits Double.doubleToLongBits verbatim, BIG-ENDIAN");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
        check(dis.readFloat() == Float.intBitsToFloat(0x12345678),
              "float bit pattern round-trips");
        check(dis.readDouble() == Double.longBitsToDouble(0x123456789ABCDEFL),
              "double bit pattern round-trips");
    }

    // ---- 13. writeUTF encoded-length boundary (ascii / CJK / NUL forms) ----
    static void testWriteUTFBoundary() throws Exception {
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 65535; i++) a.append('a');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(a.toString());
        byte[] b = bos.toByteArray();
        check(b.length == 65537, "65535-byte encoded UTF fits (2 + 65535)");
        check((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xFF,
              "length prefix encodes 0xFFFF at the maximum");
        dos.writeUTF("");
        check(dos.size() == 65539, "writing continues normally after a max-length UTF");

        StringBuilder a2 = new StringBuilder(a);
        a2.append('a');
        checkThrowsMsg(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(a2.toString()),
              "writeUTF 65536 ascii bytes", "too long: 65536 bytes");

        StringBuilder cjk = new StringBuilder();
        for (int i = 0; i < 21845; i++) cjk.append('中');
        DataOutputStream dc = new DataOutputStream(new ByteArrayOutputStream());
        dc.writeUTF(cjk.toString());
        check(dc.size() == 65537, "21845 CJK chars = 65535 body bytes, still fits");
        StringBuilder cjk2 = new StringBuilder(cjk);
        cjk2.append('中');
        checkThrowsMsg(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(cjk2.toString()),
              "writeUTF 21846 CJK chars (65538 body bytes)", "too long: 65538 bytes");

        StringBuilder n = new StringBuilder();
        for (int i = 0; i < 32768; i++) n.append('\u0000');
        checkThrowsMsg(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(n.toString()),
              "writeUTF 32768 NUL chars (65536 body bytes)", "too long: 65536 bytes");
    }

    // ---- 14. counted flush/close propagation ----
    static void testFlushPropagation() throws Exception {
        CountingSink sink = new CountingSink();
        DataOutputStream dos = new DataOutputStream(sink);
        dos.writeByte(1);
        dos.flush();
        dos.flush();
        dos.write(new byte[4], 1, 2);
        check(sink.flushes == 2, "each explicit flush() reaches the sink exactly once");
        check(sink.bytes == 3, "sink received exactly the bytes written");
        dos.close();
        check(sink.flushes == 3, "close() flushes once before closing");
        check(sink.closes == 1, "close() closes the sink");
        dos.close();
        check(sink.closes >= 1, "second close() completes without error");
    }

    // ---- 15. close is idempotent and flushes pending bytes ----
    static void testCloseSemantics() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(0x12345678);
        dos.close();
        dos.close();
        check(bos.size() == 4, "close flushes pending bytes to the sink");
        check(dos.size() == 4, "counter readable after a double close");
    }

    // ---- 16. write after close on a FileOutputStream-backed sink ----
    static void testWriteAfterCloseFileBacked() throws Exception {
        File tmp = File.createTempFile("dosdeepen", ".dat");
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(tmp));
            dos.writeByte(1);
            dos.close();
            checkThrowsMsg(IOException.class, () -> dos.writeByte(2),
                  "writeByte after close (file sink)", "Stream Closed");
            check(tmp.length() == 1, "the rejected post-close write added no bytes");
        } finally {
            tmp.delete();
        }
    }

    // ---- 17. writeUTF framing is the serialization block-data payload ----
    static void testWriteUTFSerializationInterop() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeUTF("utf");
        oos.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 11, "OOS writeUTF emits header + one block");
        check((b[0] & 0xFF) == 0xAC && (b[1] & 0xFF) == 0xED, "stream magic ACED");
        check(b[2] == 0 && b[3] == 5, "stream version 5");
        check((b[4] & 0xFF) == 0x77, "TC_BLOCKDATA tag");
        check((b[5] & 0xFF) == 5, "block length = utflen(2) + 3 chars");
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(b, 6, b.length - 6);
        check("utf".equals(
                  new DataInputStream(new ByteArrayInputStream(frame.toByteArray())).readUTF()),
              "block-data payload is exactly the DataOutput.writeUTF framing");
    }

    // ---- 18. JDK-11+ OutputStream.nullOutputStream as a sink ----
    static void testNullOutputStreamSink() throws Exception {
        DataOutputStream dos = new DataOutputStream(OutputStream.nullOutputStream());
        dos.writeInt(0x12345678);
        check(dos.size() == 4, "counter counts writes into the null sink");
        dos.writeUTF("discard");
        dos.flush();
        check(dos.size() == 4 + 2 + 7, "flush on the null sink is a no-op, not an error");
        dos.close();
        check(true, "close on the null sink completes without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructorNull();
        testWriteInt();
        testWriteArrayValidation();
        testPrimitivesRoundTrip();
        testSize();
        testWriteBytesChars();
        testWriteUTF();
        testFlush();
        testExactLayouts();
        testTruncation();
        testSizeContract();
        testFloatDoubleBits();
        testWriteUTFBoundary();
        testFlushPropagation();
        testCloseSemantics();
        testWriteAfterCloseFileBacked();
        testWriteUTFSerializationInterop();
        testNullOutputStreamSink();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
