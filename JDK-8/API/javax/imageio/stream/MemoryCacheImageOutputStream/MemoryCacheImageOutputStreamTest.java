/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageOutputStream conformance: behavioral.
 * @run main MemoryCacheImageOutputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class MemoryCacheImageOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.imageio.stream.MemoryCacheImageOutputStream out = new javax.imageio.stream.MemoryCacheImageOutputStream(bos);
        out.write(1);
        out.write(new byte[]{2, 3}, 0, 2);
        out.flushBefore(out.length());
        out.close();
        check(bos.toByteArray().length == 3, "length 3");
        testDeepCtorAndState();
        testDeepWritePrimitives();
        testDeepArrayRoundTrip();
        testDeepFlushSemantics();
        testDeepSeekRead();
        testDeepBitOps();
        testDeepBounds();
        testDeepSkipBytes();
        testDeepEOFReads();
        testDeepCloseState();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== deepening: contract coverage (appended) =====================

    interface ThrowingAction { void run() throws Exception; }

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

    static void testDeepCtorAndState() throws Exception {
        expectExact(IllegalArgumentException.class,
                () -> new MemoryCacheImageOutputStream(null), "ctor(null stream)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        check(out.isCached(), "state: isCached true");
        check(out.isCachedMemory(), "state: isCachedMemory true");
        check(!out.isCachedFile(), "state: isCachedFile false");
        check(out.length() == 0L, "state: initial length 0");
        check(out.getStreamPosition() == 0L, "state: initial position 0");
        check(out.getFlushedPosition() == 0L, "state: initial flushed position 0");
        check(out.getByteOrder() == ByteOrder.BIG_ENDIAN, "state: default byte order BIG_ENDIAN");
        out.close();
    }

    static void testDeepWritePrimitives() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeBoolean(true);
        check(out.length() == 1L, "write: length 1 after writeBoolean");
        out.writeBoolean(false);
        out.writeByte(-1);   // stored as 0xFF
        out.writeByte(0x80);
        out.writeShort(0x0102);
        out.writeChar(0x0304);
        out.writeInt(0x05060708);
        out.writeLong(0x090A0B0C0D0E0F10L);
        out.writeFloat(1.5f);
        out.writeDouble(-0.25);
        out.writeBytes("Az");  // low 8 bits of each char -> 2 bytes
        out.writeChars("Az");  // 2 bytes per char -> 4 bytes
        out.writeUTF("hi");
        check(out.length() == 42L, "write: total length 42");
        check(out.getStreamPosition() == 42L, "write: position == length at end");
        out.close();
        byte[] exp = new byte[]{
            0x01, 0x00, (byte) 0xFF, (byte) 0x80,
            0x01, 0x02,
            0x03, 0x04,
            0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x3F, (byte) 0xC0, 0x00, 0x00,                       // 1.5f
            (byte) 0xBF, (byte) 0xD0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // -0.25
            0x41, 0x7A,                                          // writeBytes
            0x00, 0x41, 0x00, 0x7A,                              // writeChars
            0x00, 0x02, 0x68, 0x69                               // writeUTF "hi"
        };
        byte[] raw = bos.toByteArray();
        check(raw.length == exp.length, "write: flushed byte count 42");
        boolean eq = true;
        for (int i = 0; i < exp.length; i++) if (raw[i] != exp[i]) eq = false;
        check(eq, "write: exact flushed byte layout of all primitive writes");

        // read everything back through MemoryCacheImageInputStream
        MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(raw));
        check(in.readBoolean(), "write: readBoolean true back");
        check(!in.readBoolean(), "write: readBoolean false back");
        check(in.readByte() == -1, "write: readByte 0xFF back");
        check(in.readUnsignedByte() == 0x80, "write: readUnsignedByte 0x80 back");
        check(in.readShort() == 0x0102, "write: readShort back");
        check(in.readChar() == 0x0304, "write: readChar back");
        check(in.readInt() == 0x05060708, "write: readInt back");
        check(in.readLong() == 0x090A0B0C0D0E0F10L, "write: readLong back");
        check(Float.floatToIntBits(in.readFloat()) == 0x3FC00000, "write: readFloat bits back");
        check(Double.doubleToLongBits(in.readDouble()) == 0xBFD0000000000000L, "write: readDouble bits back");
        check(in.readUnsignedShort() == 0x417A, "write: writeBytes pair read back as short");
        check(in.readInt() == 0x0041007A, "write: writeChars pair read back as int");
        check("hi".equals(in.readUTF()), "write: readUTF back");
        in.close();
    }

    static void testDeepArrayRoundTrip() throws Exception {
        short[] sh = {0x0102, (short) 0xF0F1, 0x7FFF};
        char[] ch = {0x0102, 0xABCD};
        int[] ints = {0x01020304, (int) 0x8090A0B0};
        long[] lng = {0x0102030405060708L, (long) 0x8090A0B0C0D0E0F0L};
        float[] fl = {1.5f, -0.25f};
        double[] db = {0.5, -2.75};
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeShorts(sh, 0, 3);
        out.writeChars(ch, 0, 2);
        out.writeInts(ints, 0, 2);
        out.writeLongs(lng, 0, 2);
        out.writeFloats(fl, 0, 2);
        out.writeDoubles(db, 0, 2);
        check(out.length() == 58L, "array: total length 58 (6+4+8+16+8+16)");
        out.close();
        MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        short[] sh2 = new short[3];
        char[] ch2 = new char[2];
        int[] in2 = new int[2];
        long[] ln2 = new long[2];
        float[] fl2 = new float[2];
        double[] db2 = new double[2];
        in.readFully(sh2, 0, 3);
        in.readFully(ch2, 0, 2);
        in.readFully(in2, 0, 2);
        in.readFully(ln2, 0, 2);
        in.readFully(fl2, 0, 2);
        in.readFully(db2, 0, 2);
        check(sh2[0] == 0x0102 && sh2[1] == (short) 0xF0F1 && sh2[2] == 0x7FFF, "array: shorts BE round-trip");
        check(ch2[0] == 0x0102 && ch2[1] == 0xABCD, "array: chars BE round-trip");
        check(in2[0] == 0x01020304 && in2[1] == (int) 0x8090A0B0, "array: ints BE round-trip");
        check(ln2[0] == 0x0102030405060708L && ln2[1] == (long) 0x8090A0B0C0D0E0F0L, "array: longs BE round-trip");
        check(Float.floatToIntBits(fl2[0]) == 0x3FC00000 && Float.floatToIntBits(fl2[1]) == 0xBE800000,
                "array: floats BE round-trip");
        check(Double.doubleToLongBits(db2[0]) == 0x3FE0000000000000L && Double.doubleToLongBits(db2[1]) == 0xC006000000000000L,
                "array: doubles BE round-trip");
        in.close();

        // LITTLE_ENDIAN byte layout of array writes
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream lo = new MemoryCacheImageOutputStream(bos2);
        lo.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        check(lo.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "array: byte order set/get LITTLE_ENDIAN");
        lo.writeShorts(sh, 0, 3);
        lo.writeInts(ints, 0, 1);
        lo.close();
        byte[] raw = bos2.toByteArray();
        check(raw.length == 10, "array: LE total length 10");
        check((raw[0] & 0xFF) == 0x02 && (raw[1] & 0xFF) == 0x01, "array: LE 0x0102 swapped");
        check((raw[2] & 0xFF) == 0xF1 && (raw[3] & 0xFF) == 0xF0, "array: LE 0xF0F1 swapped");
        check((raw[4] & 0xFF) == 0xFF && (raw[5] & 0xFF) == 0x7F, "array: LE 0x7FFF swapped");
        check((raw[6] & 0xFF) == 0x04 && (raw[7] & 0xFF) == 0x03 && (raw[8] & 0xFF) == 0x02 && (raw[9] & 0xFF) == 0x01,
                "array: LE 0x01020304 swapped");

        // writeUTF round-trip with non-ASCII through the same stream
        java.io.ByteArrayOutputStream ub = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream uo = new MemoryCacheImageOutputStream(ub);
        uo.writeUTF("");
        uo.writeUTF("世界");
        uo.close();
        MemoryCacheImageInputStream ui = new MemoryCacheImageInputStream(new ByteArrayInputStream(ub.toByteArray()));
        check("".equals(ui.readUTF()), "array: writeUTF empty round-trip");
        check("世界".equals(ui.readUTF()), "array: writeUTF non-ascii round-trip");
        ui.close();
    }

    static void testDeepFlushSemantics() throws Exception {
        // flush() (inherited) flushes up to the current position into the underlying stream
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeInt(0x01020304);
        out.writeInt(0x05060708);
        check(out.getFlushedPosition() == 0L, "flush: initial flushedPos 0");
        out.flush();
        check(out.getFlushedPosition() == 8L, "flush: flush() flushes to current position");
        check(bos.size() == 8, "flush: underlying received 8 bytes");
        out.writeInt(0x99AABBCC);
        out.flushBefore(10);
        check(out.getFlushedPosition() == 10L, "flush: flushBefore(10) partial");
        check(bos.size() == 10, "flush: underlying received 10 bytes");
        byte[] part = bos.toByteArray();
        check((part[8] & 0xFF) == 0x99 && (part[9] & 0xFF) == 0xAA, "flush: incremental content correct");
        out.close();
        check(bos.size() == 12, "flush: close flushes the remainder");
        byte[] all = bos.toByteArray();
        check((all[10] & 0xFF) == 0xBB && (all[11] & 0xFF) == 0xCC, "flush: final bytes flushed on close");

        // flushBefore/seek boundary rules mirror the input-side contract
        java.io.ByteArrayOutputStream bos2 = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream o2 = new MemoryCacheImageOutputStream(bos2);
        expectExact(IndexOutOfBoundsException.class, () -> o2.flushBefore(1), "flush: pos > current rejected");
        o2.write(1);
        o2.flushBefore(1);
        expectExact(IndexOutOfBoundsException.class, () -> o2.flushBefore(0), "flush: backwards flushBefore rejected");
        expectExact(IndexOutOfBoundsException.class, () -> o2.seek(0), "seek below flushed position rejected");
        o2.close();
    }

    static void testDeepSeekRead() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeInt(0x11223344);
        out.writeShort(0x5566);
        check(out.length() == 6L, "seek: length 6");
        out.seek(0);
        check(out.getStreamPosition() == 0L, "seek: back to 0");
        check(out.readInt() == 0x11223344, "seek: readInt back from cache");
        check(out.readUnsignedShort() == 0x5566, "seek: readUnsignedShort back from cache");
        check(out.read() == -1, "seek: read at length -> -1");
        // mid-stream overwrite keeps the length
        out.seek(1);
        out.write(0xEE);
        check(out.length() == 6L, "seek: overwrite does not extend length");
        out.seek(0);
        check(out.readUnsignedByte() == 0x11, "seek: byte 0 intact");
        check(out.readUnsignedByte() == 0xEE, "seek: byte 1 overwritten");
        // write past the current length grows the cache, gap zero-filled
        out.seek(8);
        check(out.length() == 6L, "seek: seek past length does not grow yet");
        out.write(0x77);
        check(out.length() == 9L, "seek: write past length extends to 9");
        out.seek(6);
        check(out.read() == 0, "seek: hole byte 6 zero-filled");
        check(out.read() == 0, "seek: hole byte 7 zero-filled");
        check(out.read() == 0x77, "seek: byte 8 written value");
        // seek clears bit offset
        out.setBitOffset(5);
        out.seek(0);
        check(out.getBitOffset() == 0, "seek: seek resets bit offset");
        out.close();
    }

    static void testDeepBitOps() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeBit(1);
        out.writeBit(0);
        out.writeBit(1);
        check(out.length() == 1L, "bits: 3 bits -> 1 byte materialized");
        check(out.getBitOffset() == 3, "bits: bit offset 3");
        out.write(0xFF); // flushes pending bits, then writes the full byte
        check(out.getBitOffset() == 0, "bits: write(int) flushes pending bits");
        check(out.length() == 2L, "bits: length 2 after flush+write");
        out.seek(0);
        check(out.readBit() == 1, "bits: readBit back of first written bit");
        out.seek(0);
        check(out.readUnsignedByte() == 0xA0, "bits: partial byte flushed as 101----- == 0xA0");
        check(out.readUnsignedByte() == 0xFF, "bits: full byte after flush");
        out.close();

        // writeBits writes the numBits LOW bits, MSB-first; 3+6+7 = 16 bits
        java.io.ByteArrayOutputStream b2 = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream o2 = new MemoryCacheImageOutputStream(b2);
        o2.writeBits(0b101, 3);
        o2.writeBits(0b010111, 6);
        o2.writeBits(0b1, 7);
        check(o2.length() == 2L, "bits: 16 bits -> 2 bytes");
        check(o2.getStreamPosition() == 2L, "bits: position 2");
        check(o2.getBitOffset() == 0, "bits: aligned after exactly 16 bits");
        o2.writeBits(0xFFF, 0); // documented no-op
        check(o2.getStreamPosition() == 2L, "bits: writeBits(x,0) is a no-op");
        expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, 65), "bits: writeBits numBits 65");
        expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, -1), "bits: writeBits numBits negative");
        o2.seek(2);
        o2.writeBits(0xFFFFFFFFFFFFFFFFL, 64);
        check(o2.length() == 10L, "bits: writeBits(64) maximum width");
        o2.close();
        byte[] raw = b2.toByteArray();
        check(raw.length == 10, "bits: flushed 10 bytes");
        check((raw[0] & 0xFF) == 0xAB, "bits: byte 0 == 0xAB");
        check((raw[1] & 0xFF) == 0x81, "bits: byte 1 == 0x81 (low 7 bits of 1)");
        for (int i = 2; i < 10; i++) check((raw[i] & 0xFF) == 0xFF, "bits: all-ones byte " + i);

        // read-modify-write at a nonzero bit offset preserves untouched bits of the byte
        java.io.ByteArrayOutputStream b3 = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream o3 = new MemoryCacheImageOutputStream(b3);
        o3.write(0xFF);
        o3.seek(0);
        o3.setBitOffset(2);
        o3.writeBits(0b00, 2);
        check(o3.getBitOffset() == 4, "bits: bit offset 4 after RMW");
        o3.close();
        check((b3.toByteArray()[0] & 0xFF) == 0xCF, "bits: RMW cleared bits 2-3 -> 0xCF");

        // writeBit pattern + readBit round-trip through the same instance
        java.io.ByteArrayOutputStream b4 = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream o4 = new MemoryCacheImageOutputStream(b4);
        for (int i = 0; i < 10; i++) o4.writeBit(i % 2);
        check(o4.length() == 2L, "bits: 10 bits -> 2 bytes");
        check(o4.getBitOffset() == 2, "bits: trailing bit offset 2");
        o4.seek(0);
        boolean ok = true;
        for (int i = 0; i < 10; i++) if (o4.readBit() != i % 2) ok = false;
        check(ok, "bits: readBit round-trip of writeBit pattern");
        check(o4.getBitOffset() == 2, "bits: readBit leaves the same trailing offset");
        o4.close();
        byte[] rb = b4.toByteArray();
        check((rb[0] & 0xFF) == 0x55, "bits: flushed alternating byte 0x55");
        check((rb[1] & 0xFF) == 0x40, "bits: flushed trailing partial byte 0x40");
    }

    static void testDeepBounds() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        expectExact(NullPointerException.class, () -> out.write((byte[]) null, 0, 1), "bounds: write(null)");
        expectExact(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, -1), "bounds: write len < 0");
        expectExact(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 3, 0), "bounds: write off+len > length");
        expectExact(NullPointerException.class, () -> out.writeShorts(null, 0, 1), "bounds: writeShorts(null)");
        expectExact(IndexOutOfBoundsException.class, () -> out.writeShorts(new short[2], -1, 1), "bounds: writeShorts off < 0");
        expectExact(IndexOutOfBoundsException.class, () -> out.writeInts(new int[2], 0, 5), "bounds: writeInts len > length");
        expectExact(IndexOutOfBoundsException.class, () -> out.writeLongs(new long[2], 2, 1), "bounds: writeLongs off+len > length");
        expectExact(NullPointerException.class, () -> out.read(null, 0, 1), "bounds: read(null) NPE");
        expectExact(IndexOutOfBoundsException.class, () -> out.read(new byte[2], -1, 1), "bounds: read off < 0");
        expectExact(IndexOutOfBoundsException.class, () -> out.read(new byte[2], 0, 3), "bounds: read off+len > length");
        check(out.read(new byte[1], 0, 0) == 0, "bounds: read(b,0,0) returns 0");
        out.close();
    }

    static void testDeepSkipBytes() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        for (int i = 0; i < 8; i++) out.write(i + 1);
        check(out.skipBytes(2L) == 2L, "skip: skipBytes(long) forward");
        check(out.getStreamPosition() == 10L, "skip: position 10");
        check(out.skipBytes(-3) == -3L, "skip: skipBytes(-3) moves backwards (actual behavior)");
        check(out.getStreamPosition() == 7L, "skip: position 7 after backwards skip");
        out.setBitOffset(3);
        out.skipBytes(1);
        check(out.getBitOffset() == 0, "skip: skipBytes resets bit offset");
        out.close();
    }

    static void testDeepEOFReads() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.writeInt(0x01020304);
        out.seek(3);
        expectExact(EOFException.class, () -> out.readShort(), "eof: readShort with 1 byte left");
        out.seek(3);
        expectExact(EOFException.class, () -> out.readChar(), "eof: readChar with 1 byte left");
        out.seek(2);
        expectExact(EOFException.class, () -> out.readInt(), "eof: readInt with 2 bytes left");
        out.seek(4);
        expectExact(EOFException.class, () -> out.readBit(), "eof: readBit at EOF");
        out.seek(4);
        expectExact(EOFException.class, () -> out.readBits(3), "eof: readBits at EOF");
        final byte[] b = new byte[8];
        out.seek(2);
        expectExact(EOFException.class, () -> out.readFully(b), "eof: readFully beyond length");
        check(b[0] == 3 && b[1] == 4 && b[2] == 0, "eof: readFully partial fill 3,4,0...");
        out.close();
    }

    static void testDeepCloseState() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bos);
        out.write(1);
        out.close();
        check(bos.size() == 1, "close: close flushes everything");
        // actual (undocumented) behavior: a second close throws NullPointerException
        // because the implementation dereferences the already-nulled cache
        expectExact(NullPointerException.class, () -> out.close(), "close: second close NPE (actual)");
        expectExact(IOException.class, () -> out.write(2), "close: write()");
        expectExact(IOException.class, () -> out.read(), "close: read()");
        expectExact(IOException.class, () -> out.seek(0), "close: seek()");
        expectExact(IOException.class, () -> out.getStreamPosition(), "close: getStreamPosition()");
        expectExact(IOException.class, () -> out.flushBefore(0), "close: flushBefore()");
        expectExact(IOException.class, () -> out.readBit(), "close: readBit()");
        // length() swallows the closed-check internally and reports -1
        check(out.length() == -1L, "close: length() after close returns -1 without throwing");
    }
}
