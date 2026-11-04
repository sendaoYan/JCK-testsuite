/*
 * @test
 * @summary java.nio.charset.StandardCharsets conformance: white-box structure (final class,
 *          nine public static final Charset fields) plus black-box names, singletons, and
 *          byte layouts for UTF-16BE/LE/32BE plus UTF-8 roundtrip with surrogate pairs.
 * @run main StandardCharsetsTest
 *
 * 黑盒设计案:
 * 1. 契约: StandardCharsets 预置 9 个标准 Charset 常量 (US_ASCII/ISO_8859_1/UTF_8/
 *    UTF_16BE/UTF_16LE/UTF_16/UTF_32BE/UTF_32LE/UTF_32), 每个都是可注册字符集的单例.
 * 2. 场景: 用户直接取常量编码解码, 无需每次 forName; 不同 UTF 变体决定字节布局
 *    (BOM/大小端), 往返 encode -> decode 必须还原字符串.
 * 3. 组合链路: StandardCharsets.UTF_8.encode(String) -> decode(ByteBuffer) 还原;
 *    UTF_16BE/LE/32BE 编码单字符核对字节序列.
 * 4. 不变量: 每个常量 == Charset.forName(规范名) (单例); name() 与规范名一致;
 *    encode/decode 往返保持 Unicode 字符不变.
 * 5. 边界/异常: US_ASCII.encode(String) 对不可映射字符用 REPLACE 替换为 '?' (如编码 CJK
 *    得到 '?'); 更严格的 REPORT 语义见 CharsetEncoder/CharsetDecoder 用例.
 */
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StandardCharsetsTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.StandardCharsets");
        check(Modifier.isFinal(c.getModifiers()), "StandardCharsets is final");
        check(!c.isInterface(), "StandardCharsets is a class");
        check(c.getSuperclass() == Object.class, "extends Object");

        String[] names = {"US_ASCII", "ISO_8859_1", "UTF_8",
                          "UTF_16BE", "UTF_16LE", "UTF_16",
                          "UTF_32BE", "UTF_32LE", "UTF_32"};
        for (String n : names) {
            Field f = c.getField(n);
            check(f.getType() == Charset.class, n + " is Charset");
            check(Modifier.isPublic(f.getModifiers()), n + " is public");
            check(Modifier.isStatic(f.getModifiers()), n + " is static");
            check(Modifier.isFinal(f.getModifiers()), n + " is final");
        }
    }

    static void testBlackBox() {
        Charset[] all = {
            StandardCharsets.US_ASCII,
            StandardCharsets.ISO_8859_1,
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16BE,
            StandardCharsets.UTF_16LE,
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_32BE,
            StandardCharsets.UTF_32LE,
            StandardCharsets.UTF_32,
        };
        String[] names = {
            "US-ASCII", "ISO-8859-1", "UTF-8",
            "UTF-16BE", "UTF-16LE", "UTF-16",
            "UTF-32BE", "UTF-32LE", "UTF-32",
        };
        check(all.length == 9 && names.length == 9, "nine constants");
        for (int i = 0; i < all.length; i++) {
            check(names[i].equals(all[i].name()), "name of " + names[i]);
            check(all[i] == Charset.forName(names[i]), "singleton == forName " + names[i]);
        }
        check(StandardCharsets.UTF_8 != StandardCharsets.UTF_16BE, "distinct charsets");
        check(StandardCharsets.UTF_16BE != StandardCharsets.UTF_16LE, "BE != LE");
        check(StandardCharsets.UTF_16 != StandardCharsets.UTF_16BE, "UTF_16 != UTF_16BE");
        check(StandardCharsets.UTF_32 != StandardCharsets.UTF_32BE, "UTF_32 != UTF_32BE");

        ByteBuffer be16 = StandardCharsets.UTF_16BE.encode("A");
        check(be16.remaining() == 2, "UTF-16BE encodes 'A' as 2 bytes");
        check(be16.get() == 0x00 && be16.get() == 0x41, "UTF-16BE big-endian layout");

        ByteBuffer le16 = StandardCharsets.UTF_16LE.encode("A");
        check(le16.remaining() == 2, "UTF-16LE encodes 'A' as 2 bytes");
        check(le16.get() == 0x41 && le16.get() == 0x00, "UTF-16LE little-endian layout");

        ByteBuffer be32 = StandardCharsets.UTF_32BE.encode("A");
        check(be32.remaining() == 4, "UTF-32BE encodes 'A' as 4 bytes");
        check(be32.get() == 0 && be32.get() == 0 && be32.get() == 0 && be32.get() == 0x41,
              "UTF-32BE layout");

        ByteBuffer le32 = StandardCharsets.UTF_32LE.encode("A");
        check(le32.get() == 0x41 && le32.get() == 0 && le32.get() == 0 && le32.get() == 0,
              "UTF-32LE layout");

        ByteBuffer utf16 = StandardCharsets.UTF_16.encode("A");
        check(utf16.remaining() == 4, "UTF_16 encodes 'A' with BOM (4 bytes)");
        byte[] bom = new byte[2];
        utf16.get(bom);
        check((bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF)
                || (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE), "UTF_16 emits BOM");

        String emoji = "😀🚀";
        String cjk = "中文测试";
        check(emoji.equals(StandardCharsets.UTF_8.decode(
                    StandardCharsets.UTF_8.encode(emoji)).toString()), "UTF-8 emoji roundtrip");
        check(cjk.equals(StandardCharsets.UTF_8.decode(
                    StandardCharsets.UTF_8.encode(cjk)).toString()), "UTF-8 CJK roundtrip");
        check(cjk.equals(StandardCharsets.UTF_16.decode(
                    StandardCharsets.UTF_16.encode(cjk)).toString()), "UTF-16 CJK roundtrip");

        check(StandardCharsets.ISO_8859_1.newEncoder().canEncode('é'), "ISO-8859-1 encodes é");
        check(!StandardCharsets.US_ASCII.newEncoder().canEncode('é'), "US-ASCII cannot encode é");
        ByteBuffer asciiCjk = StandardCharsets.US_ASCII.encode(cjk);
        check(asciiCjk.remaining() == 4, "US-ASCII replaces 4 CJK chars with 4 '?'");
        while (asciiCjk.hasRemaining()) check(asciiCjk.get() == (byte) '?', "replacement byte is '?'");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
