/*
 * @test
 * @summary java.nio.charset.CharsetDecoder conformance: white-box structure (abstract,
 *          key methods) plus black-box charset/metrics, replacement, error actions,
 *          low-level decode+flush, convenience decode, auto-detection surface, malformed.
 * @run main CharsetDecoderTest
 *
 * 黑盒设计案:
 * 1. 契约: CharsetDecoder 把 ByteBuffer 解码为 CharBuffer; 默认 replacement 是 "�";
 *    默认 malformed/unmappable 动作是 REPORT; 非自动检测解码器 isAutoDetecting()==false.
 * 2. 场景: newDecoder() -> onMalformedInput/onUnmappableCharacter 配置 -> reset ->
 *    decode(in,out,true) 多步 + flush -> flip 读字符; 或便捷 decode(ByteBuffer).
 * 3. 组合链路: UTF-8 字节 -> 解码 ASCII/CJK/代理对; 非法 UTF-8 字节用 REPORT 抛异常,
 *    用 REPLACE 替换为 replacement 字符; decode 尊重 ByteBuffer remaining 区间.
 * 4. 不变量: 解码后 charset() 不变; average/maxCharsPerByte 描述解码宽度; replaceWith
 *    校验 replacement 非空; 非自动检测下 isCharsetDetected/detectedCharset 抛异常.
 * 5. 边界/异常: 畸形输入 + REPORT -> 便捷 decode 抛 MalformedInputException
 *    (CharacterCodingException 子类), 底层 decode 返回 CoderResult.isMalformed;
 *    replaceWith(null) 抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
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
        check(c.getInterfaces().length == 0, "implements no interfaces");

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
        padded.put((byte) 'X').put(StandardCharsets.UTF_8.encode("ok"));
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

        CharsetDecoder repl = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE);
        repl.reset();
        CharBuffer rOut = CharBuffer.allocate(8);
        CoderResult rr = repl.decode(ByteBuffer.wrap(bad), rOut, true);
        repl.flush(rOut);
        rOut.flip();
        check(rOut.length() == 2, "malformed replaced then '(' decoded (2 chars)");
        check(rOut.charAt(0) == '�' && rOut.charAt(1) == '(', "replacement char then '('");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
