/*
 * @test
 * @summary java.nio.charset.StandardCharsets conformance: white-box structure (final class,
 *          six public static final Charset fields) plus black-box names, singletons, and
 *          byte layouts for UTF-16BE/LE plus UTF-8 roundtrip with surrogate pairs.
 * @run main StandardCharsetsTest
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.StandardCharsets");
        check(Modifier.isFinal(c.getModifiers()), "StandardCharsets is final");
        check(!c.isInterface(), "StandardCharsets is a class");
        check(c.getSuperclass() == Object.class, "extends Object");

        String[] names = {"US_ASCII", "ISO_8859_1", "UTF_8",
                          "UTF_16BE", "UTF_16LE", "UTF_16"};
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
        };
        String[] names = {
            "US-ASCII", "ISO-8859-1", "UTF-8",
            "UTF-16BE", "UTF-16LE", "UTF-16",
        };
        check(all.length == 6 && names.length == 6, "six constants");
        for (int i = 0; i < all.length; i++) {
            check(names[i].equals(all[i].name()), "name of " + names[i]);
            check(all[i] == Charset.forName(names[i]), "singleton == forName " + names[i]);
        }
        check(StandardCharsets.UTF_8 != StandardCharsets.UTF_16BE, "distinct charsets");
        check(StandardCharsets.UTF_16BE != StandardCharsets.UTF_16LE, "BE != LE");
        check(StandardCharsets.UTF_16 != StandardCharsets.UTF_16BE, "UTF_16 != UTF_16BE");

        ByteBuffer be16 = StandardCharsets.UTF_16BE.encode("A");
        check(be16.remaining() == 2, "UTF-16BE encodes 'A' as 2 bytes");
        check(be16.get() == 0x00 && be16.get() == 0x41, "UTF-16BE big-endian layout");

        ByteBuffer le16 = StandardCharsets.UTF_16LE.encode("A");
        check(le16.remaining() == 2, "UTF-16LE encodes 'A' as 2 bytes");
        check(le16.get() == 0x41 && le16.get() == 0x00, "UTF-16LE little-endian layout");

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
