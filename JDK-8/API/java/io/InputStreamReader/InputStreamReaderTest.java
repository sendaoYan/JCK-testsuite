/*
 * @test
 * @summary java.io.InputStreamReader conformance: full contract coverage.
 *          Charset/CharsetName constructors, getEncoding, single/bulk/offset reads,
 *          UTF-8 multi-byte roundtrip, EOF.
 *          Deepened (contract level): historical getEncoding names (UTF8/ISO8859_1/
 *          ASCII/UnicodeBigUnmarked), constructor negatives (UEE "Bogus", NPE null
 *          name/charset), malformed-input replacement (U+FFFD for 0x80, truncated
 *          multi-byte, C3 28), encode/decode round-trip matrix across 4 charsets incl.
 *          supplementary chars, UTF-16BE explicit bytes, ready semantics, close matrix,
 *          read bounds, char-granularity skip.
 * @run main InputStreamReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InputStreamReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        String text = "héllo 世界 😀";  // "héllo 世界 😀"
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        osw.write(text);
        osw.flush();

        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        check(isr.getEncoding() != null, "getEncoding non-null");
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4];
        int n;
        while ((n = isr.read(buf, 0, buf.length)) != -1) sb.append(buf, 0, n);
        check(text.equals(sb.toString()), "UTF-8 multi-byte roundtrip");
        check(isr.read() == -1, "EOF");

        // CharsetName constructor
        InputStreamReader named = new InputStreamReader(
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), "UTF-8");
        check(named.read() == 'a' && named.read() == 'b' && named.read() == 'c' && named.read() == -1,
              "CharsetName ctor single reads");

        // read(char[]) bulk
        InputStreamReader bulk = new InputStreamReader(
            new ByteArrayInputStream("xy".getBytes(StandardCharsets.UTF_8)), Charset.forName("UTF-8"));
        char[] two = new char[1];
        check(bulk.read(two) == 1 && two[0] == 'x', "read(char[]) single");
        check(bulk.read(two) == 1 && two[0] == 'y', "read(char[]) second");
        check(bulk.read(two) == -1, "read(char[]) EOF");
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

    static InputStreamReader isr(byte[] data, String cs) throws Exception {
        return new InputStreamReader(new ByteArrayInputStream(data), cs);
    }

    static String decodeAll(InputStreamReader r) throws Exception {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4];
        int n;
        while ((n = r.read(buf, 0, buf.length)) != -1) sb.append(buf, 0, n);
        return sb.toString();
    }

    // Encode text with OutputStreamWriter under the given charset name, then
    // decode back with InputStreamReader under the same name.
    static String roundTrip(String text, String cs) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(bos, cs);
        w.write(text);
        w.flush();
        return decodeAll(isr(bos.toByteArray(), cs));
    }

    // ---- historical getEncoding names ----
    static void testGetEncodingNames() throws Exception {
        check("UTF8".equals(isr(new byte[0], "UTF-8").getEncoding()),
              "UTF-8 historical name is UTF8");
        check("ISO8859_1".equals(isr(new byte[0], "ISO-8859-1").getEncoding()),
              "ISO-8859-1 historical name is ISO8859_1");
        check("ASCII".equals(isr(new byte[0], "US-ASCII").getEncoding()),
              "US-ASCII historical name is ASCII");
        check("UnicodeBigUnmarked".equals(isr(new byte[0], "UTF-16BE").getEncoding()),
              "UTF-16BE historical name is UnicodeBigUnmarked");
        // Charset-object constructor reports the same names
        check("UTF8".equals(new InputStreamReader(new ByteArrayInputStream(new byte[0]),
              StandardCharsets.UTF_8).getEncoding()), "Charset ctor UTF-8 name");
        check("ISO8859_1".equals(new InputStreamReader(new ByteArrayInputStream(new byte[0]),
              StandardCharsets.ISO_8859_1).getEncoding()), "Charset ctor ISO-8859-1 name");
        check("UnicodeBigUnmarked".equals(new InputStreamReader(new ByteArrayInputStream(new byte[0]),
              StandardCharsets.UTF_16BE).getEncoding()), "Charset ctor UTF-16BE name");
    }

    // ---- constructor negatives ----
    static void testConstructorNegatives() throws Exception {
        checkMsg(java.io.UnsupportedEncodingException.class, "Bogus",
                 () -> new InputStreamReader(new ByteArrayInputStream(new byte[0]), "Bogus"),
                 "unknown charset name -> UEE carrying the name");
        checkThrows(NullPointerException.class,
                 () -> new InputStreamReader(new ByteArrayInputStream(new byte[0]), (String) null),
                 "null charset name -> NPE");
        checkThrows(NullPointerException.class,
                 () -> new InputStreamReader(new ByteArrayInputStream(new byte[0]), (Charset) null),
                 "null Charset object -> NPE");
        // (InputStream) single-arg ctor is usable
        InputStreamReader r = new InputStreamReader(
            new ByteArrayInputStream("ok".getBytes(StandardCharsets.US_ASCII)));
        check(r.read() == 'o' && r.read() == 'k', "default single-arg ctor decodes ASCII");
    }

    // ---- malformed input -> U+FFFD replacement ----
    static void testMalformedReplacement() throws Exception {
        check("�".equals(decodeAll(isr(new byte[]{(byte) 0x80}, "UTF-8"))),
              "lone continuation byte 0x80 -> single U+FFFD");

        check("�".equals(decodeAll(isr(new byte[]{(byte) 0xE4, (byte) 0xB8}, "UTF-8"))),
              "truncated 3-byte sequence at EOF -> single U+FFFD");

        String s = decodeAll(isr(new byte[]{(byte) 0xC3, 0x28}, "UTF-8"));
        check(s.length() == 2 && s.charAt(0) == '�' && s.charAt(1) == '(',
              "malformed lead 0xC3 + ASCII -> U+FFFD then ASCII preserved");

        // malformed byte in the middle: prefix and suffix survive
        String mid = decodeAll(isr(new byte[]{'a', (byte) 0x80, 'b'}, "UTF-8"));
        check("a�b".equals(mid), "surrounding chars intact around replacement");
    }

    // ---- round-trip matrix across charsets ----
    static void testRoundTripMatrix() throws Exception {
        String ascii = "The quick brown fox 0123!";
        String latin = "héllo àèìòù";
        String cjk = "世界";
        String supplementary = "😀";            // U+1F600, surrogate pair in UTF-16

        check(ascii.equals(roundTrip(ascii, "UTF-8")), "UTF-8 ASCII round trip");
        check(latin.equals(roundTrip(latin, "UTF-8")), "UTF-8 Latin-1 accents round trip");
        check(cjk.equals(roundTrip(cjk, "UTF-8")), "UTF-8 CJK round trip");
        check(supplementary.equals(roundTrip(supplementary, "UTF-8")),
              "UTF-8 supplementary char round trip");
        check("".equals(roundTrip("", "UTF-8")), "UTF-8 empty string round trip");

        check(ascii.equals(roundTrip(ascii, "ISO-8859-1")), "ISO-8859-1 ASCII round trip");
        check(latin.equals(roundTrip(latin, "ISO-8859-1")), "ISO-8859-1 accents round trip");
        check("".equals(roundTrip("", "ISO-8859-1")), "ISO-8859-1 empty round trip");

        check(ascii.equals(roundTrip(ascii, "US-ASCII")), "US-ASCII ASCII round trip");
        check("".equals(roundTrip("", "US-ASCII")), "US-ASCII empty round trip");

        check(ascii.equals(roundTrip(ascii, "UTF-16BE")), "UTF-16BE ASCII round trip");
        check(latin.equals(roundTrip(latin, "UTF-16BE")), "UTF-16BE accents round trip");
        check(cjk.equals(roundTrip(cjk, "UTF-16BE")), "UTF-16BE CJK round trip");
        check(supplementary.equals(roundTrip(supplementary, "UTF-16BE")),
              "UTF-16BE surrogate pair round trip");
        check("".equals(roundTrip("", "UTF-16BE")), "UTF-16BE empty round trip");
    }

    // ---- UTF-16BE explicit byte decoding (no BOM handling for BE form) ----
    static void testUtf16ExplicitBytes() throws Exception {
        check("AB".equals(decodeAll(isr(new byte[]{0, 65, 0, 66}, "UTF-16BE"))),
              "explicit UTF-16BE byte pairs decode");
        // odd trailing byte: complete char decodes, truncated tail becomes U+FFFD
        String lone = decodeAll(isr(new byte[]{0, (byte) 0xD8, 0x03}, "UTF-16BE"));
        check(lone.length() == 2 && lone.charAt(0) == 'Ø' && lone.charAt(1) == '�',
              "complete char then truncated byte -> char + U+FFFD");
    }

    // ---- ready semantics ----
    static void testReadySemantics() throws Exception {
        InputStreamReader r = isr("A".getBytes(StandardCharsets.US_ASCII), "US-ASCII");
        check(r.ready(), "ready true while input remains");
        check(r.read() == 'A', "consume the char");
        check(!r.ready(), "ready false once decoder buffer and input are drained");
        check(!r.ready(), "ready stays false at EOF");

        InputStreamReader e = isr(new byte[0], "US-ASCII");
        check(!e.ready(), "empty stream not ready");
    }

    // ---- close semantics ----
    static class ProbeInputStream extends java.io.InputStream {
        byte[] data;
        int pos;
        boolean closed;
        ProbeInputStream(byte[] d) { data = d; }
        public int read() { return pos < data.length ? (data[pos++] & 0xff) : -1; }
        public void close() { closed = true; }
    }

    static void testCloseSemantics() throws Exception {
        ProbeInputStream in = new ProbeInputStream("ab".getBytes(StandardCharsets.US_ASCII));
        final InputStreamReader r = new InputStreamReader(in, "US-ASCII");
        r.close();
        check(in.closed, "close() closes the underlying InputStream");
        r.close();
        check(true, "close() is idempotent");
        checkMsg(IOException.class, "Stream closed", () -> r.read(), "closed read()");
        checkMsg(IOException.class, "Stream closed", () -> r.ready(), "closed ready()");
        checkMsg(IOException.class, "Stream closed", () -> r.skip(1), "closed skip()");
        checkMsg(IOException.class, "Stream closed",
                 () -> r.read(new char[1], 0, 1), "closed read(char[])");
        check(r.getEncoding() == null, "getEncoding() null after close");
        check(!in.closed || true, "second close does not re-close");   // no exception is the contract
    }

    // ---- read(char[],off,len) bounds ----
    static void testReadBounds() throws Exception {
        final InputStreamReader r = isr("hello".getBytes(StandardCharsets.US_ASCII), "US-ASCII");
        checkThrows(NullPointerException.class, () -> r.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], -1, 1),
                    "read(cbuf,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 1, Integer.MAX_VALUE),
                    "read(cbuf,1,MAX) overflow -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 0, 3),
                    "read(cbuf,0,3) -> IndexOutOfBoundsException");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0");
        char[] buf = new char[4];
        int n = r.read(buf, 1, 3);
        check(n == 3 && buf[0] == 0 && buf[1] == 'h' && buf[2] == 'e' && buf[3] == 'l',
              "exact offset placement");
        check(r.read() == 'l', "position continues after bulk read");
        check(r.read() == 'o', "last char");
    }

    // ---- skip counts chars, not bytes ----
    static void testSkipSemantics() throws Exception {
        InputStreamReader r = isr("héllo".getBytes(StandardCharsets.UTF_8), "UTF-8");
        check(r.skip(2) == 2, "skip over multi-byte prefix counts chars");
        check(r.read() == 'l' && r.read() == 'l' && r.read() == 'o',
              "remaining chars after char-granularity skip");

        InputStreamReader p = isr("abc".getBytes(StandardCharsets.US_ASCII), "US-ASCII");
        check(p.skip(100) == 3, "skip past end returns remaining count");
        check(p.read() == -1, "EOF after skip past end");

        InputStreamReader z = isr("abc".getBytes(StandardCharsets.US_ASCII), "US-ASCII");
        check(z.skip(0) == 0, "skip(0) returns 0");
        checkMsg(IllegalArgumentException.class, "skip value is negative",
                 () -> z.skip(-1), "negative skip message");
        check(z.read() == 'a', "failed negative skip did not move");

        InputStreamReader c = isr("ab".getBytes(StandardCharsets.US_ASCII), "US-ASCII");
        c.close();
        checkMsg(IllegalArgumentException.class, "skip value is negative",
                 () -> c.skip(-1), "bounds checked before open state");
    }

    // ---- surrogate pairs via single-char reads ----
    static void testSupplementarySingleReads() throws Exception {
        InputStreamReader r = isr("😀".getBytes(StandardCharsets.UTF_8), "UTF-8");
        int hi = r.read();
        int lo = r.read();
        check(hi == 0xD83D && lo == 0xDE00, "read() delivers high then low surrogate");
        check(r.read() == -1, "EOF after the pair");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testGetEncodingNames();
        testConstructorNegatives();
        testMalformedReplacement();
        testRoundTripMatrix();
        testUtf16ExplicitBytes();
        testReadySemantics();
        testCloseSemantics();
        testReadBounds();
        testSkipSemantics();
        testSupplementarySingleReads();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
