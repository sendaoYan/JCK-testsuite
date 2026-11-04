/*
 * @test
 * @summary java.util.zip.ZipInputStream contract: null/bounds/closed exceptions,
 *          multi-entry round-trip with STORED+DEFLATED methods and metadata
 *          (name/size/compressedSize/crc/time), getNextEntry null at end,
 *          available/skip/closeEntry semantics, single-use read.
 * @run main ZipInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipInputStreamTest {
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

    static byte[] buildZip() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);

        ZipEntry deflated = new ZipEntry("deflated.txt");
        deflated.setTime(1600000000000L);
        zout.putNextEntry(deflated);
        zout.write("hello deflated".getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();

        byte[] storedData = "STORED".getBytes(StandardCharsets.UTF_8);
        ZipEntry stored = new ZipEntry("stored.bin");
        stored.setMethod(ZipEntry.STORED);
        stored.setSize(storedData.length);
        stored.setCompressedSize(storedData.length);
        stored.setCrc(crc32(storedData));
        zout.putNextEntry(stored);
        zout.write(storedData);
        zout.closeEntry();

        zout.close();
        return bout.toByteArray();
    }

    static long crc32(byte[] data) {
        java.util.zip.CRC32 c = new java.util.zip.CRC32();
        c.update(data, 0, data.length);
        return c.getValue();
    }

    static String readFully(ZipInputStream zin, int cap) throws Exception {
        byte[] buf = new byte[cap];
        int n = zin.read(buf);
        if (n < 0) return "";
        return new String(buf, 0, n, StandardCharsets.UTF_8);
    }

    static void testRoundTrip() throws Exception {
        byte[] zip = buildZip();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zip));

        ZipEntry e1 = zin.getNextEntry();
        check(e1 != null && "deflated.txt".equals(e1.getName()), "first entry name");
        check(e1.getMethod() == ZipEntry.DEFLATED, "deflated method");
        check("hello deflated".equals(readFully(zin, 64)), "deflated content");
        check(zin.read(new byte[8]) == -1, "read -1 after entry EOF");
        check(zin.available() == 0, "available 0 after entry EOF");

        ZipEntry e2 = zin.getNextEntry();
        check(e2 != null && "stored.bin".equals(e2.getName()), "second entry name");
        check(e2.getMethod() == ZipEntry.STORED, "stored method");
        check(e2.getSize() == 6 && e2.getCompressedSize() == 6, "stored sizes");
        check("STORED".equals(readFully(zin, 16)), "stored content");

        check(zin.getNextEntry() == null, "getNextEntry null at end");
        zin.close();
    }

    static void testMetadata() throws Exception {
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(buildZip()));
        ZipEntry e = zin.getNextEntry();
        check(e.getTime() == 1600000000000L, "entry time preserved");
        // Deflated entries written without explicit size/crc use a data
        // descriptor, so size/crc are only known after the entry is read.
        readFully(zin, 64);
        zin.read(new byte[1]);
        check(e.getSize() == "hello deflated".length(), "deflated size known after read");
        check(e.getCrc() != -1, "deflated crc known after read");
        zin.close();
    }

    static void testSkipAndCloseEntry() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        zout.putNextEntry(new ZipEntry("a"));
        zout.write("0123456789".getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();
        zout.putNextEntry(new ZipEntry("b"));
        zout.write("xx".getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();
        zout.close();

        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bout.toByteArray()));
        check(zin.getNextEntry() != null, "skip test first entry");
        check(zin.skip(4) == 4, "skip returns bytes skipped");
        check("456789".equals(readFully(zin, 16)), "skip advances entry data");
        zin.closeEntry();
        check(zin.available() == 0, "available 0 after closeEntry");
        ZipEntry b = zin.getNextEntry();
        check(b != null && "b".equals(b.getName()), "closeEntry positions to next");
        zin.close();

        ZipInputStream z2 = new ZipInputStream(new ByteArrayInputStream(bout.toByteArray()));
        z2.getNextEntry();
        checkThrows(IllegalArgumentException.class, () -> z2.skip(-1), "skip negative IAE");
        z2.close();
    }

    static void testNullAndBounds() throws IOException {
        checkThrows(NullPointerException.class, () -> new ZipInputStream(null), "null ctor NPE");
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(buildZipBytes()));
        checkThrows(NullPointerException.class, () -> zin.read((byte[]) null, 0, 1), "read null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> zin.read(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> zin.read(new byte[5], 0, 6), "len past end IOOBE");
        check(zin.read(new byte[5], 0, 0) == 0, "read len 0 returns 0");
        checkThrows(NullPointerException.class,
                () -> new ZipInputStream(new ByteArrayInputStream(new byte[0]), (java.nio.charset.Charset) null),
                "null charset ctor NPE");
    }

    static byte[] buildZipBytes() {
        try { return buildZip(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    static void testClose() throws Exception {
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(buildZip()));
        zin.close();
        checkThrows(IOException.class, () -> zin.read(new byte[8]), "read after close IOException");
        checkThrows(IOException.class, () -> zin.getNextEntry(), "getNextEntry after close IOException");
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testMetadata();
        testSkipAndCloseEntry();
        testNullAndBounds();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
