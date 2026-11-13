/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageOutputStream conformance: white-box structure + black-box behavior.
 * @run main FileCacheImageOutputStreamTest
 */

import java.nio.ByteOrder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileCacheImageOutputStream;
import java.io.*;
import javax.imageio.stream.FileCacheImageInputStream;

public class FileCacheImageOutputStreamTest {
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
    static void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursive(k);
        }
        f.delete();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.FileCacheImageOutputStream");
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Path dir = Files.createTempDirectory("jck-fcios");
        FileCacheImageOutputStream out = null;
        try {
            out = new FileCacheImageOutputStream(bos, dir.toFile());
            check(out.isCached(), "isCached");
            check(out.isCachedFile(), "isCachedFile");
            check(!out.isCachedMemory(), "not isCachedMemory");
            out.writeByte(0x11);
            out.writeInt(0x12345678);
            out.writeBoolean(true);
            out.writeUTF("hello");
            out.writeShort(0x2233);
            check(out.length() == 15L, "length after writes");
            check(out.getStreamPosition() == 15L, "position after writes");
            out.seek(0L);
            out.mark();
            out.readInt();
            out.readInt();
            out.reset();
            check(out.getStreamPosition() == 0L, "reset to mark");
            out.seek(0L);
            check(out.readByte() == 0x11, "readByte back");
            check(out.readInt() == 0x12345678, "readInt back");
            check(out.readBoolean(), "readBoolean back");
            check("hello".equals(out.readUTF()), "readUTF back");
            check(out.readShort() == 0x2233, "readShort back");
            out.seek(5L);
            out.flushBefore(5L);
            check(out.getFlushedPosition() == 5L, "flushedPos after flushBefore");
            byte[] flushed = bos.toByteArray();
            check(flushed.length == 5, "flushed 5 bytes to underlying");
            check(flushed[0] == 0x11 && flushed[1] == 0x12 && flushed[2] == 0x34 && flushed[3] == 0x56 && flushed[4] == 0x78, "flushed bytes content");
            final FileCacheImageOutputStream stream = out;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(4L), "seek below flushedPos");
            out.close();
            out = null;
            check(bos.size() == 15, "close flushes all bytes");
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ignore) {} }
            deleteRecursive(dir.toFile());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCtorAndState();
        testDeepWriteFlushLifecycle();
        testDeepArrayRoundTrip();
        testDeepSeekRead();
        testDeepBitOps();
        testDeepBounds();
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

    static int tmpCounter;

    static File newCacheDir() {
        File d = new File(System.getProperty("java.io.tmpdir"), "imgio-streamimpl-FileCacheImageOutputStream" + tmpCounter++);
        d.mkdirs();
        return d;
    }


    static void testDeepCtorAndState() throws Exception {
        File dir = newCacheDir();
        FileCacheImageOutputStream out = null;
        try {
            expectExact(IllegalArgumentException.class, () -> new FileCacheImageOutputStream(null, dir),
                    "ctor(null stream)");
            out = new FileCacheImageOutputStream(new ByteArrayOutputStream(), dir);
            check(out.isCached(), "ctor/cache: isCached true");
            check(out.isCachedFile(), "ctor/cache: isCachedFile true");
            check(!out.isCachedMemory(), "ctor/cache: isCachedMemory false");
            check(out.length() == 0L, "ctor/cache: initial length 0");
            check(out.getStreamPosition() == 0L, "ctor/cache: initial position 0");
            check(out.getFlushedPosition() == 0L, "ctor/cache: initial flushed position 0");
            check(out.getByteOrder() == ByteOrder.BIG_ENDIAN, "ctor/cache: default byte order BIG_ENDIAN");
            out.close();

            // cacheDir == null is legal: the system default temporary directory is used
            java.io.ByteArrayOutputStream dbos = new java.io.ByteArrayOutputStream();
            FileCacheImageOutputStream dflt = new FileCacheImageOutputStream(dbos, null);
            dflt.write(0x7E);
            dflt.close();
            check(dbos.size() == 1 && dbos.toByteArray()[0] == 0x7E, "ctor: null cacheDir uses system temp dir");

            // cacheDir pointing at a regular file
            File notDir = new File(System.getProperty("java.io.tmpdir"),
                    "imgio-streamimpl-FileCacheImageOutputStream-notdir" + tmpCounter++);
            FileOutputStream fos = new FileOutputStream(notDir);
            fos.write(1);
            fos.close();
            expectExact(IllegalArgumentException.class,
                    () -> new FileCacheImageOutputStream(new ByteArrayOutputStream(), notDir),
                    "ctor(cacheDir is regular file)");
            notDir.delete();

            // 只读缓存目录：各平台的“只读”语义不同。Windows 上目录的只读属性
            // 是 DOS attribute，并不阻止在其中创建文件（MSVCRT 的 _access 对目录
            // 只检查存在性），所以 setWritable(false) 后实际仍可能可写。这里用
            // 真实创建探测决定走哪条可移植契约分支。
            File ro = newCacheDir();
            ro.setWritable(false);
            try {
                File probe = new File(ro, "probe-" + System.nanoTime());
                boolean effectivelyWritable;
                try (FileOutputStream probeOut = new FileOutputStream(probe)) {
                    probeOut.write(0);
                    effectivelyWritable = true;
                } catch (IOException denied) {
                    effectivelyWritable = false;
                }
                probe.delete();
                if (effectivelyWritable) {
                    // 平台忽略目录只读属性：构造器照常创建缓存文件并可用
                    ByteArrayOutputStream bosRo = new ByteArrayOutputStream();
                    FileCacheImageOutputStream s = new FileCacheImageOutputStream(bosRo, ro);
                    s.write(0x5A);
                    s.close();
                    check(bosRo.size() == 1 && (bosRo.toByteArray()[0] & 0xFF) == 0x5A,
                          "ctor: platform ignores dir read-only attribute, cache works");
                } else {
                    expectExact(java.nio.file.AccessDeniedException.class,
                            () -> new FileCacheImageOutputStream(new ByteArrayOutputStream(), ro),
                            "ctor(unwritable cacheDir)");
                }
            } finally {
                ro.setWritable(true);
                deleteRecursive(ro);
            }
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepWriteFlushLifecycle() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
            out.writeByte(0x11);
            check(out.length() == 1L, "write: length 1 after writeByte");
            out.writeInt(0x12345678);
            out.writeBoolean(true);
            out.writeUTF("hello");
            out.writeShort(0x2233);
            check(out.length() == 15L, "write: total cached length 15");
            check(out.getStreamPosition() == 15L, "write: position 15");

            // incremental flushes push exactly [oldFlushedPos, pos) into the underlying stream
            out.seek(4);
            out.flushBefore(4);
            check(out.getFlushedPosition() == 4L, "flush: flushedPos 4");
            check(bos.size() == 4, "flush: underlying received 4 bytes");
            byte[] p1 = bos.toByteArray();
            check(p1[0] == 0x11 && p1[1] == 0x12 && p1[2] == 0x34 && p1[3] == 0x56, "flush: first 4 bytes content");
            out.seek(9);
            out.flushBefore(9);
            check(out.getFlushedPosition() == 9L, "flush: flushedPos 9");
            check(bos.size() == 9, "flush: underlying received 9 bytes");
            byte[] p2 = bos.toByteArray();
            check(p2[5] == 1, "flush: boolean byte at 5");
            check(p2[6] == 0 && (p2[7] & 0xFF) == 5, "flush: UTF length marker 00 05 at 6..7");
            check((p2[8] & 0xFF) == 0x68, "flush: UTF first char 'h' at 8");
            // flush() (inherited) flushes up to the current position
            out.seek(12);
            out.flush();
            check(out.getFlushedPosition() == 12L, "flush: flush() to current position");
            check(bos.size() == 12, "flush: underlying received 12 bytes");
            out.close();
            check(bos.size() == 15, "flush: close flushes the remainder");
            byte[] all = bos.toByteArray();
            check((all[13] & 0xFF) == 0x22 && (all[14] & 0xFF) == 0x33, "flush: trailing writeShort bytes");
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }

        // cache file lifecycle: created in cacheDir while open, removed on close
        File dir2 = newCacheDir();
        FileCacheImageOutputStream o2 = new FileCacheImageOutputStream(new ByteArrayOutputStream(), dir2);
        try {
            o2.writeInt(1);
            File[] openEntries = dir2.listFiles();
            check(openEntries != null && openEntries.length == 1, "lifecycle: one cache file while open");
            o2.close();
            File[] closedEntries = dir2.listFiles();
            check(closedEntries != null && closedEntries.length == 0, "lifecycle: cache file deleted on close");
        } finally {
            try { o2.close(); } catch (Exception ignore) {}
            deleteRecursive(dir2);
        }
    }

    static void testDeepArrayRoundTrip() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
            out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(out.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "array: byte order set/get LITTLE_ENDIAN");
            out.writeInt(0x01020304);
            out.writeLong(0x0102030405060708L);
            out.writeShort(0x0102);
            out.writeDouble(0.5);
            out.writeFloat(-2.5f);
            out.writeChar(0x4142);
            check(out.length() == 28L, "array: LE writes total length 28 (4+8+2+8+4+2)");
            out.seek(0);
            check(out.readInt() == 0x01020304, "array: LE readInt back");
            check(out.readLong() == 0x0102030405060708L, "array: LE readLong back");
            check(out.readUnsignedShort() == 0x0102, "array: LE readUnsignedShort back");
            check(Double.doubleToLongBits(out.readDouble()) == 0x3FE0000000000000L, "array: LE readDouble back");
            check(Float.floatToIntBits(out.readFloat()) == 0xC0200000, "array: LE readFloat back");
            check(out.readChar() == 0x4142, "array: LE readChar back");
            out.close();
            byte[] raw = bos.toByteArray();
            check(raw.length == 28, "array: flushed 28 bytes");
            check((raw[0] & 0xFF) == 0x04 && (raw[1] & 0xFF) == 0x03 && (raw[2] & 0xFF) == 0x02 && (raw[3] & 0xFF) == 0x01,
                    "array: LE int byte order on the wire");
            check((raw[26] & 0xFF) == 0x42 && (raw[27] & 0xFF) == 0x41, "array: LE char swapped on the wire");

            // writeBytes (low byte per char) vs writeChars (two bytes per char)
            java.io.ByteArrayOutputStream wbos = new java.io.ByteArrayOutputStream();
            FileCacheImageOutputStream w = new FileCacheImageOutputStream(wbos, dir);
            w.writeBytes("Az");
            w.writeChars("Az");
            w.close();
            byte[] wraw = wbos.toByteArray();
            check(wraw.length == 6, "array: writeBytes 2 + writeChars 4 bytes");
            check((wraw[0] & 0xFF) == 0x41 && (wraw[1] & 0xFF) == 0x7A, "array: writeBytes keeps low 8 bits");
            check((wraw[2] & 0xFF) == 0x00 && (wraw[3] & 0xFF) == 0x41 && (wraw[4] & 0xFF) == 0x00 && (wraw[5] & 0xFF) == 0x7A,
                    "array: writeChars writes two bytes per char");

            // full cross-check through FileCacheImageInputStream over the same cache dir
            java.io.ByteArrayOutputStream rbos = new java.io.ByteArrayOutputStream();
            FileCacheImageOutputStream ro = new FileCacheImageOutputStream(rbos, dir);
            short[] sh = {0x0102, (short) 0xF0F1};
            int[] ints = {0x01020304, (int) 0x8090A0B0};
            long[] lng = {0x0102030405060708L};
            float[] fl = {1.5f, -0.25f};
            double[] db = {0.5, -2.75};
            char[] ch = {0x0102, 0xABCD};
            ro.writeShorts(sh, 0, 2);
            ro.writeInts(ints, 0, 2);
            ro.writeLongs(lng, 0, 1);
            ro.writeFloats(fl, 0, 2);
            ro.writeDoubles(db, 0, 2);
            ro.writeChars(ch, 0, 2);
            check(ro.length() == 48L, "array: mixed array writes length 48");
            ro.close();
            FileCacheImageInputStream ri = new FileCacheImageInputStream(new ByteArrayInputStream(rbos.toByteArray()), dir);
            short[] sh2 = new short[2];
            int[] in2 = new int[2];
            long[] ln2 = new long[1];
            float[] fl2 = new float[2];
            double[] db2 = new double[2];
            char[] ch2 = new char[2];
            ri.readFully(sh2, 0, 2);
            ri.readFully(in2, 0, 2);
            ri.readFully(ln2, 0, 1);
            ri.readFully(fl2, 0, 2);
            ri.readFully(db2, 0, 2);
            ri.readFully(ch2, 0, 2);
            check(sh2[0] == 0x0102 && sh2[1] == (short) 0xF0F1, "array: shorts round-trip");
            check(in2[0] == 0x01020304 && in2[1] == (int) 0x8090A0B0, "array: ints round-trip");
            check(ln2[0] == 0x0102030405060708L, "array: longs round-trip");
            check(Float.floatToIntBits(fl2[0]) == 0x3FC00000 && Float.floatToIntBits(fl2[1]) == 0xBE800000,
                    "array: floats round-trip");
            check(Double.doubleToLongBits(db2[0]) == 0x3FE0000000000000L && Double.doubleToLongBits(db2[1]) == 0xC006000000000000L,
                    "array: doubles round-trip");
            check(ch2[0] == 0x0102 && ch2[1] == 0xABCD, "array: chars round-trip");
            ri.close();
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepSeekRead() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
            out.writeInt(0x01020304);
            out.writeShort(0x0506);
            check(out.length() == 6L, "seek: length 6");
            out.seek(0);
            check(out.getStreamPosition() == 0L, "seek: back to 0");
            check(out.readInt() == 0x01020304, "seek: readInt back from cache file");
            check(out.readUnsignedShort() == 0x0506, "seek: readUnsignedShort back");
            check(out.read() == -1, "seek: read at length -> -1");
            out.seek(1);
            out.write(0xEE);
            check(out.length() == 6L, "seek: overwrite does not extend length");
            out.seek(0);
            check(out.readUnsignedByte() == 0x01, "seek: byte 0 intact");
            check(out.readUnsignedByte() == 0xEE, "seek: byte 1 overwritten");
            // write past the length: gap zero-filled, length extends
            out.seek(8);
            check(out.length() == 6L, "seek: seek past length does not grow yet");
            out.write(0x77);
            check(out.length() == 9L, "seek: write past length extends to 9");
            out.seek(6);
            check(out.read() == 0, "seek: hole byte 6 zero-filled");
            check(out.read() == 0, "seek: hole byte 7 zero-filled");
            check(out.read() == 0x77, "seek: byte 8 written value");
            out.setBitOffset(5);
            out.seek(0);
            check(out.getBitOffset() == 0, "seek: seek resets bit offset");
            // seek below flushed prefix rejected
            out.seek(3);
            out.flushBefore(2);
            expectExact(IndexOutOfBoundsException.class, () -> out.seek(1), "seek below flushed position");
            out.close();
            byte[] raw = bos.toByteArray();
            check(raw.length == 9, "seek: close flushed 9 bytes");
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepBitOps() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
            out.writeBit(1);
            out.writeBit(0);
            out.writeBit(1);
            check(out.length() == 1L, "bits: 3 bits -> 1 byte materialized");
            check(out.getBitOffset() == 3, "bits: bit offset 3");
            out.write(0xFF);
            check(out.getBitOffset() == 0, "bits: write(int) flushes pending bits");
            check(out.length() == 2L, "bits: length 2 after flush+write");
            out.seek(0);
            check(out.readUnsignedByte() == 0xA0, "bits: partial byte flushed as 0xA0");
            out.seek(0);
            check(out.readBit() == 1, "bits: readBit back");
            out.seek(0);
            check(out.readBits(4) == 0b1010, "bits: readBits(4) of 0xA0");
            out.close();
            byte[] raw = bos.toByteArray();
            check(raw.length == 2, "bits: flushed 2 bytes");
            check((raw[0] & 0xFF) == 0xA0 && (raw[1] & 0xFF) == 0xFF, "bits: flushed A0 FF");

            // writeBits low-bits semantics and boundary validation
            java.io.ByteArrayOutputStream b2s = new java.io.ByteArrayOutputStream();
            FileCacheImageOutputStream o2 = new FileCacheImageOutputStream(b2s, dir);
            o2.writeBits(0b101, 3);
            o2.writeBits(0b010111, 6);
            o2.writeBits(0b1, 7);
            check(o2.length() == 2L, "bits: 16 bits -> 2 bytes");
            check(o2.getBitOffset() == 0, "bits: aligned after 16 bits");
            o2.writeBits(0xFFF, 0);
            check(o2.getStreamPosition() == 2L, "bits: writeBits(x,0) no-op");
            expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, 65), "bits: writeBits numBits 65");
            expectExact(IllegalArgumentException.class, () -> o2.writeBits(1L, -1), "bits: writeBits numBits negative");
            o2.close();
            byte[] r2 = b2s.toByteArray();
            check(r2.length == 2, "bits: flushed 2 bytes");
            check((r2[0] & 0xFF) == 0xAB, "bits: byte 0 == 0xAB");
            check((r2[1] & 0xFF) == 0x81, "bits: byte 1 == 0x81");

            // read-modify-write keeps untouched bits
            java.io.ByteArrayOutputStream b3s = new java.io.ByteArrayOutputStream();
            FileCacheImageOutputStream o3 = new FileCacheImageOutputStream(b3s, dir);
            o3.write(0xFF);
            o3.seek(0);
            o3.setBitOffset(2);
            o3.writeBits(0b00, 2);
            check(o3.getBitOffset() == 4, "bits: bit offset 4 after RMW");
            o3.close();
            check((b3s.toByteArray()[0] & 0xFF) == 0xCF, "bits: RMW cleared bits 2-3 -> 0xCF");
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepBounds() throws Exception {
        File dir = newCacheDir();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(new ByteArrayOutputStream(), dir);
        try {
            expectExact(NullPointerException.class, () -> out.write((byte[]) null, 0, 1), "bounds: write(null)");
            expectExact(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, -1), "bounds: write len < 0");
            expectExact(NullPointerException.class, () -> out.writeShorts(null, 0, 1), "bounds: writeShorts(null)");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeShorts(new short[2], -1, 1), "bounds: writeShorts off < 0");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeInts(new int[2], 0, 5), "bounds: writeInts len > length");
            expectExact(IndexOutOfBoundsException.class, () -> out.writeLongs(new long[2], 2, 1), "bounds: writeLongs off+len > length");
            expectExact(NullPointerException.class, () -> out.read(null, 0, 1), "bounds: read(null)");
            expectExact(IndexOutOfBoundsException.class, () -> out.read(new byte[2], 0, 3), "bounds: read off+len > length");
            check(out.read(new byte[1], 0, 0) == 0, "bounds: read(b,0,0) returns 0");
            out.close();
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepEOFReads() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
            out.writeInt(0x01020304);
            check(out.read() == -1, "eof: read at length -> -1");
            out.seek(3);
            expectExact(EOFException.class, () -> out.readShort(), "eof: readShort with 1 byte left");
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
        } finally {
            try { out.close(); } catch (Exception ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepCloseState() throws Exception {
        File dir = newCacheDir();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        FileCacheImageOutputStream out = new FileCacheImageOutputStream(bos, dir);
        try {
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
            check(out.length() == -1L, "close: length() after close returns -1 without throwing");
        } finally {
            deleteRecursive(dir);
        }
    }
}
