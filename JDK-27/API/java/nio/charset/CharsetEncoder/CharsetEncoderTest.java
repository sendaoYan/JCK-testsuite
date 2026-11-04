/*
 * @test
 * @summary java.nio.charset.CharsetEncoder conformance: white-box structure (abstract,
 *          key methods) plus black-box charset/metrics, replacement, error actions,
 *          low-level encode+flush, convenience encode, canEncode, malformed/unmappable.
 * @run main CharsetEncoderTest
 *
 * 黑盒设计案:
 * 1. 契约: CharsetEncoder 把 CharBuffer 编码为 ByteBuffer; 默认 replacement 是单字节
 *    '?'; 默认 malformed/unmappable 动作是 REPORT; average/maxBytesPerChar 描述编码宽度.
 * 2. 场景: newEncoder() -> onMalformedInput/onUnmappableCharacter 配置 -> reset ->
 *    encode(in,out,true) 多步 + flush -> flip 读字节; 或便捷 encode(CharBuffer).
 * 3. 组合链路: 对 UTF-8 编码 ASCII/CJK; 对 US-ASCII 用 REPLACE 把不可映射 CJK 替换成
 *    自定义字节; 对 UTF-8 孤代理项 (malformed) 用 REPORT 得到 CoderResult.isMalformed.
 * 4. 不变量: encode 后 charset() 不变; replaceWith 校验长度不超过 maxBytesPerChar;
 *    reset 恢复初始状态; encode 不推进输入超过已消费的字符.
 * 5. 边界/异常: 不可映射/畸形字符 + REPORT -> 便捷 encode 抛 CharacterCodingException,
 *    底层 encode 返回 CoderResult (isMalformed/isUnmappable); 过长 replacement 抛
 *    IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;

public class CharsetEncoderTest {
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
        Class<?> c = Class.forName("java.nio.charset.CharsetEncoder");
        check(Modifier.isAbstract(c.getModifiers()), "CharsetEncoder is abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getInterfaces().length == 0, "implements no interfaces");

        check(hasMethod(c, "charset"), "has charset()");
        check(hasMethod(c, "replacement"), "has replacement()");
        check(hasMethod(c, "replaceWith", byte[].class), "has replaceWith(byte[])");
        check(hasMethod(c, "isLegalReplacement", byte[].class), "has isLegalReplacement(byte[])");
        check(hasMethod(c, "onMalformedInput", CodingErrorAction.class), "has onMalformedInput");
        check(hasMethod(c, "onUnmappableCharacter", CodingErrorAction.class), "has onUnmappableCharacter");
        check(hasMethod(c, "encode", CharBuffer.class, ByteBuffer.class, boolean.class), "has encode(in,out,bool)");
        check(hasMethod(c, "flush", ByteBuffer.class), "has flush(ByteBuffer)");
        check(hasMethod(c, "reset"), "has reset()");
        check(hasMethod(c, "encode", CharBuffer.class), "has encode(CharBuffer)");
        check(hasMethod(c, "canEncode", char.class), "has canEncode(char)");
        check(hasMethod(c, "canEncode", CharSequence.class), "has canEncode(CharSequence)");
    }

    static void testBlackBox() throws Exception {
        CharsetEncoder enc = StandardCharsets.UTF_8.newEncoder();
        check(enc.charset().equals(StandardCharsets.UTF_8), "charset");
        check(enc.averageBytesPerChar() >= 1.0f, "averageBytesPerChar >= 1");
        check(enc.maxBytesPerChar() >= 1.0f, "maxBytesPerChar >= 1");
        check(enc.replacement().length == 1 && enc.replacement()[0] == (byte) '?',
              "default replacement is '?'");
        check(enc.malformedInputAction() == CodingErrorAction.REPORT, "default malformed REPORT");
        check(enc.unmappableCharacterAction() == CodingErrorAction.REPORT, "default unmappable REPORT");

        CharsetEncoder ascii = StandardCharsets.US_ASCII.newEncoder();
        CharsetEncoder ret = ascii.replaceWith(new byte[] {'X'});
        check(ret == ascii, "replaceWith returns this");
        check(ascii.replacement().length == 1 && ascii.replacement()[0] == 'X', "custom replacement");
        check(ascii.isLegalReplacement(new byte[] {'X'}), "isLegalReplacement true");
        checkThrows(IllegalArgumentException.class,
                    () -> ascii.replaceWith(new byte[] {'X', 'Y'}), "replacement too long");

        CharsetEncoder lo = StandardCharsets.UTF_8.newEncoder();
        lo.reset();
        CharBuffer in = CharBuffer.wrap("hello");
        ByteBuffer out = ByteBuffer.allocate(16);
        CoderResult r = lo.encode(in, out, true);
        check(r.isUnderflow(), "encode underflow");
        CoderResult f = lo.flush(out);
        check(f.isUnderflow(), "flush underflow");
        out.flip();
        byte[] bytes = new byte[out.remaining()];
        out.get(bytes);
        check("hello".equals(new String(bytes, StandardCharsets.UTF_8)), "encoded hello");
        check(!in.hasRemaining(), "input fully consumed");

        CharsetEncoder conv = StandardCharsets.UTF_8.newEncoder();
        ByteBuffer cout = conv.encode(CharBuffer.wrap("hello"));
        check("hello".equals(StandardCharsets.UTF_8.decode(cout).toString()), "convenience encode roundtrip");
        ByteBuffer cjk = conv.encode(CharBuffer.wrap("中文"));
        check("中文".equals(StandardCharsets.UTF_8.decode(cjk).toString()), "CJK roundtrip");

        CharsetEncoder ae = StandardCharsets.US_ASCII.newEncoder();
        check(ae.canEncode('A'), "ASCII canEncode 'A'");
        check(!ae.canEncode('中'), "ASCII cannot encode CJK");
        check(ae.canEncode("ABC"), "ASCII canEncode CharSequence");
        check(!ae.canEncode("中"), "ASCII cannot encode CJK CharSequence");
        check(StandardCharsets.UTF_8.newEncoder().canEncode('中'), "UTF-8 canEncode CJK");

        CharsetEncoder unm = StandardCharsets.US_ASCII.newEncoder();
        unm.reset();
        CoderResult ur = unm.encode(CharBuffer.wrap("中"), ByteBuffer.allocate(8), true);
        check(ur.isUnmappable(), "unmappable CoderResult");

        CharsetEncoder mal = StandardCharsets.UTF_8.newEncoder();
        mal.reset();
        CoderResult mr = mal.encode(CharBuffer.wrap("\uD800"), ByteBuffer.allocate(8), true);
        check(mr.isMalformed(), "malformed CoderResult");

        try {
            StandardCharsets.US_ASCII.newEncoder().encode(CharBuffer.wrap("中"));
            check(false, "convenience encode unmappable should throw");
        } catch (CharacterCodingException e) {
            check(true, "convenience encode unmappable throws");
        }
        try {
            StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap("\uD800"));
            check(false, "convenience encode malformed should throw");
        } catch (CharacterCodingException e) {
            check(true, "convenience encode malformed throws");
        }

        CharsetEncoder rep = StandardCharsets.US_ASCII.newEncoder()
                .replaceWith(new byte[] {'?'})
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        rep.reset();
        ByteBuffer rOut = ByteBuffer.allocate(8);
        CoderResult rr = rep.encode(CharBuffer.wrap("中"), rOut, true);
        check(rr.isUnderflow(), "REPLACE consumes input");
        rep.flush(rOut);
        rOut.flip();
        check(rOut.remaining() == 1 && rOut.get() == (byte) '?', "unmappable replaced with '?'");

        CharsetEncoder cfg = StandardCharsets.UTF_8.newEncoder();
        cfg.onMalformedInput(CodingErrorAction.REPLACE);
        cfg.reset();
        check(cfg.malformedInputAction() == CodingErrorAction.REPLACE, "reset keeps error action");
    }

    // ================ A3 batch appended: unmappable x Action matrix, surrogate malformed,
    // ================ byte layouts, state machine, ctor matrix + flush framework ================

    static final Charset[] SIX_A3 = {
        StandardCharsets.US_ASCII, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16,
    };

    static byte[] bytesOf(ByteBuffer b) {
        byte[] r = new byte[b.remaining()];
        b.get(r);
        return r;
    }

    static void testUnmappableMatrixA3() throws Exception {
        // {charset, input, expected unmappable length}; lengths probed identical on JDK-8/JDK-27.
        Object[][] cases = {
            {"US-ASCII", "a中b", 1}, {"US-ASCII", "é", 1}, {"US-ASCII", "\u0080", 1},
            {"US-ASCII", "😀", 2},  // a surrogate pair counts as one unmappable unit of length 2
            {"ISO-8859-1", "中", 1}, {"ISO-8859-1", "€", 1}, {"ISO-8859-1", "Ā", 1},
        };
        for (Object[] c : cases) {
            Charset cs = Charset.forName((String) c[0]);
            String in = (String) c[1];
            int expLen = (Integer) c[2];
            String label = c[0] + " " + in;
            // REPORT (default) low-level: CoderResult reports the unmappable-unit length.
            CharsetEncoder e1 = cs.newEncoder();
            CoderResult r = e1.encode(CharBuffer.wrap(in), ByteBuffer.allocate(64), true);
            check(r.isUnmappable() && r.isError(), "A3 unmappable low-level " + label);
            check(r.length() == expLen, "A3 unmappable length " + label + " == " + expLen);
            check(!r.isMalformed() && !r.isUnderflow() && !r.isOverflow(), "A3 unmappable exclusive " + label);
            // REPORT (default) convenience: UnmappableCharacterException with exact length and message.
            try {
                cs.newEncoder().encode(CharBuffer.wrap(in));
                check(false, "A3 convenience REPORT must throw " + label);
            } catch (UnmappableCharacterException e) {
                check(e.getInputLength() == expLen, "A3 UCE getInputLength " + label);
                check(("Input length = " + expLen).equals(e.getMessage()), "A3 UCE message " + label);
            }
        }
        // ISO-8859-1 maps all of U+0000..U+00FF (full Latin-1 block).
        CharsetEncoder iso = StandardCharsets.ISO_8859_1.newEncoder();
        check(iso.canEncode('ÿ') && iso.encode(CharBuffer.wrap("é")).get() == (byte) 0xE9,
              "A3 ISO-8859-1 encodes Latin-1 block");
    }

    static void testMalformedSurrogatesA3() throws Exception {
        // Lone surrogates are malformed input for an encoder (not unmappable); unit length is 1
        // on both UTF-8 and US-ASCII encoders (probed identical on JDK-8/JDK-27).
        String[] lone = {"\ud800", "\udc00", "a\ud800b", "\ud83d", "a\udc00"};
        for (String s : lone) {
            for (String cn : new String[] {"UTF-8", "US-ASCII"}) {
                CharsetEncoder e = Charset.forName(cn).newEncoder();
                CoderResult r = e.encode(CharBuffer.wrap(s), ByteBuffer.allocate(64), true);
                check(r.isMalformed() && r.length() == 1, "A3 lone surrogate malformed len 1 (" + cn + ")");
            }
        }
        try {
            StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap("\ud800"));
            check(false, "A3 convenience encode lone surrogate must throw");
        } catch (MalformedInputException e) {
            check(e.getInputLength() == 1, "A3 MIE getInputLength lone surrogate");
        }
        // A well-formed surrogate pair is legal UTF-8 input (4 output bytes, see layout test).
        CoderResult ok = StandardCharsets.UTF_8.newEncoder()
                .encode(CharBuffer.wrap("😀"), ByteBuffer.allocate(8), true);
        check(ok.isUnderflow(), "A3 surrogate pair encodes cleanly");
    }

    static void testActionsA3() throws Exception {
        // REPLACE: default replacement byte '?' substituted, encoding continues.
        CharsetEncoder rep = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        check("a?b".equals(new String(bytesOf(rep.encode(CharBuffer.wrap("a中b"))),
                StandardCharsets.ISO_8859_1)), "A3 ASCII REPLACE a中b -> a?b");
        // IGNORE: unmappable characters dropped, encoding continues.
        CharsetEncoder ign = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.IGNORE);
        check("ab".equals(new String(bytesOf(ign.encode(CharBuffer.wrap("a中b"))),
                StandardCharsets.ISO_8859_1)), "A3 ASCII IGNORE a中b -> ab");
        // REPLACE/IGNORE for malformed (lone surrogate) input on the encoder side.
        CharsetEncoder malRep = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE);
        check("a?b".equals(new String(bytesOf(malRep.encode(CharBuffer.wrap("a\ud800b"))),
                StandardCharsets.ISO_8859_1)), "A3 UTF-8 REPLACE lone surrogate -> a?b");
        CharsetEncoder malIgn = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.IGNORE);
        check("ab".equals(new String(bytesOf(malIgn.encode(CharBuffer.wrap("a\ud800b"))),
                StandardCharsets.ISO_8859_1)), "A3 UTF-8 IGNORE lone surrogate -> ab");
        CharsetEncoder isoRep = StandardCharsets.ISO_8859_1.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        check("?".equals(new String(bytesOf(isoRep.encode(CharBuffer.wrap("中"))),
                StandardCharsets.ISO_8859_1)), "A3 ISO REPLACE 中 -> ?");
        CharsetEncoder isoIgn = StandardCharsets.ISO_8859_1.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.IGNORE);
        check("".equals(new String(bytesOf(isoIgn.encode(CharBuffer.wrap("中"))),
                StandardCharsets.ISO_8859_1)), "A3 ISO IGNORE 中 -> empty");
    }

    static void testUtf8ByteLayoutA3() {
        // Exact UTF-8 byte lengths per character class and exact byte values.
        check(bytesOf(StandardCharsets.UTF_8.encode("A")).length == 1, "A3 UTF-8 'A' = 1 byte");
        check(bytesOf(StandardCharsets.UTF_8.encode("é")).length == 2, "A3 UTF-8 'é' = 2 bytes");
        check(bytesOf(StandardCharsets.UTF_8.encode("中")).length == 3, "A3 UTF-8 '中' = 3 bytes");
        check(bytesOf(StandardCharsets.UTF_8.encode("😀")).length == 4, "A3 UTF-8 emoji = 4 bytes");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_8.encode("é")), new byte[]{(byte) 0xC3, (byte) 0xA9}),
              "A3 UTF-8 'é' = C3 A9");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_8.encode("€")), new byte[]{(byte) 0xE2, (byte) 0x82, (byte) 0xAC}),
              "A3 UTF-8 '€' = E2 82 AC");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_8.encode("中")), new byte[]{(byte) 0xE4, (byte) 0xB8, (byte) 0xAD}),
              "A3 UTF-8 '中' = E4 B8 AD");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_8.encode("😀")),
              new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}), "A3 UTF-8 emoji = F0 9F 98 80");
        byte[] mix = bytesOf(StandardCharsets.UTF_8.encode("Aé中😀"));
        check(mix.length == 10, "A3 UTF-8 mixed length 10 (1+2+3+4)");
        check(Arrays.equals(mix, new byte[]{0x41, (byte) 0xC3, (byte) 0xA9, (byte) 0xE4, (byte) 0xB8,
              (byte) 0xAD, (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80}), "A3 UTF-8 mixed exact bytes");
    }

    static void testUtf16LayoutA3() {
        // UTF-16BE/LE emit no BOM; the default-named UTF-16 emits a big-endian BOM first.
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16BE.encode("A")), new byte[]{0x00, 0x41}),
              "A3 UTF-16BE 'A' = 00 41");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16LE.encode("A")), new byte[]{0x41, 0x00}),
              "A3 UTF-16LE 'A' = 41 00");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16.encode("A")), new byte[]{(byte) 0xFE, (byte) 0xFF, 0x00, 0x41}),
              "A3 UTF-16 'A' = FE FF 00 41");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16BE.encode("中")), new byte[]{(byte) 0x4E, (byte) 0x2D}),
              "A3 UTF-16BE '中' = 4E 2D");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16LE.encode("中")), new byte[]{(byte) 0x2D, (byte) 0x4E}),
              "A3 UTF-16LE '中' = 2D 4E");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16.encode("中")), new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0x4E, (byte) 0x2D}),
              "A3 UTF-16 '中' = FE FF 4E 2D");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16BE.encode("😀")), new byte[]{(byte) 0xD8, 0x3D, (byte) 0xDE, 0x00}),
              "A3 UTF-16BE emoji = D8 3D DE 00");
        check(Arrays.equals(bytesOf(StandardCharsets.UTF_16LE.encode("😀")), new byte[]{0x3D, (byte) 0xD8, 0x00, (byte) 0xDE}),
              "A3 UTF-16LE emoji = 3D D8 00 DE");
    }

    static void testConfigurationA3() {
        CodingErrorAction[] acts = {CodingErrorAction.REPORT, CodingErrorAction.REPLACE, CodingErrorAction.IGNORE};
        for (CodingErrorAction a : acts) {
            CharsetEncoder e = StandardCharsets.UTF_8.newEncoder();
            check(e.onMalformedInput(a) == e, "A3 onMalformedInput returns this " + a);
            check(e.malformedInputAction() == a, "A3 malformed getter " + a);
            check(e.onUnmappableCharacter(a) == e, "A3 onUnmappableCharacter returns this " + a);
            check(e.unmappableCharacterAction() == a, "A3 unmappable getter " + a);
        }
        // Default replacement bytes, probed identical on JDK-8/JDK-27: single-byte charsets use
        // '?', the UTF-16 family uses U+FFFD in its own byte order.
        byte[][] reps = {{0x3F}, {0x3F}, {0x3F}, {(byte) 0xFF, (byte) 0xFD}, {(byte) 0xFD, (byte) 0xFF}, {(byte) 0xFF, (byte) 0xFD}};
        for (int i = 0; i < SIX_A3.length; i++) {
            check(Arrays.equals(SIX_A3[i].newEncoder().replacement(), reps[i]),
                  "A3 default replacement bytes " + SIX_A3[i].name());
        }
        // replaceWith validation: non-null, non-empty, <= maxBytesPerChar and a legal replacement
        // for this charset (byte 0x80 alone is not valid UTF-8 -> IAE).
        checkThrows(IllegalArgumentException.class, () -> StandardCharsets.US_ASCII.newEncoder().replaceWith(null),
              "A3 replaceWith(null) IAE");
        checkThrows(IllegalArgumentException.class, () -> StandardCharsets.US_ASCII.newEncoder().replaceWith(new byte[0]),
              "A3 replaceWith(empty) IAE");
        checkThrows(IllegalArgumentException.class,
              () -> StandardCharsets.UTF_8.newEncoder().replaceWith(new byte[]{(byte) 0x80}),
              "A3 replaceWith illegal UTF-8 bytes IAE");
        checkThrows(IllegalArgumentException.class,
              () -> StandardCharsets.US_ASCII.newEncoder().replaceWith(new byte[]{'X', 'Y'}),
              "A3 replaceWith longer than maxBytesPerChar IAE");
        check(StandardCharsets.UTF_8.newEncoder().isLegalReplacement(new byte[]{'?'}),
              "A3 isLegalReplacement('?') true");
        check(!StandardCharsets.UTF_8.newEncoder().isLegalReplacement(new byte[]{(byte) 0x80}),
              "A3 isLegalReplacement(0x80) false");
        // canEncode boundaries.
        CharsetEncoder ae = StandardCharsets.US_ASCII.newEncoder();
        check(ae.canEncode('\u007F') && !ae.canEncode('\u0080'), "A3 ASCII canEncode \\u007F yes / \\u0080 no");
        CharsetEncoder ie = StandardCharsets.ISO_8859_1.newEncoder();
        check(ie.canEncode('\u00FF') && !ie.canEncode('\u0100'), "A3 ISO canEncode \\u00FF yes / \\u0100 no");
        check(ae.canEncode(""), "A3 canEncode empty sequence true");
        checkThrows(NullPointerException.class, () -> ae.canEncode((CharSequence) null),
              "A3 canEncode(null) NPE");
        // Metrics: probed constants of the six standard charsets (identical JDK-8/JDK-27).
        float[][] enc = {{1.0f, 1.0f}, {1.0f, 1.0f}, {1.1f, 3.0f},
                         {2.0f, 2.0f}, {2.0f, 2.0f}, {2.0f, 4.0f}};
        for (int i = 0; i < SIX_A3.length; i++) {
            CharsetEncoder e = SIX_A3[i].newEncoder();
            check(e.averageBytesPerChar() == enc[i][0], "A3 enc avg " + SIX_A3[i].name() + " == " + enc[i][0]);
            check(e.maxBytesPerChar() == enc[i][1], "A3 enc max " + SIX_A3[i].name() + " == " + enc[i][1]);
            check(e.maxBytesPerChar() >= e.averageBytesPerChar() && e.averageBytesPerChar() > 0,
                  "A3 enc metric invariant " + SIX_A3[i].name());
        }
    }

    static void testStateMachineA3() throws Exception {
        CharBuffer in = CharBuffer.wrap("hi");
        ByteBuffer out = ByteBuffer.allocate(16);
        // flush() before any encoding step -> IllegalStateException.
        checkThrows(IllegalStateException.class, () -> StandardCharsets.UTF_8.newEncoder().flush(out),
                    "A3 flush(fresh) ISE");
        // endOfInput must be non-decreasing: encode(true) then encode(false) -> ISE.
        CharsetEncoder e2 = StandardCharsets.UTF_8.newEncoder();
        e2.encode(in.duplicate(), out, true);
        checkThrows(IllegalStateException.class, () -> e2.encode(in.duplicate(), out, false),
                    "A3 encode(true then false) ISE");
        // Repeated flush() after a completed encode keeps returning UNDERFLOW.
        CharsetEncoder e3 = StandardCharsets.UTF_8.newEncoder();
        e3.encode(in.duplicate(), out, true);
        check(e3.flush(out).isUnderflow(), "A3 flush#1 underflow");
        check(e3.flush(out).isUnderflow(), "A3 flush#2 underflow");
        // encode() after flush without reset() -> ISE; reset() returns this and re-arms the encoder.
        checkThrows(IllegalStateException.class, () -> e3.encode(in.duplicate(), out, true),
                    "A3 encode after flush ISE");
        check(e3.reset() == e3, "A3 reset returns this");
        check(e3.encode(in.duplicate(), out, true).isUnderflow(), "A3 encode after reset ok");
    }

    static void testSubclassCtorFlushA3() throws Exception {
        // Both declared constructors (3-arg and 4-arg with explicit replacement) are protected.
        int protectedCtors = 0;
        for (java.lang.reflect.Constructor<?> k : CharsetEncoder.class.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers())) protectedCtors++;
        }
        check(protectedCtors >= 2, "A3 CharsetEncoder protected ctors incl. 4-arg replacement variant");
        // Subclass constructor round-trip.
        MetricEncoder me = new MetricEncoder(StandardCharsets.UTF_8, 1f, 3f);
        check(me.charset() == StandardCharsets.UTF_8, "A3 subclass charset round-trip");
        check(me.averageBytesPerChar() == 1.0f && me.maxBytesPerChar() == 3.0f, "A3 subclass metrics round-trip");
        // Constructor precondition matrix: avg<=0, max<=0 or max<avg -> IllegalArgumentException.
        float[][] bad = {{0f, 1f}, {1f, 0f}, {-1f, 1f}, {1f, -1f}, {0.5f, 0.25f}};
        for (float[] b : bad) {
            final float av = b[0], mx = b[1];
            checkThrows(IllegalArgumentException.class,
                  () -> new MetricEncoder(StandardCharsets.US_ASCII, av, mx),
                  "A3 encoder ctor IAE (" + av + "," + mx + ")");
        }
        check(new MetricEncoder(StandardCharsets.US_ASCII, 1f, 1f).maxBytesPerChar() == 1.0f, "A3 ctor (1,1) ok");
        // flush() framework via a custom encoder whose implFlush emits a 4-byte tail.
        // Verified against the OpenJDK state machine: while implFlush reports OVERFLOW the coder
        // stays in the end state, so encode(...,true) remains legal; once a flush reports
        // UNDERFLOW the coder is flushed and only reset() re-arms it.
        TailEncoder te = new TailEncoder(StandardCharsets.US_ASCII, 1f, 1f);
        checkThrows(IllegalStateException.class, () -> te.flush(ByteBuffer.allocate(8)), "A3 tail flush(fresh) ISE");
        ByteBuffer tout = ByteBuffer.allocate(3);
        check(te.encode(CharBuffer.wrap("ab"), tout, true).isUnderflow(), "A3 tail encode ab underflow");
        tout.flip();
        check(tout.remaining() == 2 && tout.get(0) == 'a' && tout.get(1) == 'b', "A3 tail encoded 'ab'");
        check(te.flush(tout).isOverflow(), "A3 tail flush too-small OVERFLOW");
        check(te.encode(CharBuffer.wrap("c"), ByteBuffer.allocate(4), true).isUnderflow(),
              "A3 encode(true) still legal while flush pending");
        ByteBuffer big = ByteBuffer.allocate(8);
        big.put(tout);
        check(te.flush(big).isUnderflow(), "A3 tail flush retry UNDERFLOW");
        big.flip();
        check(Arrays.equals(bytesOf(big), new byte[]{'a', 'b', (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, (byte) 0xEA}),
              "A3 tail flush emits a b E7 E8 E9 EA");
        checkThrows(IllegalStateException.class,
              () -> te.encode(CharBuffer.wrap("x"), ByteBuffer.allocate(4), true),
              "A3 encode after completed flush ISE");
        // An already-completed flush is idempotent: returns UNDERFLOW without re-running implFlush
        // (a 1-byte buffer would overflow if implFlush ran again).
        check(te.flush(ByteBuffer.allocate(1)).isUnderflow(), "A3 already-flushed flush UNDERFLOW");
        te.reset();
        ByteBuffer full = ByteBuffer.allocate(16);
        check(te.encode(CharBuffer.wrap("ab"), full, true).isUnderflow(), "A3 tail after reset encode");
        check(te.flush(full).isUnderflow(), "A3 tail after reset flush");
        full.flip();
        check(Arrays.equals(bytesOf(full), new byte[]{'a', 'b', (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, (byte) 0xEA}),
              "A3 tail full pipeline after reset");
    }

    static void testManualLoopA3() throws Exception {
        // Manual incremental encode with a 4-byte output buffer (atomic write unit: UTF-8 surrogate
        // pair = 4 bytes) equals the convenience encode, byte for byte.
        String s = "Aé中😀ok";
        for (String cn : new String[] {"UTF-8", "US-ASCII"}) {
            Charset cs = Charset.forName(cn);
            byte[] convArr = bytesOf(cs.encode(s));
            CharsetEncoder lo = cs.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            lo.reset();
            ByteBuffer accum = ByteBuffer.allocate(convArr.length + 16);
            CharBuffer in = CharBuffer.wrap(s);
            boolean done = false;
            while (!done) {
                ByteBuffer tiny = ByteBuffer.allocate(4);
                CoderResult r = lo.encode(in, tiny, true);
                tiny.flip();
                accum.put(tiny);
                if (r.isOverflow()) continue;
                if (r.isUnderflow()) done = true;
                else r.throwException();
            }
            ByteBuffer fl = ByteBuffer.allocate(8);
            lo.flush(fl);
            fl.flip();
            accum.put(fl);
            accum.flip();
            check(Arrays.equals(convArr, bytesOf(accum)), "A3 manual loop == convenience " + cn);
        }
        // ISO-8859-1 encoding is the identity map for chars U+0000..U+00FF.
        char[] chars = new char[256];
        for (int i = 0; i < 256; i++) chars[i] = (char) i;
        ByteBuffer e = StandardCharsets.ISO_8859_1.encode(CharBuffer.wrap(chars));
        boolean encId = e.remaining() == 256;
        for (int i = 0; i < 256 && e.hasRemaining(); i++) if (e.get() != (byte) i) encId = false;
        check(encId, "A3 ISO-8859-1 encode U+0000..U+00FF identity");
    }

    static class MetricEncoder extends CharsetEncoder {
        MetricEncoder(Charset cs, float avg, float max) { super(cs, avg, max); }
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) { return CoderResult.UNDERFLOW; }
    }

    static class TailEncoder extends CharsetEncoder {
        TailEncoder(Charset cs, float avg, float max) { super(cs, avg, max); }
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.hasRemaining() && out.hasRemaining()) out.put((byte) in.get());
            return in.hasRemaining() ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
        }
        protected CoderResult implFlush(ByteBuffer out) {
            if (out.remaining() < 4) return CoderResult.OVERFLOW;
            out.put((byte) 0xE7).put((byte) 0xE8).put((byte) 0xE9).put((byte) 0xEA);
            return CoderResult.UNDERFLOW;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnmappableMatrixA3();
        testMalformedSurrogatesA3();
        testActionsA3();
        testUtf8ByteLayoutA3();
        testUtf16LayoutA3();
        testConfigurationA3();
        testStateMachineA3();
        testSubclassCtorFlushA3();
        testManualLoopA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

