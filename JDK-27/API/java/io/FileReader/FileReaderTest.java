/*
 * @test
 * @summary java.io.FileReader conformance: white-box structure (reflection) + black-box behavior
 *          (UTF-8 character reads and roundtrip with FileWriter).
 *          Deepened (contract level): ctor negatives (null NPE, directory FNFE
 *          "Is a directory", missing FNFE "No such file or directory" with path),
 *          FileDescriptor ctor via FileInputStream FD, closed-state matrix
 *          ("Stream closed"), read bounds, empty file, mixed single/bulk/offset reads,
 *          skip semantics; JDK-11+ Charset ctors: (File,Charset)/(String,Charset)
 *          UTF-16BE decoding, null Charset NPE, historical getEncoding name.
 * @run main FileReaderTest
 *
 * 黑盒设计案:
 * 1. 契约: 读文件的字符流，默认按系统编码；显式 FileReader(File, Charset) 可固定 UTF-8。
 * 2. 场景: FileWriter(File, UTF-8) 写非 ASCII -> FileReader(File, UTF-8) 读回。
 * 3. 组合: 写 "héllo 世界" -> read() 逐字符/read(char[],off,len) 批量 -> 拼接断言一致。
 * 4. 不变量: 显式 Charset 构造保证确定性编码；读到末尾 read()==-1。
 * 5. 边界: 打开不存在的文件抛 FileNotFoundException。
 */

import java.io.File;
import java.io.FileInputStream;
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

public class FileReaderTest {
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
        Class<?> c = Class.forName("java.io.FileReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStreamReader.class, "extends InputStreamReader");
        c.getConstructor(File.class, Charset.class);
        passed++;
        hasMethod(c, "read");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-fr");
        File f = dir.resolve("text.txt").toFile();
        try {
            try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8)) {
                fw.write("héllo 世界");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] buf = new char[100];
                int n = fr.read(buf, 0, buf.length);
                check("héllo 世界".equals(new String(buf, 0, n)), "read(char[],off,len) roundtrip");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                check(fr.read() == 'h', "read h");
                check(fr.read() == 'é', "read é");
                check(fr.read() == 'l', "read l");
                check(fr.read() != -1, "more chars");
            }

            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                char[] all = new char[200];
                int n = fr.read(all);
                check(n == "héllo 世界".length(), "read count equals length");
                check(fr.read() == -1, "EOF");
            }

            boolean fnf = false;
            try { new FileReader(dir.resolve("nope.txt").toFile(), StandardCharsets.UTF_8); }
            catch (FileNotFoundException e) { fnf = true; }
            check(fnf, "FileNotFoundException");
        } finally {
            Files.deleteIfExists(f.toPath());
            Files.deleteIfExists(dir);
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

        final File d = dir;
        checkMsg(FileNotFoundException.class, "Is a directory", () -> new FileReader(d),
                 "directory -> FNFE \"Is a directory\"");
        try {
            new FileReader(dir);
            check(false, "directory ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains(dir.getName()),
                  "directory FNFE message contains the path");
        }

        File missing = new File(dir, "nope.txt");
        checkMsg(FileNotFoundException.class, "No such file or directory",
                 () -> new FileReader(missing), "missing file -> FNFE message");
        try {
            new FileReader(missing.getPath());
            check(false, "missing String path ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("nope.txt"),
                  "FNFE message contains file name");
            check(e.getMessage() != null && e.getMessage().contains("No such file or directory"),
                  "FNFE message contains OS reason");
        }
    }

    // ---- FileDescriptor constructor ----
    static void testFileDescriptorCtor(File dir) throws Exception {
        File f = new File(dir, "fd.txt");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("hi".getBytes(StandardCharsets.ISO_8859_1));
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
        fos.write("hello".getBytes(StandardCharsets.ISO_8859_1));
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

    // ---- byte-level read back ----
    static void testReadBackBytes(File dir) throws Exception {
        File f = new File(dir, "rb.txt");
        String text = "byte-level read back 0123";
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(text.getBytes(StandardCharsets.ISO_8859_1));
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
        fos.write(text.getBytes(StandardCharsets.ISO_8859_1));
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
        fos.write(text.getBytes(StandardCharsets.ISO_8859_1));
        fos.close();

        FileReader r = new FileReader(f);
        check(r.ready(), "ready true while file has data");
        while (r.read() != -1) { }
        check(!r.ready(), "ready false at EOF");
        checkMsg(IllegalArgumentException.class, "skip value is negative",
                 () -> r.skip(-1), "negative skip message (inherited from ISR)");
        check(r.skip(0) == 0, "skip(0) at EOF returns 0");
        r.close();

        FileReader a = new FileReader(f);
        FileReader b = new FileReader(f);
        check(a.read() == 's' && b.read() == 's', "both start at position 0");
        a.read();
        check(b.read() == 'e', "advancing a does not move b");
        check(a.read() == 'm', "a advanced to third char");
        a.close();
        check(b.read() == 'm', "closing a does not affect b");
        b.close();

        try {
            new FileReader(dir.getPath());
            check(false, "directory via String ctor must throw");
        } catch (FileNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("Is a directory"),
                  "directory String ctor FNFE message");
        }

        FileReader m = new FileReader(f);
        check(!m.markSupported(), "markSupported false");
        m.close();
    }

    // ---- JDK-11+ Charset constructors ----
    static void testCharsetCtors(File dir) throws Exception {
        File f = new File(dir, "u16.txt");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("ABé".getBytes(StandardCharsets.UTF_16BE));
        fos.close();

        FileReader r = new FileReader(f, StandardCharsets.UTF_16BE);
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = r.read()) != -1) sb.append((char) c);
        r.close();
        check("ABé".contentEquals(sb), "(File,Charset) decodes UTF-16BE exactly");

        FileReader rs = new FileReader(f.getPath(), StandardCharsets.UTF_16BE);
        check(rs.read() == 'A' && rs.read() == 'B', "(String,Charset) decodes");
        check("UnicodeBigUnmarked".equals(rs.getEncoding()),
              "Charset ctor reports historical UTF-16BE name");
        rs.close();

        FileReader rb = new FileReader(f, StandardCharsets.UTF_16BE);
        char[] buf = new char[3];
        check(rb.read(buf, 0, 3) == 3 && buf[2] == 'é', "(File,Charset) bulk offset read");
        rb.close();

        checkThrows(NullPointerException.class,
                 () -> new FileReader(f, (Charset) null),
                 "(File,null Charset) -> NPE");
        checkThrows(NullPointerException.class,
                 () -> new FileReader(f.getPath(), (Charset) null),
                 "(String,null Charset) -> NPE");
        checkMsg(FileNotFoundException.class, "No such file or directory",
                 () -> new FileReader(new File(dir, "nope2"), StandardCharsets.UTF_8),
                 "(File,Charset) missing -> FNFE");
        checkMsg(FileNotFoundException.class, "Is a directory",
                 () -> new FileReader(dir, StandardCharsets.UTF_8),
                 "(File,Charset) directory -> FNFE");

        // UTF-8 non-ASCII decode through the Charset ctor
        File g = new File(dir, "u8.txt");
        FileOutputStream fo2 = new FileOutputStream(g);
        fo2.write("héllo 世界".getBytes(StandardCharsets.UTF_8));
        fo2.close();
        FileReader rg = new FileReader(g, StandardCharsets.UTF_8);
        sb.setLength(0);
        while ((c = rg.read()) != -1) sb.append((char) c);
        rg.close();
        check("héllo 世界".contentEquals(sb), "(File,UTF_8) multi-script decode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        Path dir = Files.createTempDirectory("jck-fr-deep");
        File d = dir.toFile();
        try {
            testConstructorNegatives(d);
            testFileDescriptorCtor(d);
            testClosedMatrix(d);
            testReadBounds(d);
            testReadBackBytes(d);
            testMixedAndSkip(d);
            testMoreSemantics(d);
            testCharsetCtors(d);
        } finally {
            Files.walk(dir)
                 .sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> p.toFile().delete());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
