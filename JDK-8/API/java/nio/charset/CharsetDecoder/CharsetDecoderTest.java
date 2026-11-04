/*
 * @test
 * @summary java.nio.charset.CharsetDecoder conformance: white-box structure (abstract,
 *          key methods) plus black-box charset/metrics, replacement, error actions,
 *          low-level decode+flush, convenience decode, auto-detection surface, malformed.
 * @run main CharsetDecoderTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;

public class CharsetDecoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CharsetDecoder");
        check(Modifier.isAbstract(c.getModifiers()), "CharsetDecoder is abstract");
        check(c.getSuperclass() == Object.class, "extends Object");

        check(hasMethod(c, "charset"), "has charset()");
        check(hasMethod(c, "replacement"), "has replacement()");
        check(hasMethod(c, "replaceWith", String.class), "has replaceWith(String)");
        check(hasMethod(c, "onMalformedInput", CodingErrorAction.class), "has onMalformedInput");
        check(hasMethod(c, "onUnmappableCharacter", CodingErrorAction.class), "has onUnmappableCharacter");
        check(hasMethod(c, "decode", ByteBuffer.class, CharBuffer.class, boolean.class), "has decode(in,out,bool)");
        check(hasMethod(c, "flush", CharBuffer.class), "has flush(CharBuffer)");
        check(hasMethod(c, "reset"), "has reset()");
        check(hasMethod(c, "decode", ByteBuffer.class), "has decode(ByteBuffer)");
        check(hasMethod(c, "isAutoDetecting"), "has isAutoDetecting()");
        check(hasMethod(c, "isCharsetDetected"), "has isCharsetDetected()");
        check(hasMethod(c, "detectedCharset"), "has detectedCharset()");
    }

    static void testBlackBox() throws Exception {
        CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder();
        check(dec.charset().equals(StandardCharsets.UTF_8), "charset");
        check(dec.averageCharsPerByte() >= 0.5f, "averageCharsPerByte");
        check(dec.maxCharsPerByte() >= 1.0f, "maxCharsPerByte");
        check("�".equals(dec.replacement()), "default replacement U+FFFD");
        check(dec.malformedInputAction() == CodingErrorAction.REPORT, "default malformed REPORT");
        check(dec.unmappableCharacterAction() == CodingErrorAction.REPORT, "default unmappable REPORT");

        check(!dec.isAutoDetecting(), "UTF-8 decoder not auto-detecting");
        checkThrows(UnsupportedOperationException.class, () -> dec.isCharsetDetected(),
                    "isCharsetDetected when not auto-detecting");
        checkThrows(UnsupportedOperationException.class, () -> dec.detectedCharset(),
                    "detectedCharset when not auto-detecting");

        CharsetDecoder rp = StandardCharsets.UTF_8.newDecoder();
        CharsetDecoder rpRet = rp.replaceWith("?");
        check(rpRet == rp, "replaceWith returns this");
        check("?".equals(rp.replacement()), "custom replacement");
        checkThrows(IllegalArgumentException.class, () -> rp.replaceWith(null), "replaceWith null");

        CharsetDecoder lo = StandardCharsets.UTF_8.newDecoder();
        lo.reset();
        ByteBuffer in = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        CharBuffer out = CharBuffer.allocate(16);
        CoderResult r = lo.decode(in, out, true);
        check(r.isUnderflow(), "decode underflow");
        CoderResult f = lo.flush(out);
        check(f.isUnderflow(), "flush underflow");
        out.flip();
        check("hello".contentEquals(out), "decoded hello");
        check(!in.hasRemaining(), "input fully consumed");

        CharsetDecoder conv = StandardCharsets.UTF_8.newDecoder();
        String cjk = "中文";
        String emoji = "😀";
        check(cjk.equals(conv.decode(StandardCharsets.UTF_8.encode(cjk)).toString()), "CJK roundtrip");
        check(emoji.equals(conv.decode(StandardCharsets.UTF_8.encode(emoji)).toString()), "emoji roundtrip");

        ByteBuffer padded = ByteBuffer.allocate(8);
        padded.put((byte) 'X');
        ByteBuffer okBytes = StandardCharsets.UTF_8.encode("ok");
        padded.put(okBytes);
        padded.position(1).limit(3);
        check("ok".contentEquals(conv.decode(padded)), "decode uses remaining region");

        byte[] bad = {(byte) 0xC3, (byte) 0x28};
        CharsetDecoder report = StandardCharsets.UTF_8.newDecoder();
        report.reset();
        CoderResult mr = report.decode(ByteBuffer.wrap(bad), CharBuffer.allocate(8), true);
        check(mr.isMalformed(), "malformed CoderResult");
        try {
            StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bad));
            check(false, "convenience decode malformed should throw");
        } catch (CharacterCodingException e) {
            check(true, "convenience decode malformed throws");
        }

        CharsetDecoder repl = StandardCharsets.UTF_8.newDecoder();
        repl.onMalformedInput(CodingErrorAction.REPLACE);
        repl.reset();
        CharBuffer rOut = CharBuffer.allocate(8);
        CoderResult rr = repl.decode(ByteBuffer.wrap(bad), rOut, true);
        repl.flush(rOut);
        rOut.flip();
        check(rOut.length() == 2, "malformed replaced then '(' decoded (2 chars)");
        check(rOut.charAt(0) == '�' && rOut.charAt(1) == '(', "replacement char then '('");
    }

    // ================ A3 batch appended: malformed x Action matrix, endOfInput state machine,
    // ================ OVERFLOW continuation loop, configuration round-trips, ctor matrix ================

    static final Charset[] SIX_A3 = {
        StandardCharsets.US_ASCII, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16,
    };

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    static void testMalformedMatrixA3() throws Exception {
        // Malformed UTF-8 units (REPORT length, REPLACE output, IGNORE output), all probed
        // identical on JDK-8 and JDK-27.
        Object[][] cases = {
            {new byte[]{0x41, (byte) 0xC3, 0x28}, 1, "A�(", "A("},           // truncated 2-byte header
            {new byte[]{(byte) 0xE0, (byte) 0x80}, 1, "��", ""},        // E0 followed by 80 (needs A0..BF)
            {new byte[]{(byte) 0xED, (byte) 0xA0, (byte) 0x80}, 3, "�", ""},// CESU-8 surrogate encoding
            {new byte[]{(byte) 0xC0, (byte) 0x80}, 1, "��", ""},        // overlong NUL
            {new byte[]{(byte) 0xF5}, 1, "�", ""},                            // leading byte > U+10FFFF range
            {new byte[]{0x41, (byte) 0xF5, 0x42, (byte) 0xC0, (byte) 0x80, 0x43}, 1,
                "A�B��C", "ABC"},                                   // compound: two malformed units
        };
        for (Object[] c : cases) {
            byte[] bad = (byte[]) c[0];
            int expLen = (Integer) c[1];
            String expReplace = (String) c[2];
            String expIgnore = (String) c[3];
            String label = hex(bad);
            // REPORT (default) low-level: CoderResult reports the exact malformed-unit length.
            CharsetDecoder d1 = StandardCharsets.UTF_8.newDecoder();
            CoderResult r = d1.decode(ByteBuffer.wrap(bad), CharBuffer.allocate(32), true);
            check(r.isMalformed() && r.isError(), "A3 malformed low-level " + label);
            check(r.length() == expLen, "A3 malformed length " + label + " == " + expLen);
            check(!r.isUnmappable() && !r.isUnderflow() && !r.isOverflow(), "A3 malformed exclusive " + label);
            // REPORT (default) convenience: MalformedInputException with the exact length and message.
            try {
                StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bad));
                check(false, "A3 convenience REPORT must throw " + label);
            } catch (MalformedInputException e) {
                check(e.getInputLength() == expLen, "A3 MIE getInputLength " + label);
                check(("Input length = " + expLen).equals(e.getMessage()), "A3 MIE message " + label);
            }
            // REPLACE: every malformed unit becomes one U+FFFD, decoding continues afterwards.
            CharsetDecoder d2 = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE);
            check(expReplace.equals(d2.decode(ByteBuffer.wrap(bad)).toString()), "A3 REPLACE " + label);
            // IGNORE: malformed units are dropped, decoding continues afterwards.
            CharsetDecoder d3 = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.IGNORE);
            check(expIgnore.equals(d3.decode(ByteBuffer.wrap(bad)).toString()), "A3 IGNORE " + label);
        }
        // Additional malformed shapes: exact REPORT lengths probed identical on both sides.
        byte[][] more = {
            {(byte) 0x80},                                          // stray continuation byte
            {(byte) 0xC3},                                          // truncated at end of input
            {(byte) 0xE2, (byte) 0x82},                             // truncated 3-byte tail
            {(byte) 0xF0, (byte) 0x9F},                             // truncated 4-byte tail
            {(byte) 0xC1, (byte) 0xBF},                             // overlong via C1
            {(byte) 0xF4, (byte) 0x90, (byte) 0x80, (byte) 0x80},   // 4-byte > U+10FFFF
        };
        int[] lens = {1, 1, 2, 2, 1, 1};
        for (int i = 0; i < more.length; i++) {
            try {
                StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(more[i]));
                check(false, "A3 extra malformed must throw " + hex(more[i]));
            } catch (MalformedInputException e) {
                check(e.getInputLength() == lens[i], "A3 extra malformed len " + hex(more[i]) + " == " + lens[i]);
            }
        }
        // US-ASCII: bytes >= 0x80 are malformed (decoders never report unmappable).
        try {
            StandardCharsets.US_ASCII.newDecoder().decode(ByteBuffer.wrap(new byte[]{0x41, (byte) 0x80, 0x42}));
            check(false, "A3 ASCII 0x80 must be malformed");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 1, "A3 ASCII 0x80 malformed len 1");
        }
        CharsetDecoder ar = StandardCharsets.US_ASCII.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE);
        check("A�B".equals(ar.decode(ByteBuffer.wrap(new byte[]{0x41, (byte) 0x80, 0x42})).toString()),
              "A3 ASCII REPLACE U+FFFD");
        CharsetDecoder ai = StandardCharsets.US_ASCII.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE);
        check("AB".equals(ai.decode(ByteBuffer.wrap(new byte[]{0x41, (byte) 0x80, 0x42})).toString()),
              "A3 ASCII IGNORE drops");
        CharsetDecoder ac = StandardCharsets.US_ASCII.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE).replaceWith("#");
        check("A#B".equals(ac.decode(ByteBuffer.wrap(new byte[]{0x41, (byte) 0x80, 0x42})).toString()),
              "A3 ASCII REPLACE with custom '#'");
    }

    static void testEndOfInputA3() {
        ByteBuffer in = ByteBuffer.wrap("hi".getBytes(StandardCharsets.US_ASCII));
        CharBuffer out = CharBuffer.allocate(16);
        // flush() before any decoding step -> IllegalStateException.
        checkThrows(IllegalStateException.class, () -> StandardCharsets.UTF_8.newDecoder().flush(out),
                    "A3 flush(fresh) ISE");
        // flush() requires a completed endOfInput=true step: after decode(false) -> ISE.
        CharsetDecoder d2 = StandardCharsets.UTF_8.newDecoder();
        check(d2.decode(in.duplicate(), out, false).isUnderflow(), "A3 decode(false) mid-stream underflow");
        checkThrows(IllegalStateException.class, () -> d2.flush(out), "A3 flush after eoi=false ISE");
        // endOfInput must be non-decreasing: true then false -> ISE.
        CharsetDecoder d3 = StandardCharsets.UTF_8.newDecoder();
        d3.decode(in.duplicate(), out, true);
        checkThrows(IllegalStateException.class, () -> d3.decode(in.duplicate(), out, false),
                    "A3 decode(true then false) ISE");
        // Repeated flush() after a completed decode keeps returning UNDERFLOW.
        CharsetDecoder d4 = StandardCharsets.UTF_8.newDecoder();
        d4.decode(in.duplicate(), out, true);
        check(d4.flush(out).isUnderflow(), "A3 flush#1 underflow");
        check(d4.flush(out).isUnderflow(), "A3 flush#2 underflow");
        // decode() after flush without reset() -> ISE; reset() returns this and re-arms the decoder.
        checkThrows(IllegalStateException.class, () -> d4.decode(in.duplicate(), out, true),
                    "A3 decode after flush ISE");
        check(d4.reset() == d4, "A3 reset returns this");
        check(d4.decode(in.duplicate(), out, true).isUnderflow(), "A3 decode after reset ok");
        // Truncated input with endOfInput=false reports UNDERFLOW and leaves the pending bytes in
        // the input buffer (position rewound to the start of the incomplete unit).
        CharsetDecoder d5 = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer part = ByteBuffer.allocate(4);
        part.put((byte) 0xE4).put((byte) 0xB8);
        part.flip();
        CharBuffer pout = CharBuffer.allocate(4);
        check(d5.decode(part, pout, false).isUnderflow(), "A3 truncated eoi=false underflow");
        check(part.position() == 0, "A3 truncated input not consumed");
        part.limit(3);
        part.put(2, (byte) 0xAD); // absolute put completes the 3-byte sequence of U+4E2D
        check(d5.decode(part, pout, true).isUnderflow(), "A3 completed after same-buffer append");
        pout.flip();
        check(pout.length() == 1 && pout.charAt(0) == '中', "A3 partial+append decodes U+4E2D");
        // A malformed prefix is reported immediately even though more input may follow.
        CharsetDecoder d6 = StandardCharsets.UTF_8.newDecoder();
        CoderResult m = d6.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3, 0x28}), CharBuffer.allocate(4), false);
        check(m.isMalformed() && m.length() == 1, "A3 malformed detected with eoi=false");
    }

    static void testOverflowLoopA3() throws Exception {
        // Output-space exhaustion: OVERFLOW with partial output, continuation completes the decode.
        CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer in = ByteBuffer.wrap("hello".getBytes(StandardCharsets.US_ASCII));
        CharBuffer small = CharBuffer.allocate(2);
        CoderResult r = d.decode(in, small, true);
        check(r.isOverflow() && !r.isError() && !r.isUnderflow(), "A3 decode overflow result");
        small.flip();
        check(small.length() == 2 && small.charAt(0) == 'h' && small.charAt(1) == 'e', "A3 overflow produced 'he'");
        check(in.remaining() == 3, "A3 overflow leaves 3 input bytes");
        CharBuffer rest = CharBuffer.allocate(8);
        check(d.decode(in, rest, true).isUnderflow(), "A3 continuation underflow");
        rest.flip();
        check(rest.length() == 3 && "llo".contentEquals(rest), "A3 continuation decodes rest");
        // Manual tiny-buffer decode loop equals the convenience result. Tiny capacity is 2 chars
        // because an astral code point decodes to a surrogate pair written atomically.
        String s = "Aé中😀ok";
        ByteBuffer bytes = StandardCharsets.UTF_8.encode(s);
        CharsetDecoder lo = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        lo.reset();
        StringBuilder sb = new StringBuilder();
        ByteBuffer bin = bytes.duplicate();
        boolean done = false;
        while (!done) {
            CharBuffer tiny = CharBuffer.allocate(2);
            CoderResult cr = lo.decode(bin, tiny, true);
            tiny.flip();
            sb.append(tiny);
            if (cr.isOverflow()) continue;
            if (cr.isUnderflow()) done = true;
            else cr.throwException();
        }
        check(s.contentEquals(sb), "A3 manual tiny decode loop == convenience");
    }

    static void testConfigurationA3() {
        CodingErrorAction[] acts = {CodingErrorAction.REPORT, CodingErrorAction.REPLACE, CodingErrorAction.IGNORE};
        for (CodingErrorAction a : acts) {
            CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
            check(d.onMalformedInput(a) == d, "A3 onMalformedInput returns this " + a);
            check(d.malformedInputAction() == a, "A3 malformed getter " + a);
            check(d.onUnmappableCharacter(a) == d, "A3 onUnmappableCharacter returns this " + a);
            check(d.unmappableCharacterAction() == a, "A3 unmappable getter " + a);
        }
        // Default replacement of every standard decoder is U+FFFD.
        for (Charset c : SIX_A3) {
            check("�".equals(c.newDecoder().replacement()), "A3 default replacement U+FFFD " + c.name());
        }
        // replaceWith is validated by length only (must be non-empty and <= maxCharsPerByte);
        // encodability in the target charset is NOT required ("é" accepted for US-ASCII, probed).
        CharsetDecoder r1 = StandardCharsets.US_ASCII.newDecoder();
        check(r1.replaceWith("é") == r1 && "é".equals(r1.replacement()),
              "A3 decoder replaceWith length-checked only");
        CharsetDecoder r2 = StandardCharsets.US_ASCII.newDecoder();
        r2.replaceWith("\ud800");
        check("\ud800".equals(r2.replacement()), "A3 lone surrogate accepted (length 1)");
        checkThrows(IllegalArgumentException.class, () -> StandardCharsets.UTF_8.newDecoder().replaceWith(""),
              "A3 replaceWith(\"\") IAE");
        checkThrows(IllegalArgumentException.class, () -> StandardCharsets.UTF_16.newDecoder().replaceWith("AB"),
              "A3 replaceWith longer than maxCharsPerByte IAE");
        // Metrics: probed constants of the six standard charsets (identical JDK-8/JDK-27).
        float[][] dec = {{1.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, 1.0f},
                         {0.5f, 1.0f}, {0.5f, 1.0f}, {0.5f, 1.0f}};
        for (int i = 0; i < SIX_A3.length; i++) {
            CharsetDecoder d = SIX_A3[i].newDecoder();
            check(d.averageCharsPerByte() == dec[i][0], "A3 dec avg " + SIX_A3[i].name() + " == " + dec[i][0]);
            check(d.maxCharsPerByte() == dec[i][1], "A3 dec max " + SIX_A3[i].name() + " == " + dec[i][1]);
            check(d.maxCharsPerByte() >= d.averageCharsPerByte() && d.averageCharsPerByte() > 0,
                  "A3 dec metric invariant " + SIX_A3[i].name());
            check(!d.isAutoDetecting(), "A3 not auto-detecting " + SIX_A3[i].name());
        }
        // isCharsetDetected/detectedCharset are unsupported when the decoder is not auto-detecting.
        CharsetDecoder nd = StandardCharsets.US_ASCII.newDecoder();
        checkThrows(UnsupportedOperationException.class, () -> nd.isCharsetDetected(), "A3 ASCII isCharsetDetected UOE");
        checkThrows(UnsupportedOperationException.class, () -> nd.detectedCharset(), "A3 ASCII detectedCharset UOE");
    }

    static void testSubclassAndCtorA3() throws Exception {
        // The (Charset,float,float) constructor is protected and subclass-callable.
        boolean protectedThreeArg = false;
        for (java.lang.reflect.Constructor<?> k : CharsetDecoder.class.getDeclaredConstructors()) {
            if (k.getParameterTypes().length == 3 && Modifier.isProtected(k.getModifiers()))
                protectedThreeArg = true;
        }
        check(protectedThreeArg, "A3 CharsetDecoder has protected (Charset,float,float) ctor");
        MetricDecoder md = new MetricDecoder(StandardCharsets.UTF_8, 0.5f, 1.0f);
        check(md.charset() == StandardCharsets.UTF_8, "A3 subclass charset round-trip");
        check(md.averageCharsPerByte() == 0.5f && md.maxCharsPerByte() == 1.0f, "A3 subclass metrics round-trip");
        // Constructor precondition matrix: avg<=0, max<=0 or max<avg -> IllegalArgumentException.
        float[][] bad = {{0f, 1f}, {1f, 0f}, {-1f, 1f}, {1f, -1f}, {0.5f, 0.25f}};
        for (float[] b : bad) {
            final float av = b[0], mx = b[1];
            checkThrows(IllegalArgumentException.class,
                  () -> new MetricDecoder(StandardCharsets.US_ASCII, av, mx),
                  "A3 decoder ctor IAE (" + av + "," + mx + ")");
        }
        check(new MetricDecoder(StandardCharsets.US_ASCII, 1f, 1f).maxCharsPerByte() == 1.0f, "A3 ctor (1,1) ok");
    }

    static void testIsoIdentityA3() throws Exception {
        // ISO-8859-1 decoding is the identity map for all 256 byte values.
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) all[i] = (byte) i;
        CharBuffer c = StandardCharsets.ISO_8859_1.decode(ByteBuffer.wrap(all));
        boolean identity = c.length() == 256;
        for (int i = 0; i < 256 && c.hasRemaining(); i++) if (c.get() != (char) i) identity = false;
        check(identity, "A3 ISO-8859-1 decode 0..255 identity");
        // Round trip: encoding chars U+0000..U+00FF yields the same 256 bytes.
        char[] chars = new char[256];
        for (int i = 0; i < 256; i++) chars[i] = (char) i;
        ByteBuffer e = StandardCharsets.ISO_8859_1.encode(CharBuffer.wrap(chars));
        boolean encId = e.remaining() == 256;
        for (int i = 0; i < 256 && e.hasRemaining(); i++) if (e.get() != (byte) i) encId = false;
        check(encId, "A3 ISO-8859-1 encode U+0000..U+00FF identity");
    }

    static class MetricDecoder extends CharsetDecoder {
        MetricDecoder(Charset cs, float avg, float max) { super(cs, avg, max); }
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) { return CoderResult.UNDERFLOW; }
    }

    // A3 补充契约：UTF-16 族畸形、BOM 变体无 BOM 行为、空输入、便捷法缓冲终态、reset() 不改配置
    //（P6 探针在 JDK-8 与 JDK-27 上输出完全一致）
    static void testExtraDecoderContractsA3() throws Exception {
        // UTF-16 family malformed units x three actions.
        Object[][] cases = {
            // {charset name, bytes, malformed length, bytes consumed at report, REPLACE out, IGNORE out}
            {"UTF-16BE", new byte[]{0x00, 0x41, 0x00}, 1, 2, "A�", "A"},              // odd trailing byte
            {"UTF-16BE", new byte[]{0x00}, 1, 0, "�", ""},                              // single odd byte
            {"UTF-16BE", new byte[]{(byte) 0xD8, 0x00}, 2, 0, "�", ""},                // lone high surrogate unit
            {"UTF-16BE", new byte[]{0x00, 0x41, (byte) 0xDC, 0x00}, 2, 2, "A�", "A"},  // lone low surrogate unit
            {"UTF-16LE", new byte[]{0x41, 0x00, 0x41}, 1, 2, "A�", "A"},              // odd trailing byte (LE)
        };
        for (Object[] c : cases) {
            Charset cs = Charset.forName((String) c[0]);
            byte[] bad = (byte[]) c[1];
            int expLen = (Integer) c[2];
            int consumed = (Integer) c[3];
            String expReplace = (String) c[4];
            String expIgnore = (String) c[5];
            String label = (String) c[0] + " " + hex(bad);
            CharsetDecoder d1 = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT);
            ByteBuffer in = ByteBuffer.wrap(bad);
            CoderResult r = d1.decode(in, CharBuffer.allocate(16), true);
            check(r.isMalformed() && r.length() == expLen, "A3 UTF-16 malformed len " + label);
            check(in.position() == consumed, "A3 UTF-16 consumed before report " + label);
            check(expReplace.equals(cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
                    .decode(ByteBuffer.wrap(bad)).toString()), "A3 UTF-16 REPLACE " + label);
            check(expIgnore.equals(cs.newDecoder().onMalformedInput(CodingErrorAction.IGNORE)
                    .decode(ByteBuffer.wrap(bad)).toString()), "A3 UTF-16 IGNORE " + label);
        }
        // BOM-detecting "UTF-16" without a BOM never reports malformed: with two bytes and
        // endOfInput=true it still returns UNDERFLOW, and convenience decoding assumes
        // big-endian with no replacement character inserted.
        CharsetDecoder d = StandardCharsets.UTF_16.newDecoder();
        ByteBuffer in2 = ByteBuffer.wrap(new byte[]{0x00, 0x41});
        CoderResult r2 = d.decode(in2, CharBuffer.allocate(8), true);
        check(r2.isUnderflow() && !r2.isError(), "A3 UTF-16 no-BOM eoi=true is UNDERFLOW");
        check(in2.position() == 2, "A3 UTF-16 no-BOM consumed all input");
        checkThrows(UnsupportedOperationException.class, () -> { r2.length(); },
                "A3 CoderResult.length() on non-error result");
        check("A".equals(StandardCharsets.UTF_16.decode(ByteBuffer.wrap(new byte[]{0x00, 0x41})).toString()),
                "A3 UTF-16 no-BOM defaults to big-endian");
        check("A".equals(StandardCharsets.UTF_16.decode(
                ByteBuffer.wrap(new byte[]{(byte) 0xFF, (byte) 0xFE, 0x41, 0x00})).toString()),
                "A3 UTF-16 LE BOM stripped");
        // Empty input at end-of-input: clean UNDERFLOW; convenience decode yields an empty buffer.
        check(StandardCharsets.UTF_8.newDecoder()
                .decode(ByteBuffer.allocate(0), CharBuffer.allocate(4), true).isUnderflow(),
                "A3 empty input low-level UNDERFLOW");
        check(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.allocate(0)).length() == 0,
                "A3 convenience decode of empty input");
        // Convenience decode consumes the whole input and returns a positioned, writable buffer.
        ByteBuffer conv = ByteBuffer.wrap(new byte[]{0x41, 0x42});
        CharBuffer out = StandardCharsets.UTF_8.newDecoder().decode(conv);
        check(conv.position() == conv.limit(), "A3 convenience decode consumes input fully");
        check("AB".equals(out.toString()) && out.hasRemaining() && !out.isReadOnly(),
                "A3 convenience decode output buffer state");
        // reset() rewinds the coding state but keeps action and replacement configuration.
        CharsetDecoder d4 = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE)
                .onUnmappableCharacter(CodingErrorAction.IGNORE)
                .replaceWith("#");
        d4.decode(ByteBuffer.wrap(new byte[]{(byte) 0xC3, 0x28}), CharBuffer.allocate(8), true);
        d4.reset();
        check(d4.malformedInputAction() == CodingErrorAction.IGNORE
                && d4.unmappableCharacterAction() == CodingErrorAction.IGNORE
                && "#".equals(d4.replacement()), "A3 reset keeps configuration");
        // charset() returns the canonical singleton for every constant.
        for (Charset cs : SIX_A3) check(cs.newDecoder().charset() == cs, "A3 decoder charset() singleton");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMalformedMatrixA3();
        testEndOfInputA3();
        testOverflowLoopA3();
        testConfigurationA3();
        testSubclassAndCtorA3();
        testIsoIdentityA3();
        testExtraDecoderContractsA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
