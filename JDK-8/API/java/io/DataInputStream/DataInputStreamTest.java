/*
 * @test
 * @summary java.io.DataInputStream API conformance: full contract coverage.
 *          Constructor null semantics, read primitive round-trip against
 *          DataOutputStream, unsigned byte/short, EOFException for every readXxx
 *          at end of stream, readFully (exact/EOF/len<0->IOBE/null->NPE),
 *          skipBytes actual-count, readUTF modified-UTF-8 round-trip + static
 *          readUTF(DataInput), readLine, read(byte[],off,len) validation.
 *          Deepening: boundary-value round-trip matrix (MIN/MAX/-0.0/NaN/Inf),
 *          exact BIG-ENDIAN byte order from hand-built bytes, hand-crafted
 *          modified-UTF-8 decoding (2/3-byte/CESU-8 surrogate/malformed/short),
 *          writeUTF 65535-byte boundary, readLine terminator matrix, readFully
 *          prefix preservation, closed-stream behavior, static readUTF over
 *          RandomAccessFile.
 * @run main DataInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;

public class DataInputStreamTest {
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

    static DataInputStream stream(byte[] data) {
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    // ---- 1. constructor null semantics ----
    static void testConstructorNull() throws Exception {
        // constructor does not eagerly validate; first read hits NPE on null delegate
        DataInputStream dis = new DataInputStream(null);
        checkThrows(NullPointerException.class, () -> dis.readByte(), "readByte on null-backed");
    }

    // ---- 2. primitive round-trip ----
    static void testReadPrimitivesRoundTrip() throws Exception {
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

        DataInputStream dis = stream(bos.toByteArray());
        check(dis.readBoolean(), "readBoolean true");
        check(!dis.readBoolean(), "readBoolean false");
        check(dis.readByte() == 0x7F, "readByte");
        check(dis.readShort() == 0x1234, "readShort");
        check(dis.readChar() == 'Z', "readChar");
        check(dis.readInt() == 0x12345678, "readInt");
        check(dis.readLong() == 0x1234567890ABCDEFL, "readLong");
        check(dis.readFloat() == 1.5f, "readFloat");
        check(dis.readDouble() == -2.25, "readDouble");
    }

    // ---- 3. unsigned reads ----
    static void testUnsignedReads() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(0xFF);
        dos.writeShort(0xFFFF);
        dos.flush();
        DataInputStream dis = stream(bos.toByteArray());
        check(dis.readUnsignedByte() == 255, "readUnsignedByte 0xFF -> 255");
        check(dis.readUnsignedShort() == 65535, "readUnsignedShort 0xFFFF -> 65535");
    }

    // ---- 4. EOFException for every readXxx ----
    static void testEOFExceptions() {
        final DataInputStream empty = stream(new byte[0]);
        checkThrows(EOFException.class, () -> empty.readBoolean(), "readBoolean EOF");
        checkThrows(EOFException.class, () -> empty.readByte(), "readByte EOF");
        checkThrows(EOFException.class, () -> empty.readUnsignedByte(), "readUnsignedByte EOF");
        checkThrows(EOFException.class, () -> empty.readShort(), "readShort EOF");
        checkThrows(EOFException.class, () -> empty.readUnsignedShort(), "readUnsignedShort EOF");
        checkThrows(EOFException.class, () -> empty.readChar(), "readChar EOF");
        checkThrows(EOFException.class, () -> empty.readInt(), "readInt EOF");
        checkThrows(EOFException.class, () -> empty.readLong(), "readLong EOF");
        checkThrows(EOFException.class, () -> empty.readFloat(), "readFloat EOF");
        checkThrows(EOFException.class, () -> empty.readDouble(), "readDouble EOF");
        checkThrows(EOFException.class, () -> empty.readUTF(), "readUTF EOF");

        // truncated: one byte where two are needed
        final DataInputStream oneByte = stream(new byte[]{0x00});
        checkThrows(EOFException.class, () -> oneByte.readShort(), "readShort truncated");
    }

    // ---- 5. readFully ----
    static void testReadFully() throws Exception {
        // exact fill
        DataInputStream dis = stream(new byte[]{1, 2, 3, 4});
        byte[] b = new byte[4];
        dis.readFully(b);
        check(b[0] == 1 && b[1] == 2 && b[2] == 3 && b[3] == 4, "readFully exact");

        // offset/length fill
        DataInputStream off = stream(new byte[]{10, 20, 30});
        byte[] ob = new byte[4];
        off.readFully(ob, 1, 2);
        check(ob[0] == 0 && ob[1] == 10 && ob[2] == 20 && ob[3] == 0, "readFully(off,len)");

        // short stream -> EOFException
        final DataInputStream shortIn = stream(new byte[]{1, 2});
        checkThrows(EOFException.class, () -> shortIn.readFully(new byte[3]),
                    "readFully short -> EOFException");

        // len < 0 -> IndexOutOfBoundsException
        final DataInputStream neg = stream(new byte[]{1, 2, 3});
        checkThrows(IndexOutOfBoundsException.class, () -> neg.readFully(new byte[3], 0, -1),
                    "readFully len<0 -> IndexOutOfBoundsException");

        // null -> NPE
        final DataInputStream nul = stream(new byte[]{1, 2, 3});
        checkThrows(NullPointerException.class, () -> nul.readFully(null, 0, 1),
                    "readFully null -> NPE");
    }

    // ---- 6. skipBytes ----
    static void testSkipBytes() throws Exception {
        DataInputStream dis = stream(new byte[]{1, 2, 3, 4, 5});
        check(dis.skipBytes(2) == 2, "skipBytes(2)");
        check(dis.readByte() == 3, "read after skipBytes");
        check(dis.skipBytes(100) == 2, "skipBytes past end returns available");
        check(dis.skipBytes(1) == 0, "skipBytes at EOF returns 0");
    }

    // ---- 7. readUTF ----
    static void testReadUTF() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF("");
        dos.writeUTF("hello");
        dos.writeUTF("héllo é中文");
        dos.flush();

        DataInputStream dis = stream(bos.toByteArray());
        check("".equals(dis.readUTF()), "readUTF empty");
        check("hello".equals(dis.readUTF()), "readUTF ascii");
        check("héllo é中文".equals(dis.readUTF()), "readUTF non-ASCII modified UTF-8");

        // static readUTF(DataInput)
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        DataOutputStream d2 = new DataOutputStream(b2);
        d2.writeUTF("static");
        d2.flush();
        DataInputStream s = stream(b2.toByteArray());
        check("static".equals(DataInputStream.readUTF(s)), "static readUTF(DataInput)");
    }

    // ---- 8. readLine (deprecated but present) ----
    static void testReadLine() throws Exception {
        DataInputStream dis = stream("one\ntwo\r\nthree".getBytes("US-ASCII"));
        check("one".equals(dis.readLine()), "readLine first");
        check("two".equals(dis.readLine()), "readLine second (CRLF)");
        check("three".equals(dis.readLine()), "readLine third (EOF)");
    }

    // ---- 9. read(byte[],off,len) validation ----
    static void testReadArrayValidation() {
        final DataInputStream dis = stream(new byte[]{1, 2, 3});
        checkThrows(NullPointerException.class, () -> dis.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> dis.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
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

    // ---- 10. boundary-value round-trip matrix (JCK-style value tables) ----
    static void testBoundaryRoundTrip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBoolean(true);
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
        dos.writeFloat(Float.POSITIVE_INFINITY);
        dos.writeDouble(0.0);
        dos.writeDouble(-0.0);
        dos.writeDouble(Double.NaN);
        dos.writeDouble(Double.NEGATIVE_INFINITY);
        dos.writeDouble(Double.POSITIVE_INFINITY);
        dos.write(0x00);
        dos.flush();

        DataInputStream dis = stream(bos.toByteArray());
        check(dis.readBoolean(), "boundary boolean");
        check(dis.readByte() == Byte.MIN_VALUE, "readByte Byte.MIN_VALUE");
        check(dis.readByte() == Byte.MAX_VALUE, "readByte Byte.MAX_VALUE");
        check(dis.readShort() == Short.MIN_VALUE, "readShort Short.MIN_VALUE");
        check(dis.readShort() == Short.MAX_VALUE, "readShort Short.MAX_VALUE");
        check(dis.readChar() == '\u0000', "readChar \\u0000");
        check(dis.readChar() == '￿', "readChar \\uFFFF");
        check(dis.readInt() == Integer.MIN_VALUE, "readInt Integer.MIN_VALUE");
        check(dis.readInt() == Integer.MAX_VALUE, "readInt Integer.MAX_VALUE");
        check(dis.readLong() == Long.MIN_VALUE, "readLong Long.MIN_VALUE");
        check(dis.readLong() == Long.MAX_VALUE, "readLong Long.MAX_VALUE");
        float f1 = dis.readFloat();
        float fnz = dis.readFloat();
        float fnan = dis.readFloat();
        float fninf = dis.readFloat();
        float fpinf = dis.readFloat();
        check(f1 == 0.0f && 1.0f / f1 == Float.POSITIVE_INFINITY, "readFloat +0.0 (sign bit clear)");
        check(Float.floatToRawIntBits(fnz) == 0x80000000, "readFloat -0.0 exact bit pattern");
        check(Float.isNaN(fnan), "readFloat NaN");
        check(fninf == Float.NEGATIVE_INFINITY, "readFloat -Infinity");
        check(fpinf == Float.POSITIVE_INFINITY, "readFloat +Infinity");
        double d1 = dis.readDouble();
        double dnz = dis.readDouble();
        double dnan = dis.readDouble();
        double dninf = dis.readDouble();
        double dpinf = dis.readDouble();
        check(d1 == 0.0 && 1.0 / d1 == Double.POSITIVE_INFINITY, "readDouble +0.0 (sign bit clear)");
        check(Double.doubleToRawLongBits(dnz) == 0x8000000000000000L, "readDouble -0.0 exact bits");
        check(Double.isNaN(dnan), "readDouble NaN");
        check(dninf == Double.NEGATIVE_INFINITY, "readDouble -Infinity");
        check(dpinf == Double.POSITIVE_INFINITY, "readDouble +Infinity");
        check(dis.read() == 0, "read() single unsigned byte");
    }

    // ---- 11. exact BIG-ENDIAN byte order from hand-built bytes ----
    static void testByteOrderExact() throws Exception {
        check(stream(new byte[]{0x12, 0x34, 0x56, 0x78}).readInt() == 0x12345678,
              "readInt {12 34 56 78} -> 0x12345678 (BIG-ENDIAN fixed)");
        check(stream(new byte[]{0x12, 0x34}).readShort() == 0x1234,
              "readShort {12 34} -> 0x1234");
        check(stream(new byte[]{(byte) 0x80, 0x00}).readShort() == Short.MIN_VALUE,
              "readShort {80 00} -> Short.MIN_VALUE");
        check(stream(new byte[]{0x12, 0x34}).readUnsignedShort() == 0x1234,
              "readUnsignedShort byte order");
        check(stream(new byte[]{0x00, 0x41}).readChar() == 'A',
              "readChar {00 41} -> 'A'");
        check(stream(new byte[]{(byte) 0xFF, (byte) 0xFF}).readChar() == '￿',
              "readChar {FF FF} -> \\uFFFF");
        check(stream(new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x87, 0x65, 0x43, 0x21})
                  .readLong() == 0x1234567887654321L,
              "readLong 8-byte BIG-ENDIAN order");
        check(stream(new byte[]{0x40, 0x20, 0x00, 0x00}).readFloat() == 2.5f,
              "readFloat {40 20 00 00} -> 2.5f (Float.intBitsToFloat order)");
        check(stream(new byte[]{(byte) 0xC0, 0x04, 0, 0, 0, 0, 0, 0}).readDouble() == -2.5,
              "readDouble {C0 04 00*6} -> -2.5");
        float nanBits = stream(new byte[]{0x7F, (byte) 0xC0, 0, 0}).readFloat();
        check(Float.isNaN(nanBits), "readFloat {7F C0 00 00} -> NaN bits");
    }

    // ---- 12. readUTF against hand-crafted legal / illegal modified UTF-8 ----
    static void testReadUTFHandcrafted() throws Exception {
        check("hello".equals(stream(new byte[]{0, 5, 'h', 'e', 'l', 'l', 'o'}).readUTF()),
              "readUTF handcrafted ASCII {0005 'hello'}");
        String e = stream(new byte[]{0, 2, (byte) 0xC3, (byte) 0xA9}).readUTF();
        check(e.length() == 1 && e.charAt(0) == 'é', "readUTF 2-byte {C3 A9} -> U+00E9");
        String c = stream(new byte[]{0, 3, (byte) 0xE4, (byte) 0xB8, (byte) 0xAD}).readUTF();
        check(c.length() == 1 && c.charAt(0) == '中', "readUTF 3-byte CJK {E4 B8 AD} -> U+4E2D");
        String nul = stream(new byte[]{0, 2, (byte) 0xC0, (byte) 0x80}).readUTF();
        check(nul.length() == 1 && nul.charAt(0) == '\u0000',
              "readUTF modified-UTF-8 NUL {C0 80} -> U+0000 (never a raw 0x00 byte)");
        String sur = stream(new byte[]{0, 3, (byte) 0xED, (byte) 0xA0, (byte) 0x80}).readUTF();
        check(sur.length() == 1 && sur.charAt(0) == '\uD800',
              "readUTF CESU-8 lone surrogate {ED A0 80} -> U+D800 (no surrogate validation)");
        check("".equals(stream(new byte[]{0, 0}).readUTF()), "readUTF declared length 0 -> \"\"");
        DataInputStream extra = stream(new byte[]{0, 2, (byte) 0xC3, (byte) 0xA9, 'X'});
        check("é".equals(extra.readUTF()) && extra.available() == 1,
              "readUTF consumes exactly utflen bytes, trailing bytes stay in the stream");

        checkThrows(UTFDataFormatException.class,
              () -> stream(new byte[]{0, 1, (byte) 0x80}).readUTF(),
              "readUTF leading continuation byte 0x80 -> UTFDataFormatException");
        checkThrows(UTFDataFormatException.class,
              () -> stream(new byte[]{0, 2, (byte) 0xC3, 0x28}).readUTF(),
              "readUTF broken 2-byte sequence {C3 28} -> UTFDataFormatException");
        checkThrows(EOFException.class, () -> stream(new byte[]{0, 5, 'a', 'b'}).readUTF(),
              "readUTF body shorter than declared -> EOFException");
        checkThrows(EOFException.class,
              () -> stream(new byte[]{0, 3, (byte) 0xE4, (byte) 0xB8}).readUTF(),
              "readUTF multi-byte sequence cut at EOF -> EOFException");
        checkThrows(EOFException.class,
              () -> stream(new byte[]{(byte) 0xFF, (byte) 0xFF, 'a'}).readUTF(),
              "readUTF declared utflen 65535 with 1 byte present -> EOFException");

        String mixed = "a\u0000b中é";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new DataOutputStream(bos).writeUTF(mixed);
        check(mixed.equals(stream(bos.toByteArray()).readUTF()),
              "writeUTF/readUTF mixed NUL+latin+CJK round-trip");
        byte[] img = bos.toByteArray();
        check(img.length == 11 && (img[0] & 0xFF) == 0 && (img[1] & 0xFF) == 9,
              "writeUTF mixed encodes to 9 body bytes (1+2+1+3+2)");
    }

    // ---- 13. writeUTF/readUTF 65535-byte encoded-length boundary ----
    static void testUTFLengthBoundary() throws Exception {
        StringBuilder a65535 = new StringBuilder();
        for (int i = 0; i < 65535; i++) a65535.append('a');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(a65535.toString());
        check(dos.size() == 65537, "writeUTF of exactly 65535 encoded bytes is legal");
        check(stream(bos.toByteArray()).readUTF().length() == 65535,
              "65535-byte UTF string reads back in full");

        StringBuilder a65536 = new StringBuilder();
        for (int i = 0; i < 65536; i++) a65536.append('a');
        checkThrowsMsg(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(a65536.toString()),
              "writeUTF 65536 ascii bytes", "too long: 65536 bytes");

        StringBuilder n32767 = new StringBuilder();
        for (int i = 0; i < 32767; i++) n32767.append('\u0000');
        DataOutputStream dn = new DataOutputStream(new ByteArrayOutputStream());
        dn.writeUTF(n32767.toString());
        check(dn.size() == 65536, "32767 NUL chars encode to 65534 body bytes (2 bytes each)");
        StringBuilder n32768 = new StringBuilder(n32767);
        n32768.append('\u0000');
        checkThrowsMsg(UTFDataFormatException.class,
              () -> new DataOutputStream(new ByteArrayOutputStream()).writeUTF(n32768.toString()),
              "writeUTF 32768 NUL chars (65536 body bytes)", "too long: 65536 bytes");

        String emoji = "😀";
        ByteArrayOutputStream be = new ByteArrayOutputStream();
        new DataOutputStream(be).writeUTF(emoji);
        byte[] eb = be.toByteArray();
        check((eb[1] & 0xFF) == 6 && eb.length == 8,
              "writeUTF surrogate pair -> 6-byte CESU-8 body");
        check(emoji.equals(stream(eb).readUTF()),
              "supplementary code point round-trips as two surrogate chars");
    }

    // ---- 14. readFully prefix preservation + len 0 + precise region ----
    static void testReadFullyContract() throws Exception {
        final DataInputStream partial = stream(new byte[]{7, 8});
        final byte[] p = new byte[4];
        checkThrows(EOFException.class, () -> partial.readFully(p),
              "readFully over a short stream");
        check(p[0] == 7 && p[1] == 8 && p[2] == 0 && p[3] == 0,
              "readFully kept the bytes already transferred; remainder untouched");

        DataInputStream exact = stream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        byte[] r = new byte[8];
        exact.readFully(r, 2, 4);
        check(r[0] == 0 && r[1] == 0 && r[2] == 1 && r[5] == 4 && r[6] == 0 && r[7] == 0,
              "readFully(b,off,len) writes exactly the [off,off+len) region");
        check(exact.read() == 5, "stream position advanced by len after region readFully");

        DataInputStream eof0 = stream(new byte[0]);
        byte[] zero = new byte[2];
        eof0.readFully(zero, 0, 0);
        check(zero[0] == 0 && zero[1] == 0, "readFully len==0 at EOF is a no-op (no throw)");
        checkThrows(IndexOutOfBoundsException.class, () -> eof0.readFully(new byte[2], 1, 2),
              "readFully off+len beyond array -> IndexOutOfBoundsException");
        checkThrows(NullPointerException.class, () -> eof0.readFully((byte[]) null),
              "readFully((byte[])null) -> NPE");
    }

    // ---- 15. skipBytes negative / zero / partial ----
    static void testSkipBytesContract() throws Exception {
        DataInputStream dis = stream(new byte[]{1, 2, 3, 4, 5});
        check(dis.skipBytes(-5) == 0, "skipBytes(negative) returns 0");
        check(dis.skipBytes(0) == 0, "skipBytes(0) returns 0");
        check(dis.readByte() == 1, "negative/zero skipBytes left the position untouched");
        check(dis.skipBytes(1) == 1 && dis.readByte() == 3, "skipBytes(1) then read is precise");
        check(dis.skipBytes(100) == 2, "skipBytes clamps to the remaining bytes");
        check(dis.skipBytes(1) == 0, "skipBytes at EOF returns 0");
        checkThrows(EOFException.class, () -> dis.readByte(), "readByte after clamped skip");
    }

    // ---- 16. readLine terminator matrix (deprecated but contractual) ----
    static void testReadLineContract() throws Exception {
        DataInputStream dis = stream("x\r\r\ny\r".getBytes("US-ASCII"));
        check("x".equals(dis.readLine()), "readLine stops at \\r");
        check("".equals(dis.readLine()),
              "readLine treats \\r\\n after \\r as one terminator (empty line)");
        check("y".equals(dis.readLine()), "readLine stops at trailing \\r");
        check(dis.readLine() == null, "readLine null at EOF");
        check(stream(new byte[0]).readLine() == null, "readLine null on an empty stream");
        DataInputStream blanks = stream("\n\n".getBytes("US-ASCII"));
        check("".equals(blanks.readLine()) && "".equals(blanks.readLine()),
              "readLine yields empty lines for bare newlines");
        check(stream("tail".getBytes("US-ASCII")).readLine().equals("tail"),
              "readLine returns the final unterminated line");
        DataInputStream crlfOnly = stream("\r\n".getBytes("US-ASCII"));
        check("".equals(crlfOnly.readLine()), "readLine CRLF-only is a single empty line");
        check(crlfOnly.readLine() == null, "nothing after the CRLF-only line");
    }

    // ---- 17. close semantics (FileInputStream-backed so close is observable) ----
    static void testClosedStream() throws Exception {
        File tmp = File.createTempFile("disdeepen", ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(new byte[]{0, 0, 0, 1});
            fos.close();
            DataInputStream dis = new DataInputStream(new FileInputStream(tmp));
            check(dis.readInt() == 1, "readInt before close works");
            dis.close();
            checkThrowsMsg(IOException.class, () -> dis.read(),
                  "read() after close", "Stream Closed");
            checkThrowsMsg(IOException.class, () -> dis.readInt(),
                  "readInt() after close", "Stream Closed");
            checkThrowsMsg(IOException.class, () -> dis.readLine(),
                  "readLine() after close", "Stream Closed");
            checkThrowsMsg(IOException.class, () -> dis.available(),
                  "available() after close", "Stream Closed");
            // message text differs across releases ("Stream Closed" vs "Bad file
            // descriptor"), so this one is asserted at type level only
            checkThrows(IOException.class, () -> dis.skipBytes(1),
                  "skipBytes() after close -> IOException");
        } finally {
            tmp.delete();
        }
    }

    // ---- 18. read()/read(byte[])/available/skip delegation + double layer ----
    static void testStreamBasics() throws Exception {
        DataInputStream dis = stream(new byte[]{(byte) 0xFF, 2, 3});
        check(dis.read() == 255, "read() returns unsigned 0..255");
        check(dis.available() == 2, "available() delegates the remaining count");
        byte[] b = new byte[2];
        check(dis.read(b) == 2 && b[0] == 2 && b[1] == 3, "read(byte[]) bulk fill");
        check(dis.skip(1) == 0, "skip(1) at EOF returns 0");
        check(dis.read() == -1, "read() at EOF returns -1 (not EOFException)");
        DataInputStream dbl = new DataInputStream(
                new DataInputStream(new ByteArrayInputStream(new byte[]{0, 1})));
        check(dbl.readShort() == 1, "double-layer DataInputStream framing intact");
        check(stream(new byte[0]).available() == 0, "available 0 on an empty stream");
    }

    // ---- 19. static readUTF over RandomAccessFile (cross-implementation) ----
    static void testStaticReadUTFOverRaf() throws Exception {
        File tmp = File.createTempFile("disdeepenutf", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(new byte[]{0, 3, 'r', 'a', 'f'});
            raf.seek(0);
            check("raf".equals(DataInputStream.readUTF(raf)),
                  "static readUTF(DataInput) decodes through RandomAccessFile");
            check(raf.getFilePointer() == 5, "static readUTF advances the RAF pointer");
            checkThrows(EOFException.class, () -> DataInputStream.readUTF(raf),
                  "static readUTF at EOF -> EOFException");
        } finally {
            tmp.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testConstructorNull();
        testReadPrimitivesRoundTrip();
        testUnsignedReads();
        testEOFExceptions();
        testReadFully();
        testSkipBytes();
        testReadUTF();
        testReadLine();
        testReadArrayValidation();
        testBoundaryRoundTrip();
        testByteOrderExact();
        testReadUTFHandcrafted();
        testUTFLengthBoundary();
        testReadFullyContract();
        testSkipBytesContract();
        testReadLineContract();
        testClosedStream();
        testStreamBasics();
        testStaticReadUTFOverRaf();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
