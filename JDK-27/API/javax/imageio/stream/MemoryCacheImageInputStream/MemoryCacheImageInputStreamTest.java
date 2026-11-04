/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageInputStream conformance: white-box structure + black-box behavior.
 * @run main MemoryCacheImageInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;

import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.*;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class MemoryCacheImageInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
            return;
        }
        check(false, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.MemoryCacheImageInputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "concrete");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = new byte[24];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i + 1);
        MemoryCacheImageInputStream in = null;
        try {
            in = new MemoryCacheImageInputStream(new ByteArrayInputStream(data));
            check(in.isCached(), "isCached");
            check(in.isCachedMemory(), "isCachedMemory");
            check(!in.isCachedFile(), "not isCachedFile");
            check(in.length() == -1L, "length unknown -1");
            check(in.getStreamPosition() == 0L, "initial position 0");
            check(in.read() == 1, "read first byte");
            check(in.readByte() == 2, "readByte");
            check(in.readUnsignedByte() == 3, "readUnsignedByte");
            in.seek(0L);
            check(in.readInt() == 0x01020304, "readInt big-endian");
            in.seek(0L);
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(in.readInt() == 0x04030201, "readInt little-endian");
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            in.seek(4L);
            in.mark();
            byte[] four = new byte[4];
            in.readFully(four);
            check(four[0] == 5 && four[3] == 8, "readFully values");
            in.reset();
            check(in.getStreamPosition() == 4L, "reset to mark");
            long skipped = in.skipBytes(10);
            check(skipped == 10L, "skipBytes count");
            check(in.getStreamPosition() == 14L, "position after skip");
            in.flushBefore(5L);
            check(in.getFlushedPosition() == 5L, "flushBefore flushedPos");
            final MemoryCacheImageInputStream stream = in;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(4L), "seek below flushedPos");
            in.close();
            in = null;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ignore) {} }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorAndCacheState();
        testDeepPrimitiveReads();
        testDeepEOF();
        testDeepBufferBounds();
        testDeepSeekMarkReset();
        testDeepFlushBefore();
        testDeepSkipBytes();
        testDeepBitOps();
        testDeepByteOrder();
        testDeepCloseState();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== deepening: contract coverage (appended) =====================

    interface ThrowingAction { void run() throws Exception; }

    /** Exact-class negative assertion: the thrown class must be identical, not a subclass. */
    static void expectExact(Class<? extends Throwable> expected, ThrowingAction r, String msg) {
        try {
            r.run();
            check(false, msg + ": expected " + expected.getName() + " but no exception was thrown");
        } catch (Throwable t) {
            if (t.getClass() != expected) {
                throw new AssertionError(msg + ": expected exactly " + expected.getName()
                        + " but got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            }
            passed++;
        }
    }

    /** Deterministic payload 1..n. */
    static byte[] mk(int n) {
        byte[] d = new byte[n];
        for (int i = 0; i < n; i++) d[i] = (byte) (i + 1);
        return d;
    }

    static MemoryCacheImageInputStream open(byte[] d) {
        return new MemoryCacheImageInputStream(new ByteArrayInputStream(d));
    }

    static void testDeepCtorAndCacheState() throws Exception {
        expectExact(IllegalArgumentException.class,
                () -> new MemoryCacheImageInputStream(null), "ctor(null stream)");
        MemoryCacheImageInputStream in = open(mk(3));
        check(in.isCached(), "ctor/cache: isCached true");
        check(in.isCachedMemory(), "ctor/cache: isCachedMemory true");
        check(!in.isCachedFile(), "ctor/cache: isCachedFile false");
        check(in.length() == -1L, "ctor/cache: length -1 before any read");
        check(in.getStreamPosition() == 0L, "ctor/cache: initial stream position 0");
        check(in.getFlushedPosition() == 0L, "ctor/cache: initial flushed position 0");
        check(in.getBitOffset() == 0, "ctor/cache: initial bit offset 0");
        in.read();
        check(in.length() == -1L, "ctor/cache: length stays -1 after read (never knowable)");
        in.close();
    }

    static void testDeepPrimitiveReads() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        check(in.read() == 1, "prim: read() first byte");
        check(in.getStreamPosition() == 1L, "prim: position after read()");
        check(in.readByte() == 2, "prim: readByte");
        check(in.readUnsignedByte() == 3, "prim: readUnsignedByte");
        check(in.readBoolean(), "prim: readBoolean nonzero -> true");
        in.seek(0);
        check(in.readShort() == 0x0102, "prim: readShort BE");
        in.seek(0);
        check(in.readUnsignedShort() == 0x0102, "prim: readUnsignedShort BE");
        in.seek(0);
        check(in.readInt() == 0x01020304, "prim: readInt BE");
        in.seek(0);
        check(in.readUnsignedInt() == 0x01020304L, "prim: readUnsignedInt BE in int range");
        in.seek(0);
        check(in.readLong() == 0x0102030405060708L, "prim: readLong BE");
        in.seek(0);
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(in.readShort() == 0x0201, "prim: readShort LE");
        in.seek(0);
        check(in.readInt() == 0x04030201, "prim: readInt LE");
        in.seek(0);
        check(in.readLong() == 0x0807060504030201L, "prim: readLong LE");
        in.seek(0);
        check(in.readChar() == 0x0201, "prim: readChar LE");
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        in.seek(0);
        check(in.readChar() == 0x0102, "prim: readChar BE");
        in.close();

        // signed vs unsigned boundaries
        MemoryCacheImageInputStream s = open(new byte[]{(byte) 0x80, (byte) 0xFF, 0, 0x00, 0x41});
        check(s.readByte() == -128, "prim: readByte 0x80 -> -128");
        check(s.readByte() == -1, "prim: readByte 0xFF -> -1");
        check(!s.readBoolean(), "prim: readBoolean zero -> false");
        check(s.readChar() == 'A', "prim: readChar 0x0041");
        s.close();

        MemoryCacheImageInputStream u = open(new byte[]{(byte) 0x80, 0});
        check(u.readUnsignedByte() == 128, "prim: readUnsignedByte 0x80 -> 128");
        check(u.getStreamPosition() == 1L, "prim: position 1 after readUnsignedByte");
            u.seek(0);
            check(u.readUnsignedShort() == 0x8000, "prim: readUnsignedShort 0x8000 -> 32768");
        u.close();

        MemoryCacheImageInputStream u32 = open(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        check(u32.readUnsignedInt() == 4294967295L, "prim: readUnsignedInt 0xFFFFFFFF -> 4294967295L (long semantics)");
        u32.close();

        // exact float/double bit patterns (deterministic, no FP ambiguity)
        MemoryCacheImageInputStream f = open(new byte[]{0x3F, (byte) 0x80, 0, 0});
        check(Float.floatToIntBits(f.readFloat()) == 0x3F800000, "prim: readFloat 1.0f bits");
        f.close();
        MemoryCacheImageInputStream d = open(new byte[]{0x3F, (byte) 0xF0, 0, 0, 0, 0, 0, 0});
        check(Double.doubleToLongBits(d.readDouble()) == 0x3FF0000000000000L, "prim: readDouble 1.0 bits");
        d.close();

        // readUTF over data produced by MemoryCacheImageOutputStream (modified UTF-8)
        java.io.ByteArrayOutputStream ub = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream uo = new MemoryCacheImageOutputStream(ub);
        uo.writeUTF("ABC");
        uo.writeUTF("");
        uo.writeUTF("世界");
        uo.close();
        MemoryCacheImageInputStream ri = open(ub.toByteArray());
        check("ABC".equals(ri.readUTF()), "prim: readUTF ascii");
        check("".equals(ri.readUTF()), "prim: readUTF empty string");
        check("世界".equals(ri.readUTF()), "prim: readUTF non-ascii BMP chars");
        ri.close();

        // readLine terminator matrix: \n, \r\n, \r, EOF
        MemoryCacheImageInputStream rl = open("ab\ncd\r\nef\rgh".getBytes("US-ASCII"));
        check("ab".equals(rl.readLine()), "prim: readLine LF");
        check("cd".equals(rl.readLine()), "prim: readLine CRLF");
        check("ef".equals(rl.readLine()), "prim: readLine CR");
        check("gh".equals(rl.readLine()), "prim: readLine terminated by EOF");
        check(rl.readLine() == null, "prim: readLine at EOF -> null");
        rl.close();
    }

    static void testDeepEOF() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        in.seek(23);
        expectExact(EOFException.class, () -> in.readShort(), "eof: readShort with 1 byte left");
        in.seek(23);
        expectExact(EOFException.class, () -> in.readUnsignedShort(), "eof: readUnsignedShort with 1 byte left");
        in.seek(23);
        expectExact(EOFException.class, () -> in.readChar(), "eof: readChar with 1 byte left");
        in.seek(21);
        expectExact(EOFException.class, () -> in.readInt(), "eof: readInt with 3 bytes left");
        in.seek(21);
        expectExact(EOFException.class, () -> in.readUnsignedInt(), "eof: readUnsignedInt with 3 bytes left");
        in.seek(21);
        expectExact(EOFException.class, () -> in.readFloat(), "eof: readFloat with 3 bytes left");
        in.seek(17);
        expectExact(EOFException.class, () -> in.readLong(), "eof: readLong with 7 bytes left");
        in.seek(17);
        expectExact(EOFException.class, () -> in.readDouble(), "eof: readDouble with 7 bytes left");
        in.seek(24);
        check(in.read() == -1, "eof: read() at EOF -> -1");
        check(in.read(new byte[2]) == -1, "eof: read(byte[]) at EOF -> -1");
        check(in.read(new byte[2], 0, 0) == 0, "eof: read(b,off,0) at EOF -> 0");
        in.seek(24);
        expectExact(EOFException.class, () -> in.readBit(), "eof: readBit at EOF");
        in.seek(24);
        expectExact(EOFException.class, () -> in.readBits(4), "eof: readBits at EOF");
        in.seek(23);
        expectExact(EOFException.class, () -> in.readUTF(), "eof: readUTF with truncated length marker");
        in.close();

        // readFully partial transfer: bytes read before EOF stay in the buffer
        MemoryCacheImageInputStream p = open(new byte[]{5, 6});
        final byte[] b = new byte[4];
        expectExact(EOFException.class, () -> p.readFully(b), "eof: readFully longer than stream");
        check(b[0] == 5 && b[1] == 6 && b[2] == 0 && b[3] == 0, "eof: readFully partial fill 5,6,0,0");
        MemoryCacheImageInputStream p2 = open(new byte[]{5, 6});
        p2.read();
        p2.readFully(new byte[1], 0, 0);
        check(p2.getStreamPosition() == 1L, "eof: readFully len 0 advances nothing");
        p2.close();
        p.close();

        MemoryCacheImageInputStream u = open(new byte[]{0, 3, 0x41});
        expectExact(EOFException.class, () -> u.readUTF(), "eof: readUTF declared length beyond stream");
        u.close();
    }

    static void testDeepBufferBounds() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        expectExact(NullPointerException.class, () -> in.read(null, 0, 1), "bounds: read(null,0,1)");
        expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], -1, 1), "bounds: read off < 0");
        expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], 0, -1), "bounds: read len < 0");
        expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], 0, 4), "bounds: read off+len > length");
        expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new byte[3], -1, 1), "bounds: readFully off < 0");
        expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new int[2], 1, 2), "bounds: readFully(int[]) off+len > length");
        expectExact(NullPointerException.class, () -> in.readFully((short[]) null, 0, 1), "bounds: readFully((short[])null)");
        expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new float[1], 0, 2), "bounds: readFully(float[]) len > length");
        in.close();
    }

    static void testDeepSeekMarkReset() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        // reset() without any mark is a documented silent no-op
        in.seek(5);
        in.reset();
        check(in.getStreamPosition() == 5L, "mark: reset without mark keeps position");
        // nested marks unwind LIFO
        in.seek(2);
        in.mark();
        in.seek(6);
        in.mark();
        in.seek(10);
        in.reset();
        check(in.getStreamPosition() == 6L, "mark: nested reset LIFO first (inner mark)");
        in.reset();
        check(in.getStreamPosition() == 2L, "mark: nested reset LIFO second (outer mark)");
        in.reset();
        check(in.getStreamPosition() == 2L, "mark: reset on exhausted stack is a no-op");
        // mark captures the bit offset too
        MemoryCacheImageInputStream bi = open(mk(24));
        bi.setBitOffset(3);
        bi.mark();
        bi.readBit();
        bi.readBit();
        check(bi.getStreamPosition() == 0L, "mark: two bits stay inside byte 0");
            check(bi.getBitOffset() == 5, "mark: bit offset 5 after two readBit");
            bi.readBits(6);
            check(bi.getStreamPosition() == 1L, "mark: readBit crossed the byte boundary");
        bi.reset();
        check(bi.getStreamPosition() == 0L, "mark: reset restores byte position");
        check(bi.getBitOffset() == 3, "mark: reset restores bit offset");
        bi.close();
        // reset() into the flushed prefix throws the precise IIOException subclass
        MemoryCacheImageInputStream fi = open(mk(24));
        fi.seek(4);
        fi.mark();
        fi.read();
        fi.flushBefore(5);
        expectExact(javax.imageio.IIOException.class, () -> fi.reset(),
                "mark: reset into flushed prefix");
        fi.close();
        // seek clears the bit offset
        MemoryCacheImageInputStream si = open(mk(24));
        si.setBitOffset(5);
        si.seek(0);
        check(si.getBitOffset() == 0, "mark: seek resets bit offset");
        // seek below flushed position / negative
        MemoryCacheImageInputStream ni = open(mk(24));
        ni.read();
        ni.flushBefore(1);
        expectExact(IndexOutOfBoundsException.class, () -> ni.seek(0), "seek below flushed position");
        expectExact(IndexOutOfBoundsException.class, () -> ni.seek(-1), "seek(-1)");
        in.close();
    }

    static void testDeepFlushBefore() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        check(in.getFlushedPosition() == 0L, "flush: initial flushedPos 0");
        in.flushBefore(0L);
        check(in.getFlushedPosition() == 0L, "flush: flushBefore(0) keeps flushedPos 0");
        expectExact(IndexOutOfBoundsException.class, () -> in.flushBefore(1), "flush: pos > stream position");
        in.read();
        in.read();
        in.flushBefore(2);
        check(in.getFlushedPosition() == 2L, "flush: flushedPos advances to 2");
        expectExact(IndexOutOfBoundsException.class, () -> in.flushBefore(1), "flush: backwards flushBefore rejected");
        expectExact(IndexOutOfBoundsException.class, () -> in.flushBefore(-1), "flush: negative pos rejected");
        in.flushBefore(2);
        check(in.getFlushedPosition() == 2L, "flush: flushBefore at same pos is a no-op");
        // data at/after the flush boundary is still readable
        in.seek(2);
        check(in.read() == 3, "flush: read continues from flushed boundary");
        // flush() (inherited) == flushBefore(current position)
        in.seek(5);
        in.flush();
        check(in.getFlushedPosition() == 5L, "flush: flush() flushes up to current position");
        in.close();
    }

    static void testDeepSkipBytes() throws Exception {
        MemoryCacheImageInputStream in = open(mk(24));
        check(in.skipBytes(0) == 0L, "skip: skipBytes(0) returns 0");
        check(in.getStreamPosition() == 0L, "skip: skipBytes(0) does not move");
        check(in.skipBytes(5) == 5L, "skip: skipBytes(int) returns 5");
        check(in.getStreamPosition() == 5L, "skip: position 5 after skip");
        check(in.read() == 6, "skip: read after skip");
        check(in.skipBytes(3L) == 3L, "skip: skipBytes(long) overload");
        // actual (undocumented) behavior: a negative n moves backwards and returns negative
        in.seek(5);
        long neg = in.skipBytes(-3);
        check(neg == -3L, "skip: skipBytes(-3) returns -3 (moves backwards, actual behavior)");
        check(in.getStreamPosition() == 2L, "skip: position moved back to 2");
        // skipping past available data still returns n (pure seek semantics), reads then hit EOF
        MemoryCacheImageInputStream pe = open(mk(3));
        check(pe.skipBytes(10) == 10L, "skip: skipBytes past EOF returns n (seek semantics)");
        check(pe.read() == -1, "skip: read after skipping past EOF -> -1");
        // skipping clears the bit offset
        MemoryCacheImageInputStream bi = open(mk(24));
        bi.setBitOffset(4);
        bi.skipBytes(1);
        check(bi.getBitOffset() == 0, "skip: skipBytes resets bit offset");
        in.close();
        pe.close();
        bi.close();
    }

    static void testDeepBitOps() throws Exception {
        MemoryCacheImageInputStream in = open(new byte[]{(byte) 0xA5, 0x0F});
        expectExact(IllegalArgumentException.class, () -> in.setBitOffset(8), "bits: setBitOffset(8)");
        expectExact(IllegalArgumentException.class, () -> in.setBitOffset(-1), "bits: setBitOffset(-1)");
        in.setBitOffset(0);
        check(in.getBitOffset() == 0, "bits: setBitOffset(0) round-trip");
        in.setBitOffset(7);
        check(in.getBitOffset() == 7, "bits: setBitOffset(7) boundary accepted");
        in.setBitOffset(0);
        check(in.readBit() == 1, "bits: readBit returns MSB first (0xA5 -> 1)");
        check(in.getBitOffset() == 1, "bits: bit offset 1 after readBit");
        check(in.readBits(3) == 0b010, "bits: readBits(3) -> 010");
        check(in.getBitOffset() == 4, "bits: bit offset 4");
        check(in.readBits(4) == 0b0101, "bits: readBits(4) crosses into next byte");
        check(in.getStreamPosition() == 1L, "bits: byte position advanced after crossing");
        check(in.getBitOffset() == 0, "bits: bit offset 0 after whole byte consumed");
        in.seek(0);
        expectExact(EOFException.class, () -> in.readBits(17), "bits: readBits beyond stream");
        in.seek(0);
        check(in.readBits(0) == 0L, "bits: readBits(0) returns 0");
        check(in.getStreamPosition() == 0L, "bits: readBits(0) does not advance");
        in.seek(0);
        check(in.readBits(16) == 0xA50FL, "bits: readBits(16) whole two bytes");
        in.close();

        MemoryCacheImageInputStream b64 = open(mk(24));
        check(b64.readBits(64) == 0x0102030405060708L, "bits: readBits(64) maximum width");
        b64.seek(0);
        check(b64.readBits(32) == 0x01020304L, "bits: readBits(32)");
        b64.seek(0);
        expectExact(IllegalArgumentException.class, () -> b64.readBits(65), "bits: readBits(65)");
        expectExact(IllegalArgumentException.class, () -> b64.readBits(-1), "bits: readBits(-1)");
        b64.close();
    }

    static void testDeepByteOrder() throws Exception {
        MemoryCacheImageInputStream in = open(mk(8));
        check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "order: default BIG_ENDIAN");
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(in.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "order: set/get LITTLE_ENDIAN");
        check(in.readInt() == 0x04030201, "order: readInt LE");
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "order: set/get BIG_ENDIAN");
        in.seek(0);
        check(in.readInt() == 0x01020304, "order: readInt BE restored");
        // actual (undocumented) behavior: setByteOrder(null) is accepted; multi-byte
        // reads then decode as LITTLE_ENDIAN because the BIG_ENDIAN comparison is false
        in.setByteOrder(null);
        check(in.getByteOrder() == null, "order: setByteOrder(null) stored as-is");
        in.seek(0);
        check(in.readInt() == 0x04030201, "order: null order decodes as LITTLE_ENDIAN (actual)");
        in.close();
    }

    static void testDeepCloseState() throws Exception {
        MemoryCacheImageInputStream in = open(mk(3));
        in.read();
        in.close();
        expectExact(IOException.class, () -> in.close(), "close: second close");
        expectExact(IOException.class, () -> in.read(), "close: read()");
        expectExact(IOException.class, () -> in.readByte(), "close: readByte()");
        expectExact(IOException.class, () -> in.read(new byte[1], 0, 1), "close: read(buf)");
        expectExact(IOException.class, () -> in.seek(0), "close: seek()");
        expectExact(IOException.class, () -> in.getStreamPosition(), "close: getStreamPosition()");
        expectExact(IOException.class, () -> in.getBitOffset(), "close: getBitOffset()");
        expectExact(IOException.class, () -> in.setBitOffset(0), "close: setBitOffset()");
        expectExact(IOException.class, () -> in.flushBefore(0), "close: flushBefore()");
        expectExact(IOException.class, () -> in.skipBytes(1), "close: skipBytes()");
        expectExact(IOException.class, () -> in.readBit(), "close: readBit()");
        // mark() after close silently does nothing and reset() on the empty stack is silent too
        in.mark();
        in.reset();
        // length() performs no closed check in this implementation: it keeps returning -1
        check(in.length() == -1L, "close: length() after close returns -1 without throwing");
        // a mark pushed before close survives; reset() then fails through seek's closed check
        MemoryCacheImageInputStream m = open(mk(3));
        m.mark();
        m.close();
        expectExact(IOException.class, () -> m.reset(), "close: reset() with pre-close mark");
    }
}
