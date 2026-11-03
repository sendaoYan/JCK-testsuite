/*
 * @test
 * @summary java.nio.charset.Charset conformance: white-box structure (abstract, implements
 *          Comparable, static factories) plus black-box lookup (forName/isSupported/
 *          availableCharsets/fallback), identity, contains, encode/decode roundtrip
 *          incl. CJK and surrogate pairs, and exceptions.
 * @run main CharsetTest
 *
 * 黑盒设计案:
 * 1. 契约: Charset 描述字符集与字节序列之间的编解码; 名字大小写不敏感, 标准字符集
 *    isRegistered()==true; availableCharsets() 返回按名字排序的 SortedMap.
 * 2. 场景: 用 Charset.forName("UTF-8") 取编码器 -> encode(String) 得 ByteBuffer ->
 *    decode(ByteBuffer) 还原 CharBuffer, 覆盖 ASCII/CJK/代理对 emoji.
 * 3. 组合链路: forName(大小写/别名) -> encode -> decode 往返; forName(name,fallback) 在
 *    找不到时回落; equals/hashCode/compareTo 基于规范名.
 * 4. 不变量: 相同规范名 (大小写/别名归一化) 得到同一逻辑 Charset (equals/hashCode 一致);
 *    contains() 描述字符集子集关系; encode/decode 往返保持字符串不变.
 * 5. 边界/异常: forName 未知抛 UnsupportedCharsetException; 空/非法名字抛
 *    IllegalCharsetNameException; Charset.encode(String) 对不可映射字符用 REPLACE 替换为
 *    '?' (不抛), 而 newEncoder().encode(CharBuffer) (REPORT) 抛 CharacterCodingException.
 */
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.SortedMap;

public class CharsetTest {
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

    static boolean implementsInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.Charset");
        check(Modifier.isAbstract(c.getModifiers()), "Charset is abstract");
        check(c.getSuperclass() == Object.class, "Charset extends Object");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");

        check(hasMethod(c, "forName", String.class), "has forName(String)");
        check(hasMethod(c, "forName", String.class, Charset.class), "has forName(String,Charset)");
        check(hasMethod(c, "isSupported", String.class), "has isSupported(String)");
        check(hasMethod(c, "availableCharsets"), "has availableCharsets()");
        check(hasMethod(c, "defaultCharset"), "has defaultCharset()");
        check(hasMethod(c, "name"), "has name()");
        check(hasMethod(c, "aliases"), "has aliases()");
        check(hasMethod(c, "displayName"), "has displayName()");
        check(hasMethod(c, "isRegistered"), "has isRegistered()");
        check(hasMethod(c, "contains", Charset.class), "has contains(Charset)");
        check(hasMethod(c, "newDecoder"), "has newDecoder()");
        check(hasMethod(c, "newEncoder"), "has newEncoder()");
        check(hasMethod(c, "canEncode"), "has canEncode()");
        check(hasMethod(c, "encode", String.class), "has encode(String)");
        check(hasMethod(c, "encode", CharBuffer.class), "has encode(CharBuffer)");
        check(hasMethod(c, "decode", ByteBuffer.class), "has decode(ByteBuffer)");
    }

    static void testBlackBox() {
        Charset utf8 = Charset.forName("UTF-8");
        check("UTF-8".equals(utf8.name()), "canonical name");
        check("UTF-8".equals(Charset.forName("utf-8").name()), "case-insensitive lookup");
        check("UTF-8".equals(Charset.forName("UTF8").name()), "alias lookup");
        check(utf8.aliases().contains("UTF8"), "aliases contains UTF8");

        check(Charset.isSupported("UTF-8"), "isSupported true");
        check(!Charset.isSupported("no-such-charset"), "isSupported false");

        Charset fallback = Charset.forName("no-such-charset", utf8);
        check(fallback == utf8, "forName fallback");

        SortedMap<String, Charset> avail = Charset.availableCharsets();
        check(avail.size() > 100, "availableCharsets has many entries");
        check(avail.containsKey("UTF-8"), "contains UTF-8");
        check(avail.containsKey("US-ASCII"), "contains US-ASCII");
        check(avail.containsKey("UTF-16"), "contains UTF-16");
        check(avail.containsKey("ISO-8859-1"), "contains ISO-8859-1");

        Charset a = Charset.forName("UTF-8");
        Charset b = Charset.forName("utf8");
        check(a.equals(b), "equals normalizes name");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.compareTo(b) == 0, "compareTo equal");
        check(a.compareTo(StandardCharsets.US_ASCII) != 0, "compareTo distinct");

        check("UTF-8".equals(a.toString()), "toString is name");
        check("UTF-8".equals(a.displayName()), "displayName");
        check(a.isRegistered(), "standard charset registered");
        check(a.contains(a), "contains itself");
        check(a.contains(StandardCharsets.US_ASCII), "UTF-8 contains US-ASCII");
        check(!StandardCharsets.US_ASCII.contains(a), "US-ASCII does not contain UTF-8");
        check(StandardCharsets.UTF_16.contains(StandardCharsets.US_ASCII), "UTF-16 contains US-ASCII");
        check(Charset.defaultCharset() != null, "default charset non-null");

        String ascii = "hello";
        String cjk = "中文测试";
        String emoji = "😀🚀";
        check(ascii.equals(utf8.decode(utf8.encode(ascii)).toString()), "ASCII roundtrip");
        check(cjk.equals(utf8.decode(utf8.encode(cjk)).toString()), "CJK roundtrip");
        check(emoji.equals(utf8.decode(utf8.encode(emoji)).toString()), "surrogate-pair roundtrip");

        ByteBuffer cjkBytes = utf8.encode(cjk);
        check(cjkBytes.remaining() == 12, "4 CJK chars -> 12 UTF-8 bytes");
        check(utf8.canEncode(), "UTF-8 canEncode");

        ByteBuffer padded = ByteBuffer.allocate(10);
        padded.put((byte) 'X').put(utf8.encode("ok"));
        padded.position(1).limit(3);
        CharBuffer decoded = utf8.decode(padded);
        check("ok".contentEquals(decoded), "decode uses remaining region");

        checkThrows(UnsupportedCharsetException.class, () -> Charset.forName("no-such"), "forName unknown");
        checkThrows(IllegalCharsetNameException.class, () -> Charset.forName(""), "forName empty name");
        checkThrows(IllegalCharsetNameException.class, () -> Charset.forName("bad name"), "forName illegal name");
        ByteBuffer asciiCjk = StandardCharsets.US_ASCII.encode("中");
        check(asciiCjk.remaining() == 1 && asciiCjk.get() == (byte) '?',
              "US-ASCII replaces unmappable CJK with '?'");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
