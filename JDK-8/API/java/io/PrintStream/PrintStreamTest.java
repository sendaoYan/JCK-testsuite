/*
 * @test
 * @summary java.io.PrintStream conformance: full contract coverage.
 *          print/println/printf/format with a fixed Locale and explicit UTF-8
 *          encoding, checkError, append, println platform line separator.
 *          Deepened: swallowed-IOException/checkError matrix (sticky error,
 *          close-error, post-close), all println/print overloads with exact
 *          bytes, write family bounds, autoFlush probe counts, constructor
 *          matrix incl. charset-checked-before-file-open, null asymmetry.
 * @run main PrintStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- deepening helpers ----
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

    static String text(ByteArrayOutputStream bos) {
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X ", x));
        return sb.toString().trim();
    }

    /** Recording/failing probe stream used for all swallow-matrix tests. */
    static class ProbeStream extends OutputStream {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int flushCount;
        boolean failWrite, failFlush, failClose;
        boolean closed;
        ProbeStream failAllWrites() { failWrite = true; return this; }
        public void write(int b) throws IOException {
            if (failWrite) throw new IOException("probe write failure");
            buf.write(b);
        }
        public void write(byte[] b, int off, int len) throws IOException {
            if (failWrite) throw new IOException("probe write failure");
            buf.write(b, off, len);
        }
        public void flush() throws IOException {
            if (failFlush) throw new IOException("probe flush failure");
            flushCount++;
        }
        public void close() throws IOException {
            closed = true;
            if (failClose) throw new IOException("probe close failure");
        }
        String text() { return new String(buf.toByteArray(), StandardCharsets.UTF_8); }
    }

    static PrintStream utf8(OutputStream os, boolean autoFlush) {
        try { return new PrintStream(os, autoFlush, "UTF-8"); }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    static File tempDir() throws IOException {
        File d = new File(System.getProperty("java.io.tmpdir"), "ioG-ps-" + System.nanoTime());
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = utf8(bos, false);
        ps.println(true);
        ps.println(false);
        ps.println('x');
        ps.println(42);
        ps.println(-7);
        ps.println(Integer.MIN_VALUE);
        ps.println(12345678901L);
        ps.println(Long.MIN_VALUE);
        ps.println(1.5f);
        ps.println(2.0d);
        ps.println(0.0d);
        ps.println(-0.0d);
        ps.println(Float.NaN);
        ps.println(Float.POSITIVE_INFINITY);
        ps.println(Float.NEGATIVE_INFINITY);
        ps.println(Double.NaN);
        ps.println(Double.POSITIVE_INFINITY);
        ps.println(Double.NEGATIVE_INFINITY);
        ps.println(new char[]{'a', 'b'});
        ps.println((String) null);
        ps.println((Object) null);
        ps.println();
        ps.println(new Object() { public String toString() { return "OBJ"; } });
        ps.flush();
        String exp = "true" + SEP + "false" + SEP + "x" + SEP + "42" + SEP + "-7" + SEP
            + Integer.MIN_VALUE + SEP + "12345678901" + SEP + Long.MIN_VALUE + SEP
            + "1.5" + SEP + "2.0" + SEP + "0.0" + SEP + "-0.0" + SEP
            + "NaN" + SEP + "Infinity" + SEP + "-Infinity" + SEP
            + "NaN" + SEP + "Infinity" + SEP + "-Infinity" + SEP
            + "ab" + SEP + "null" + SEP + "null" + SEP + SEP + "OBJ" + SEP;
        check(exp.equals(text(bos)), "println overload matrix exact bytes: " + text(bos));
        check(!ps.checkError(), "println matrix leaves no error");
        // one probe stream, one println(String): exact bytes via probe
        ProbeStream os = new ProbeStream();
        utf8(os, false).println("line");
        check(("line" + SEP).equals(os.text()), "println(String) exact via probe");
    }

    // ---- 2. print: every overload, exact conversions ----
    static void testPrintOverloads() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = utf8(bos, false);
        ps.print(true);
        ps.print(false);
        ps.print('x');
        ps.print('中');
        ps.print(0);
        ps.print(Integer.MIN_VALUE);
        ps.print(Long.MAX_VALUE);
        ps.print(1.5f);
        ps.print(2.0d);
        ps.print(Float.NaN);
        ps.print(Float.POSITIVE_INFINITY);
        ps.print(Float.NEGATIVE_INFINITY);
        ps.print(Double.NaN);
        ps.print(Double.POSITIVE_INFINITY);
        ps.print(Double.NEGATIVE_INFINITY);
        ps.print(new char[]{'a', 'b', 'c'});
        ps.print(new Object() { public String toString() { return "CUSTOM"; } });
        ps.flush();
        String exp = "truefalsex中0" + Integer.MIN_VALUE + Long.MAX_VALUE
            + "1.52.0NaNInfinity-InfinityNaNInfinity-InfinityabcCUSTOM";
        check(exp.equals(text(bos)), "print overload matrix exact bytes: " + text(bos));
        check(!ps.checkError(), "print matrix leaves no error");
        // println(float)/(double) go through String.valueOf: locale independent
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        PrintStream p2 = utf8(b2, false);
        p2.println(1234.56);
        p2.println(0.1f);
        p2.flush();
        check(("1234.56" + SEP + "0.1" + SEP).equals(text(b2)),
              "println(double) uses String.valueOf, not locale formatting");
    }

    // ---- 3. null asymmetry: print(String/Object) null -> "null", others NPE ----
    static void testNullAsymmetry() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = utf8(bos, false);
        ps.print((String) null);
        ps.print((Object) null);
        ps.flush();
        check("nullnull".equals(text(bos)), "print((String)null) and print((Object)null) write \"null\"");
        expectThrows("print((char[]) null)", NullPointerException.class,
                     new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).print((char[]) null); } });
        expectThrows("println((char[]) null)", NullPointerException.class,
                     new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).println((char[]) null); } });
        expectThrows("write((byte[]) null, 0, 2)", NullPointerException.class,
                     new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).write(null, 0, 2); } });
        check(!ps.checkError(), "null print does not set error");
    }

    // ---- 4. write family: raw bytes, low-8-bit truncation, bounds ----
    static void testWriteFamily() throws IOException {
        ProbeStream os = new ProbeStream();
        PrintStream ps = utf8(os, false);
        ps.write(0x141);  // only low 8 bits survive: 0x41 'A'
        ps.write(0xFF);
        ps.write(-1);     // -> 0xFF
        ps.flush();
        check("41 FF FF".equals(hex(os.buf.toByteArray())), "write(int) keeps only low 8 bits");
        check(!ps.checkError(), "write(int) clean");
        // write(int) bypasses the charset encoder (raw byte path)
        ProbeStream osRaw = new ProbeStream();
        PrintStream psRaw = utf8(osRaw, false);
        psRaw.write(0xE4); psRaw.write(0xB8); psRaw.write(0xAD); // UTF-8 bytes of 中
        psRaw.flush();
        check(osRaw.buf.size() == 3, "write(int) writes raw bytes without re-encoding");
        // write(byte[],off,len) content and offset handling
        ProbeStream os2 = new ProbeStream();
        PrintStream ps2 = utf8(os2, false);
        ps2.write(new byte[]{'a', 'b', 'c', 'd'}, 1, 2);
        ps2.flush();
        check(os2.buf.size() == 2 && os2.buf.toByteArray()[0] == 'b' && os2.buf.toByteArray()[1] == 'c',
              "write(byte[],off,len) honors off/len");
        // bounds: IndexOutOfBoundsException is NOT swallowed (RuntimeException)
        PrintStream ps3 = utf8(new ByteArrayOutputStream(), false);
        byte[] three = new byte[3];
        int[][] bad = { {-1, 2}, {0, 4}, {4, 0}, {2, -1}, {4, -1} };
        for (int[] r : bad) {
            final int off = r[0], len = r[1];
            expectThrows("write(byte[]," + off + "," + len + ")", IndexOutOfBoundsException.class,
                new Action() { public void run() { ps3.write(three, off, len); } });
        }
        check(!ps3.checkError(), "IndexOutOfBoundsException does not set checkError");
        // write(byte[]) is public API (inherited from FilterOutputStream here);
        // it delegates to write(byte[],int,int), so IO failures are swallowed
        ProbeStream os4 = new ProbeStream();
        PrintStream ps4 = utf8(os4, false);
        try { ps4.write(new byte[]{'x', 'y'}); }
        catch (IOException e) { throw new AssertionError("write(byte[]) must not throw here", e); }
        check(os4.buf.size() == 2, "write(byte[]) writes all bytes");
        expectThrows("write((byte[]) null)", NullPointerException.class,
                     new Action() { public void run() throws Exception { utf8(new ByteArrayOutputStream(), false).write((byte[]) null); } });
        // failing write(byte[]) still does not propagate IOException
        ProbeStream os5 = new ProbeStream().failAllWrites();
        PrintStream ps5 = utf8(os5, false);
        try { ps5.write(new byte[]{'x'}); }
        catch (IOException e) { throw new AssertionError("write(byte[]) swallows IOException", e); }
        check(ps5.checkError(), "write(byte[]) failure sets checkError");
        // zero-length write at valid offset is a no-op
        ProbeStream os6 = new ProbeStream();
        utf8(os6, false).write(new byte[]{'a'}, 1, 0);
        check(os6.buf.size() == 0, "zero-length write writes nothing");
    }

    // ---- 5. swallowed IOException + checkError semantics (the core contract) ----
    static void testSwallowAndCheckError() {
        // (a) print: underlying write throws -> no propagation, error flag set
        ProbeStream os = new ProbeStream().failAllWrites();
        PrintStream ps = utf8(os, false);
        ps.print("gone");
        check(true, "print survived underlying IOException");
        check(ps.checkError(), "checkError true after swallowed print failure");
        // (b) error is sticky: underlying recovers, flag stays
        os.failWrite = false;
        ps.print("kept");
        check(ps.checkError(), "checkError sticky after underlying recovery");
        check("kept".equals(os.text()), "recovered print reaches the underlying stream");
        // (c) flush does not clear the error
        ps.flush();
        check(ps.checkError(), "flush() does not clear the error flag");
        // (d) flush failure is swallowed as well
        ProbeStream os2 = new ProbeStream();
        PrintStream ps2 = utf8(os2, false);
        os2.failFlush = true;
        ps2.print("x");
        ps2.flush();
        check(true, "flush survived underlying flush IOException");
        check(ps2.checkError(), "checkError true after flush failure");
        // (e) println, write(int), write(byte[],int,int), printf failures swallowed
        ProbeStream os3 = new ProbeStream();
        PrintStream ps3 = utf8(os3, true);
        os3.failWrite = true;
        ps3.println("line");
        check(ps3.checkError(), "println failure swallowed -> error");
        ps3.write('c');
        check(ps3.checkError(), "write(int) failure swallowed -> error");
        ps3.write(new byte[]{'b'}, 0, 1);
        check(ps3.checkError(), "write(byte[],int,int) failure swallowed -> error");
        ps3.printf(Locale.ROOT, "%d", 1);
        check(ps3.checkError(), "printf failure swallowed -> error");
        check(os3.buf.size() == 0, "failing underlying received no bytes");
        // (f) checkError() itself flushes a healthy underlying stream
        ProbeStream os4 = new ProbeStream();
        PrintStream ps4 = utf8(os4, false);
        ps4.print("x");
        int before = os4.flushCount;
        check(!ps4.checkError(), "healthy stream: no error");
        check(os4.flushCount == before + 1, "checkError() flushes the underlying stream");
        // (g) healthy path: no error anywhere
        ProbeStream os5 = new ProbeStream();
        PrintStream ps5 = utf8(os5, false);
        ps5.print("a"); ps5.println("b"); ps5.write('c'); ps5.append('d'); ps5.flush();
        check(!ps5.checkError(), "clean stream checkError false");
    }

    // ---- 6. close semantics: close-error path, post-close swallow, idempotence ----
    static void testCloseSemantics() throws IOException {
        // (a) underlying throws on close -> swallowed, error set, underlying marked closed
        ProbeStream os = new ProbeStream();
        os.failClose = true;
        PrintStream ps = utf8(os, false);
        ps.print("pre");
        ps.close();
        check(true, "close survived underlying close IOException");
        check(os.closed, "underlying close() was invoked");
        check(ps.checkError(), "checkError true after close failure");
        // (b) after close: every output op is swallowed and sets error
        ps.print("after");
        check(true, "print after close does not throw");
        ps.println("l");
        check(true, "println after close does not throw");
        ps.write('w');
        check(true, "write after close does not throw");
        ps.flush();
        check(true, "flush after close does not throw (sets error)");
        check(ps.checkError(), "post-close output sets checkError");
        // (c) close is idempotent on a healthy stream, no error
        ProbeStream os2 = new ProbeStream();
        PrintStream ps2 = utf8(os2, false);
        ps2.print("x");
        ps2.close();
        ps2.close();
        check(true, "double close does not throw");
        check(os2.closed, "underlying closed exactly via close()");
        check(!ps2.checkError(), "clean close leaves checkError false");
        // (d) try-with-resources closes the underlying stream
        ProbeStream os3 = new ProbeStream();
        try (PrintStream tps = utf8(os3, false)) {
            tps.print("twr");
        }
        check(os3.closed, "try-with-resources closes underlying");
        check("twr".equals(os3.text()), "try-with-resources content flushed to underlying");
        // (e) data written before close is visible in the underlying stream
        ProbeStream os4 = new ProbeStream();
        PrintStream ps4 = utf8(os4, false);
        ps4.print("visible");
        ps4.close();
        check("visible".equals(os4.text()), "content reaches underlying before close");
    }

    // ---- 7. autoFlush semantics with a counting probe ----
    static void testAutoFlush() {
        // autoFlush = true
        ProbeStream os = new ProbeStream();
        PrintStream ps = utf8(os, true);
        ps.write('a');
        check(os.flushCount == 0, "autoFlush: write(int) of non-newline does not flush");
        ps.write('\n');
        check(os.flushCount == 1, "autoFlush: write(int) newline flushes underlying");
        int before = os.flushCount;
        ps.println("line");
        check(os.flushCount > before, "autoFlush: println flushes underlying");
        before = os.flushCount;
        ps.write(new byte[]{'x'}, 0, 1);
        check(os.flushCount == before + 1, "autoFlush: write(byte[],int,int) flushes underlying");
        // autoFlush = false
        ProbeStream os2 = new ProbeStream();
        PrintStream ps2 = utf8(os2, false);
        ps2.write('\n');
        ps2.print("hello");
        ps2.println("world");
        ps2.write(new byte[]{'x'}, 0, 1);
        check(os2.flushCount == 0, "no autoFlush: nothing flushes the underlying");
        check(os2.text().contains("hello"), "no autoFlush: content still sinks through the encoder");
        ps2.flush();
        check(os2.flushCount == 1, "explicit flush() flushes the underlying exactly once");
        // single-arg constructor defaults to autoFlush = false
        ProbeStream os3 = new ProbeStream();
        PrintStream ps3 = new PrintStream(os3);
        ps3.println("dflt");
        check(os3.flushCount == 0, "PrintStream(OutputStream) defaults to autoFlush=false");
    }

    // ---- 8. printf/format: exact output under Locale.ROOT, return this,
    //         format exceptions are NOT swallowed (only IOExceptions are) ----
    static void testPrintfFormat() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = utf8(bos, false);
        PrintStream r1 = ps.printf(Locale.ROOT, "%d|%s|%f|%x|%05d", 42, "str", 1.5, 255, 42);
        check(r1 == ps, "printf(Locale,String,Object...) returns this");
        PrintStream r2 = ps.format(Locale.ROOT, "%b|%c|%3s|", Boolean.TRUE, 'Z', "e");
        check(r2 == ps, "format(Locale,String,Object...) returns this");
        PrintStream r3 = ps.printf("%d-", 7);
        check(r3 == ps, "printf(String,Object...) returns this");
        ps.flush();
        check("42|str|1.500000|ff|00042true|Z|  e|7-".equals(text(bos)),
              "printf/format exact Locale.ROOT output: " + text(bos));
        // format-string errors propagate (IllegalFormatException, not swallowed)
        expectThrows("printf %z", java.util.UnknownFormatConversionException.class,
            new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).printf(Locale.ROOT, "%z", 1); } });
        expectThrows("printf %d with String", java.util.IllegalFormatConversionException.class,
            new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).printf(Locale.ROOT, "%d", "s"); } });
        expectThrows("printf null format", NullPointerException.class,
            new Action() { public void run() { utf8(new ByteArrayOutputStream(), false).printf(Locale.ROOT, (String) null, 1); } });
        // a format exception does not set the error flag (no IOException happened)
        PrintStream pe = utf8(new ByteArrayOutputStream(), false);
        try { pe.printf(Locale.ROOT, "%z", 1); } catch (java.util.IllegalFormatException expected) { }
        check(!pe.checkError(), "IllegalFormatException does not set checkError");
    }

    // ---- 9. append: all three overloads return this and write equivalent text ----
    static void testAppend() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = utf8(bos, false);
        check(ps.append("AB") == ps, "append(CharSequence) returns this");
        check(ps.append("ABCD", 1, 3) == ps, "append(CharSequence,int,int) returns this");
        check(ps.append('x') == ps, "append(char) returns this");
        check(ps.append((CharSequence) null) == ps, "append(null) returns this");
        ps.flush();
        check("ABBCxnull".equals(text(bos)), "append content: AB + subsequence BC + x + null");
        // append(null, start, end) treats null as "null" and takes the subsequence
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        PrintStream p2 = utf8(b2, false);
        p2.append((CharSequence) null, 0, 2);
        p2.append((CharSequence) null, 0, 4);
        p2.flush();
        check("nunull".equals(text(b2)), "append(null,start,end) writes subsequence of \"null\"");
        // append(char) goes through the charset encoder
        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        PrintStream p3 = utf8(b3, false);
        p3.append('中');
        p3.flush();
        check("中".equals(text(b3)), "append(char) encoded via stream charset");
        // append on a failing underlying: swallowed
        ProbeStream os = new ProbeStream().failAllWrites();
        PrintStream p4 = utf8(os, false);
        p4.append("gone");
        check(p4.checkError(), "append failure swallowed -> checkError");
    }

    // ---- 10. file-name/file constructors: creation, truncation, charset ----
    static void testFileConstructors() throws Exception {
        File dir = tempDir();
        try {
            // (File,String) writes with the named charset
            File f1 = new File(dir, "c1.txt");
            PrintStream p1 = new PrintStream(f1, "UTF-8");
            p1.print("file-utf8");
            p1.close();
            check("file-utf8".equals(new String(readFile(f1), StandardCharsets.UTF_8)),
                  "(File,String) writes exact UTF-8 content");
            // (String,String)
            File f2 = new File(dir, "c2.txt");
            PrintStream p2 = new PrintStream(f2.getPath(), "UTF-8");
            p2.println("path-utf8");
            p2.close();
            check(("path-utf8" + SEP).equals(new String(readFile(f2), StandardCharsets.UTF_8)),
                  "(String,String) writes content + line separator");
            // (File) truncates an existing file
            PrintStream p3 = new PrintStream(f2);
            p3.print("ab");
            p3.close();
            check("ab".equals(new String(readFile(f2), StandardCharsets.UTF_8)),
                  "(File) truncates the existing file to zero");
            // (String) default-charset path, ASCII only for determinism
            File f4 = new File(dir, "c4.txt");
            PrintStream p4 = new PrintStream(f4.getPath());
            p4.print("ascii");
            p4.close();
            check("ascii".equals(new String(readFile(f4), StandardCharsets.US_ASCII)),
                  "(String) writes via file path");
            // (File,String) with UTF-16BE: exact raw bytes
            File f5 = new File(dir, "c5.txt");
            PrintStream p5 = new PrintStream(f5, "UTF-16BE");
            p5.print("cs16");
            p5.close();
            check("00 63 00 73 00 31 00 36".equals(hex(readFile(f5))),
                  "(File,String) UTF-16BE exact bytes");
            // constructor failure: missing parent dir -> FileNotFoundException
            final File missing = new File(dir, "no-such-dir" + File.separatorChar + "f.txt");
            expectThrowsMsg("(File) with missing parent", java.io.FileNotFoundException.class,
                            "(No such file or directory)",
                new Action() { public void run() throws Exception { new PrintStream(missing); } });
            expectThrowsMsg("(String) with missing parent", java.io.FileNotFoundException.class,
                            "(No such file or directory)",
                new Action() { public void run() throws Exception { new PrintStream(missing.getPath()); } });
            // charset is validated BEFORE the file is opened for (File,String)
            final File bad = new File(dir, "bad.txt");
            expectThrowsMsg("(File,\"Bogus\")", UnsupportedEncodingException.class, "Bogus",
                new Action() { public void run() throws Exception { new PrintStream(bad, "Bogus"); } });
            check(!bad.exists(), "(File,Bogus) does not create the file (charset checked first)");
        } finally {
            deleteRec(dir);
        }
    }

    // ---- 11. constructor negative matrix ----
    static void testConstructorNegatives() {
        expectThrowsMsg("(OutputStream) null", NullPointerException.class, "Null output stream",
            new Action() { public void run() throws Exception { new PrintStream((OutputStream) null); } });
        expectThrowsMsg("(OutputStream,true,null csn)", NullPointerException.class, "charsetName",
            new Action() { public void run() throws Exception { new PrintStream(new ByteArrayOutputStream(), true, (String) null); } });
        expectThrowsMsg("(OutputStream,true,\"Bogus\")", UnsupportedEncodingException.class, "Bogus",
            new Action() { public void run() throws Exception { new PrintStream(new ByteArrayOutputStream(), true, "Bogus"); } });
        expectThrows("(String) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintStream((String) null); } });
        expectThrows("(File) null", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintStream((File) null); } });
        expectThrows("(File,null csn)", NullPointerException.class,
            new Action() { public void run() throws Exception { new PrintStream((File) null, (String) null); } });
        // JDK-8 baseline: the Charset-based API surface does not exist yet
        expectThrows("getDeclaredMethod charset()", NoSuchMethodException.class,
            new Action() { public void run() throws Exception { PrintStream.class.getDeclaredMethod("charset"); } });
        expectThrows("getDeclaredMethod writeBytes(byte[])", NoSuchMethodException.class,
            new Action() { public void run() throws Exception { PrintStream.class.getDeclaredMethod("writeBytes", byte[].class); } });
        expectThrows("getConstructor (OutputStream,boolean,Charset)", NoSuchMethodException.class,
            new Action() { public void run() throws Exception {
                PrintStream.class.getConstructor(OutputStream.class, boolean.class, java.nio.charset.Charset.class); } });
    }

    static void testBehavior() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, false, "UTF-8");
        check(ps.printf(Locale.US, "%d %.2f", 42, 3.14159) == ps, "printf returns this");
        ps.println();
        ps.print(true);
        ps.append(" ok");
        ps.flush();
        String out = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        check(out.startsWith("42 3.14"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("ok"), "append");
        check(!ps.checkError(), "checkError false");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        PrintStream p2 = new PrintStream(b2, false, "UTF-8");
        p2.format(Locale.US, "%05d", 42);
        p2.flush();
        check("00042".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)), "format");

        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        PrintStream p3 = new PrintStream(b3, false, "UTF-8");
        p3.println("héllo 世界");
        p3.flush();
        check(("héllo 世界" + System.lineSeparator()).equals(new String(b3.toByteArray(), StandardCharsets.UTF_8)),
              "UTF-8 println roundtrip");

        // println() alone emits just the line separator
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        PrintStream p4 = new PrintStream(b4, false, "UTF-8");
        p4.println();
        p4.flush();
        check(System.lineSeparator().equals(new String(b4.toByteArray(), StandardCharsets.UTF_8)),
              "bare println emits the line separator");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testPrintlnOverloads();
        testPrintOverloads();
        testNullAsymmetry();
        testWriteFamily();
        testSwallowAndCheckError();
        testCloseSemantics();
        testAutoFlush();
        testPrintfFormat();
        testAppend();
        testFileConstructors();
        testConstructorNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
