/*
 * @test
 * @summary java.io.FileReader conformance: full contract coverage.
 *          File/String constructors, character reads (single/bulk/offset),
 *          EOF, FileNotFoundException for a missing file. ASCII content keeps
 *          the default-charset behavior deterministic (JDK 8 has no Charset ctor).
 *          Deepened (contract level): ctor negatives (null NPE, directory FNFE
 *          with a platform OS reason text, missing FNFE with path + OS reason),
 *          FileDescriptor ctor via FileInputStream FD, closed-state matrix
 *          ("Stream closed"), read bounds, empty file, mixed single/bulk/offset reads,
 *          skip semantics, byte-level read-back via FileOutputStream (ASCII only:
 *          JDK-8 FileReader has no Charset ctor, non-ASCII would be default-charset
 *          dependent).
 * @run main FileReaderTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fr", ".tmp");
        if (!d.delete() || !d.mkdir()) throw new AssertionError("temp dir");
        return d;
    }

    static void deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursively(k);
        }
        f.delete();
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "text.txt");
            FileWriter fw = new FileWriter(f);
            fw.write("hello-file");
            fw.close();

            FileReader fr = new FileReader(f);
            check(fr.read() == 'h', "read h");
            check(fr.read() == 'e', "read e");
            check(fr.read() == 'l', "read l");
            char[] buf = new char[4];
            check(fr.read(buf, 1, 3) == 3, "read(char[],off,len) count");
            check(new String(buf, 1, 3).equals("lo-"), "read(char[],off,len) content");
            check(fr.read() == 'f', "read f after bulk");
            check(fr.read() != -1, "more chars remain");
            fr.close();

            FileReader fr2 = new FileReader(f.getPath());
            char[] all = new char[100];
            int n = fr2.read(all);
            check("hello-file".equals(new String(all, 0, n)), "String ctor full read");
            check(fr2.read() == -1, "EOF");
            fr2.close();

            try {
                new FileReader(new File(dir, "nope.txt"));
                check(false, "missing file must throw FileNotFoundException");
            } catch (FileNotFoundException expected) {
                passed++;
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkMsg(Class<? extends Throwable> expected, String sub,
                         ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(sub)) {
                passed++;
                return;
            }
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " containing \"" + sub + "\" but got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " containing \"" + sub + "\" but nothing was thrown");
    }

    // ---- constructor negatives with exact messages ----
    static void testConstructorNegatives(File dir) throws Exception {
        checkThrows(NullPointerException.class, () -> new FileReader((File) null),
                    "FileReader((File)null) -> NPE");
        checkThrows(NullPointerException.class, () -> new FileReader((String) null),
                    "FileReader((String)null) -> NPE");

        checkThrows(FileNotFoundException.class, () -> new FileReader(dir),
                    "directory -> FNFE");
        try {
            new FileReader(dir);
            check(false, "directory ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains(dir.getName()),
                  "directory FNFE message contains the path");
        }

        File missing = new File(dir, "nope.txt");
        checkThrows(FileNotFoundException.class, () -> new FileReader(missing),
                    "missing file -> FNFE");
        try {
            new FileReader(missing.getPath());
            check(false, "missing String path ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("nope.txt"),
                  "FNFE message contains file name");
        }
    }

    // ---- FileDescriptor constructor ----
    static void testFileDescriptorCtor(File dir) throws Exception {
        File f = new File(dir, "fd.txt");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("hi".getBytes("ISO-8859-1"));
        fos.close();
        FileInputStream fis = new FileInputStream(f);
        FileReader r = new FileReader(fis.getFD());
        check(r.read() == 'h' && r.read() == 'i', "FileReader(FileDescriptor) reads content");
        check(r.read() == -1, "EOF through FD");
        r.close();
        try { fis.close(); } catch (IOException ignored) { }
    }

    // ---- closed-state matrix ----
    static void testClosedMatrix(File dir) throws Exception {
        File f = new File(dir, "c.txt");
        new FileOutputStream(f).close();
        final FileReader r = new FileReader(f);
        check(!r.markSupported(), "markSupported false (inherits InputStreamReader)");
        r.close();
        r.close();
        check(true, "close idempotent");
        checkMsg(IOException.class, "Stream closed", () -> r.read(), "closed read()");
        checkMsg(IOException.class, "Stream closed", () -> r.skip(1), "closed skip()");
        checkMsg(IOException.class, "Stream closed", () -> r.ready(), "closed ready()");
        checkMsg(IOException.class, "Stream closed",
                 () -> r.read(new char[1], 0, 1), "closed read(char[])");
    }

    // ---- read bounds ----
    static void testReadBounds(File dir) throws Exception {
        File f = new File(dir, "b.txt");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("hello".getBytes("ISO-8859-1"));
        fos.close();
        final FileReader r = new FileReader(f);
        checkThrows(NullPointerException.class, () -> r.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], -1, 1),
                    "read(cbuf,-1,1) -> IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], 0, 3),
                    "read(cbuf,0,3) -> IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 1, Integer.MAX_VALUE),
                    "read(cbuf,1,MAX) overflow -> IOOBE");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0");
        char[] buf = new char[5];
        int n = r.read(buf, 1, 3);
        check(n == 3 && buf[0] == 0 && buf[1] == 'h' && buf[4] == 0,
              "exact offset placement");
        check(r.read() == 'l', "position after bounded bulk read");
        r.close();
    }

    // ---- byte-level read back (ASCII keeps default charset irrelevant) ----
    static void testReadBackBytes(File dir) throws Exception {
        File f = new File(dir, "rb.txt");
        String text = "byte-level read back 0123";
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(text.getBytes("ISO-8859-1"));
        fos.close();

        FileReader r = new FileReader(f);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[7];
        int n;
        while ((n = r.read(buf, 0, buf.length)) != -1) sb.append(buf, 0, n);
        r.close();
        check(text.equals(sb.toString()), "chunked read reassembles exact content");

        FileReader r2 = new FileReader(f.getPath());
        check(r2.read() == 'b', "single char read after bulk reader closed");
        r2.close();

        // empty file
        File e = new File(dir, "empty.txt");
        new FileOutputStream(e).close();
        FileReader er = new FileReader(e);
        check(er.read() == -1, "empty file read() -> -1");
        check(er.read(new char[4]) == -1, "empty file bulk read -> -1");
        check(!er.ready(), "empty file not ready");
        er.close();
    }

    // ---- mixed single/bulk reads + skip ----
    static void testMixedAndSkip(File dir) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append((char) ('0' + i % 10));
        String text = sb.toString();
        File f = new File(dir, "mix.txt");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(text.getBytes("ISO-8859-1"));
        fos.close();

        FileReader r = new FileReader(f);
        check(r.read() == '0', "first char");
        check(r.skip(2) == 2, "skip 2 chars");
        check(r.read() == '3', "char after skip");
        char[] buf = new char[100];
        check(r.read(buf) == 100, "bulk 100 after skip");
        check(new String(buf, 0, 100).equals(text.substring(4, 104)),
              "bulk content matches source region");
        check(r.read(buf, 0, 96) == 96, "second bulk returns remainder");
        check(r.read() == -1, "EOF");
        check(r.skip(5) == 0, "skip at EOF returns 0");
        r.close();
    }

    // ---- further semantics: ready, independent readers, skip validation ----
    static void testMoreSemantics(File dir) throws Exception {
        File f = new File(dir, "sem.txt");
        String text = "semicolon semantics";
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(text.getBytes("ISO-8859-1"));
        fos.close();

        FileReader r = new FileReader(f);
        check(r.ready(), "ready true while file has data");
        while (r.read() != -1) { }
        check(!r.ready(), "ready false at EOF");
        checkMsg(IllegalArgumentException.class, "skip value is negative",
                 () -> r.skip(-1), "negative skip message (inherited from ISR)");
        check(r.skip(0) == 0, "skip(0) at EOF returns 0");
        r.close();

        // two independent readers keep separate positions
        FileReader a = new FileReader(f);
        FileReader b = new FileReader(f);
        check(a.read() == 's' && b.read() == 's', "both start at position 0");
        a.read();
        check(b.read() == 'e', "advancing a does not move b");
        check(a.read() == 'm', "a advanced to third char");
        a.close();
        check(b.read() == 'm', "closing a does not affect b");
        b.close();

        // directory path via String ctor
        try {
            new FileReader(dir.getPath());
            check(false, "directory via String ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null, "directory String ctor FNFE message non-null");
        }

        // getFD-backed reader: mark/reset unsupported (inherits InputStreamReader)
        FileReader m = new FileReader(f);
        check(!m.markSupported(), "markSupported false");
        m.close();
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        File dir = newTempDir();
        try {
            testConstructorNegatives(dir);
            testFileDescriptorCtor(dir);
            testClosedMatrix(dir);
            testReadBounds(dir);
            testReadBackBytes(dir);
            testMixedAndSkip(dir);
            testMoreSemantics(dir);
        } finally {
            deleteRecursively(dir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
