/*
 * @test
 * @summary java.nio.charset.CharsetEncoder conformance: white-box structure (abstract,
 *          key methods) plus black-box charset/metrics, replacement, error actions,
 *          low-level encode+flush, convenience encode, canEncode, malformed/unmappable.
 * @run main CharsetEncoderTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

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

        CharsetEncoder rep = StandardCharsets.US_ASCII.newEncoder();
        rep.replaceWith(new byte[] {'?'}).onUnmappableCharacter(CodingErrorAction.REPLACE);
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
