/*
 * @test
 * @summary javax.imageio.stream.FileImageInputStream conformance: behavioral.
 * @run main FileImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.FileImageInputStream;

public class FileImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.io.File f = java.io.File.createTempFile("jck", ".img");
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(new byte[]{10, 20, 30});
        fos.close();
        javax.imageio.stream.FileImageInputStream in = new javax.imageio.stream.FileImageInputStream(f);
        check(in.length() == 3, "length");
        check(in.read() == 10, "read");
        in.seek(0);
        check(in.read() == 10, "read after seek");
        in.close();
        testDeepCtorVariants();
        testDeepLengthSemantics();
        testDeepPrimitiveReads();
        testDeepEOF();
        testDeepBufferBounds();
        testDeepSeekMarkReset();
        testDeepFlushBefore();
        testDeepSkipBytes();
        testDeepBitOps();
        testDeepByteOrder();
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
        File f = new File(System.getProperty("java.io.tmpdir"), "imgio-streamimpl-FileImageInputStream" + tmpCounter++);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content);
        fos.close();
        return f;
    }

    static byte[] mk(int n) {
        byte[] d = new byte[n];
        for (int i = 0; i < n; i++) d[i] = (byte) (i + 1);
        return d;
    }

    static void closeQuietly(javax.imageio.stream.ImageInputStream s) {
        if (s != null) { try { s.close(); } catch (IOException ignore) {} }
    }

    static void testDeepCtorVariants() throws Exception {
        expectExact(IllegalArgumentException.class, () -> new FileImageInputStream((File) null),
                "ctor(File null)");
        expectExact(IllegalArgumentException.class, () -> new FileImageInputStream((RandomAccessFile) null),
                "ctor(RandomAccessFile null)");
        File ghost = new File(System.getProperty("java.io.tmpdir"),
                "imgio-streamimpl-FileImageInputStream-ghost" + tmpCounter++);
        expectExact(FileNotFoundException.class, () -> new FileImageInputStream(ghost),
                "ctor(nonexistent file)");
        // opening a directory fails with FileNotFoundException (reason text is OS/locale specific)
        File dir = new File(System.getProperty("java.io.tmpdir"),
                "imgio-streamimpl-FileImageInputStream-dir" + tmpCounter++);
        dir.mkdirs();
        expectExact(FileNotFoundException.class, () -> new FileImageInputStream(dir),
                "ctor(directory)");
        dir.delete();
    }

    static void testDeepLengthSemantics() throws Exception {
        File f = newFile(mk(16));
        FileImageInputStream in = new FileImageInputStream(f);
        try {
            check(in.length() == 16L, "length: real file length 16");
            in.read();
            check(in.length() == 16L, "length: unchanged after read");
            in.seek(8);
            check(in.length() == 16L, "length: unchanged after seek");
            check(in.read() == 9, "length: read after seek");
            in.seek(1000);
            check(in.read() == -1, "length: read after seek past EOF -> -1");
            in.seek(0);
            check(in.read() == 1, "length: seek back from past-EOF works");
            in.close();
        } finally {
            closeQuietly(in);
            f.delete();
        }
        // empty file
        File empty = newFile(new byte[0]);
        FileImageInputStream ein = new FileImageInputStream(empty);
        try {
            check(ein.length() == 0L, "length: empty file length 0");
            check(ein.read() == -1, "length: read on empty file -> -1");
            expectExact(EOFException.class, () -> ein.readBit(), "length: readBit on empty file");
            ein.close();
        } finally {
            closeQuietly(ein);
            empty.delete();
        }
    }

    static void testDeepPrimitiveReads() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
        try {
            check(in.getStreamPosition() == 0L, "prim: initial position 0");
            check(in.getFlushedPosition() == 0L, "prim: initial flushed position 0");
            check(in.getBitOffset() == 0, "prim: initial bit offset 0");
            check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "prim: default byte order BIG_ENDIAN");
            check(!in.isCached(), "prim: not cached");
            check(!in.isCachedFile(), "prim: not file-cached");
            check(!in.isCachedMemory(), "prim: not memory-cached");
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
            check(in.readUnsignedInt() == 0x01020304L, "prim: readUnsignedInt in int range");
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
        } finally {
            closeQuietly(in);
            f.delete();
        }

        File sf = newFile(new byte[]{(byte) 0x80, (byte) 0xFF, 0, 0x00, 0x41});
        FileImageInputStream s = new FileImageInputStream(sf);
        try {
            check(s.readByte() == -128, "prim: readByte 0x80 -> -128");
            check(s.readByte() == -1, "prim: readByte 0xFF -> -1");
            check(!s.readBoolean(), "prim: readBoolean zero -> false");
            check(s.readChar() == 'A', "prim: readChar 0x0041");
            s.close();
        } finally {
            closeQuietly(s);
            sf.delete();
        }

        File uf = newFile(new byte[]{(byte) 0x80, 0});
        FileImageInputStream u = new FileImageInputStream(uf);
        try {
            check(u.readUnsignedByte() == 128, "prim: readUnsignedByte 0x80 -> 128");
            check(u.getStreamPosition() == 1L, "prim: position 1 after readUnsignedByte");
            u.seek(0);
            check(u.readUnsignedShort() == 0x8000, "prim: readUnsignedShort 0x8000 -> 32768");
            u.close();
        } finally {
            closeQuietly(u);
            uf.delete();
        }

        File u32f = newFile(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        FileImageInputStream u32 = new FileImageInputStream(u32f);
        try {
            check(u32.readUnsignedInt() == 4294967295L, "prim: readUnsignedInt 0xFFFFFFFF -> 4294967295L");
            u32.close();
        } finally {
            closeQuietly(u32);
            u32f.delete();
        }

        File ff = newFile(new byte[]{0x3F, (byte) 0x80, 0, 0});
        FileImageInputStream fl = new FileImageInputStream(ff);
        try {
            check(Float.floatToIntBits(fl.readFloat()) == 0x3F800000, "prim: readFloat 1.0f bits");
            fl.close();
        } finally {
            closeQuietly(fl);
            ff.delete();
        }

        File df = newFile(new byte[]{0x3F, (byte) 0xF0, 0, 0, 0, 0, 0, 0});
        FileImageInputStream dd = new FileImageInputStream(df);
        try {
            check(Double.doubleToLongBits(dd.readDouble()) == 0x3FF0000000000000L, "prim: readDouble 1.0 bits");
            dd.close();
        } finally {
            closeQuietly(dd);
            df.delete();
        }

        File lf = newFile("ab\ncd\r\nef\rgh".getBytes("US-ASCII"));
        FileImageInputStream rl = new FileImageInputStream(lf);
        try {
            check("ab".equals(rl.readLine()), "prim: readLine LF");
            check("cd".equals(rl.readLine()), "prim: readLine CRLF");
            check("ef".equals(rl.readLine()), "prim: readLine CR");
            check("gh".equals(rl.readLine()), "prim: readLine terminated by EOF");
            check(rl.readLine() == null, "prim: readLine at EOF -> null");
            rl.close();
        } finally {
            closeQuietly(rl);
            lf.delete();
        }
    }

    static void testDeepEOF() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
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
        } finally {
            closeQuietly(in);
            f.delete();
        }

        File pf = newFile(new byte[]{5, 6});
        FileImageInputStream p = new FileImageInputStream(pf);
        final byte[] b = new byte[4];
        try {
            expectExact(EOFException.class, () -> p.readFully(b), "eof: readFully longer than file");
            check(b[0] == 5 && b[1] == 6 && b[2] == 0 && b[3] == 0, "eof: readFully partial fill 5,6,0,0");
            p.seek(2);
            p.readFully(new byte[1], 0, 0);
            check(p.getStreamPosition() == 2L, "eof: readFully len 0 advances nothing");
            p.close();
        } finally {
            closeQuietly(p);
            pf.delete();
        }
    }

    static void testDeepBufferBounds() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
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
            closeQuietly(in);
            f.delete();
        }
    }

    static void testDeepSeekMarkReset() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
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
        } finally {
            closeQuietly(in);
            f.delete();
        }

        File bf = newFile(mk(24));
        FileImageInputStream bi = new FileImageInputStream(bf);
        try {
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
        } finally {
            closeQuietly(bi);
            bf.delete();
        }

        File ff2 = newFile(mk(24));
        FileImageInputStream fi = new FileImageInputStream(ff2);
        try {
            fi.seek(4);
            fi.mark();
            fi.read();
            fi.flushBefore(5);
            expectExact(javax.imageio.IIOException.class, () -> fi.reset(),
                    "mark: reset into flushed prefix");
            fi.close();
        } finally {
            closeQuietly(fi);
            ff2.delete();
        }

        File nf = newFile(mk(24));
        FileImageInputStream ni = new FileImageInputStream(nf);
        try {
            ni.read();
            ni.flushBefore(1);
            expectExact(IndexOutOfBoundsException.class, () -> ni.seek(0), "seek below flushed position");
            expectExact(IndexOutOfBoundsException.class, () -> ni.seek(-1), "seek(-1)");
            ni.close();
        } finally {
            closeQuietly(ni);
            nf.delete();
        }
    }

    static void testDeepFlushBefore() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
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
            closeQuietly(in);
            f.delete();
        }
    }

    static void testDeepSkipBytes() throws Exception {
        File f = newFile(mk(24));
        FileImageInputStream in = new FileImageInputStream(f);
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
            // skipping past EOF: seek-based, so the file pointer may move past the length
            in.skipBytes(1000);
            check(in.getStreamPosition() == 1002L, "skip: skipBytes past EOF is pure seek");
            check(in.read() == -1, "skip: read after skipping past EOF -> -1");
            in.seek(0);
            check(in.read() == 1, "skip: seek back after past-EOF skip");
            in.close();
        } finally {
            closeQuietly(in);
            f.delete();
        }
    }

    static void testDeepBitOps() throws Exception {
        File f = newFile(new byte[]{(byte) 0xA5, 0x0F});
        FileImageInputStream in = new FileImageInputStream(f);
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
            expectExact(EOFException.class, () -> in.readBits(17), "bits: readBits beyond file");
            in.seek(0);
            check(in.readBits(0) == 0L, "bits: readBits(0) returns 0");
            check(in.getStreamPosition() == 0L, "bits: readBits(0) does not advance");
            in.seek(0);
            check(in.readBits(16) == 0xA50FL, "bits: readBits(16) whole two bytes");
            in.close();
        } finally {
            closeQuietly(in);
            f.delete();
        }

        File bf = newFile(mk(24));
        FileImageInputStream b64 = new FileImageInputStream(bf);
        try {
            check(b64.readBits(64) == 0x0102030405060708L, "bits: readBits(64) maximum width");
            b64.seek(0);
            check(b64.readBits(32) == 0x01020304L, "bits: readBits(32)");
            b64.seek(0);
            expectExact(IllegalArgumentException.class, () -> b64.readBits(65), "bits: readBits(65)");
            expectExact(IllegalArgumentException.class, () -> b64.readBits(-1), "bits: readBits(-1)");
            b64.close();
        } finally {
            closeQuietly(b64);
            bf.delete();
        }
    }

    static void testDeepByteOrder() throws Exception {
        File f = newFile(mk(8));
        FileImageInputStream in = new FileImageInputStream(f);
        try {
            check(in.getByteOrder() == ByteOrder.BIG_ENDIAN, "order: default BIG_ENDIAN");
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(in.getByteOrder() == ByteOrder.LITTLE_ENDIAN, "order: set/get LITTLE_ENDIAN");
            check(in.readInt() == 0x04030201, "order: readInt LE");
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            in.seek(0);
            check(in.readInt() == 0x01020304, "order: readInt BE restored");
            in.setByteOrder(null);
            check(in.getByteOrder() == null, "order: setByteOrder(null) stored as-is (actual)");
            in.seek(0);
            check(in.readInt() == 0x04030201, "order: null order decodes as LITTLE_ENDIAN (actual)");
            in.close();
        } finally {
            closeQuietly(in);
            f.delete();
        }
    }

    static void testDeepCloseState() throws Exception {
        File f = newFile(mk(3));
        FileImageInputStream in = new FileImageInputStream(f);
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
            // length() swallows the closed-check internally and reports -1
            check(in.length() == -1L, "close: length() after close returns -1 without throwing");
        } finally {
            closeQuietly(in);
            f.delete();
        }
    }

    static void testDeepRafCtor() throws Exception {
        File f = newFile(mk(8));
        // first RAF: probe the initial stream position semantics
        RandomAccessFile probeRaf = new RandomAccessFile(f, "r");
        try {
            probeRaf.seek(4);
            FileImageInputStream probe = new FileImageInputStream(probeRaf);
            // JDK-8 actual behavior: the constructor does NOT adopt the RAF file pointer,
            // the reported stream position starts at 0 regardless of the RAF position.
            check(probe.getStreamPosition() == 0L, "raf: JDK-8 initial stream position is 0 (pointer not adopted)");
            check(probe.length() == 8L, "raf: length via RandomAccessFile ctor");
            probe.close();
            expectExact(IOException.class, () -> probeRaf.read(),
                    "raf: underlying closed after FileImageInputStream.close");
        } finally {
            try { probeRaf.close(); } catch (IOException ignore) {}
        }
        // second RAF on the same file: normal reading through the RandomAccessFile ctor
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        try {
            FileImageInputStream in = new FileImageInputStream(raf);
            in.seek(0);
            check(in.read() == 1, "raf: read first byte after seek(0)");
            check(in.getStreamPosition() == 1L, "raf: position 1");
            in.skipBytes(2);
            check(in.read() == 4, "raf: read after skip");
            in.close();
        } finally {
            try { raf.close(); } catch (IOException ignore) {}
            f.delete();
        }
    }
}
