/*
 * @test
 * @summary javax.imageio.stream.ImageOutputStreamImpl conformance: abstract hierarchy and methods.
 * @run main ImageOutputStreamImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageOutputStreamImpl;

public class ImageOutputStreamImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingAction { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingAction r, String msg) {
        boolean threw = false;
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { threw = true; caught = t; }
        check(threw, msg + " should throw");
        check(caught.getClass() == expected,
              msg + " expected " + expected.getName() + " but got " + caught.getClass().getName());
    }

    // Minimal checked subclass: write paths call checkClosed like the JDK's own subclasses do.
    static class CheckedByteArrayImageOutputStream extends ImageOutputStreamImpl {
        final byte[] data;
        CheckedByteArrayImageOutputStream(int size) { data = new byte[size]; }
        public int read() throws IOException {
            checkClosed();
            bitOffset = 0;
            if (streamPos >= data.length) return -1;
            return data[(int) streamPos++] & 0xff;
        }
        public int read(byte[] b, int off, int len) throws IOException {
            checkClosed();
            bitOffset = 0;
            if (b == null) throw new NullPointerException("b == null!");
            if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
                throw new IndexOutOfBoundsException("off < 0 || len < 0!");
            }
            if (len == 0) return 0;
            if (streamPos >= data.length) return -1;
            int n = (int) Math.min(len, data.length - streamPos);
            System.arraycopy(data, (int) streamPos, b, off, n);
            streamPos += n;
            return n;
        }
        public void write(int b) throws IOException {
            flushBits();
            checkClosed();
            data[(int) streamPos++] = (byte) b;
        }
        public void write(byte[] b, int off, int len) throws IOException {
            flushBits();
            checkClosed();
            if (b == null) throw new NullPointerException("b == null!");
            if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
                throw new IndexOutOfBoundsException("off < 0 || len < 0!");
            }
            System.arraycopy(b, off, data, (int) streamPos, len);
            streamPos += len;
        }
        public long length() { return data.length; }
    }

    // ---- Part 2: white-box structure ----
    static void testWhiteBoxExtra() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStreamImpl");
        check(c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"),
              "superclass is ImageInputStreamImpl");
        Method fb = c.getDeclaredMethod("flushBits");
        check(Modifier.isProtected(fb.getModifiers()) && Modifier.isFinal(fb.getModifiers()),
              "flushBits is protected final");
        check(fb.getExceptionTypes().length == 1
              && fb.getExceptionTypes()[0] == IOException.class, "flushBits throws IOException");
        Method w1 = c.getDeclaredMethod("write", int.class);
        Method w2 = c.getDeclaredMethod("write", byte[].class, int.class, int.class);
        check(Modifier.isAbstract(w1.getModifiers()), "write(int) abstract");
        check(Modifier.isAbstract(w2.getModifiers()), "write(byte[],int,int) abstract");
        Method w3 = c.getDeclaredMethod("write", byte[].class);
        check(!Modifier.isAbstract(w3.getModifiers()), "write(byte[]) concrete");
        check(c.getConstructor().getParameterTypes().length == 0, "public no-arg constructor");
        boolean hasFinalize = false;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("finalize")) hasFinalize = true;
        check(!hasFinalize, "ImageOutputStreamImpl declares no finalize()");
        // protected state is inherited from ImageInputStreamImpl
        Class<?> sup = c.getSuperclass();
        boolean hasStreamPos = false, hasBitOffset = false;
        for (Field f : sup.getDeclaredFields()) {
            if (f.getName().equals("streamPos") && Modifier.isProtected(f.getModifiers())) hasStreamPos = true;
            if (f.getName().equals("bitOffset") && Modifier.isProtected(f.getModifiers())) hasBitOffset = true;
        }
        check(hasStreamPos, "inherits protected streamPos");
        check(hasBitOffset, "inherits protected bitOffset");
    }

    static String hex(byte[] b, int off, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(String.format("%02X", b[off + i]));
        return sb.toString();
    }

    // ---- Part 3: byte-order layouts of every write width ----
    static void testWriteByteOrders() throws Exception {
        CheckedByteArrayImageOutputStream os = new CheckedByteArrayImageOutputStream(64);
        os.writeShort(0x1234);
        os.writeInt(0x12345678);
        os.writeLong(0x0102030405060708L);
        os.writeFloat(1.5f);
        os.writeDouble(2.25);
        check(os.getStreamPosition() == 2 + 4 + 8 + 4 + 8L, "BIG widths 2+4+8+4+8");
        check("1234".equals(hex(os.data, 0, 2)), "writeShort BIG bytes");
        check("12345678".equals(hex(os.data, 2, 4)), "writeInt BIG bytes");
        check("0102030405060708".equals(hex(os.data, 6, 8)), "writeLong BIG bytes");
        check("3FC00000".equals(hex(os.data, 14, 4)), "writeFloat BIG bits of 1.5f");
        check("4002000000000000".equals(hex(os.data, 18, 8)), "writeDouble BIG bits of 2.25");
        // read back with same order through the inherited input side
        os.seek(0);
        check(os.readShort() == 0x1234, "readShort back BIG");
        check(os.readInt() == 0x12345678, "readInt back BIG");
        check(os.readLong() == 0x0102030405060708L, "readLong back BIG");
        check(os.readFloat() == 1.5f, "readFloat back BIG");
        check(os.readDouble() == 2.25, "readDouble back BIG");

        CheckedByteArrayImageOutputStream os2 = new CheckedByteArrayImageOutputStream(64);
        os2.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os2.writeShort(0x1234);
        os2.writeInt(0x12345678);
        os2.writeLong(0x0102030405060708L);
        os2.writeFloat(1.5f);
        os2.writeDouble(2.25);
        check("3412".equals(hex(os2.data, 0, 2)), "writeShort LITTLE bytes");
        check("78563412".equals(hex(os2.data, 2, 4)), "writeInt LITTLE bytes");
        check("0807060504030201".equals(hex(os2.data, 6, 8)), "writeLong LITTLE bytes");
        check("0000C03F".equals(hex(os2.data, 14, 4)), "writeFloat LITTLE bits");
        check("0000000000000240".equals(hex(os2.data, 18, 8)), "writeDouble LITTLE bits");
        // self-consistent read back under LITTLE
        os2.seek(0);
        check(os2.readShort() == 0x1234, "readShort back LITTLE");
        check(os2.readInt() == 0x12345678, "readInt back LITTLE");
        check(os2.readLong() == 0x0102030405060708L, "readLong back LITTLE");
        // cross-order: LITTLE-written int read as BIG swaps halves
        os2.setByteOrder(ByteOrder.BIG_ENDIAN);
        os2.seek(2);
        check(os2.readInt() == 0x78563412, "LITTLE bytes interpreted as BIG");
        // setByteOrder(null) tolerated
        os2.setByteOrder(null);
        check(os2.getByteOrder() == null, "setByteOrder(null) stored verbatim");
        os2.setByteOrder(ByteOrder.BIG_ENDIAN);

        // writeBoolean/writeByte/writeChar/writeBytes/writeChars layouts
        CheckedByteArrayImageOutputStream os3 = new CheckedByteArrayImageOutputStream(64);
        os3.writeBoolean(true);
        os3.writeBoolean(false);
        os3.writeByte(0xFF);
        os3.writeChar('Z');
        os3.writeBytes("AŁB"); // low byte of U+0141 is 0x41
        check("0100FF005A414142".equals(hex(os3.data, 0, 8)), "boolean/byte/char/bytes layouts");
        check(os3.getStreamPosition() == 8L, "widths after primitive writes");
        os3.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os3.writeChar('Z');
        check("5A00".equals(hex(os3.data, 8, 2)), "writeChar LITTLE bytes");
        os3.setByteOrder(ByteOrder.BIG_ENDIAN);
        os3.writeChars("AB");
        check("00410042".equals(hex(os3.data, 10, 4)), "writeChars(String) BIG bytes");
        os3.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os3.writeChars("CD");
        check("43004400".equals(hex(os3.data, 14, 4)), "writeChars(String) LITTLE bytes");
    }

    // ---- Part 4: writeUTF contract ----
    static void testWriteUTFContract() throws Exception {
        CheckedByteArrayImageOutputStream os = new CheckedByteArrayImageOutputStream(64);
        os.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os.writeUTF("hi");
        check("00026869".equals(hex(os.data, 0, 4)), "writeUTF prefix is big-endian under LITTLE order");
        check(os.getStreamPosition() == 4L, "writeUTF width = 2 + payload");
        os.seek(0);
        check("hi".equals(os.readUTF()), "writeUTF/readUTF round trip under LITTLE order");
        check(os.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "byte order untouched by write/readUTF");
        // modified UTF-8 widths: NUL = 2 bytes, U+0800.. = 3 bytes
        os.writeUTF("a\u0000bࠀ");
        check(os.getStreamPosition() == 4L + 2 + 7, "modified-UTF8 width accounting");
        os.seek(4);
        check("a\u0000bࠀ".equals(os.readUTF()), "NUL + 3-byte round trip");
        os.writeUTF("");
        check("0000".equals(hex(os.data, (int) os.getStreamPosition() - 2, 2)), "writeUTF empty emits zero prefix");

        // utflen > 65535 rejected; nothing written
        final CheckedByteArrayImageOutputStream big = new CheckedByteArrayImageOutputStream(70000);
        final StringBuilder ascii = new StringBuilder();
        for (int i = 0; i < 65536; i++) ascii.append('a');
        expectExact(java.io.UTFDataFormatException.class,
                    new ThrowingAction() { public void run() throws Exception { big.writeUTF(ascii.toString()); } },
                    "writeUTF ascii utflen 65536");
        check(big.getStreamPosition() == 0L, "rejected writeUTF writes nothing");
        final StringBuilder wide = new StringBuilder();
        for (int i = 0; i < 32768; i++) wide.append('ࠀ');
        expectExact(java.io.UTFDataFormatException.class,
                    new ThrowingAction() { public void run() throws Exception { big.writeUTF(wide.toString()); } },
                    "writeUTF 32768 x U+0800 utflen 65536");
        // utflen == 65535 boundary accepted and readable back
        StringBuilder edge = new StringBuilder();
        for (int i = 0; i < 65535; i++) edge.append('a');
        big.writeUTF(edge.toString());
        check(big.getStreamPosition() == 65537L, "writeUTF utflen 65535 accepted");
        big.seek(0);
        String rt = big.readUTF();
        check(rt.length() == 65535 && rt.charAt(0) == 'a' && rt.charAt(65534) == 'a',
              "65535-char round trip");
        big.close();
    }

    // ---- Part 5: bit-level writing ----
    static void testBitWriting() throws Exception {
        // writeBit followed by write(int) zero-pads the partial byte
        CheckedByteArrayImageOutputStream os = new CheckedByteArrayImageOutputStream(16);
        os.writeBit(1);
        check(os.getStreamPosition() == 0L && os.getBitOffset() == 1, "writeBit holds position inside byte");
        os.write(96);
        check("8060".equals(hex(os.data, 0, 2)), "writeBit(1) then write(96) pads to 0x80");
        check(os.getStreamPosition() == 2L && os.getBitOffset() == 0, "bit offset cleared after byte write");

        // writeBit masks its argument to a single bit
        CheckedByteArrayImageOutputStream os2 = new CheckedByteArrayImageOutputStream(16);
        os2.writeBit(3);
        check(os2.getBitOffset() == 1, "writeBit consumes exactly one bit");
        check((os2.data[0] & 0xFF) == 0x80, "writeBit(3) stores the low bit (1)");
        os2.writeBits(1L, 2); // bits 1..2 hold 01
        check(os2.getStreamPosition() == 0L && os2.getBitOffset() == 3, "writeBits continues in same byte");
        os2.writeBits(-1L, 5); // fill bits 3..7 with 1s
        check(os2.getStreamPosition() == 1L && os2.getBitOffset() == 0, "byte completed on bit boundary");
        check("BF".equals(hex(os2.data, 0, 1)), "bit pattern 1,01,11111 == 0xBF");
        os2.writeBits(0xFL, 4);
        check(os2.getStreamPosition() == 1L && os2.getBitOffset() == 4, "writeBits(4) opens next byte");
        os2.writeByte(0x5A);
        check("F05A".equals(hex(os2.data, 1, 2)), "writeByte pads remaining bits with zeros");

        // writeBits(10, 3) continuation from bitOffset 3 (OpenJDK 4507868 scenario)
        CheckedByteArrayImageOutputStream os3 = new CheckedByteArrayImageOutputStream(16);
        os3.setBitOffset(3);
        os3.writeBits(10, 3);
        check("08".equals(hex(os3.data, 0, 1)), "writeBits(10,3) at bitOffset 3 notches into byte");
        check(os3.getStreamPosition() == 0L && os3.getBitOffset() == 6, "bookkeeping after notch");
        os3.writeBits(10, 3);
        check("0900".equals(hex(os3.data, 0, 2)), "second writeBits completes byte and spills 1 bit");
        check(os3.getStreamPosition() == 1L && os3.getBitOffset() == 1, "bookkeeping after spill");

        // full 64-bit write
        CheckedByteArrayImageOutputStream os4 = new CheckedByteArrayImageOutputStream(16);
        os4.writeBits(-1L, 64);
        check("FFFFFFFFFFFFFFFF".equals(hex(os4.data, 0, 8)), "writeBits(-1,64) fills 8 bytes");
        check(os4.getStreamPosition() == 8L && os4.getBitOffset() == 0, "writeBits(64) bookkeeping");

        // overwriting bits inside existing bytes preserves neighbours
        CheckedByteArrayImageOutputStream os5 = new CheckedByteArrayImageOutputStream(16);
        os5.writeByte(0xFF);
        os5.seek(0);
        os5.setBitOffset(4);
        os5.writeBits(0L, 4);
        check("F0".equals(hex(os5.data, 0, 1)), "writeBits clears low nibble, keeps high");
        check(os5.getStreamPosition() == 1L && os5.getBitOffset() == 0, "mid-byte write completes byte");
        os5.writeByte(0xFF);
        os5.writeByte(0xFF);
        os5.seek(1);
        os5.setBitOffset(4);
        os5.writeBits(0L, 8);
        check("F0F00F".equals(hex(os5.data, 0, 3)), "cross-byte writeBits preserves both ends");
        check(os5.getStreamPosition() == 2L && os5.getBitOffset() == 4, "cross-byte bookkeeping");

        // validation
        final CheckedByteArrayImageOutputStream os6 = new CheckedByteArrayImageOutputStream(16);
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { os6.writeBits(1L, -1); } },
                    "writeBits numBits -1");
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { os6.writeBits(1L, 65); } },
                    "writeBits numBits 65");
        os6.writeBits(0xFFL, 0);
        check(os6.getStreamPosition() == 0L && os6.getBitOffset() == 0, "writeBits(_,0) is a no-op");
        os6.writeBit(-1); // argument masked to its low bit, no validation
        check(os6.getBitOffset() == 1, "writeBit(-1) stores bit 1 and advances");
        check((os6.data[0] & 0xFF) == 0x80, "writeBit(-1) wrote a set high bit");
        os6.setBitOffset(0);
    }

    // ---- Part 6: array writes in both orders, bounds, zero-length ----
    static void testArrayWrites() throws Exception {
        CheckedByteArrayImageOutputStream os = new CheckedByteArrayImageOutputStream(64);
        os.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        os.writeShorts(new short[]{0x0201, 0x0403}, 0, 2);
        os.writeChars(new char[]{'A', 'B'}, 0, 2);
        os.writeInts(new int[]{0x04030201}, 0, 1);
        os.writeLongs(new long[]{0x0807060504030201L}, 0, 1);
        os.writeFloats(new float[]{1.5f}, 0, 1);
        os.writeDoubles(new double[]{2.25}, 0, 1);
        check(("0102030441004200010203040102030405060708" + "0000C03F" + "0000000000000240")
              .equals(hex(os.data, 0, 2 * 2 + 2 * 2 + 4 + 8 + 4 + 8)), "LITTLE array write layouts");
        os.seek(0);
        short[] sh = new short[2];
        os.readFully(sh, 0, 2);
        check(sh[0] == 0x0201 && sh[1] == 0x0403, "writeShorts LITTLE round trip");
        char[] ch = new char[2];
        os.readFully(ch, 0, 2);
        check(ch[0] == 'A' && ch[1] == 'B', "writeChars(char[]) LITTLE round trip");
        int[] ia = new int[1];
        os.readFully(ia, 0, 1);
        check(ia[0] == 0x04030201, "writeInts LITTLE round trip");
        long[] la = new long[1];
        os.readFully(la, 0, 1);
        check(la[0] == 0x0807060504030201L, "writeLongs LITTLE round trip");
        float[] fa = new float[1];
        os.readFully(fa, 0, 1);
        check(fa[0] == 1.5f, "writeFloats LITTLE round trip");
        double[] da = new double[1];
        os.readFully(da, 0, 1);
        check(da[0] == 2.25, "writeDoubles LITTLE round trip");

        // off/len bounds for all six array writers
        final CheckedByteArrayImageOutputStream os2 = new CheckedByteArrayImageOutputStream(64);
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeShorts(new short[2], -1, 1); } }, "writeShorts off<0");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeShorts(new short[2], 0, 3); } }, "writeShorts len>length");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeChars(new char[2], -1, 1); } }, "writeChars off<0");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeInts(new int[2], 0, 3); } }, "writeInts len>length");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeLongs(new long[2], 0, 3); } }, "writeLongs len>length");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeFloats(new float[2], 0, 3); } }, "writeFloats len>length");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { os2.writeDoubles(new double[2], 0, 3); } }, "writeDoubles len>length");
        // zero-length writes emit nothing
        os2.writeShorts(new short[3], 1, 0);
        os2.writeChars(new char[3], 1, 0);
        os2.writeInts(new int[3], 1, 0);
        os2.writeLongs(new long[3], 1, 0);
        os2.writeFloats(new float[3], 1, 0);
        os2.writeDoubles(new double[3], 1, 0);
        check(os2.getStreamPosition() == 0L, "zero-length array writes emit nothing");
        // write(byte[]) delegates to write(byte[],int,int)
        os2.write(new byte[]{9, 8, 7});
        check("090807".equals(hex(os2.data, 0, 3)), "write(byte[]) full array");
        check(os2.getStreamPosition() == 3L, "write(byte[]) position");
        os2.write(new byte[]{0, 6, 5, 4}, 1, 2);
        check("0908070605".equals(hex(os2.data, 0, 5)), "write(byte[],off,len) honors window");
    }

    // ---- Part 7: random access, flush and close on the write side ----
    static void testRandomAccessFlushClose() throws Exception {
        // overwrite through seek
        CheckedByteArrayImageOutputStream os = new CheckedByteArrayImageOutputStream(16);
        os.writeInt(0x11223344);
        os.writeInt(0x55667788);
        os.seek(2);
        os.writeShort(0xABCD);
        check("1122ABCD55667788".equals(hex(os.data, 0, 8)), "seek + writeShort overwrites in place");
        check(os.getStreamPosition() == 4L, "position after overwrite");
        // read/write interleaving
        os.seek(0);
        check(os.readByte() == 0x11, "read intermixed with write");
        os.writeByte(0xEE); // overwrites byte 1
        check("11EE".equals(hex(os.data, 0, 2)), "write after read continues at position");

        // flushBefore constraints
        CheckedByteArrayImageOutputStream os2 = new CheckedByteArrayImageOutputStream(16);
        os2.writeInt(0x11223344);
        os2.flushBefore(2);
        check(os2.getFlushedPosition() == 2L, "flushBefore sets flushed position");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os2.flushBefore(1); } },
                    "flushBefore below flushed position");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os2.flushBefore(100); } },
                    "flushBefore into the future");
        expectExact(IndexOutOfBoundsException.class,
                    new ThrowingAction() { public void run() throws Exception { os2.seek(1); } },
                    "seek into flushed region");
        // writing into the flushed region is not rejected by the base class itself
        os2.seek(3);
        os2.writeByte(0x77);
        check((os2.data[3] & 0xFF) == 0x77, "write above flushed position works");

        // close contract on the write side
        final CheckedByteArrayImageOutputStream os3 = new CheckedByteArrayImageOutputStream(16);
        os3.writeByte(0x11);
        os3.mark();
        os3.close();
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.write(1); } }, "write(int) after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.write(new byte[1]); } }, "write(byte[]) after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.writeInt(1); } }, "writeInt after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.writeShort(1); } }, "writeShort after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.writeUTF("x"); } }, "writeUTF after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.writeBit(1); } }, "writeBit after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.writeShorts(new short[1], 0, 1); } }, "writeShorts after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.seek(0); } }, "seek after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.getStreamPosition(); } }, "getStreamPosition after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { os3.close(); } }, "second close");
        check(os3.length() == 16L, "length unaffected by close");
        check(os3.getFlushedPosition() == 0L, "getFlushedPosition unaffected by close");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStreamImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"), "extends javax.imageio.stream.ImageInputStreamImpl");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageOutputStream"), "implements javax.imageio.stream.ImageOutputStream");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("writeBoolean"), "method writeBoolean");
        check(dm.contains("writeByte"), "method writeByte");
        check(dm.contains("writeShort"), "method writeShort");
        check(dm.contains("writeChar"), "method writeChar");
        check(dm.contains("writeInt"), "method writeInt");
        check(dm.contains("writeLong"), "method writeLong");
        check(dm.contains("writeFloat"), "method writeFloat");
        check(dm.contains("writeDouble"), "method writeDouble");
        check(dm.contains("writeBytes"), "method writeBytes");
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeUTF"), "method writeUTF");
        check(dm.contains("writeShorts"), "method writeShorts");
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeInts"), "method writeInts");
        check(dm.contains("writeLongs"), "method writeLongs");
        check(dm.contains("writeFloats"), "method writeFloats");
        check(dm.contains("writeDoubles"), "method writeDoubles");
        check(dm.contains("writeBit"), "method writeBit");
        check(dm.contains("writeBits"), "method writeBits");
        testWhiteBoxExtra();
        testWriteByteOrders();
        testWriteUTFContract();
        testBitWriting();
        testArrayWrites();
        testRandomAccessFlushClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
