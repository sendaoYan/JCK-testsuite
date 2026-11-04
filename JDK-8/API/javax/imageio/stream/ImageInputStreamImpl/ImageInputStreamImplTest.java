/*
 * @test
 * @summary javax.imageio.stream.ImageInputStreamImpl conformance: abstract hierarchy and methods.
 * @run main ImageInputStreamImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStreamImpl;

public class ImageInputStreamImplTest {
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

    // Minimal concrete subclass: only the two abstract reads plus length.
    static class ByteArrayImageInputStream extends ImageInputStreamImpl {
        final byte[] data;
        ByteArrayImageInputStream(byte[] data) { this.data = data; }
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
                throw new IndexOutOfBoundsException("off < 0 || len < 0 || off+len > b.length!");
            }
            if (len == 0) return 0;
            if (streamPos >= data.length) return -1;
            int n = (int) Math.min(len, data.length - streamPos);
            System.arraycopy(data, (int) streamPos, b, off, n);
            streamPos += n;
            return n;
        }
        public long length() { return data.length; }
    }

    // Subclass overriding nothing beyond the abstract reads: exercises the defaults.
    static class BareImageInputStream extends ImageInputStreamImpl {
        BareImageInputStream() {}
        public int read() { return -1; }
        public int read(byte[] b, int off, int len) { return -1; }
    }

    // ---- Part 2: white-box structure of the abstract class ----
    static void testWhiteBoxExtra() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStreamImpl");
        // protected state fields subclasses rely on
        boolean hasStreamPos = false, hasBitOffset = false, hasFlushedPos = false, hasByteOrder = false;
        for (Field f : c.getDeclaredFields()) {
            int m = f.getModifiers();
            if (f.getName().equals("streamPos") && Modifier.isProtected(m)) hasStreamPos = true;
            if (f.getName().equals("bitOffset") && Modifier.isProtected(m)) hasBitOffset = true;
            if (f.getName().equals("flushedPos") && Modifier.isProtected(m)) hasFlushedPos = true;
            if (f.getName().equals("byteOrder") && Modifier.isProtected(m)) hasByteOrder = true;
        }
        check(hasStreamPos, "protected field streamPos");
        check(hasBitOffset, "protected field bitOffset");
        check(hasFlushedPos, "protected field flushedPos");
        check(hasByteOrder, "protected field byteOrder");
        Method cc = c.getDeclaredMethod("checkClosed");
        check(Modifier.isProtected(cc.getModifiers()) && Modifier.isFinal(cc.getModifiers()),
              "checkClosed is protected final");
        check(cc.getExceptionTypes().length == 1
              && cc.getExceptionTypes()[0] == IOException.class, "checkClosed throws IOException");
        Method r1 = c.getDeclaredMethod("read");
        Method r2 = c.getDeclaredMethod("read", byte[].class, int.class, int.class);
        check(Modifier.isAbstract(r1.getModifiers()), "read() abstract");
        check(Modifier.isAbstract(r2.getModifiers()), "read(byte[],int,int) abstract");
        // constructor is public and takes no arguments
        check(c.getConstructor().getParameterTypes().length == 0, "public no-arg constructor");
        // JDK-8 still declares finalize() on this class (removed in later JDKs)
        boolean hasFinalize = false;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("finalize")) hasFinalize = true;
        check(hasFinalize, "JDK-8 declares finalize() on ImageInputStreamImpl");
    }

    // ---- Part 3: byte order interpretation across every width ----
    static void testByteOrderValues() throws Exception {
        byte[] raw = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0};
        ByteArrayImageInputStream big = new ByteArrayImageInputStream(raw);
        check(big.getByteOrder() == ByteOrder.BIG_ENDIAN, "default byte order BIG_ENDIAN");
        check(big.readShort() == (short) 0x1234, "readShort BIG");
        check(big.readInt() == 0x56789ABC, "readInt BIG across bytes 2-5");
        big.seek(0);
        check(big.readLong() == 0x123456789ABCDEF0L, "readLong BIG full 8 bytes");
        big.seek(0);
        check(big.readFloat() == Float.intBitsToFloat(0x12345678), "readFloat follows int order");
        big.seek(0);
        check(big.readDouble() == Double.longBitsToDouble(0x123456789ABCDEF0L), "readDouble follows long order");

        ByteArrayImageInputStream lit = new ByteArrayImageInputStream(raw);
        lit.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(lit.readShort() == (short) 0x3412, "readShort LITTLE swapped");
        lit.seek(0);
        check(lit.readInt() == 0x78563412, "readInt LITTLE swapped");
        lit.seek(0);
        check(lit.readLong() == 0xF0DEBC9A78563412L, "readLong LITTLE swapped");
        lit.seek(0);
        check(lit.readFloat() == Float.intBitsToFloat(0x78563412), "readFloat LITTLE");
        lit.seek(0);
        check(lit.readDouble() == Double.longBitsToDouble(0xF0DEBC9A78563412L), "readDouble LITTLE");

        // unsigned interpretations
        ByteArrayImageInputStream u = new ByteArrayImageInputStream(
            new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        check(u.readUnsignedByte() == 255, "readUnsignedByte 0xFF");
        check(u.readUnsignedShort() == 65535, "readUnsignedShort 0xFFFF");
        check(u.readUnsignedInt() == 4294967295L, "readUnsignedInt 0xFFFFFFFF as long");
        check(u.getStreamPosition() == 7L, "unsigned reads consumed exactly 7 bytes");

        // readFully typed arrays under LITTLE_ENDIAN
        ByteArrayImageInputStream arr = new ByteArrayImageInputStream(raw);
        arr.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        short[] sh = new short[2];
        arr.readFully(sh, 0, 2);
        check(sh[0] == (short) 0x3412 && sh[1] == (short) 0x7856, "readFully(short[]) LITTLE");
        arr.seek(0);
        char[] ch = new char[2];
        arr.readFully(ch, 0, 2);
        check(ch[0] == (char) 0x3412 && ch[1] == (char) 0x7856, "readFully(char[]) LITTLE");
        arr.seek(0);
        int[] ia = new int[2];
        arr.readFully(ia, 0, 2);
        check(ia[0] == 0x78563412 && ia[1] == 0xF0DEBC9A, "readFully(int[]) LITTLE");
        arr.seek(0);
        long[] la = new long[1];
        arr.readFully(la, 0, 1);
        check(la[0] == 0xF0DEBC9A78563412L, "readFully(long[]) LITTLE");
        arr.seek(0);
        float[] fa = new float[2];
        arr.readFully(fa, 0, 2);
        check(fa[0] == Float.intBitsToFloat(0x78563412), "readFully(float[]) LITTLE");
        arr.seek(0);
        double[] da = new double[1];
        arr.readFully(da, 0, 1);
        check(da[0] == Double.longBitsToDouble(0xF0DEBC9A78563412L), "readFully(double[]) LITTLE");

        // readUTF always decodes as big-endian and restores the previous order
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF("hé中");
        }
        final ByteArrayImageInputStream ru = new ByteArrayImageInputStream(bos.toByteArray());
        ru.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check("hé中".equals(ru.readUTF()), "readUTF ignores stream byte order");
        check(ru.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "readUTF restores byte order");
        final ByteArrayImageInputStream bad = new ByteArrayImageInputStream(
            new byte[]{0, 2, (byte) 0xC8, 0x41});
        bad.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        expectExact(java.io.UTFDataFormatException.class,
                    new ThrowingAction() { public void run() throws Exception { bad.readUTF(); } },
                    "readUTF malformed payload");
        check(bad.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "byte order restored after readUTF failure");
        final ByteArrayImageInputStream trunc = new ByteArrayImageInputStream(new byte[]{0, 5, 'a'});
        expectExact(EOFException.class,
                    new ThrowingAction() { public void run() throws Exception { trunc.readUTF(); } },
                    "readUTF truncated payload");
    }

    // ---- Part 4: bit-level reading (MSB-first, cross-byte, offsets) ----
    static void testBitOps() throws Exception {
        ByteArrayImageInputStream s = new ByteArrayImageInputStream(
            new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(s.readBit());
        check("10101011".equals(sb.toString()), "readBit MSB-first bit stream of 0xAB");
        check(s.getStreamPosition() == 1L && s.getBitOffset() == 0, "readBit consumes byte on 8th bit");
        check(s.readBit() == 1, "readBit continues into next byte");
        check(s.getStreamPosition() == 1L && s.getBitOffset() == 1, "readBit mid-byte rewinds position");
        s.setBitOffset(0);
        check(s.readBits(8) == 0xCDL, "readBits(8) aligned");
        s.seek(0);
        s.setBitOffset(4);
        check(s.readBits(8) == 0xBCL, "readBits(8) unaligned crosses byte");
        check(s.getStreamPosition() == 1L && s.getBitOffset() == 4, "readBits cross-byte position/offset");
        check(s.readBits(4) == 0xDL, "readBits(4) continues into low nibble");
        check(s.getStreamPosition() == 2L && s.getBitOffset() == 0, "back on boundary");
        ByteArrayImageInputStream s2 = new ByteArrayImageInputStream(new byte[]{(byte) 0xA5});
        s2.setBitOffset(3);
        check(s2.readBits(5) == 0x5L, "readBits(5) from offset 3 of 0xA5");
        check(s2.getStreamPosition() == 1L && s2.getBitOffset() == 0, "readBits to exact end of stream");
        ByteArrayImageInputStream s3 = new ByteArrayImageInputStream(new byte[8]);
        check(s3.readBits(0) == 0L, "readBits(0) returns 0");
        check(s3.getStreamPosition() == 0L, "readBits(0) does not advance");
        check(s3.readBits(64) == 0L, "readBits(64) maximum width");
        check(s3.getStreamPosition() == 8L, "readBits(64) consumes exactly 8 bytes");
        ByteArrayImageInputStream s4 = new ByteArrayImageInputStream(
            new byte[]{(byte) 0x80, 0x7F});
        check(s4.readBit() == 1, "readBit first bit of 0x80");
        check(s4.readUnsignedByte() == 0x80, "byte read restarts current byte discarding bit offset");
        check(s4.getStreamPosition() == 1L && s4.getBitOffset() == 0, "bit offset cleared by byte read");

        // validation
        final ByteArrayImageInputStream v = new ByteArrayImageInputStream(new byte[1]);
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { v.setBitOffset(-1); } },
                    "setBitOffset(-1)");
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { v.setBitOffset(8); } },
                    "setBitOffset(8)");
        v.setBitOffset(7);
        check(v.getBitOffset() == 7, "setBitOffset(7) accepted at boundary");
        v.setBitOffset(0);
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { v.readBits(-1); } },
                    "readBits(-1)");
        expectExact(IllegalArgumentException.class,
                    new ThrowingAction() { public void run() throws Exception { v.readBits(65); } },
                    "readBits(65)");
        final ByteArrayImageInputStream eofb = new ByteArrayImageInputStream(new byte[0]);
        expectExact(EOFException.class,
                    new ThrowingAction() { public void run() throws Exception { eofb.readBit(); } },
                    "readBit at EOF");
        expectExact(EOFException.class,
                    new ThrowingAction() { public void run() throws Exception { eofb.readBits(3); } },
                    "readBits at EOF");
        // failed setBitOffset must not corrupt current offset
        v.setBitOffset(5);
        try { v.setBitOffset(9); } catch (IllegalArgumentException ignore) {}
        check(v.getBitOffset() == 5, "failed setBitOffset leaves offset intact");
    }

    // ---- Part 5: readLine termination handling ----
    static void testReadLine() throws Exception {
        ByteArrayImageInputStream s = new ByteArrayImageInputStream(
            "ab\r\ncd\nef\rgh".getBytes("UTF-8"));
        check("ab".equals(s.readLine()), "readLine CRLF");
        check(s.getStreamPosition() == 4L, "readLine consumes both CR and LF");
        check("cd".equals(s.readLine()), "readLine LF");
        check("ef".equals(s.readLine()), "readLine bare CR");
        check(s.getStreamPosition() == 10L, "readLine bare CR peeks and rewinds one byte");
        check("gh".equals(s.readLine()), "readLine unterminated tail");
        check(s.readLine() == null, "readLine at EOF returns null");
        check(s.readLine() == null, "readLine stays null at EOF");
        ByteArrayImageInputStream e = new ByteArrayImageInputStream(new byte[0]);
        check(e.readLine() == null, "readLine empty stream null");
        ByteArrayImageInputStream cr = new ByteArrayImageInputStream(new byte[]{'x', '\r'});
        check("x".equals(cr.readLine()), "readLine CR as last byte still terminates");
    }

    // ---- Part 6: EOF and bounds matrix ----
    static void testBoundsAndEof() throws Exception {
        final ByteArrayImageInputStream empty = new ByteArrayImageInputStream(new byte[0]);
        check(empty.read() == -1, "read() at EOF returns -1");
        check(empty.read(new byte[3]) == -1, "read(byte[]) at EOF returns -1");
        check(empty.read(new byte[3], 0, 0) == 0, "read(b,off,0) returns 0 without IO");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readBoolean(); } }, "readBoolean at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readByte(); } }, "readByte at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readUnsignedByte(); } }, "readUnsignedByte at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readShort(); } }, "readShort at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readInt(); } }, "readInt at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readLong(); } }, "readLong at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readChar(); } }, "readChar at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readFloat(); } }, "readFloat at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readDouble(); } }, "readDouble at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readUnsignedShort(); } }, "readUnsignedShort at EOF");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { empty.readUnsignedInt(); } }, "readUnsignedInt at EOF");
        final ByteArrayImageInputStream one = new ByteArrayImageInputStream(new byte[]{0x12});
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { one.readShort(); } }, "readShort 1 of 2 bytes");
        final ByteArrayImageInputStream four = new ByteArrayImageInputStream(new byte[]{1, 2, 3, 4});
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { four.readLong(); } }, "readLong 4 of 8 bytes");
        // partial reads leave the position at EOF
        check(four.getStreamPosition() == 4L, "failed readLong consumed to EOF");

        final ByteArrayImageInputStream b = new ByteArrayImageInputStream(new byte[1]);
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new byte[4], -1, 2); } }, "readFully(byte[]) off<0");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new byte[4], 0, 5); } }, "readFully(byte[]) len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new byte[4], 3, 2); } }, "readFully(byte[]) off+len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new short[2], 0, 3); } }, "readFully(short[]) len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new char[2], -1, 1); } }, "readFully(char[]) off<0");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new int[2], 0, 3); } }, "readFully(int[]) len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new long[2], 0, 3); } }, "readFully(long[]) len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new float[2], 0, 3); } }, "readFully(float[]) len too big");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new double[2], 0, 3); } }, "readFully(double[]) len too big");
        expectExact(EOFException.class, new ThrowingAction() { public void run() throws Exception { b.readFully(new byte[4]); } }, "readFully past EOF");

        // readBytes: IIOByteBuffer semantics
        ByteArrayImageInputStream rb = new ByteArrayImageInputStream(new byte[]{1, 2, 3, 4, 5});
        final IIOByteBuffer buf = new IIOByteBuffer(new byte[0], 0, 0);
        rb.readBytes(buf, 3);
        check(buf.getLength() == 3 && buf.getOffset() == 0, "readBytes sets offset 0 + length");
        check(buf.getData().length == 3 && buf.getData()[0] == 1, "readBytes installs fresh array");
        check(rb.getStreamPosition() == 3L, "readBytes advances position");
        rb.readBytes(buf, 2);
        check(buf.getLength() == 2 && buf.getData()[0] == 4, "readBytes reuses buffer object");
        expectExact(NullPointerException.class, new ThrowingAction() { public void run() throws Exception { rb.readBytes(null, 1); } }, "readBytes null buffer");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rb.readBytes(buf, -1); } }, "readBytes negative length");
        check(rb.getStreamPosition() == 5L, "failed readBytes leaves position");
        rb.readBytes(buf, 4); // at EOF: no exception, length reported as -1
        check(buf.getLength() == -1, "readBytes at EOF reports length -1");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { rb.readBytes(null, -1); } }, "readBytes length validated before null buffer");

        // seek / skipBytes / flushBefore
        final ByteArrayImageInputStream sk = new ByteArrayImageInputStream(new byte[4]);
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { sk.seek(-1); } }, "seek(-1)");
        sk.seek(100); // beyond length is allowed: seek does not bound-check
        check(sk.getStreamPosition() == 100L, "seek beyond length allowed");
        check(sk.read() == -1, "read beyond length returns -1");
        sk.seek(0);
        check(sk.skipBytes(10) == 10, "skipBytes may pass end of stream");
        check(sk.getStreamPosition() == 10L, "position after skipBytes past end");
        check(sk.skipBytes(0) == 0, "skipBytes(0) no-op");
        check(sk.skipBytes(0L) == 0L, "skipBytes(0L) no-op");
        check(sk.skipBytes(-1) == -1, "skipBytes(-1) may seek backwards when above flushed position");
        check(sk.getStreamPosition() == 9L, "position after backward skipBytes");
        sk.seek(0);
        final ByteArrayImageInputStream sk0 = new ByteArrayImageInputStream(new byte[1]);
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { sk0.skipBytes(-1); } }, "skipBytes(int -1) into negative position");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { sk0.skipBytes(-1L); } }, "skipBytes(long -1) into negative position");
        final ByteArrayImageInputStream fl = new ByteArrayImageInputStream(new byte[4]);
        fl.seek(2);
        fl.flushBefore(2);
        check(fl.getFlushedPosition() == 2L, "flushBefore sets flushed position");
        fl.flush(); // flush at current position is legal
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.flushBefore(1); } }, "flushBefore below flushed position");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.flushBefore(3); } }, "flushBefore into the future");
        expectExact(IndexOutOfBoundsException.class, new ThrowingAction() { public void run() throws Exception { fl.seek(1); } }, "seek into flushed region");
    }

    // ---- Part 7: mark/reset stack and its interaction with flushing ----
    static void testMarkReset() throws Exception {
        ByteArrayImageInputStream s = new ByteArrayImageInputStream(new byte[]{1, 2, 3, 4, 5});
        s.reset();
        check(s.getStreamPosition() == 0L, "reset without mark is a no-op");
        s.seek(1);
        s.setBitOffset(3);
        s.mark();
        check(s.readBit() == 0, "readBit after mark");
        s.reset();
        check(s.getStreamPosition() == 1L, "reset restores byte position");
        check(s.getBitOffset() == 3, "reset restores bit offset");
        s.mark();
        s.seek(4);
        s.mark();
        s.seek(5);
        s.reset();
        check(s.getStreamPosition() == 4L, "nested marks pop LIFO (inner)");
        s.reset();
        check(s.getStreamPosition() == 1L, "nested marks pop LIFO (outer)");
        s.reset();
        check(s.getStreamPosition() == 1L, "extra reset on empty stack stays put");
        s.setBitOffset(2);
        s.mark();
        s.setBitOffset(6);
        s.reset();
        check(s.getBitOffset() == 2, "mark captures bit offset mid-byte");
        final ByteArrayImageInputStream fr = new ByteArrayImageInputStream(new byte[]{1, 2, 3, 4, 5});
        fr.seek(1);
        fr.mark();
        fr.seek(3);
        fr.flushBefore(2);
        expectExact(javax.imageio.IIOException.class,
                    new ThrowingAction() { public void run() throws Exception { fr.reset(); } },
                    "reset into flushed region");
        check(fr.getFlushedPosition() == 2L, "failed reset leaves flushed position");
    }

    // ---- Part 8: close contract and stateless survivors ----
    static void testCloseContract() throws Exception {
        final ByteArrayImageInputStream s = new ByteArrayImageInputStream(new byte[]{1, 2, 3});
        check(s.read() == 1, "read before close");
        s.mark();
        s.close();
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.read(); } }, "read after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.read(new byte[1]); } }, "read(byte[]) after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.getStreamPosition(); } }, "getStreamPosition after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.getBitOffset(); } }, "getBitOffset after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.setBitOffset(0); } }, "setBitOffset after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.seek(0); } }, "seek after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.skipBytes(1); } }, "skipBytes after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.flushBefore(0); } }, "flushBefore after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.readBits(1); } }, "readBits after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.reset(); } }, "reset to pre-close mark after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.close(); } }, "second close");
        s.mark(); // mark swallows the internal IOException: must not throw
        check(s.length() == 3L, "length() unaffected by close");
        check(s.getFlushedPosition() == 0L, "getFlushedPosition unaffected by close");
        check(!s.isCached() && !s.isCachedMemory() && !s.isCachedFile(), "cache queries unaffected by close");
        check(s.getByteOrder() == ByteOrder.BIG_ENDIAN, "getByteOrder unaffected by close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.readByte(); } }, "readByte after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.readFully(new byte[1]); } }, "readFully after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.readLine(); } }, "readLine after close");
        expectExact(IOException.class, new ThrowingAction() { public void run() throws Exception { s.readUTF(); } }, "readUTF after close");
    }

    // ---- Part 9: defaults when the subclass overrides nothing extra ----
    static void testDefaults() throws Exception {
        BareImageInputStream s = new BareImageInputStream();
        check(s.length() == -1L, "default length() is -1");
        check(!s.isCached(), "default isCached false");
        check(!s.isCachedMemory(), "default isCachedMemory false");
        check(!s.isCachedFile(), "default isCachedFile false");
        check(s.getStreamPosition() == 0L, "default position 0");
        check(s.getBitOffset() == 0, "default bit offset 0");
        check(s.getFlushedPosition() == 0L, "default flushed position 0");
        check(s.read() == -1, "subclass EOF honored");
        check(s.readLine() == null, "readLine on EOF-only stream is null");
        check(s.skipBytes(5) == 5, "skipBytes works without data");
        s.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(s.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "setByteOrder works on bare subclass");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStreamImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageInputStream"), "implements javax.imageio.stream.ImageInputStream");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setByteOrder"), "method setByteOrder");
        check(dm.contains("getByteOrder"), "method getByteOrder");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("readBytes"), "method readBytes");
        check(dm.contains("readBoolean"), "method readBoolean");
        check(dm.contains("readByte"), "method readByte");
        check(dm.contains("readUnsignedByte"), "method readUnsignedByte");
        check(dm.contains("readShort"), "method readShort");
        check(dm.contains("readUnsignedShort"), "method readUnsignedShort");
        check(dm.contains("readChar"), "method readChar");
        check(dm.contains("readInt"), "method readInt");
        check(dm.contains("readUnsignedInt"), "method readUnsignedInt");
        check(dm.contains("readLong"), "method readLong");
        check(dm.contains("readFloat"), "method readFloat");
        check(dm.contains("readDouble"), "method readDouble");
        check(dm.contains("readLine"), "method readLine");
        check(dm.contains("readUTF"), "method readUTF");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("getStreamPosition"), "method getStreamPosition");
        check(dm.contains("getBitOffset"), "method getBitOffset");
        check(dm.contains("setBitOffset"), "method setBitOffset");
        check(dm.contains("readBit"), "method readBit");
        check(dm.contains("readBits"), "method readBits");
        check(dm.contains("length"), "method length");
        check(dm.contains("skipBytes"), "method skipBytes");
        check(dm.contains("skipBytes"), "method skipBytes");
        check(dm.contains("seek"), "method seek");
        check(dm.contains("mark"), "method mark");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("flushBefore"), "method flushBefore");
        check(dm.contains("flush"), "method flush");
        check(dm.contains("getFlushedPosition"), "method getFlushedPosition");
        check(dm.contains("isCached"), "method isCached");
        check(dm.contains("isCachedMemory"), "method isCachedMemory");
        check(dm.contains("isCachedFile"), "method isCachedFile");
        check(dm.contains("close"), "method close");
        testWhiteBoxExtra();
        testByteOrderValues();
        testBitOps();
        testReadLine();
        testBoundsAndEof();
        testMarkReset();
        testCloseContract();
        testDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
