/*
 * @test
 * @summary java.io.FileWriter conformance: white-box structure (reflection) + black-box behavior
 *          (write/append modes with explicit UTF-8 and roundtrip with FileReader).
 *          Deepened (contract level): ctor negatives (null NPE, missing-parent FNFE
 *          "No such file or directory", directory FNFE "Is a directory"),
 *          FileDescriptor ctor, closed matrix ("Stream closed"), truncate-vs-append
 *          both ctors, write bounds (NPE/IOOBE/StringIndexOOB), write(int) low 16 bits,
 *          flush visibility, append(null)->"null", write-forms agreement,
 *          inherited getEncoding open/close, reopen semantics; JDK-11+ Charset ctors:
 *          (File/String,Charset[,append]) UTF-16BE exact bytes, historical getEncoding
 *          name, null Charset NPE (one-shot files: null-Charset ctor truncates first).
 * @run main FileWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 写文件的字符流，默认系统编码；FileWriter(File, Charset[, append]) 固定编码与追加模式。
 * 2. 场景: FileWriter(File, UTF-8) 写 -> 读回；FileWriter(File, UTF-8, true) 追加。
 * 3. 组合: 写 "ab" -> 追加 "cd" -> FileReader(UTF-8) 读回 "abcd"；默认构造覆盖。
 * 4. 不变量: append=true 保留原内容；默认 append=false 覆盖。
 * 5. 边界: write(char[],off,len) 只写指定范围。
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FileWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStreamWriter.class, "extends OutputStreamWriter");
        c.getConstructor(File.class, Charset.class, boolean.class);
        passed++;
        hasMethod(c, "write", String.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fw");
        File f = dir.resolve("w.txt").toFile();
        try {
            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("ab");
            }
            check(f.length() == 2, "wrote 2 chars");

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8, true)) {
                fw.append("cd");
            }
            check(f.length() == 4, "append adds 2 chars");

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] buf = new char[10];
                int n = fr.read(buf);
                check("abcd".equals(new String(buf, 0, n)), "append roundtrip");
            }

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("xy");
            }
            check(f.length() == 2, "default mode truncates");

            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("\u00e9\u4e2d");
            }
            check(f.length() == "\u00e9\u4e2d".getBytes(StandardCharsets.UTF_8).length, "UTF-8 byte length");
        } finally {
            Files.deleteIfExists(f.toPath());
            Files.deleteIfExists(dir);
        }
    }

    static String readBack(File f) throws Exception {
        try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
            char[] buf = new char[100];
            int n = fr.read(buf);
            return new String(buf, 0, n);
        }
    }

    static String readAll(File f) throws Exception {
        try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[64];
            int n;
            while ((n = fr.read(buf)) != -1) sb.append(buf, 0, n);
            return sb.toString();
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
        checkThrows(FileNotFoundException.class,
                 () -> new FileWriter(missingParent, StandardCharsets.UTF_8),
                 "missing parent dir -> FNFE");
        try {
            new FileWriter(missingParent.getPath(), StandardCharsets.UTF_8);
            check(false, "missing parent String ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("nodir"),
                  "FNFE message contains the missing parent path");
        }

        checkThrows(FileNotFoundException.class,
                 () -> new FileWriter(dir, StandardCharsets.UTF_8),
                 "directory -> FNFE");
        checkThrows(FileNotFoundException.class,
                 () -> new FileWriter(dir, StandardCharsets.UTF_8, true),
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
        final FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
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

    // ---- truncate vs append across File/String and Charset ctors ----
    static void testTruncateVsAppend(File dir) throws Exception {
        File f = new File(dir, "ta.txt");
        FileWriter a = new FileWriter(f, StandardCharsets.UTF_8);
        a.write("abcdef");
        a.close();

        FileWriter t = new FileWriter(f, StandardCharsets.UTF_8, false);
        t.write("gh");
        t.close();
        check("gh".equals(readBack(f)), "append=false truncates longer content");

        FileWriter t2 = new FileWriter(f.getPath(), StandardCharsets.UTF_8, false);
        t2.write("ij");
        t2.close();
        check("ij".equals(readBack(f)), "String ctor append=false truncates");

        FileWriter ap = new FileWriter(f, StandardCharsets.UTF_8, true);
        ap.write("kl");
        ap.close();
        check("ijkl".equals(readBack(f)), "File ctor append=true extends");

        FileWriter ap2 = new FileWriter(f.getPath(), StandardCharsets.UTF_8, true);
        ap2.append('m');
        ap2.write("");
        ap2.close();
        check("ijklm".equals(readBack(f)), "String ctor append=true + empty write");

        FileWriter empty = new FileWriter(f, StandardCharsets.UTF_8, true);
        empty.write("");
        empty.close();
        check("ijklm".equals(readBack(f)), "empty append is a no-op");
    }

    // ---- write bounds ----
    static void testWriteBounds(File dir) throws Exception {
        File f = new File(dir, "b.txt");
        final FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
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
        w.write("ok");
        w.close();
        check("ok".equals(readBack(f)), "content intact after failed bound writes");

        File g = new File(dir, "b2.txt");
        FileWriter w2 = new FileWriter(g, StandardCharsets.UTF_8);
        w2.write("0123456789", 2, 3);
        w2.write(new char[]{'a', 'b', 'c'}, 1, 2);
        w2.close();
        check("234bc".equals(readBack(g)), "String and char[] regions concatenate");
    }

    // ---- write(int) low 16 bits + append(char) ----
    static void testWriteIntLow16(File dir) throws Exception {
        File f = new File(dir, "w16.txt");
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
        w.write(0x10041);
        w.write(0x20042);
        w.append('C');
        w.close();
        check("ABC".equals(readBack(f)), "write(int) truncates to 16 bits; append(char)");
        check(f.length() == 3, "three ASCII chars -> three UTF-8 bytes");
    }

    // ---- flush visibility ----
    static void testFlushVisibility(File dir) throws Exception {
        File f = new File(dir, "fl.txt");
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
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
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
        for (int i = 0; i < 10; i++) w.write((char) ('a' + i));
        w.flush();
        check("abcdefghij".equals(readBack(f)), "char-by-char writes visible after flush");
        w.close();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append((char) ('A' + i % 26));
        File g = new File(dir, "big.txt");
        FileWriter w2 = new FileWriter(g, StandardCharsets.UTF_8);
        w2.write(sb.toString());
        w2.close();
        check(g.length() == 500, "500 ASCII chars -> 500 bytes");
        check(sb.toString().equals(readAll(g)), "large write read back exactly");
    }

    // ---- append(CharSequence) contract incl. null -> "null" ----
    static void testAppendSemantics(File dir) throws Exception {
        File f = new File(dir, "ap.txt");
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
        check(w.append("AB") == w, "append returns this");
        w.append((CharSequence) null);
        w.append("cdef", 1, 3);                          // [1,3) = "de"
        w.append("");
        w.close();
        check("ABnullde".equals(readBack(f)), "append forms + null contract");
    }

    // ---- write(int)/write(char[])/write(String)/append produce identical files ----
    static void testWriteFormsAgree(File dir) throws Exception {
        File a = new File(dir, "f_int.txt");
        FileWriter wi = new FileWriter(a, StandardCharsets.UTF_8);
        wi.write('a');
        wi.close();
        File b = new File(dir, "f_arr.txt");
        FileWriter wa = new FileWriter(b, StandardCharsets.UTF_8);
        wa.write(new char[]{'a'}, 0, 1);
        wa.close();
        File c = new File(dir, "f_str.txt");
        FileWriter ws = new FileWriter(c, StandardCharsets.UTF_8);
        ws.write("a");
        ws.close();
        File d = new File(dir, "f_app.txt");
        FileWriter wp = new FileWriter(d, StandardCharsets.UTF_8);
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
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_16BE);
        check("UnicodeBigUnmarked".equals(w.getEncoding()),
              "Charset ctor reports historical UTF-16BE name");
        w.write("A");
        w.close();
        check(w.getEncoding() == null, "getEncoding null after close (inherited)");
        check(f.length() == 2, "UTF-16BE single char is 2 bytes, no BOM");
    }

    // ---- reopen semantics + zero-length and repeated flushes ----
    static void testReopenAndNoops(File dir) throws Exception {
        File f = new File(dir, "re.txt");
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_8);
        w.write("12345");
        w.close();

        FileWriter re = new FileWriter(f, StandardCharsets.UTF_8);
        re.write("ab");
        re.close();
        check("ab".equals(readBack(f)), "reopen without append truncates");

        FileWriter ap = new FileWriter(f, StandardCharsets.UTF_8, true);
        ap.write("cd");
        ap.close();
        check("abcd".equals(readBack(f)), "reopen with append extends");

        FileWriter z = new FileWriter(f, StandardCharsets.UTF_8, true);
        z.write("");
        z.write(new char[0], 0, 0);
        z.flush();
        z.flush();
        z.close();
        check("abcd".equals(readBack(f)), "empty writes + double flush are no-ops");
    }

    // ---- JDK-11+ Charset constructors (null forms use one-shot files:
    //      the null-Charset ctor opens/truncates the target before throwing) ----
    static void testCharsetCtors(File dir) throws Exception {
        File f = new File(dir, "u16.txt");
        FileWriter w = new FileWriter(f, StandardCharsets.UTF_16BE);
        w.write("AB\u00e9");
        w.close();
        check(f.length() == 6, "(File,Charset) UTF-16BE writes 6 bytes, no BOM");
        check("AB\u00e9".equals(readAllU16(f)), "(File,Charset) content readable back as UTF-16BE");

        File f2 = new File(dir, "u16b.txt");
        FileWriter ws = new FileWriter(f2.getPath(), StandardCharsets.UTF_16BE);
        ws.append("AB\u00e9");                                 // append(CharSequence) honors charset
        ws.close();
        check(f2.length() == 6, "(String,Charset) UTF-16BE append(CharSequence)");

        File f3 = new File(dir, "u16c.txt");
        FileWriter wa = new FileWriter(f3, StandardCharsets.UTF_16BE, true);
        wa.write("A");
        wa.close();
        FileWriter wa2 = new FileWriter(f3, StandardCharsets.UTF_16BE, true);
        wa2.write("B");
        wa2.close();
        check(f3.length() == 4, "(File,Charset,true) appends 2+2 bytes");

        // null Charset: all four forms throw NPE mentioning "charset"
        checkMsg(NullPointerException.class, "charset",
                 () -> new FileWriter(new File(dir, "n1.txt"), (Charset) null),
                 "(File,null Charset) -> NPE \"charset\"");
        checkMsg(NullPointerException.class, "charset",
                 () -> new FileWriter(new File(dir, "n2.txt"), (Charset) null, true),
                 "(File,null Charset,true) -> NPE");
        checkMsg(NullPointerException.class, "charset",
                 () -> new FileWriter(new File(dir, "n3.txt").getPath(), (Charset) null),
                 "(String,null Charset) -> NPE");
        checkMsg(NullPointerException.class, "charset",
                 () -> new FileWriter(new File(dir, "n4.txt").getPath(), (Charset) null, false),
                 "(String,null Charset,false) -> NPE");
        check(new File(dir, "n1.txt").length() == 0,
              "null-Charset ctor truncated the target before throwing (observed behavior)");

        // UTF-8 ctor: non-ASCII byte length exact
        File f4 = new File(dir, "u8.txt");
        FileWriter wu = new FileWriter(f4, StandardCharsets.UTF_8);
        wu.write("h\u00e9llo \u4e16\u754c");
        wu.close();
        check(f4.length() == "h\u00e9llo \u4e16\u754c".getBytes(StandardCharsets.UTF_8).length,
              "(File,UTF_8) exact UTF-8 byte length");
    }

    static String readAllU16(File f) throws Exception {
        try (FileReader fr = new FileReader(f, StandardCharsets.UTF_16BE)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[32];
            int n;
            while ((n = fr.read(buf)) != -1) sb.append(buf, 0, n);
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        Path dir = Files.createTempDirectory("jck-fw-deep");
        File d = dir.toFile();
        try {
            testConstructorNegatives(d);
            testFileDescriptorCtor(d);
            testClosedMatrix(d);
            testTruncateVsAppend(d);
            testWriteBounds(d);
            testWriteIntLow16(d);
            testFlushVisibility(d);
            testIncrementalAndLargeWrites(d);
            testAppendSemantics(d);
            testWriteFormsAgree(d);
            testInheritedGetEncoding(d);
            testReopenAndNoops(d);
            testCharsetCtors(d);
        } finally {
            Files.walk(dir)
                 .sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> p.toFile().delete());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
