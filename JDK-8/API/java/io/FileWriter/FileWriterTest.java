/*
 * @test
 * @summary java.io.FileWriter conformance: full contract coverage.
 *          File/String constructors, truncate vs append, write(String)/append(char),
 *          write(String,off,len) region, flush. ASCII content keeps the
 *          default-charset behavior deterministic (JDK 8 has no Charset ctor).
 *          Deepened (contract level): ctor negatives (null NPE, missing-parent FNFE
 *          "No such file or directory", directory FNFE "Is a directory"),
 *          FileDescriptor ctor, closed matrix ("Stream closed"), truncate-vs-append
 *          both ctors, write bounds (NPE/IOOBE/StringIndexOOB), write(int) low 16 bits,
 *          flush visibility, char-by-char writes, reopen semantics (ASCII only:
 *          JDK-8 FileWriter has no Charset ctor).
 * @run main FileWriterTest
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static File newTempDir() throws Exception {
        File d = File.createTempFile("jck-fw", ".tmp");
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

    static String readBack(File f) throws Exception {
        FileReader fr = new FileReader(f);
        char[] buf = new char[100];
        int n = fr.read(buf);
        fr.close();
        return new String(buf, 0, n);
    }

    static String readAll(File f) throws Exception {
        FileReader fr = new FileReader(f);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[64];
        int n;
        while ((n = fr.read(buf)) != -1) sb.append(buf, 0, n);
        fr.close();
        return sb.toString();
    }

    static void testBehavior() throws Exception {
        File dir = newTempDir();
        try {
            File f = new File(dir, "w.txt");

            FileWriter fw = new FileWriter(f);
            fw.write("ab");
            fw.close();
            check(f.length() == 2, "ASCII length 2");

            FileWriter app = new FileWriter(f, true);
            app.append("cd");
            app.close();
            check(f.length() == 4, "append adds chars");
            check("abcd".equals(readBack(f)), "append roundtrip");

            FileWriter trunc = new FileWriter(f);
            trunc.write("xy");
            trunc.close();
            check(f.length() == 2, "default mode truncates");
            check("xy".equals(readBack(f)), "truncate content");

            FileWriter region = new FileWriter(f);
            region.write("abcdef", 1, 3);
            region.close();
            check("bcd".equals(readBack(f)), "write(String,off,len)");

            FileWriter sc = new FileWriter(f.getPath(), true);
            sc.write("z");
            sc.flush();
            sc.close();
            check("bcdz".equals(readBack(f)), "String ctor append + flush");

            char[] chars = {'P', 'Q', 'R'};
            FileWriter cw = new FileWriter(f);
            cw.write(chars, 1, 2);
            cw.close();
            check("QR".equals(readBack(f)), "write(char[],off,len)");
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
        checkThrows(NullPointerException.class, () -> new FileWriter((File) null),
                    "FileWriter((File)null) -> NPE");
        checkThrows(NullPointerException.class, () -> new FileWriter((String) null),
                    "FileWriter((String)null) -> NPE");
        checkThrows(NullPointerException.class, () -> new FileWriter((File) null, true),
                    "FileWriter((File)null,true) -> NPE");
        checkThrows(NullPointerException.class, () -> new FileWriter((String) null, false),
                    "FileWriter((String)null,false) -> NPE");

        File missingParent = new File(new File(dir, "nodir"), "x.txt");
        checkThrows(java.io.FileNotFoundException.class, () -> new FileWriter(missingParent),
                    "missing parent dir -> FNFE");
        try {
            new FileWriter(missingParent.getPath());
            check(false, "missing parent String ctor must throw");
        } catch (java.io.FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("nodir"),
                  "FNFE message contains the missing parent path");
        }

        checkThrows(java.io.FileNotFoundException.class, () -> new FileWriter(dir),
                    "directory -> FNFE");
        checkThrows(java.io.FileNotFoundException.class, () -> new FileWriter(dir, true),
                    "directory append=true -> FNFE");
    }

    // ---- FileDescriptor constructor ----
    static void testFileDescriptorCtor(File dir) throws Exception {
        File f = new File(dir, "fdw.txt");
        FileOutputStream fos = new FileOutputStream(f);
        FileDescriptor fd = fos.getFD();
        FileWriter w = new FileWriter(fd);
        w.write("ok");
        w.close();
        check("ok".equals(readBack(f)), "FileWriter(FileDescriptor) writes readable content");
        fos.close();
    }

    // ---- closed-state matrix ----
    static void testClosedMatrix(File dir) throws Exception {
        File f = new File(dir, "c.txt");
        final FileWriter w = new FileWriter(f);
        w.close();
        w.close();
        check(true, "close idempotent");
        checkMsg(IOException.class, "Stream closed", () -> w.write('x'), "closed write(int)");
        checkMsg(IOException.class, "Stream closed",
                 () -> w.write(new char[]{'x'}, 0, 1), "closed write(char[])");
        checkMsg(IOException.class, "Stream closed", () -> w.write("x"), "closed write(String)");
        checkMsg(IOException.class, "Stream closed", () -> w.flush(), "closed flush()");
        check(f.length() == 0, "closed-before-write leaves file empty");
    }

    // ---- truncate vs append across both ctors ----
    static void testTruncateVsAppend(File dir) throws Exception {
        File f = new File(dir, "ta.txt");
        FileWriter a = new FileWriter(f);
        a.write("abcdef");
        a.close();

        FileWriter t = new FileWriter(f, false);         // explicit truncate
        t.write("gh");
        t.close();
        check("gh".equals(readBack(f)), "append=false truncates longer content");

        FileWriter t2 = new FileWriter(f.getPath(), false);
        t2.write("ij");
        t2.close();
        check("ij".equals(readBack(f)), "String ctor append=false truncates");

        FileWriter ap = new FileWriter(f, true);
        ap.write("kl");
        ap.close();
        check("ijkl".equals(readBack(f)), "File ctor append=true extends");

        FileWriter ap2 = new FileWriter(f.getPath(), true);
        ap2.append('m');                                  // append(char) respects mode
        ap2.write("");
        ap2.close();
        check("ijklm".equals(readBack(f)), "String ctor append=true + empty write");

        FileWriter empty = new FileWriter(f, true);
        empty.write("");
        empty.close();
        check("ijklm".equals(readBack(f)), "empty append is a no-op");
    }

    // ---- write bounds ----
    static void testWriteBounds(File dir) throws Exception {
        File f = new File(dir, "b.txt");
        final FileWriter w = new FileWriter(f);
        checkThrows(NullPointerException.class, () -> w.write((String) null, 0, 1),
                    "write((String)null,0,1) -> NPE");
        checkThrows(NullPointerException.class, () -> w.write((char[]) null, 0, 1),
                    "write((char[])null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> w.write(new char[2], 1, Integer.MAX_VALUE),
                    "write(cbuf,1,MAX) overflow -> IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> w.write(new char[2], 0, 3),
                    "write(cbuf,0,3) -> IOOBE");
        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> w.write("abc", 1, 5),
                    "write(str,1,5) -> StringIndexOOB");
        w.write("ok");                                     // writer still usable
        w.close();
        check("ok".equals(readBack(f)), "content intact after failed bound writes");

        File g = new File(dir, "b2.txt");
        FileWriter w2 = new FileWriter(g);
        w2.write("0123456789", 2, 3);
        w2.write(new char[]{'a', 'b', 'c'}, 1, 2);
        w2.close();
        check("234bc".equals(readBack(g)), "String and char[] regions concatenate");
    }

    // ---- write(int) low 16 bits + append(char) ----
    static void testWriteIntLow16(File dir) throws Exception {
        File f = new File(dir, "w16.txt");
        FileWriter w = new FileWriter(f);
        w.write(0x10041);                                  // low 16 bits 'A'
        w.write(0x20042);                                  // low 16 bits 'B'
        w.append('C');
        w.close();
        check("ABC".equals(readBack(f)), "write(int) truncates to 16 bits; append(char)");
        check(f.length() == 3, "three ASCII chars -> three bytes");
    }

    // ---- flush visibility ----
    static void testFlushVisibility(File dir) throws Exception {
        File f = new File(dir, "fl.txt");
        FileWriter w = new FileWriter(f);
        w.write("flushme");
        w.flush();
        check(f.length() == 7, "flush makes content visible on disk immediately");
        w.write("!");
        check(f.length() == 7, "post-flush buffered char not yet visible");
        w.close();
        check(f.length() == 8, "close flushes the remainder");
        check("flushme!".equals(readBack(f)), "full content after close");
    }

    // ---- char-by-char and large writes ----
    static void testIncrementalAndLargeWrites(File dir) throws Exception {
        File f = new File(dir, "inc.txt");
        FileWriter w = new FileWriter(f);
        for (int i = 0; i < 10; i++) w.write((char) ('a' + i));
        w.flush();
        check("abcdefghij".equals(readBack(f)), "char-by-char writes visible after flush");
        w.close();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append((char) ('A' + i % 26));
        File g = new File(dir, "big.txt");
        FileWriter w2 = new FileWriter(g);
        w2.write(sb.toString());
        w2.close();
        check(g.length() == 500, "500 ASCII chars -> 500 bytes");
        check(sb.toString().equals(readAll(g)), "large write read back exactly");
    }

    // ---- append(CharSequence) contract incl. null -> "null" ----
    static void testAppendSemantics(File dir) throws Exception {
        File f = new File(dir, "ap.txt");
        FileWriter w = new FileWriter(f);
        check(w.append("AB") == w, "append returns this");
        w.append((CharSequence) null);                  // documented: literal "null"
        w.append("cdef", 1, 3);                         // [1,3) = "de"
        w.append("");                                   // no-op
        w.close();
        check("ABnullde".equals(readBack(f)), "append forms + null contract");
    }

    // ---- write(int)/write(char[])/write(String)/append produce identical files ----
    static void testWriteFormsAgree(File dir) throws Exception {
        File a = new File(dir, "f_int.txt");
        FileWriter wi = new FileWriter(a);
        wi.write('a');
        wi.close();
        File b = new File(dir, "f_arr.txt");
        FileWriter wa = new FileWriter(b);
        wa.write(new char[]{'a'}, 0, 1);
        wa.close();
        File c = new File(dir, "f_str.txt");
        FileWriter ws = new FileWriter(c);
        ws.write("a");
        ws.close();
        File d = new File(dir, "f_app.txt");
        FileWriter wp = new FileWriter(d);
        wp.append('a');
        wp.close();
        check("a".equals(readBack(a)) && a.length() == 1, "write(int) single char file");
        check("a".equals(readBack(b)) && b.length() == 1, "write(char[]) single char file");
        check("a".equals(readBack(c)) && c.length() == 1, "write(String) single char file");
        check("a".equals(readBack(d)) && d.length() == 1, "append(char) single char file");
    }

    // ---- inherited OutputStreamWriter.getEncoding semantics ----
    static void testInheritedGetEncoding(File dir) throws Exception {
        File f = new File(dir, "enc.txt");
        FileWriter w = new FileWriter(f);
        check(w.getEncoding() != null, "getEncoding non-null while open (inherited)");
        w.close();
        check(w.getEncoding() == null, "getEncoding null after close (inherited)");
    }

    // ---- reopen semantics + zero-length and repeated flushes ----
    static void testReopenAndNoops(File dir) throws Exception {
        File f = new File(dir, "re.txt");
        FileWriter w = new FileWriter(f);
        w.write("12345");
        w.close();

        FileWriter re = new FileWriter(f);              // default mode truncates on open
        re.write("ab");
        re.close();
        check("ab".equals(readBack(f)), "reopen without append truncates");

        FileWriter ap = new FileWriter(f, true);
        ap.write("cd");
        ap.close();
        check("abcd".equals(readBack(f)), "reopen with append extends");

        FileWriter z = new FileWriter(f, true);
        z.write("");
        z.write(new char[0], 0, 0);
        z.flush();
        z.flush();                                      // repeated flush is idempotent
        z.close();
        check("abcd".equals(readBack(f)), "empty writes + double flush are no-ops");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        File dir = newTempDir();
        try {
            testConstructorNegatives(dir);
            testFileDescriptorCtor(dir);
            testClosedMatrix(dir);
            testTruncateVsAppend(dir);
            testWriteBounds(dir);
            testWriteIntLow16(dir);
            testFlushVisibility(dir);
            testIncrementalAndLargeWrites(dir);
            testAppendSemantics(dir);
            testWriteFormsAgree(dir);
            testInheritedGetEncoding(dir);
            testReopenAndNoops(dir);
        } finally {
            deleteRecursively(dir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
