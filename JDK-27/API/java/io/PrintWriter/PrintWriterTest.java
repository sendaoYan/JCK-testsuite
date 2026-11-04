/*
 * @test
 * @summary java.io.PrintWriter conformance: white-box structure (reflection) + black-box behavior
 *          (print/println/printf/format, checkError, UTF-8 output).
 *          Deepened: swallowed-IOException/checkError matrix (sticky error,
 *          close-error, post-close), all println/print overloads exact, write
 *          family bounds, deterministic autoFlush counts, full constructor
 *          matrix incl. JDK-27 Charset overloads, null asymmetry.
 * @run main PrintWriterTest
 *
 * 黑盒设计案:
 * 1. 契约: 格式化字符输出流，print/println/printf 各类型，从不抛 IOException（错误记入 checkError）。
 * 2. 场景: PrintWriter(StringWriter)/PrintWriter(OutputStream,boolean,Charset) -> print/println/printf/format。
 * 3. 组合: 固定 Locale.US printf 定点小数 -> toString 断言；UTF-8 输出到流 -> InputStreamReader 读回。
 * 4. 不变量: printf 返回 this 支持链式；checkError 正常写入为 false。
 * 5. 边界: println 追加平台行分隔符。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintWriterTest {
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
        Class<?> c = Class.forName("java.io.PrintWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "printf", String.class, Object[].class);
        hasMethod(c, "format", String.class, Object[].class);
        hasMethod(c, "println", String.class);
        hasMethod(c, "checkError");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        check(pw.printf(Locale.US, "%d %.2f %s", 42, 3.14159, "x") == pw, "printf returns this");
        pw.println();
        pw.print(true);
        pw.print(' ');
        pw.print(7L);
        pw.flush();
        String out = sw.toString();
        check(out.startsWith("42 3.14 x"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("7"), "print long");
        check(!pw.checkError(), "checkError false");

        StringWriter sw2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw2);
        pw2.format(Locale.US, "%05d", 42);
        pw2.flush();
        check("00042".equals(sw2.toString()), "format");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw3 = new PrintWriter(bos, false, StandardCharsets.UTF_8);
        pw3.println("héllo 世界");
        pw3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        char[] buf = new char[100];
        int n = isr.read(buf);
        check(("héllo 世界" + System.lineSeparator()).equals(new String(buf, 0, n)),
              "UTF-8 println roundtrip");
    }

    // ===================== deepening (contract level) =====================

    interface Action { void run() throws Exception; }

    static void expectThrows(String what, Class<? extends Throwable> type, Action a) {
        try { a.run(); }
        catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(what + ": expected " + type.getName() + " but got " + t);
        }
        throw new AssertionError(what + ": expected " + type.getName() + " but nothing thrown");
    }

    static void expectThrowsMsg(String what, Class<? extends Throwable> type, String msg, Action a) {
        try { a.run(); }
        catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(what + ": expected " + type.getName() + " but got " + t);
            if (t.getMessage() == null || !t.getMessage().contains(msg))
                throw new AssertionError(what + ": message \"" + t.getMessage()
                                         + "\" lacks \"" + msg + "\"");
            passed++; return;
        }
        throw new AssertionError(what + ": expected " + type.getName() + " but nothing thrown");
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X ", x));
        return sb.toString().trim();
    }

    /** Recording/failing probe writer used for all swallow-matrix tests. */
    static class ProbeWriter extends Writer {
        final StringBuilder sb = new StringBuilder();
        int flushCount;
        boolean failWrite, failFlush, failClose;
        boolean closed;
        ProbeWriter failAllWrites() { failWrite = true; return this; }
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (failWrite) throw new IOException("probe write failure");
            sb.append(cbuf, off, len);
        }
        public void flush() throws IOException {
            if (failFlush) throw new IOException("probe flush failure");
            flushCount++;
        }
        public void close() throws IOException {
            closed = true;
            if (failClose) throw new IOException("probe close failure");
        }
        String text() { return sb.toString(); }
    }

    static File tempDir() throws IOException {
        File d = new File(System.getProperty("java.io.tmpdir"), "ioG-pw27-" + System.nanoTime());
        if (!d.mkdirs()) throw new IOException("cannot create " + d);
        return d;
    }

    static byte[] readFile(File f) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        FileInputStream fi = new FileInputStream(f);
        try {
            byte[] b = new byte[1024]; int n;
            while ((n = fi.read(b)) > 0) bo.write(b, 0, n);
        } finally { fi.close(); }
        return bo.toByteArray();
    }

    static void deleteRec(File f) {
        if (f.isDirectory()) {
            File[] cs = f.listFiles();
            if (cs != null) for (File c : cs) deleteRec(c);
        }
        f.delete();
    }

    static final String SEP = System.lineSeparator();

    // ---- 1. println: every overload writes exact value + platform separator ----
    static void testPrintlnOverloads() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(true);
        pw.println(false);
        pw.println('x');
        pw.println(42);
        pw.println(-7);
        pw.println(Integer.MIN_VALUE);
        pw.println(12345678901L);
        pw.println(Long.MIN_VALUE);
        pw.println(1.5f);
        pw.println(2.0d);
        pw.println(0.0d);
        pw.println(-0.0d);
        pw.println(Float.NaN);
        pw.println(Float.POSITIVE_INFINITY);
        pw.println(Float.NEGATIVE_INFINITY);
        pw.println(Double.NaN);
        pw.println(Double.POSITIVE_INFINITY);
        pw.println(Double.NEGATIVE_INFINITY);
        pw.println(new char[]{'a', 'b'});
        pw.println((String) null);
        pw.println((Object) null);
        pw.println();
        pw.println(new Object() { public String toString() { return "OBJ"; } });
        pw.flush();
        String exp = "true" + SEP + "false" + SEP + "x" + SEP + "42" + SEP + "-7" + SEP
            + Integer.MIN_VALUE + SEP + "12345678901" + SEP + Long.MIN_VALUE + SEP
            + "1.5" + SEP + "2.0" + SEP + "0.0" + SEP + "-0.0" + SEP
            + "NaN" + SEP + "Infinity" + SEP + "-Infinity" + SEP
            + "NaN" + SEP + "Infinity" + SEP + "-Infinity" + SEP
            + "ab" + SEP + "null" + SEP + "null" + SEP + SEP + "OBJ" + SEP;
        check(exp.equals(sw.toString()), "println overload matrix exact text: " + sw.toString());
        check(!pw.checkError(), "println matrix leaves no error");
        StringWriter sw2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw2);
        pw2.println(1234.56);
        pw2.println(0.1f);
        pw2.flush();
        check(("1234.56" + SEP + "0.1" + SEP).equals(sw2.toString()),
              "println(double) uses String.valueOf, not locale formatting");
    }

    // ---- 2. print: every overload, exact conversions ----
    static void testPrintOverloads() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(true);
        pw.print(false);
        pw.print('x');
        pw.print('中');
        pw.print(0);
        pw.print(Integer.MIN_VALUE);
        pw.print(Long.MAX_VALUE);
        pw.print(1.5f);
        pw.print(2.0d);
        pw.print(Float.NaN);
        pw.print(Float.POSITIVE_INFINITY);
        pw.print(Float.NEGATIVE_INFINITY);
        pw.print(Double.NaN);
        pw.print(Double.POSITIVE_INFINITY);
        pw.print(Double.NEGATIVE_INFINITY);
        pw.print(new char[]{'a', 'b', 'c'});
        pw.print(new Object() { public String toString() { return "CUSTOM"; } });
        pw.flush();
        String exp = "truefalsex中0" + Integer.MIN_VALUE + Long.MAX_VALUE
            + "1.52.0NaNInfinity-InfinityNaNInfinity-InfinityabcCUSTOM";
        check(exp.equals(sw.toString()), "print overload matrix exact text: " + sw.toString());
        check(!pw.checkError(), "print matrix leaves no error");
    }

    // ---- 3. null asymmetry ----
    static void testNullAsymmetry() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print((String) null);
        pw.print((Object) null);
        pw.flush();
        check("nullnull".equals(sw.toString()),
              "print((String)null) and print((Object)null) write \"null\"");
        expectThrows("print((char[]) null)", NullPointerException.class,
                     new Action() { public void run() { new PrintWriter(new StringWriter()).print((char[]) null); } });
        expectThrows("println((char[]) null)", NullPointerException.class,
                     new Action() { public void run() { new PrintWriter(new StringWriter()).println((char[]) null); } });
        expectThrows("write((String) null)", NullPointerException.class,
                     new Action() { public void run() { new PrintWriter(new StringWriter()).write((String) null); } });
        expectThrows("write((char[]) null)", NullPointerException.class,
                     new Action() { public void run() { new PrintWriter(new StringWriter()).write((char[]) null); } });
        check(!pw.checkError(), "null print does not set error");
    }

    // ---- 4. write family ----
    static void testWriteFamily() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.write(0x10041);                        // low 16 bits -> 'A'
        pw.write("BCD", 1, 2);                    // "CD"
        pw.write(new char[]{'x', 'y', 'z'}, 1, 2);// "yz"
        pw.write('q');
        pw.write("W");
        pw.write(new char[]{'m', 'n'});
        pw.flush();
        check("ACDyzqWmn".equals(sw.toString()), "write family content order: " + sw.toString());
        PrintWriter p2 = new PrintWriter(new StringWriter());
        expectThrows("write(String,0,4) len beyond end", IndexOutOfBoundsException.class,
            new Action() { public void run() { p2.write("abc", 0, 4); } });
        expectThrows("write(String,-1,1)", IndexOutOfBoundsException.class,
            new Action() { public void run() { p2.write("abc", -1, 1); } });
        expectThrows("write(char[],3,0) off beyond length", IndexOutOfBoundsException.class,
            new Action() { public void run() { p2.write(new char[2], 3, 0); } });
        expectThrows("write(char[],-1,1)", IndexOutOfBoundsException.class,
            new Action() { public void run() { p2.write(new char[2], -1, 1); } });
        check(!p2.checkError(), "bounds exceptions do not set checkError");
        StringWriter sw3 = new StringWriter();
        PrintWriter p3 = new PrintWriter(sw3);
        p3.write("abc", 1, 0);
        p3.write(new char[2], 0, 0);
        p3.flush();
        check("".equals(sw3.toString()), "zero-length writes produce nothing");
    }

    // ---- 5. swallowed IOException + checkError semantics ----
    static void testSwallowAndCheckError() {
        ProbeWriter w = new ProbeWriter().failAllWrites();
        PrintWriter pw = new PrintWriter(w);
        pw.print("gone");
        check(true, "print survived underlying IOException");
        check(pw.checkError(), "checkError true after swallowed print failure");
        w.failWrite = false;
        pw.print("kept");
        check(pw.checkError(), "checkError sticky after underlying recovery");
        check("kept".equals(w.text()), "recovered print reaches the underlying writer");
        pw.flush();
        check(pw.checkError(), "flush() does not clear the error flag");
        ProbeWriter w2 = new ProbeWriter();
        PrintWriter pw2 = new PrintWriter(w2);
        w2.failFlush = true;
        pw2.print("x");
        pw2.flush();
        check(true, "flush survived underlying flush IOException");
        check(pw2.checkError(), "checkError true after flush failure");
        ProbeWriter w3 = new ProbeWriter();
        PrintWriter pw3 = new PrintWriter(w3);
        w3.failWrite = true;
        pw3.println("line");
        check(pw3.checkError(), "println failure swallowed -> error");
        pw3.write('c');
        check(pw3.checkError(), "write(int) failure swallowed -> error");
        pw3.write("str");
        check(pw3.checkError(), "write(String) failure swallowed -> error");
        pw3.write(new char[]{'a'});
        check(pw3.checkError(), "write(char[]) failure swallowed -> error");
        pw3.write(new char[]{'a'}, 0, 1);
        check(pw3.checkError(), "write(char[],int,int) failure swallowed -> error");
        pw3.write("st", 0, 2);
        check(pw3.checkError(), "write(String,int,int) failure swallowed -> error");
        pw3.append("ap");
        check(pw3.checkError(), "append failure swallowed -> error");
        pw3.printf(Locale.ROOT, "%d", 1);
        check(pw3.checkError(), "printf failure swallowed -> error");
        check(w3.sb.length() == 0, "failing underlying received no chars");
        ProbeWriter w4 = new ProbeWriter();
        PrintWriter pw4 = new PrintWriter(w4);
        pw4.print("x");
        int before = w4.flushCount;
        check(!pw4.checkError(), "healthy writer: no error");
        check(w4.flushCount == before + 1, "checkError() flushes the underlying writer");
        ProbeWriter w5 = new ProbeWriter();
        PrintWriter pw5 = new PrintWriter(w5);
        pw5.print("a"); pw5.println("b"); pw5.write('c'); pw5.append('d'); pw5.flush();
        check(!pw5.checkError(), "clean writer checkError false");
        check(("ab" + SEP + "cd").equals(w5.text()), "clean ops order in underlying: " + w5.text());
    }

    // ---- 6. close semantics ----
    static void testCloseSemantics() throws IOException {
        ProbeWriter w = new ProbeWriter();
        w.failClose = true;
        PrintWriter pw = new PrintWriter(w);
        pw.print("pre");
        pw.close();
        check(true, "close survived underlying close IOException");
        check(w.closed, "underlying close() was invoked");
        check(pw.checkError(), "checkError true after close failure");
        pw.println("after");
        check(true, "println after close does not throw");
        pw.print("p");
        check(true, "print after close does not throw");
        pw.write('w');
        check(true, "write after close does not throw");
        pw.flush();
        check(true, "flush after close does not throw (sets error)");
        check(pw.checkError(), "post-close output sets checkError");
        pw.close();
        check(true, "second close after failing close does not throw");
        check(pw.checkError(), "repeated close keeps error set");
        ProbeWriter w2 = new ProbeWriter();
        PrintWriter pw2 = new PrintWriter(w2);
        pw2.print("x");
        pw2.close();
        pw2.close();
        check(true, "double close does not throw");
        check(w2.closed, "underlying closed via close()");
        check(!pw2.checkError(), "clean close leaves checkError false");
        ProbeWriter w3 = new ProbeWriter();
        try (PrintWriter tpw = new PrintWriter(w3)) {
            tpw.print("twr");
        }
        check(w3.closed, "try-with-resources closes underlying");
        check("twr".equals(w3.text()), "try-with-resources content flushed to underlying");
        ProbeWriter w4 = new ProbeWriter();
        PrintWriter pw4 = new PrintWriter(w4);
        pw4.print("visible");
        pw4.close();
        check("visible".equals(w4.text()), "content reaches underlying before close");
    }

    // ---- 7. autoFlush: deterministic flush counts ----
    static void testAutoFlush() {
        ProbeWriter w = new ProbeWriter();
        PrintWriter pw = new PrintWriter(w, true);
        pw.write('a');
        check(w.flushCount == 0, "autoFlush: write(int) does not flush");
        pw.print("hello");
        check(w.flushCount == 0, "autoFlush: print(String) does not flush (no newline yet)");
        pw.println("world");
        check(w.flushCount == 1, "autoFlush: println flushes exactly once");
        pw.printf(Locale.ROOT, "%d", 7);
        check(w.flushCount == 2, "autoFlush: printf flushes exactly once");
        pw.format(Locale.ROOT, "%s", "z");
        check(w.flushCount == 3, "autoFlush: format flushes exactly once");
        check(("ahelloworld" + SEP + "7z").equals(w.text()), "autoFlush text order");
        ProbeWriter w2 = new ProbeWriter();
        PrintWriter pw2 = new PrintWriter(w2, true);
        pw2.print("with\nnewline");
        check(w2.flushCount == 0, "autoFlush: print(String with \\n) does not flush (char writer)");
        ProbeWriter w3 = new ProbeWriter();
        PrintWriter pw3 = new PrintWriter(w3, false);
        pw3.write('a'); pw3.print("hello"); pw3.println("world"); pw3.printf(Locale.ROOT, "%d", 7);
        check(w3.flushCount == 0, "no autoFlush: nothing flushes the underlying");
        pw3.flush();
        check(w3.flushCount == 1, "explicit flush() flushes the underlying exactly once");
        ProbeWriter w4 = new ProbeWriter();
        new PrintWriter(w4).println("d");
        check(w4.flushCount == 0, "PrintWriter(Writer) defaults to autoFlush=false");
        ProbeWriter w5 = new ProbeWriter();
        PrintWriter pw5 = new PrintWriter(w5);
        pw5.println("d");
        check(w5.flushCount == 0, "PrintWriter(Writer,boolean=false) never auto-flushes");
    }

    // ---- 8. printf/format ----
    static void testPrintfFormat() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        PrintWriter r1 = pw.printf(Locale.ROOT, "%d|%s|%f|%x|%05d", 42, "str", 1.5, 255, 42);
        check(r1 == pw, "printf(Locale,String,Object...) returns this");
        PrintWriter r2 = pw.format(Locale.ROOT, "%b|%c|%3s|", Boolean.TRUE, 'Z', "e");
        check(r2 == pw, "format(Locale,String,Object...) returns this");
        PrintWriter r3 = pw.printf("%d-", 7);
        check(r3 == pw, "printf(String,Object...) returns this");
        pw.flush();
        check("42|str|1.500000|ff|00042true|Z|  e|7-".equals(sw.toString()),
              "printf/format exact Locale.ROOT output: " + sw.toString());
        expectThrows("printf %z", java.util.UnknownFormatConversionException.class,
            new Action() { public void run() { new PrintWriter(new StringWriter()).printf(Locale.ROOT, "%z", 1); } });
        expectThrows("format %d with String", java.util.IllegalFormatConversionException.class,
            new Action() { public void run() { new PrintWriter(new StringWriter()).format(Locale.ROOT, "%d", "s"); } });
        expectThrows("printf null format", NullPointerException.class,
            new Action() { public void run() { new PrintWriter(new StringWriter()).printf(Locale.ROOT, (String) null, 1); } });
        PrintWriter pe = new PrintWriter(new StringWriter());
        try { pe.printf(Locale.ROOT, "%z", 1); } catch (java.util.IllegalFormatException expected) { }
        check(!pe.checkError(), "IllegalFormatException does not set checkError");
    }

    // ---- 9. append ----
    static void testAppend() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        check(pw.append("AB") == pw, "append(CharSequence) returns this");
        check(pw.append("ABCD", 1, 3) == pw, "append(CharSequence,int,int) returns this");
        check(pw.append('x') == pw, "append(char) returns this");
        check(pw.append((CharSequence) null) == pw, "append(null) returns this");
        pw.flush();
        check("ABBCxnull".equals(sw.toString()), "append content: AB + subsequence BC + x + null");
        StringWriter sw2 = new StringWriter();
        PrintWriter p2 = new PrintWriter(sw2);
        p2.append((CharSequence) null, 0, 2);
        p2.append((CharSequence) null, 0, 4);
        p2.flush();
        check("nunull".equals(sw2.toString()), "append(null,start,end) writes subsequence of \"null\"");
        ProbeWriter w = new ProbeWriter().failAllWrites();
        PrintWriter p3 = new PrintWriter(w);
        p3.append("gone");
        check(p3.checkError(), "append failure swallowed -> checkError");
    }

    // ---- 10. OutputStream wrapping ----
    static void testOutputWrapping() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);
        pw.print("ascii");
        check(bos.size() == 0, "PrintWriter(OutputStream) buffers: nothing sinks before flush");
        pw.flush();
        check("ascii".equals(new String(bos.toByteArray(), StandardCharsets.US_ASCII)),
              "flush() sinks buffered content to the OutputStream");
        pw.close();
    }

    // ---- 11. write + print interleaving preserves order ----
    static void testWritePrintMixing() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.write("A");
        pw.print("B");
        pw.write("CD", 1, 1);
        pw.append('E');
        pw.print(70);
        pw.write(71);
        pw.println();
        pw.flush();
        check(("ABDE70G" + SEP).equals(sw.toString()), "write/print/append interleaving order");
    }

    // ---- 12. file constructors ----
    static void testFileConstructors() throws Exception {
        File dir = tempDir();
        try {
            File f1 = new File(dir, "c1.txt");
            PrintWriter p1 = new PrintWriter(f1, "UTF-8");
            p1.print("file-utf8");
            p1.close();
            check("file-utf8".equals(new String(readFile(f1), StandardCharsets.UTF_8)),
                  "(File,String) writes exact UTF-8 content");
            File f2 = new File(dir, "c2.txt");
            PrintWriter p2 = new PrintWriter(f2.getPath(), "UTF-8");
            p2.println("path-utf8");
            p2.close();
            check(("path-utf8" + SEP).equals(new String(readFile(f2), StandardCharsets.UTF_8)),
                  "(String,String) writes content + line separator");
            PrintWriter p3 = new PrintWriter(f2);
            p3.print("ab");
            p3.close();
            check("ab".equals(new String(readFile(f2), StandardCharsets.UTF_8)),
                  "(File) truncates the existing file to zero");
            File f4 = new File(dir, "c4.txt");
            PrintWriter p4 = new PrintWriter(f4.getPath());
            p4.print("ascii");
            p4.close();
            check("ascii".equals(new String(readFile(f4), StandardCharsets.US_ASCII)),
                  "(String) writes via file path");
            File f5 = new File(dir, "c5.txt");
            PrintWriter p5 = new PrintWriter(f5, "UTF-16BE");
            p5.print("cs16");
            p5.close();
            check("00 63 00 73 00 31 00 36".equals(hex(readFile(f5))),
                  "(File,String) UTF-16BE exact bytes");
            final File missing = new File(dir, "no-such-dir" + File.separatorChar + "f.txt");
            expectThrowsMsg("(File) with missing parent", java.io.FileNotFoundException.class,
                            "(No such file or directory)",
                new Action() { public void run() throws Exception { new PrintWriter(missing); } });
            expectThrowsMsg("(String) with missing parent", java.io.FileNotFoundException.class,
                            "(No such file or directory)",
                new Action() { public void run() throws Exception { new PrintWriter(missing.getPath()); } });
            final File bad = new File(dir, "bad.txt");
            expectThrowsMsg("(File,\"Bogus\")", java.io.UnsupportedEncodingException.class, "Bogus",
                new Action() { public void run() throws Exception { new PrintWriter(bad, "Bogus"); } });
            check(!bad.exists(), "(File,Bogus) does not create the file (charset checked first)");
        } finally {
            deleteRec(dir);
        }
    }

    // ---- 13. constructor negatives ----
    static void testConstructorNegatives() {
        expectThrows("(Writer) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((Writer) null); } });
        expectThrows("(Writer,true) null writer", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((Writer) null, true); } });
        expectThrows("(OutputStream) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((OutputStream) null); } });
        expectThrows("(OutputStream,true) null stream", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((OutputStream) null, true); } });
        expectThrows("(String) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((String) null); } });
        expectThrows("(File) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((File) null); } });
        expectThrows("(File,null csn)", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintWriter((File) null, (String) null); } });
        expectThrowsMsg("(OutputStream,true,null Charset)", NullPointerException.class, "charset",
            new Action() { public void run() throws Exception {
                new PrintWriter(new ByteArrayOutputStream(), true, (java.nio.charset.Charset) null); } });
    }

    // ---- 14. JDK-27-only API surface: Charset constructors ----
    static void testJdk27Additions() throws Exception {
        // (OutputStream,boolean,Charset) encodes through the given charset
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos, false, StandardCharsets.UTF_8);
        pw.print('中');
        pw.flush();
        check(hex(bos.toByteArray()).equals("E4 B8 AD"),
              "(OutputStream,boolean,Charset) encodes through the given charset");
        pw.close();
        // (File,Charset) / (String,Charset) with UTF-16BE
        File dir = tempDir();
        try {
            File f1 = new File(dir, "cs1.txt");
            PrintWriter p1 = new PrintWriter(f1, StandardCharsets.UTF_16BE);
            p1.print("cs16");
            p1.close();
            check("00 63 00 73 00 31 00 36".equals(hex(readFile(f1))),
                  "(File,Charset) UTF-16BE exact bytes");
            File f2 = new File(dir, "cs2.txt");
            PrintWriter p2 = new PrintWriter(f2.getPath(), StandardCharsets.UTF_16BE);
            p2.print("p16");
            p2.close();
            check("00 70 00 31 00 36".equals(hex(readFile(f2))),
                  "(String,Charset) UTF-16BE exact bytes");
            expectThrows("(File,null Charset)", NullPointerException.class,
                new Action() { public void run() throws Exception {
                    new PrintWriter(new File(dir, "cs3.txt"), (java.nio.charset.Charset) null); } });
            expectThrows("(String,null Charset)", NullPointerException.class,
                new Action() { public void run() throws Exception {
                    new PrintWriter(new File(dir, "cs4.txt").getPath(), (java.nio.charset.Charset) null); } });
        } finally {
            deleteRec(dir);
        }
        // declared presence of the Charset constructors (reflection white-box)
        Class<?> c = PrintWriter.class;
        try {
            c.getConstructor(OutputStream.class, boolean.class, java.nio.charset.Charset.class);
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("PrintWriter(OutputStream,boolean,Charset) should exist in JDK-27");
        }
        try {
            c.getConstructor(File.class, java.nio.charset.Charset.class);
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("PrintWriter(File,Charset) should exist in JDK-27");
        }
        try {
            c.getConstructor(String.class, java.nio.charset.Charset.class);
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("PrintWriter(String,Charset) should exist in JDK-27");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPrintlnOverloads();
        testPrintOverloads();
        testNullAsymmetry();
        testWriteFamily();
        testSwallowAndCheckError();
        testCloseSemantics();
        testAutoFlush();
        testPrintfFormat();
        testAppend();
        testOutputWrapping();
        testWritePrintMixing();
        testFileConstructors();
        testConstructorNegatives();
        testJdk27Additions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
