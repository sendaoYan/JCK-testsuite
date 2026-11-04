/*
 * @test
 * @summary java.util.zip.ZipOutputStream contract: null/bounds/closed exceptions,
 *          setMethod/setLevel/setComment validation, putNextEntry state machine
 *          (write-before-entry, duplicate name, STORED size/crc requirements),
 *          STORED/DEFLATED round-trip with comment, finish/close idempotence.
 * @run main ZipOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamTest {
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

    static long crc32(byte[] data) {
        java.util.zip.CRC32 c = new java.util.zip.CRC32();
        c.update(data, 0, data.length);
        return c.getValue();
    }

    static void testValidation() {
        checkThrows(NullPointerException.class, () -> new ZipOutputStream(null), "null ctor NPE");
        ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream());
        checkThrows(IllegalArgumentException.class, () -> z.setMethod(42), "setMethod invalid IAE");
        checkThrows(IllegalArgumentException.class, () -> z.setLevel(-2), "setLevel(-2) IAE");
        checkThrows(IllegalArgumentException.class, () -> z.setLevel(10), "setLevel(10) IAE");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) sb.append('a');
        checkThrows(IllegalArgumentException.class, () -> z.setComment(sb.toString()), "comment too long IAE");
    }

    static void testStateMachine() throws IOException {
        ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream());
        checkThrows(ZipException.class, () -> z.write(new byte[]{1}, 0, 1), "write before entry ZipException");
        checkThrows(NullPointerException.class, () -> z.putNextEntry(null), "putNextEntry null NPE");

        // STORED entry missing size/crc
        ZipEntry missing = new ZipEntry("missing");
        missing.setMethod(ZipEntry.STORED);
        checkThrows(ZipException.class, () -> z.putNextEntry(missing), "STORED missing size/crc ZipException");

        // STORED entry size != compressed size
        ZipEntry mismatch = new ZipEntry("mismatch");
        mismatch.setMethod(ZipEntry.STORED);
        mismatch.setSize(3);
        mismatch.setCompressedSize(4);
        mismatch.setCrc(crc32(new byte[]{1, 2, 3}));
        checkThrows(ZipException.class, () -> z.putNextEntry(mismatch), "STORED size != csize ZipException");

        // duplicate entry name
        ZipOutputStream dup = new ZipOutputStream(new ByteArrayOutputStream());
        dup.putNextEntry(new ZipEntry("dup"));
        dup.closeEntry();
        checkThrows(ZipException.class, () -> dup.putNextEntry(new ZipEntry("dup")), "duplicate entry ZipException");
    }

    static void testStoredWritePastEnd() throws Exception {
        ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream());
        ZipEntry e = new ZipEntry("stored");
        e.setMethod(ZipEntry.STORED);
        e.setSize(3);
        e.setCompressedSize(3);
        e.setCrc(crc32(new byte[]{1, 2, 3}));
        z.putNextEntry(e);
        z.write(new byte[]{1, 2, 3});
        checkThrows(ZipException.class, () -> z.write(new byte[]{4}), "write past STORED end ZipException");
    }

    static void testStoredSizeMismatchOnCloseEntry() throws Exception {
        ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream());
        ZipEntry e = new ZipEntry("short");
        e.setMethod(ZipEntry.STORED);
        e.setSize(3);
        e.setCompressedSize(3);
        e.setCrc(crc32(new byte[]{1, 2, 3}));
        z.putNextEntry(e);
        z.write(new byte[]{1, 2});
        checkThrows(ZipException.class, () -> z.closeEntry(), "STORED short write ZipException on closeEntry");
    }

    static void testRoundTripAndComment() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream z = new ZipOutputStream(bout);
        z.setComment("archive comment");
        z.setMethod(ZipEntry.DEFLATED);
        z.setLevel(Deflater.BEST_SPEED);

        z.putNextEntry(new ZipEntry("a.txt"));
        z.write("hello".getBytes(StandardCharsets.UTF_8));
        z.closeEntry();

        byte[] storedData = "stored-data".getBytes(StandardCharsets.UTF_8);
        ZipEntry stored = new ZipEntry("b.bin");
        stored.setMethod(ZipEntry.STORED);
        stored.setSize(storedData.length);
        stored.setCompressedSize(storedData.length);
        stored.setCrc(crc32(storedData));
        z.putNextEntry(stored);
        z.write(storedData);
        z.closeEntry();

        z.finish();
        z.finish(); // idempotent
        z.close();
        z.close(); // idempotent
        checkThrows(IOException.class, () -> z.putNextEntry(new ZipEntry("after")), "use after close IOException");

        byte[] zip = bout.toByteArray();
        check(zip.length > 0, "zip produced");

        // verify comment + entries via ZipFile (write to temp file)
        File tmp = File.createTempFile("jcapi", ".zip");
        tmp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmp);
        fos.write(zip);
        fos.close();
        ZipFile zf = new ZipFile(tmp);
        check("archive comment".equals(zf.getComment()), "comment round-trip");
        check(zf.size() == 2, "two entries");
        check(zf.getEntry("a.txt") != null && zf.getEntry("b.bin") != null, "entries present");
        zf.close();

        // verify via ZipInputStream
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zip));
        ZipEntry a = zin.getNextEntry();
        check("a.txt".equals(a.getName()), "first entry name");
        byte[] buf = new byte[64];
        int n = zin.read(buf);
        check("hello".equals(new String(buf, 0, n, StandardCharsets.UTF_8)), "deflated content");
        ZipEntry b = zin.getNextEntry();
        check("b.bin".equals(b.getName()) && b.getMethod() == ZipEntry.STORED, "stored entry");
        zin.close();
    }

    static void testNullAndBounds() {
        ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream());
        checkThrows(NullPointerException.class, () -> z.write((byte[]) null, 0, 0), "write null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> z.write(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> z.write(new byte[5], 0, 6), "len past end IOOBE");
    }

    public static void main(String[] args) throws Exception {
        testValidation();
        testStateMachine();
        testStoredWritePastEnd();
        testStoredSizeMismatchOnCloseEntry();
        testRoundTripAndComment();
        testNullAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
