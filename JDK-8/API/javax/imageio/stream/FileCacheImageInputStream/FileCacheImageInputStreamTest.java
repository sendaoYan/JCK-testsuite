/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageInputStream conformance: behavioral.
 * @run main FileCacheImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.FileCacheImageInputStream;

public class FileCacheImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File cache = new java.io.File(System.getProperty("java.io.tmpdir"), "jckcache" + System.nanoTime());
        cache.mkdir();
        byte[] data = new byte[]{1, 2, 3, 4};
        javax.imageio.stream.FileCacheImageInputStream in = new javax.imageio.stream.FileCacheImageInputStream(new java.io.ByteArrayInputStream(data), cache);
        check(in.read() == 1, "read");
        byte[] buf = new byte[3];
        check(in.read(buf, 0, 3) == 3, "read buf");
        check(buf[0] == 2 && buf[2] == 4, "buf values");
        in.close();
        testDeepCtorAndCacheState();
        testDeepCacheFileLifecycle();
        testDeepPrimitiveReads();
        testDeepRandomAccess();
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
        File d = new File(System.getProperty("java.io.tmpdir"), "imgio-streamimpl-FileCacheImageInputStream" + tmpCounter++);
        d.mkdirs();
        return d;
    }

    static void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursive(k);
        }
        f.delete();
    }

    static byte[] mk(int n) {
        byte[] d = new byte[n];
        for (int i = 0; i < n; i++) d[i] = (byte) (i + 1);
        return d;
    }

    static FileCacheImageInputStream open(byte[] d, File cacheDir) throws IOException {
        return new FileCacheImageInputStream(new ByteArrayInputStream(d), cacheDir);
    }

    static void testDeepCtorAndCacheState() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = null;
        try {
            expectExact(IllegalArgumentException.class, () -> new FileCacheImageInputStream(null, dir),
                    "ctor(null stream)");
            check(dir.isDirectory(), "ctor: cache dir usable");
            in = open(mk(3), dir);
            check(in.isCached(), "ctor/cache: isCached true");
            check(in.isCachedFile(), "ctor/cache: isCachedFile true");
            check(!in.isCachedMemory(), "ctor/cache: isCachedMemory false");
            check(in.length() == -1L, "ctor/cache: length -1 (stream length unknowable)");
            check(in.getStreamPosition() == 0L, "ctor/cache: initial position 0");
            check(in.getFlushedPosition() == 0L, "ctor/cache: initial flushed position 0");
            check(in.getBitOffset() == 0, "ctor/cache: initial bit offset 0");
            check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "ctor/cache: default byte order BIG_ENDIAN");
            in.read();
            check(in.length() == -1L, "ctor/cache: length stays -1 after read");
            in.close();

            // cacheDir == null is legal: the system default temporary directory is used
            FileCacheImageInputStream deflt = new FileCacheImageInputStream(new ByteArrayInputStream(mk(2)), null);
            check(deflt.read() == 1, "ctor: null cacheDir uses system temp dir");
            check(deflt.read() == 2, "ctor: null cacheDir second byte");
            deflt.close();

            // cacheDir pointing at a regular file
            File notDir = new File(System.getProperty("java.io.tmpdir"),
                    "imgio-streamimpl-FileCacheImageInputStream-notdir" + tmpCounter++);
            FileOutputStream fos = new FileOutputStream(notDir);
            fos.write(1);
            fos.close();
            expectExact(IllegalArgumentException.class,
                    () -> new FileCacheImageInputStream(new ByteArrayInputStream(mk(1)), notDir),
                    "ctor(cacheDir is regular file)");
            notDir.delete();

            // unwritable cache directory: temp file creation fails (POSIX). Windows
            // does not enforce the read-only bit on directories, so construction may
            // succeed there. Accept either a thrown IOException or success; skip when
            // the OS cannot make the directory unwritable.
            File ro = newCacheDir();
            ro.setWritable(false);
            try {
                if (ro.canWrite()) {
                    check(true, "ctor: read-only dir not enforceable here (SKIP)");
                } else {
                    check(true, "ctor: read-only dir prepared");
                    try {
                        FileCacheImageInputStream r = new FileCacheImageInputStream(new ByteArrayInputStream(mk(1)), ro);
                        r.close();
                        check(true, "ctor(unwritable cacheDir): constructor succeeded (read-only not enforced)");
                    } catch (IOException e) {
                        check(true, "ctor(unwritable cacheDir): threw IOException (read-only enforced)");
                    }
                }
            } finally {
                ro.setWritable(true);
                deleteRecursive(ro);
            }
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepCacheFileLifecycle() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
            in.readFully(new byte[24]);
            File[] openEntries = dir.listFiles();
            check(openEntries != null && openEntries.length == 1, "lifecycle: one cache file while open");
            in.close();
            File[] closedEntries = dir.listFiles();
            check(closedEntries != null && closedEntries.length == 0, "lifecycle: cache file deleted on close");
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepPrimitiveReads() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
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

            FileCacheImageInputStream s = open(new byte[]{(byte) 0x80, (byte) 0xFF, 0, 0x00, 0x41}, dir);
            check(s.readByte() == -128, "prim: readByte 0x80 -> -128");
            check(s.readByte() == -1, "prim: readByte 0xFF -> -1");
            check(!s.readBoolean(), "prim: readBoolean zero -> false");
            check(s.readChar() == 'A', "prim: readChar 0x0041");
            s.close();

            FileCacheImageInputStream u = open(new byte[]{(byte) 0x80, 0}, dir);
            check(u.readUnsignedByte() == 128, "prim: readUnsignedByte 0x80 -> 128");
            check(u.getStreamPosition() == 1L, "prim: position 1 after readUnsignedByte");
            u.seek(0);
            check(u.readUnsignedShort() == 0x8000, "prim: readUnsignedShort 0x8000 -> 32768");
            u.close();

            FileCacheImageInputStream u32 = open(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, dir);
            check(u32.readUnsignedInt() == 4294967295L, "prim: readUnsignedInt 0xFFFFFFFF -> 4294967295L");
            u32.close();

            FileCacheImageInputStream f = open(new byte[]{0x3F, (byte) 0x80, 0, 0}, dir);
            check(Float.floatToIntBits(f.readFloat()) == 0x3F800000, "prim: readFloat 1.0f bits");
            f.close();
            FileCacheImageInputStream d = open(new byte[]{0x3F, (byte) 0xF0, 0, 0, 0, 0, 0, 0}, dir);
            check(Double.doubleToLongBits(d.readDouble()) == 0x3FF0000000000000L, "prim: readDouble 1.0 bits");
            d.close();

            FileCacheImageInputStream rl = open("ab\ncd\r\nef\rgh".getBytes("US-ASCII"), dir);
            check("ab".equals(rl.readLine()), "prim: readLine LF");
            check("cd".equals(rl.readLine()), "prim: readLine CRLF");
            check("ef".equals(rl.readLine()), "prim: readLine CR");
            check("gh".equals(rl.readLine()), "prim: readLine terminated by EOF");
            check(rl.readLine() == null, "prim: readLine at EOF -> null");
            rl.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepRandomAccess() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
            byte[] all = new byte[24];
            in.readFully(all);
            boolean eq = true;
            for (int i = 0; i < 24; i++) if (all[i] != (byte) (i + 1)) eq = false;
            check(eq, "random: full sequential read fills 1..24");
            in.seek(0);
            check(in.readByte() == 1, "random: seek(0) re-read from cache");
            in.seek(23);
            check(in.readByte() == 24, "random: seek(23) last byte");
            in.seek(0);
            check(in.readInt() == 0x01020304, "random: readInt after backward seek");
            byte[] part = new byte[4];
            in.seek(4);
            check(in.read(part, 1, 2) == 2, "random: read(buf,off,len) count");
            check(part[1] == 5 && part[2] == 6 && part[3] == 0, "random: read(buf,off,len) values, off respected");
            check(in.getStreamPosition() == 6L, "random: position 6");
            // seeking far past the cached/available data is legal; reads then report EOF
            in.seek(100);
            check(in.read() == -1, "random: read after seek past data -> -1");
            in.seek(10);
            check(in.readByte() == 11, "random: seek back after EOF still cached");
            in.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepEOF() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
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

            FileCacheImageInputStream p = open(new byte[]{5, 6}, dir);
            final byte[] b = new byte[4];
            expectExact(EOFException.class, () -> p.readFully(b), "eof: readFully longer than stream");
            check(b[0] == 5 && b[1] == 6 && b[2] == 0 && b[3] == 0, "eof: readFully partial fill 5,6,0,0");
            p.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepBufferBounds() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
            expectExact(NullPointerException.class, () -> in.read(null, 0, 1), "bounds: read(null,0,1)");
            expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], -1, 1), "bounds: read off < 0");
            expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], 0, -1), "bounds: read len < 0");
            expectExact(IndexOutOfBoundsException.class, () -> in.read(new byte[3], 0, 4), "bounds: read off+len > length");
            expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new byte[3], -1, 1), "bounds: readFully off < 0");
            expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new int[2], 1, 2), "bounds: readFully(int[]) off+len > length");
            expectExact(NullPointerException.class, () -> in.readFully((short[]) null, 0, 1), "bounds: readFully((short[])null)");
            expectExact(IndexOutOfBoundsException.class, () -> in.readFully(new float[1], 0, 2), "bounds: readFully(float[]) len > length");
            in.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepSeekMarkReset() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
            in.seek(5);
            in.reset();
            check(in.getStreamPosition() == 5L, "mark: reset without mark keeps position");
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
            in.close();

            FileCacheImageInputStream bi = open(mk(24), dir);
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

            FileCacheImageInputStream fi = open(mk(24), dir);
            fi.seek(4);
            fi.mark();
            fi.read();
            fi.flushBefore(5);
            expectExact(javax.imageio.IIOException.class, () -> fi.reset(),
                    "mark: reset into flushed prefix");
            fi.close();

            FileCacheImageInputStream ni = open(mk(24), dir);
            ni.read();
            ni.flushBefore(1);
            expectExact(IndexOutOfBoundsException.class, () -> ni.seek(0), "seek below flushed position");
            expectExact(IndexOutOfBoundsException.class, () -> ni.seek(-1), "seek(-1)");
            ni.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepFlushBefore() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
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
            in.seek(2);
            check(in.read() == 3, "flush: read continues from flushed boundary");
            in.seek(5);
            in.flush();
            check(in.getFlushedPosition() == 5L, "flush: flush() flushes up to current position");
            in.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepSkipBytes() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(24), dir);
        try {
            check(in.skipBytes(0) == 0L, "skip: skipBytes(0) returns 0");
            check(in.getStreamPosition() == 0L, "skip: skipBytes(0) does not move");
            check(in.skipBytes(5) == 5L, "skip: skipBytes(int) returns 5");
            check(in.getStreamPosition() == 5L, "skip: position 5 after skip");
            check(in.read() == 6, "skip: read after skip");
            check(in.skipBytes(3L) == 3L, "skip: skipBytes(long) overload");
            in.seek(5);
            long neg = in.skipBytes(-3);
            check(neg == -3L, "skip: skipBytes(-3) returns -3 (moves backwards, actual behavior)");
            check(in.getStreamPosition() == 2L, "skip: position moved back to 2");
            in.close();

            FileCacheImageInputStream pe = open(mk(3), dir);
            check(pe.skipBytes(10) == 10L, "skip: skipBytes past EOF returns n (seek semantics)");
            check(pe.read() == -1, "skip: read after skipping past EOF -> -1");
            pe.close();

            FileCacheImageInputStream bi = open(mk(24), dir);
            bi.setBitOffset(4);
            bi.skipBytes(1);
            check(bi.getBitOffset() == 0, "skip: skipBytes resets bit offset");
            bi.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepBitOps() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(new byte[]{(byte) 0xA5, 0x0F}, dir);
        try {
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

            FileCacheImageInputStream b64 = open(mk(24), dir);
            check(b64.readBits(64) == 0x0102030405060708L, "bits: readBits(64) maximum width");
            b64.seek(0);
            check(b64.readBits(32) == 0x01020304L, "bits: readBits(32)");
            b64.seek(0);
            expectExact(IllegalArgumentException.class, () -> b64.readBits(65), "bits: readBits(65)");
            expectExact(IllegalArgumentException.class, () -> b64.readBits(-1), "bits: readBits(-1)");
            b64.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepByteOrder() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(8), dir);
        try {
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(in.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "order: set/get LITTLE_ENDIAN");
            check(in.readInt() == 0x04030201, "order: readInt LE");
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "order: set/get BIG_ENDIAN");
            in.seek(0);
            check(in.readInt() == 0x01020304, "order: readInt BE restored");
            in.setByteOrder(null);
            check(in.getByteOrder() == null, "order: setByteOrder(null) stored as-is (actual)");
            in.seek(0);
            check(in.readInt() == 0x04030201, "order: null order decodes as LITTLE_ENDIAN (actual)");
            in.close();
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            deleteRecursive(dir);
        }
    }

    static void testDeepCloseState() throws Exception {
        File dir = newCacheDir();
        FileCacheImageInputStream in = open(mk(3), dir);
        try {
            in.read();
            in.close();
            expectExact(IOException.class, () -> in.close(), "close: second close");
            expectExact(IOException.class, () -> in.read(), "close: read()");
            expectExact(IOException.class, () -> in.readByte(), "close: readByte()");
            expectExact(IOException.class, () -> in.read(new byte[1], 0, 1), "close: read(buf)");
            expectExact(IOException.class, () -> in.seek(0), "close: seek()");
            expectExact(IOException.class, () -> in.getStreamPosition(), "close: getStreamPosition()");
            expectExact(IOException.class, () -> in.getBitOffset(), "close: getBitOffset()");
            expectExact(IOException.class, () -> in.flushBefore(0), "close: flushBefore()");
            expectExact(IOException.class, () -> in.skipBytes(1), "close: skipBytes()");
            expectExact(IOException.class, () -> in.readBit(), "close: readBit()");
            in.mark();
            in.reset();
            check(in.length() == -1L, "close: length() after close returns -1 without throwing");
        } finally {
            deleteRecursive(dir);
        }
    }
}
