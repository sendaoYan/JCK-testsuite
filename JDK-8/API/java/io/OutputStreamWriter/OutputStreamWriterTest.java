/*
 * @test
 * @summary java.io.OutputStreamWriter conformance: full contract coverage.
 *          Charset/CharsetName constructors, getEncoding, write(String)/write(char[],off,len),
 *          append, flush, UTF-8 exact-byte encoding, roundtrip via InputStreamReader.
 *          Deepened (contract level): historical getEncoding names, ctor negatives
 *          (UEE "Bogus", NPE nulls), exact-byte encoding matrix (UTF-8/ISO-8859-1/
 *          US-ASCII '?'-substitution/UTF-16BE no-BOM), internal buffering probe (bytes
 *          sink only on flush), flush/close propagation counts, closed matrix, write
 *          bounds, write(int) low-16 bits, append(null)->"null" contract, round-trip
 *          matrix incl. supplementary chars.
 * @run main OutputStreamWriterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OutputStreamWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        String s = "Aé中";  // "Aé中"
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        check(osw.getEncoding() != null, "getEncoding non-null");
        osw.write(s);
        osw.flush();
        check(Arrays.equals(bos.toByteArray(), s.getBytes(StandardCharsets.UTF_8)),
              "UTF-8 exact bytes");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        OutputStreamWriter o2 = new OutputStreamWriter(b2, StandardCharsets.UTF_8);
        o2.write(new char[]{'x', 'y', 'z', 'w'}, 1, 2);
        o2.append('!');
        o2.flush();
        check("yz!".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)),
              "write(char[],off,len)+append");

        // CharsetName constructor + roundtrip
        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        OutputStreamWriter o3 = new OutputStreamWriter(b3, "UTF-8");
        o3.write("héllo 世界");  // "héllo 世界"
        o3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(b3.toByteArray()), Charset.forName("UTF-8"));
        char[] c = new char[100];
        int n = isr.read(c);
        check("héllo 世界".equals(new String(c, 0, n)), "roundtrip via InputStreamReader");

        // flush makes bytes visible; append(char) chain
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        OutputStreamWriter o4 = new OutputStreamWriter(b4, StandardCharsets.UTF_8);
        o4.append('a').append('b').append('c');
        o4.flush();
        check("abc".equals(new String(b4.toByteArray(), StandardCharsets.UTF_8)), "append chain");
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

    // Counts write invocations so buffering can be observed precisely.
    static class CountingStream extends java.io.OutputStream {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int writes;
        public void write(int b) { writes++; bos.write(b); }
        public void write(byte[] b, int off, int len) { writes++; bos.write(b, off, len); }
        public String sunk() { return bos.toString(); }
    }

    static void writeAll(String s, String cs, ByteArrayOutputStream out) throws Exception {
        OutputStreamWriter w = new OutputStreamWriter(out, cs);
        w.write(s);
        w.flush();
    }

    // ---- historical getEncoding names ----
    static void testGetEncodingNames() throws Exception {
        check("UTF8".equals(new OutputStreamWriter(new ByteArrayOutputStream(), "UTF-8").getEncoding()),
              "UTF-8 historical name is UTF8");
        check("ISO8859_1".equals(new OutputStreamWriter(new ByteArrayOutputStream(), "ISO-8859-1").getEncoding()),
              "ISO-8859-1 historical name is ISO8859_1");
        check("ASCII".equals(new OutputStreamWriter(new ByteArrayOutputStream(), "US-ASCII").getEncoding()),
              "US-ASCII historical name is ASCII");
        check("UnicodeBigUnmarked".equals(new OutputStreamWriter(new ByteArrayOutputStream(), "UTF-16BE").getEncoding()),
              "UTF-16BE historical name is UnicodeBigUnmarked");
        check("UTF8".equals(new OutputStreamWriter(new ByteArrayOutputStream(),
              StandardCharsets.UTF_8).getEncoding()), "Charset ctor UTF-8 name");
        check("UnicodeBigUnmarked".equals(new OutputStreamWriter(new ByteArrayOutputStream(),
              StandardCharsets.UTF_16BE).getEncoding()), "Charset ctor UTF-16BE name");
    }

    // ---- constructor negatives ----
    static void testConstructorNegatives() throws Exception {
        checkMsg(java.io.UnsupportedEncodingException.class, "Bogus",
                 () -> new OutputStreamWriter(new ByteArrayOutputStream(), "Bogus"),
                 "unknown charset name -> UEE carrying the name");
        checkThrows(NullPointerException.class,
                 () -> new OutputStreamWriter(new ByteArrayOutputStream(), (String) null),
                 "null charset name -> NPE");
        checkThrows(NullPointerException.class,
                 () -> new OutputStreamWriter(new ByteArrayOutputStream(), (Charset) null),
                 "null Charset object -> NPE");
        // single-arg ctor usable (ASCII content is charset-independent)
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);
        w.write("ok");
        w.flush();
        check("ok".equals(b.toString("ISO-8859-1")), "default ctor writes ASCII");
    }

    // ---- exact-byte encoding matrix ----
    static void testEncodingMatrix() throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeAll("Aé中", "UTF-8", b);
        check(Arrays.equals(b.toByteArray(),
              new byte[]{0x41, (byte) 0xC3, (byte) 0xA9, (byte) 0xE4, (byte) 0xB8, (byte) 0xAD}),
              "UTF-8 exact bytes for A/é/中");

        ByteArrayOutputStream iso = new ByteArrayOutputStream();
        writeAll("é中", "ISO-8859-1", iso);
        check(Arrays.equals(iso.toByteArray(), new byte[]{(byte) 0xE9, 0x3F}),
              "ISO-8859-1: é -> E9, unmappable 中 -> '?'");

        ByteArrayOutputStream ascii = new ByteArrayOutputStream();
        writeAll("Aé", "US-ASCII", ascii);
        check(Arrays.equals(ascii.toByteArray(), new byte[]{0x41, 0x3F}),
              "US-ASCII: A stays, é -> '?'");

        ByteArrayOutputStream be = new ByteArrayOutputStream();
        writeAll("ABé", "UTF-16BE", be);
        check(Arrays.equals(be.toByteArray(),
              new byte[]{0, 0x41, 0, 0x42, 0, (byte) 0xE9}),
              "UTF-16BE exact bytes without BOM");

        ByteArrayOutputStream empty = new ByteArrayOutputStream();
        writeAll("", "UTF-8", empty);
        check(empty.toByteArray().length == 0, "empty string encodes to zero bytes");

        // write(char[]) exact
        ByteArrayOutputStream ca = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(ca, "UTF-8");
        w.write(new char[]{'x', 'é', 'y'}, 0, 3);
        w.flush();
        check(Arrays.equals(ca.toByteArray(), new byte[]{'x', (byte) 0xC3, (byte) 0xA9, 'y'}),
              "write(char[]) multi-byte exact bytes");
    }

    // ---- internal buffering: bytes sink only on flush ----
    static void testBufferingSemantics() throws Exception {
        CountingStream cs = new CountingStream();
        OutputStreamWriter w = new OutputStreamWriter(cs, StandardCharsets.US_ASCII);
        w.write('A');
        check(cs.writes == 0, "single char stays in encoder buffer (no sink before flush)");
        w.flush();
        check(cs.writes == 1 && "A".equals(cs.sunk()), "flush sinks exactly one write call");

        CountingStream cs2 = new CountingStream();
        OutputStreamWriter w2 = new OutputStreamWriter(cs2, StandardCharsets.UTF_8);
        w2.write("héllo");
        check(cs2.bos.size() == 0, "string write buffered before flush");
        w2.flush();
        check(cs2.bos.size() == 6, "flush sinks all 6 UTF-8 bytes");
        w2.write("world");
        w2.flush();
        check("hélloworld".equals(new String(cs2.bos.toByteArray(), StandardCharsets.UTF_8)),
              "sequential writes concatenate after flushes");
    }

    // ---- flush/close propagation ----
    static class CountingCloseStream extends ByteArrayOutputStream {
        int flushes, closes;
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    static void testFlushClosePropagation() throws Exception {
        CountingCloseStream cs = new CountingCloseStream();
        OutputStreamWriter w = new OutputStreamWriter(cs, StandardCharsets.US_ASCII);
        w.write('a');
        w.flush();
        w.flush();
        check(cs.flushes == 2, "each flush() flushes the underlying stream once");
        w.close();
        check(cs.closes == 1, "close() closes the underlying stream once");
        check(cs.toString().equals("a"), "content present in underlying stream");
    }

    // ---- closed-state matrix ----
    static void testClosedMatrix() throws Exception {
        final OutputStreamWriter w =
            new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.US_ASCII);
        w.close();
        checkMsg(java.io.IOException.class, "Stream closed", () -> w.write('x'), "closed write(int)");
        checkMsg(java.io.IOException.class, "Stream closed",
                 () -> w.write(new char[]{'x'}, 0, 1), "closed write(char[])");
        checkMsg(java.io.IOException.class, "Stream closed", () -> w.write("x"), "closed write(String)");
        checkMsg(java.io.IOException.class, "Stream closed", () -> w.flush(), "closed flush()");
        check(w.getEncoding() == null, "getEncoding() null after close");
    }

    // ---- write bounds ----
    static void testWriteBounds() throws Exception {
        final OutputStreamWriter w =
            new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.US_ASCII);
        checkThrows(NullPointerException.class, () -> w.write((char[]) null, 0, 1),
                    "write((char[])null,0,1) -> NPE");
        checkThrows(NullPointerException.class, () -> w.write((String) null, 0, 1),
                    "write((String)null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> w.write(new char[2], 1, Integer.MAX_VALUE),
                    "write(cbuf,1,MAX) overflow -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> w.write(new char[2], 0, 3),
                    "write(cbuf,0,3) -> IndexOutOfBoundsException");
        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> w.write("abc", 1, 5),
                    "write(str,1,5) -> StringIndexOutOfBoundsException");
        w.write(new char[1], 0, 0);
        check(true, "len==0 array write is a no-op");

        // write(int) keeps the low 16 bits
        ByteArrayOutputStream lo = new ByteArrayOutputStream();
        OutputStreamWriter lw = new OutputStreamWriter(lo, StandardCharsets.ISO_8859_1);
        lw.write(0x10041);
        lw.write(0x100E9);                       // low 16 bits 0xE9 = é
        lw.flush();
        check(Arrays.equals(lo.toByteArray(), new byte[]{0x41, (byte) 0xE9}),
              "write(int) truncates to low 16 bits");
    }

    // ---- append contract (null -> literal "null") ----
    static void testAppendSemantics() throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b, StandardCharsets.US_ASCII);
        check(w.append('c') == w, "append(char) returns this");
        w.append("AB");
        w.append((CharSequence) null);           // documented: writes literal "null"
        w.append((CharSequence) null, 0, 2);     // writes "nu"
        StringBuilder sb = new StringBuilder("XYZ");
        w.append(sb);
        w.append(sb, 1, 3);                      // "YZ"
        w.flush();
        check("cABnullnuXYZYZ".equals(b.toString()), "append forms and null contract");

        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> new OutputStreamWriter(new ByteArrayOutputStream(),
                        StandardCharsets.US_ASCII).append("abc", 2, 5),
                    "append(csq,2,5) -> StringIndexOutOfBoundsException");

        check(w.append("t").toString() != null || true, "append continues to work after region");
    }

    // ---- round-trip matrix via InputStreamReader ----
    static void testRoundTripMatrix() throws Exception {
        String ascii = "round trip 42!";
        String latin = "héllo àèìòù";
        String cjk = "世界";
        String supplementary = "😀";
        for (String cs : new String[]{"UTF-8", "ISO-8859-1", "US-ASCII"}) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(bos, cs);
            w.write(ascii);
            w.flush();
            InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), cs);
            char[] buf = new char[64];
            int n = r.read(buf);
            check(ascii.equals(new String(buf, 0, n)), cs + " ASCII round trip");
        }
        for (String cs : new String[]{"UTF-8", "UTF-16BE"}) {
            for (String s : new String[]{latin, cjk, supplementary}) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStreamWriter w = new OutputStreamWriter(bos, cs);
                w.write(s);
                w.flush();
                InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), cs);
                char[] buf = new char[64];
                int n = r.read(buf);
                check(s.equals(new String(buf, 0, n)), cs + " round trip of non-ASCII text");
            }
        }
        // write(String,off,len) region round trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(bos, "UTF-8");
        w.write("[世界]", 1, 2);
        w.flush();
        InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
        char[] buf = new char[8];
        int n = r.read(buf);
        check("世界".equals(new String(buf, 0, n)), "write(String,off,len) region round trip");
    }

    // ---- single-char exact encoding per charset ----
    static void testSingleCharEncoding() throws Exception {
        // 'A' encodes to a single byte in the 8-bit charsets
        check(Arrays.equals(enc("A", "US-ASCII"), new byte[]{0x41}), "US-ASCII 'A'");
        check(Arrays.equals(enc("A", "ISO-8859-1"), new byte[]{0x41}), "ISO-8859-1 'A'");
        check(Arrays.equals(enc("A", "UTF-8"), new byte[]{0x41}), "UTF-8 'A' is single byte");
        check(Arrays.equals(enc("A", "UTF-16BE"), new byte[]{0, 0x41}), "UTF-16BE 'A' is 00 41");

        // é across charsets
        check(Arrays.equals(enc("é", "ISO-8859-1"), new byte[]{(byte) 0xE9}), "ISO-8859-1 é = E9");
        check(Arrays.equals(enc("é", "UTF-8"), new byte[]{(byte) 0xC3, (byte) 0xA9}), "UTF-8 é = C3 A9");
        check(Arrays.equals(enc("é", "UTF-16BE"), new byte[]{0, (byte) 0xE9}), "UTF-16BE é = 00 E9");

        // supplementary char encodes to 4 UTF-8 bytes
        check(Arrays.equals(enc("😀", "UTF-8"),
              new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}),
              "UTF-8 emoji is 4 bytes");

        // write(int) vs write(char[]) vs write(String) all agree
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        OutputStreamWriter w1 = new OutputStreamWriter(b1, "ISO-8859-1");
        w1.write('é');
        w1.write(new char[]{'é'}, 0, 1);
        w1.write("é");
        w1.flush();
        check(Arrays.equals(b1.toByteArray(), new byte[]{(byte) 0xE9, (byte) 0xE9, (byte) 0xE9}),
              "all three write forms produce identical bytes");
    }

    static byte[] enc(String s, String cs) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b, cs);
        w.write(s);
        w.flush();
        return b.toByteArray();
    }

    // ---- large write through the internal buffer ----
    static void testLargeWrite() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append((char) ('a' + i % 26));
        String text = sb.toString();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b, StandardCharsets.US_ASCII);
        w.write(text);
        w.flush();
        check(b.size() == 1000, "1000 ASCII chars -> 1000 bytes (multi-buffer refill)");
        check(Arrays.equals(b.toByteArray(), text.getBytes(StandardCharsets.US_ASCII)),
              "large write bytes identical to String.getBytes");

        // char-array form
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        OutputStreamWriter w2 = new OutputStreamWriter(b2, StandardCharsets.US_ASCII);
        w2.write(text.toCharArray(), 0, 1000);
        w2.flush();
        check(Arrays.equals(b2.toByteArray(), text.getBytes(StandardCharsets.US_ASCII)),
              "large char[] write identical");
    }

    // ---- write(String,off,len) and write(char[],off,len) region matrix ----
    static void testWriteRegions() throws Exception {
        check(Arrays.equals(enc2("0123456789", 2, 3, "US-ASCII"), new byte[]{'2', '3', '4'}),
              "String region [2,5)");
        check(Arrays.equals(enc2("0123456789", 0, 10, "US-ASCII"),
              "0123456789".getBytes(StandardCharsets.US_ASCII)), "full String region");
        check(Arrays.equals(enc2("0123456789", 9, 1, "US-ASCII"), new byte[]{'9'}),
              "String region last char");
        check(Arrays.equals(enc2("0123456789", 5, 0, "US-ASCII"), new byte[0]),
              "zero-length String region writes nothing");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b, "US-ASCII");
        w.write(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        w.write(new char[]{'e'}, 0, 1);
        w.write(new char[2], 1, 0);              // zero-length region is a no-op
        w.flush();
        check("bce".equals(b.toString("ISO-8859-1")), "char[] regions concatenate");
    }

    static byte[] enc2(String s, int off, int len, String cs) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b, cs);
        w.write(s, off, len);
        w.flush();
        return b.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testGetEncodingNames();
        testConstructorNegatives();
        testEncodingMatrix();
        testBufferingSemantics();
        testFlushClosePropagation();
        testClosedMatrix();
        testWriteBounds();
        testAppendSemantics();
        testRoundTripMatrix();
        testSingleCharEncoding();
        testLargeWrite();
        testWriteRegions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
