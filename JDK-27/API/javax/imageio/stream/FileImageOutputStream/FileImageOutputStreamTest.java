/*
 * @test
 * @summary javax.imageio.stream.FileImageOutputStream conformance: white-box structure + black-box behavior.
 * @run main FileImageOutputStreamTest
 */

import java.nio.ByteOrder;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.*;

public class FileImageOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.FileImageOutputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "concrete");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.stream.ImageOutputStreamImpl"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path tmp = Files.createTempFile("jck-fios", ".img");
        File f = tmp.toFile();
        FileImageOutputStream out = null;
        try {
            out = new FileImageOutputStream(f);
            check(out.length() == 0L, "initial length 0");
            out.writeByte(0x11);
            out.writeInt(0x12345678);
            out.writeBoolean(true);
            out.writeUTF("hi");
            out.writeShort(0x2233);
            check(out.length() == 12L, "length after writes");
            check(out.getStreamPosition() == 12L, "position after writes");
            out.seek(0L);
            check(out.readByte() == 0x11, "readByte back");
            check(out.readInt() == 0x12345678, "readInt back");
            check(out.readBoolean(), "readBoolean back");
            check("hi".equals(out.readUTF()), "readUTF back");
            check(out.readShort() == 0x2233, "readShort back");
            out.close();
            out = null;

            FileImageInputStream in = new FileImageInputStream(f);
            check(in.length() == 12L, "file length after close");
            in.seek(0L);
            check(in.readByte() == 0x11, "file readByte");
            check(in.readInt() == 0x12345678, "file readInt");
            in.close();
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ignore) {} }
            Files.deleteIfExists(tmp);
        }

        // RandomAccessFile constructor round-trip
        Path tmp2 = Files.createTempFile("jck-fios-raf", ".img");
        try (RandomAccessFile raf = new RandomAccessFile(tmp2.toFile(), "rw")) {
            FileImageOutputStream out2 = new FileImageOutputStream(raf);
            out2.write(0x7E);
            out2.close();
            byte[] wrote = Files.readAllBytes(tmp2);
            check(wrote.length == 1, "raf ctor wrote 1 byte");
            check(wrote[0] == (byte) 0x7E, "raf ctor byte value");
        } finally {
            Files.deleteIfExists(tmp2);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorVariants();
        testDeepLengthAndFileLayout();
        testDeepWritePrimitives();
        testDeepArrayRoundTrip();
        testDeepSeekRead();
        testDeepBitOps();
        testDeepBounds();
        testDeepEOFReads();
        testDeepFlushBefore();
        testDeepCloseState();
        testDeepRafCtor();
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

    static int tmpCounter;

    static File newFile(byte[] content) throws IOException {
        File f = new File(System.getProperty("java.io.tmpdir"), "imgio-streamimpl-FileImageOutputStream" + tmpCounter++);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content);
        fos.close();
        return f;
    }

    static void closeQuietly(FileImageOutputStream s) {
        if (s != null) { try { s.close(); } catch (IOException ignore) {} }
    }

    static void testDeepCtorVariants() throws Exception {
        expectExact(IllegalArgumentException.class, () -> new FileImageOutputStream((File) null),
                "ctor(File null)");
        expectExact(IllegalArgumentException.class, () -> new FileImageOutputStream((RandomAccessFile) null),
                "ctor(RandomAccessFile null)");
        // a nonexistent file inside an existing directory is created empty
        File ghost = new File(System.getProperty("java.io.tmpdir"),
                "imgio-streamimpl-FileImageOutputStream-created" + tmpCounter++);
        FileImageOutputStream out = new FileImageOutputStream(ghost);
        try {
            check(out.length() == 0L, "ctor: nonexistent file created empty");
            check(out.getStreamPosition() == 0L, "ctor: initial position 0");
            check(out.getByteOrder() == ByteOrder.BIG_ENDIAN, "ctor: default byte order BIG_ENDIAN");
            check(!out.isCached(), "ctor: not cached");
            check(!out.isCachedFile(), "ctor: not file-cached");
            check(!out.isCachedMemory(), "ctor: not memory-cached");
        } finally {
            closeQuietly(out);
        }
        ghost.delete();
    }

    static void testDeepLengthAndFileLayout() throws Exception {
        // writes hit the file immediately: length() and File.length() track each other
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.write(0x55);
            check(out.length() == 1L, "layout: length 1 after first write");
            check(f.length() == 1L, "layout: File.length 1 without close");
            out.write(new byte[]{2, 3}, 0, 2);
            check(out.length() == 3L, "layout: length 3 after write(byte[])");
            check(f.length() == 3L, "layout: File.length 3 without close");
            out.writeInt(0x04050607);
            check(out.length() == 7L, "layout: length 7 after writeInt");
            check(f.length() == 7L, "layout: File.length 7 without close");
            out.close();
        } finally {
            closeQuietly(out);
        }
        byte[] raw1 = readAll(f);
        check(raw1.length == 7, "layout: file holds 7 bytes");
        check((raw1[0] & 0xFF) == 0x55 && raw1[1] == 2 && raw1[2] == 3
                && raw1[3] == 4 && raw1[4] == 5 && raw1[5] == 6 && raw1[6] == 7, "layout: exact bytes on disk");
        f.delete();

        // an existing file is NOT truncated: length is the old size, position starts at 0
        File pre = newFile(new byte[]{7, 7, 7});
        FileImageOutputStream po = new FileImageOutputStream(pre);
        try {
            check(po.length() == 3L, "layout: preexisting length kept 3");
            check(po.getStreamPosition() == 0L, "layout: position starts 0 on preexisting file");
            po.write(0x09); // overwrite byte 0
            check(po.length() == 3L, "layout: overwrite keeps length 3");
            check(pre.length() == 3L, "layout: File.length still 3");
            po.close();
        } finally {
            closeQuietly(po);
        }
        byte[] preB = readAll(pre);
        check(preB[0] == 9 && preB[1] == 7 && preB[2] == 7, "layout: preexisting bytes overwritten in place");
        pre.delete();

        // seek past the end then write: the gap is zero-filled
        File hole = newFile(new byte[0]);
        FileImageOutputStream ho = new FileImageOutputStream(hole);
        try {
            ho.write(0x55);
            ho.seek(5);
            check(ho.length() == 1L, "layout: seek past end does not grow yet");
            ho.write(0xAA);
            check(ho.length() == 6L, "layout: length 6 after hole write");
            check(hole.length() == 6L, "layout: File.length 6 after hole write");
            ho.seek(0);
            check(ho.read() == 0x55, "layout: hole first byte intact");
            check(ho.read() == 0, "layout: hole byte 1 zero");
            check(ho.read() == 0, "layout: hole byte 2 zero");
            check(ho.read() == 0, "layout: hole byte 3 zero");
            check(ho.read() == 0, "layout: hole byte 4 zero");
            check(ho.read() == 0xAA, "layout: hole final byte written");
            ho.close();
        } finally {
            closeQuietly(ho);
        }
        hole.delete();
    }

    static byte[] readAll(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[512];
            int n;
            while ((n = fis.read(b)) > 0) bos.write(b, 0, n);
            return bos.toByteArray();
        } finally {
            fis.close();
        }
    }

    static void testDeepWritePrimitives() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.writeBoolean(true);
            out.writeBoolean(false);
            out.writeByte(0xFF);
            out.writeShort(0x0102);
            out.writeChar(0x0304);
            out.writeInt(0x05060708);
            out.writeLong(0x090A0B0C0D0E0F10L);
            out.writeFloat(1.5f);
            out.writeDouble(-0.25);
            out.writeBytes("Az");
            out.writeChars("Az");
            out.writeUTF("hi");
            check(out.length() == 41L, "write: total length 41");
            check(out.getStreamPosition() == 41L, "write: position == length");
            out.close();
        } finally {
            closeQuietly(out);
        }
        byte[] exp = new byte[]{
            0x01, 0x00, (byte) 0xFF,
            0x01, 0x02,
            0x03, 0x04,
            0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x3F, (byte) 0xC0, 0x00, 0x00,
            (byte) 0xBF, (byte) 0xD0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x41, 0x7A,
            0x00, 0x41, 0x00, 0x7A,
            0x00, 0x02, 0x68, 0x69
        };
        byte[] raw = readAll(f);
        check(raw.length == exp.length, "write: file holds 41 bytes");
        boolean eq = true;
        for (int i = 0; i < exp.length; i++) if (raw[i] != exp[i]) eq = false;
        check(eq, "write: exact on-disk byte layout of all primitive writes");
        f.delete();

        // LITTLE_ENDIAN layout, read back through FileImageInputStream
        File lf = newFile(new byte[0]);
        FileImageOutputStream lo = new FileImageOutputStream(lf);
        try {
            lo.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(lo.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "write: byte order set/get");
            lo.writeInt(0x01020304);
            lo.writeLong(0x0102030405060708L);
            lo.writeShort(0x0102);
            lo.writeDouble(0.5);
            lo.writeFloat(-2.5f);
            lo.writeChar(0x4142);
            lo.writeUTF("hi");
            check(lo.length() == 32L, "write: LE total length 32");
            lo.close();
        } finally {
            closeQuietly(lo);
        }
        byte[] lraw = readAll(lf);
        check(lraw.length == 32, "write: LE file 32 bytes");
        check((lraw[0] & 0xFF) == 0x04 && (lraw[1] & 0xFF) == 0x03 && (lraw[2] & 0xFF) == 0x02 && (lraw[3] & 0xFF) == 0x01,
                "write: LE int swapped on disk");
        check((lraw[4] & 0xFF) == 0x08 && (lraw[11] & 0xFF) == 0x01, "write: LE long swapped on disk");
        FileImageInputStream li = new FileImageInputStream(lf);
        try {
            li.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(li.readInt() == 0x01020304, "write: LE readInt back");
            check(li.readLong() == 0x0102030405060708L, "write: LE readLong back");
            check(li.readUnsignedShort() == 0x0102, "write: LE readUnsignedShort back");
            check(Double.doubleToLongBits(li.readDouble()) == 0x3FE0000000000000L, "write: LE readDouble back");
            check(Float.floatToIntBits(li.readFloat()) == 0xC0200000, "write: LE readFloat back");
            check(li.readChar() == 0x4142, "write: LE readChar back");
            check("hi".equals(li.readUTF()), "write: readUTF back");
            li.close();
        } finally {
            try { li.close(); } catch (IOException ignore) {}
            lf.delete();
        }
    }

    static void testDeepArrayRoundTrip() throws Exception {
        File f = newFile(new byte[0]);
        short[] sh = {0x0102, (short) 0xF0F1, 0x7FFF};
        char[] ch = {0x0102, 0xABCD};
        int[] ints = {0x01020304, (int) 0x8090A0B0};
        long[] lng = {0x0102030405060708L, (long) 0x8090A0B0C0D0E0F0L};
        float[] fl = {1.5f, -0.25f};
        double[] db = {0.5, -2.75};
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.writeShorts(sh, 0, 3);
            out.writeChars(ch, 0, 2);
            out.writeInts(ints, 0, 2);
            out.writeLongs(lng, 0, 2);
            out.writeFloats(fl, 0, 2);
            out.writeDoubles(db, 0, 2);
            check(out.length() == 58L, "array: total length 58");
            out.close();
        } finally {
            closeQuietly(out);
        }
        FileImageInputStream in = new FileImageInputStream(f);
        try {
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
            check(sh2[0] == 0x0102 && sh2[1] == (short) 0xF0F1 && sh2[2] == 0x7FFF, "array: shorts round-trip");
            check(ch2[0] == 0x0102 && ch2[1] == 0xABCD, "array: chars round-trip");
            check(in2[0] == 0x01020304 && in2[1] == (int) 0x8090A0B0, "array: ints round-trip");
            check(ln2[0] == 0x0102030405060708L && ln2[1] == (long) 0x8090A0B0C0D0E0F0L, "array: longs round-trip");
            check(Float.floatToIntBits(fl2[0]) == 0x3FC00000 && Float.floatToIntBits(fl2[1]) == 0xBE800000,
                    "array: floats round-trip");
            check(Double.doubleToLongBits(db2[0]) == 0x3FE0000000000000L && Double.doubleToLongBits(db2[1]) == 0xC006000000000000L,
                    "array: doubles round-trip");
            in.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            f.delete();
        }
    }

    static void testDeepSeekRead() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.writeInt(0x11223344);
            out.writeShort(0x5566);
            check(out.length() == 6L, "seek: length 6");
            out.seek(0);
            check(out.getStreamPosition() == 0L, "seek: back to 0");
            check(out.readInt() == 0x11223344, "seek: readInt back");
            check(out.readUnsignedShort() == 0x5566, "seek: readUnsignedShort back");
            check(out.read() == -1, "seek: read at length -> -1");
            out.seek(1);
            out.write(0xEE);
            check(out.length() == 6L, "seek: overwrite keeps length");
            out.seek(0);
            check(out.readUnsignedByte() == 0x11, "seek: byte 0 intact");
            check(out.readUnsignedByte() == 0xEE, "seek: byte 1 overwritten");
            check(out.readUnsignedShort() == 0x3344, "seek: int lower half intact");
            check(out.readUnsignedShort() == 0x5566, "seek: trailing short intact");
            out.setBitOffset(5);
            out.seek(0);
            check(out.getBitOffset() == 0, "seek: seek resets bit offset");
            check(!out.isCached(), "seek: stream reports uncached");
            out.close();
        } finally {
            closeQuietly(out);
        }
        f.delete();
    }

    static void testDeepBitOps() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.writeBit(1);
            out.writeBit(0);
            out.writeBit(1);
            check(out.length() == 1L, "bits: 3 bits -> 1 byte on disk");
            check(out.getBitOffset() == 3, "bits: bit offset 3");
            out.write(0xFF); // flushes pending bits, then the full byte
            check(out.getBitOffset() == 0, "bits: write(int) flushes pending bits");
            check(out.length() == 2L, "bits: length 2");
            out.seek(0);
            check(out.readUnsignedByte() == 0xA0, "bits: partial byte 101----- == 0xA0");
            check(out.readUnsignedByte() == 0xFF, "bits: full byte after flush");
            out.close();
        } finally {
            closeQuietly(out);
        }
        byte[] raw = readAll(f);
        check(raw.length == 2, "bits: file 2 bytes");
        check((raw[0] & 0xFF) == 0xA0 && (raw[1] & 0xFF) == 0xFF, "bits: A0 FF on disk");
        f.delete();

        // writeBits low-bits semantics: 3+6+7 = 16 bits -> AB 81
        File f2 = newFile(new byte[0]);
        FileImageOutputStream o2 = new FileImageOutputStream(f2);
        try {
            o2.writeBits(0b101, 3);
            o2.writeBits(0b010111, 6);
            o2.writeBits(0b1, 7);
            check(o2.length() == 2L, "bits: 16 bits -> 2 bytes");
            check(o2.getBitOffset() == 0, "bits: aligned after 16 bits");
            o2.writeBits(0xFFF, 0);
            check(o2.getStreamPosition() == 2L, "bits: writeBits(x,0) no-op");
            expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, 65), "bits: writeBits numBits 65");
            expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, -1), "bits: writeBits numBits negative");
            o2.seek(2);
            o2.writeBits(0xFFFFFFFFFFFFFFFFL, 64);
            check(o2.length() == 10L, "bits: writeBits(64) maximum width");
            o2.close();
        } finally {
            closeQuietly(o2);
        }
        byte[] raw2 = readAll(f2);
        check(raw2.length == 10, "bits: file 10 bytes");
        check((raw2[0] & 0xFF) == 0xAB, "bits: byte 0 == 0xAB");
        check((raw2[1] & 0xFF) == 0x81, "bits: byte 1 == 0x81");
        for (int i = 2; i < 10; i++) check((raw2[i] & 0xFF) == 0xFF, "bits: all-ones byte " + i);
        f2.delete();

        // read-modify-write at a nonzero bit offset preserves untouched bits
        File f3 = newFile(new byte[0]);
        FileImageOutputStream o3 = new FileImageOutputStream(f3);
        try {
            o3.write(0xFF);
            o3.seek(0);
            o3.setBitOffset(2);
            o3.writeBits(0b00, 2);
            check(o3.getBitOffset() == 4, "bits: bit offset 4 after RMW");
            o3.close();
        } finally {
            closeQuietly(o3);
        }
        check((readAll(f3)[0] & 0xFF) == 0xCF, "bits: RMW cleared bits 2-3 -> 0xCF");
        f3.delete();
    }

    static void testDeepBounds() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            expectExact(NullPointerException.class, () -> out.write((byte[]) null, 0, 1), "bounds: write(null)");
            expectExact(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, -1), "bounds: write len < 0");
            expectExact(NullPointerException.class, () -> out.writeShorts(null, 0, 1), "bounds: writeShorts(null)");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeShorts(new short[2], -1, 1), "bounds: writeShorts off < 0");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeInts(new int[2], 0, 5), "bounds: writeInts len > length");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeLongs(new long[2], 2, 1), "bounds: writeLongs off+len > length");
            out.write(1);
            expectExact(NullPointerException.class, () -> out.read(null, 0, 1), "bounds: read(null)");
            check(out.read(new byte[1], 0, 0) == 0, "bounds: read(b,0,0) returns 0");
            out.close();
        } finally {
            closeQuietly(out);
        }
        f.delete();
    }

    static void testDeepEOFReads() throws Exception {
        File f = newFile(new byte[]{1, 2, 3});
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.seek(3);
            check(out.read() == -1, "eof: read at length -> -1");
            out.seek(2);
            expectExact(EOFException.class, () -> out.readShort(), "eof: readShort with 1 byte left");
            out.seek(1);
            expectExact(EOFException.class, () -> out.readInt(), "eof: readInt with 2 bytes left");
            out.seek(0);
            expectExact(EOFException.class, () -> out.readLong(), "eof: readLong with 3 bytes left");
            out.seek(3);
            expectExact(EOFException.class, () -> out.readBit(), "eof: readBit at EOF");
            out.seek(3);
            expectExact(EOFException.class, () -> out.readBits(3), "eof: readBits at EOF");
            final byte[] b = new byte[8];
            out.seek(1);
            expectExact(EOFException.class, () -> out.readFully(b), "eof: readFully beyond length");
            check(b[0] == 2 && b[1] == 3 && b[2] == 0, "eof: readFully partial fill 2,3,0...");
            out.close();
        } finally {
            closeQuietly(out);
        }
        f.delete();
    }

    static void testDeepFlushBefore() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            check(out.getFlushedPosition() == 0L, "flush: initial flushedPos 0");
            out.writeInt(0x11223344);
            expectExact(IndexOutOfBoundsException.class, () -> out.flushBefore(5), "flush: pos > current rejected");
            out.flush();
            check(out.getFlushedPosition() == 4L, "flush: flush() to current position");
            expectExact(IndexOutOfBoundsException.class, () -> out.seek(1), "seek below flushed position");
            expectExact(IndexOutOfBoundsException.class, () -> out.flushBefore(2), "flush: backwards flushBefore rejected");
            out.write(0x55);
            out.flushBefore(5);
            check(out.getFlushedPosition() == 5L, "flush: flushBefore(5) at current position");
            out.close();
        } finally {
            closeQuietly(out);
        }
        f.delete();
    }

    static void testDeepCloseState() throws Exception {
        File f = newFile(new byte[0]);
        FileImageOutputStream out = new FileImageOutputStream(f);
        try {
            out.write(1);
            out.close();
            check(f.length() == 1L, "close: byte persisted to file");
            expectExact(IOException.class, () -> out.close(), "close: second close");
            expectExact(IOException.class, () -> out.write(2), "close: write()");
            expectExact(IOException.class, () -> out.read(), "close: read()");
            expectExact(IOException.class, () -> out.seek(0), "close: seek()");
            expectExact(IOException.class, () -> out.getStreamPosition(), "close: getStreamPosition()");
            expectExact(IOException.class, () -> out.flushBefore(0), "close: flushBefore()");
            expectExact(IOException.class, () -> out.readBit(), "close: readBit()");
            // length() swallows the closed-check internally and reports -1
            check(out.length() == -1L, "close: length() after close returns -1 without throwing");
        } finally {
            f.delete();
        }
    }

    static void testDeepRafCtor() throws Exception {
        File f = newFile(new byte[]{1, 2, 3, 4, 5, 6, 7});
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        try {
            raf.seek(3);
            raf.writeInt(0xAABBCCDD);
            raf.seek(2);
            FileImageOutputStream out = new FileImageOutputStream(raf);
            // JDK-27 actual behavior: the constructor ADOPTS the RAF file pointer,
            // the reported stream position starts at the RAF position (divergence from JDK-8).
            check(out.getStreamPosition() == 2L, "raf: JDK-27 initial stream position adopts RAF pointer (2)");
            check(out.length() == 7L, "raf: length via RandomAccessFile ctor");
            // writes land at the RAF's current pointer (2), not at reported position 0
            out.write(0xEE);
            out.close();
        } finally {
            try { raf.close(); } catch (IOException ignore) {}
        }
        byte[] raw = readAll(f);
        check(raw.length == 7, "raf: file length 7");
        check(raw[0] == 1 && raw[1] == 2 && raw[2] == (byte) 0xEE, "raf: write landed at raf pointer 2");
        check((raw[3] & 0xFF) == 0xAA && (raw[6] & 0xFF) == 0xDD, "raf: earlier raf writes intact");
        f.delete();
    }
}
