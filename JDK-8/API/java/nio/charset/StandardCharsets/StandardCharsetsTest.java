/*
 * @test
 * @summary java.nio.charset.StandardCharsets conformance: white-box structure (final class,
 *          six public static final Charset fields) plus black-box names, singletons, and
 *          byte layouts for UTF-16BE/LE plus UTF-8 roundtrip with surrogate pairs.
 * @run main StandardCharsetsTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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

    // ================ A3 batch appended: alias table, alias->singleton resolution, identity,
    // ================ contains subset, BOM decoding, ISO identity, private-ctor whitebox ================

    static final Charset[] SIX_A3 = {
        StandardCharsets.US_ASCII, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16,
    };

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

    static void testAliasesA3() {
        // Each constant exposes its well-known aliases (iteration order differs across JDKs).
        check(StandardCharsets.UTF_8.aliases().contains("UTF8"), "A3 UTF-8 alias UTF8");
        check(StandardCharsets.UTF_8.aliases().contains("unicode-1-1-utf-8"), "A3 UTF-8 alias unicode-1-1-utf-8");
        check(StandardCharsets.ISO_8859_1.aliases().contains("latin1"), "A3 ISO-8859-1 alias latin1");
        check(StandardCharsets.ISO_8859_1.aliases().contains("ISO_8859_1"), "A3 ISO-8859-1 alias ISO_8859_1");
        check(StandardCharsets.ISO_8859_1.aliases().contains("8859_1"), "A3 ISO-8859-1 alias 8859_1");
        check(StandardCharsets.US_ASCII.aliases().contains("ASCII"), "A3 US-ASCII alias ASCII");
        check(StandardCharsets.US_ASCII.aliases().contains("ISO646-US"), "A3 US-ASCII alias ISO646-US");
        check(StandardCharsets.US_ASCII.aliases().contains("ANSI_X3.4-1968"), "A3 US-ASCII alias ANSI_X3.4-1968");
        check(StandardCharsets.UTF_16BE.aliases().contains("UnicodeBigUnmarked"), "A3 UTF-16BE alias UnicodeBigUnmarked");
        check(StandardCharsets.UTF_16BE.aliases().contains("X-UTF-16BE"), "A3 UTF-16BE alias X-UTF-16BE");
        check(StandardCharsets.UTF_16LE.aliases().contains("UnicodeLittleUnmarked"), "A3 UTF-16LE alias UnicodeLittleUnmarked");
        check(StandardCharsets.UTF_16LE.aliases().contains("X-UTF-16LE"), "A3 UTF-16LE alias X-UTF-16LE");
        check(StandardCharsets.UTF_16.aliases().contains("unicode"), "A3 UTF-16 alias unicode");
        check(StandardCharsets.UTF_16.aliases().contains("utf16"), "A3 UTF-16 alias utf16");
        check(StandardCharsets.UTF_16.aliases().contains("UnicodeBig"), "A3 UTF-16 alias UnicodeBig");
        // The alias sets are unmodifiable snapshots.
        checkThrows(UnsupportedOperationException.class, () -> StandardCharsets.UTF_8.aliases().clear(),
              "A3 aliases() clear UOE");
        checkThrows(UnsupportedOperationException.class, () -> StandardCharsets.UTF_8.aliases().add("X-A3"),
              "A3 aliases() add UOE");
        // The canonical name never appears in the alias set.
        for (Charset c : SIX_A3) {
            check(!c.aliases().contains(c.name()), "A3 aliases exclude canonical name " + c.name());
        }
    }

    static void testAliasSingletonA3() {
        check(Charset.forName("UTF8") == StandardCharsets.UTF_8, "A3 UTF8 resolves to UTF_8 singleton");
        check(Charset.forName("latin1") == StandardCharsets.ISO_8859_1, "A3 latin1 resolves to ISO_8859_1 singleton");
        check(Charset.forName("ascii") == StandardCharsets.US_ASCII, "A3 ascii resolves to US_ASCII singleton");
        check(Charset.forName("UnicodeBigUnmarked") == StandardCharsets.UTF_16BE, "A3 UnicodeBigUnmarked -> UTF_16BE");
        check(Charset.forName("UnicodeLittleUnmarked") == StandardCharsets.UTF_16LE, "A3 UnicodeLittleUnmarked -> UTF_16LE");
        check(Charset.forName("unicode") == StandardCharsets.UTF_16, "A3 unicode resolves to UTF_16 singleton");
    }

    static void testIdentityA3() {
        for (Charset c : SIX_A3) {
            check(c.canEncode(), "A3 canEncode " + c.name());
            check(c.isRegistered(), "A3 isRegistered " + c.name());
            check(c.toString().equals(c.name()), "A3 toString == name " + c.name());
            check(c.newDecoder() != null && c.newEncoder() != null, "A3 coder factories " + c.name());
        }
    }

    static void testContainsA3() {
        check(StandardCharsets.UTF_8.contains(StandardCharsets.US_ASCII), "A3 UTF-8 contains US-ASCII");
        check(StandardCharsets.UTF_8.contains(StandardCharsets.ISO_8859_1), "A3 UTF-8 contains ISO-8859-1");
        check(StandardCharsets.ISO_8859_1.contains(StandardCharsets.US_ASCII), "A3 ISO-8859-1 contains US-ASCII");
        check(!StandardCharsets.US_ASCII.contains(StandardCharsets.UTF_8), "A3 US-ASCII not contains UTF-8");
        check(StandardCharsets.UTF_16.contains(StandardCharsets.UTF_8), "A3 UTF-16 contains UTF-8");
        check(StandardCharsets.UTF_16BE.contains(StandardCharsets.UTF_16LE), "A3 UTF-16BE contains UTF-16LE");
        for (Charset c : SIX_A3) {
            check(c.contains(c), "A3 contains itself " + c.name());
        }
    }

    static void testBomAndLayoutA3() throws Exception {
        // The UTF-16 decoder consumes a BOM of either byte order; UTF-16BE does not.
        check("A".equals(StandardCharsets.UTF_16.decode(
                ByteBuffer.wrap(new byte[]{(byte) 0xFE, (byte) 0xFF, 0x00, 0x41})).toString()),
              "A3 UTF-16 decode strips BE BOM");
        check("A".equals(StandardCharsets.UTF_16.decode(
                ByteBuffer.wrap(new byte[]{(byte) 0xFF, (byte) 0xFE, 0x41, 0x00})).toString()),
              "A3 UTF-16 decode strips LE BOM");
        CharBuffer be = StandardCharsets.UTF_16BE.decode(
                ByteBuffer.wrap(new byte[]{(byte) 0xFE, (byte) 0xFF, 0x00, 0x41}));
        check(be.length() == 2 && be.charAt(0) == '\uFEFF' && be.charAt(1) == 'A',
              "A3 UTF-16BE decode keeps U+FEFF as a char");
        // Euro sign exact UTF-8 bytes.
        ByteBuffer euro = StandardCharsets.UTF_8.encode("\u20AC");
        check(euro.remaining() == 3 && euro.get() == (byte) 0xE2 && euro.get() == (byte) 0x82
              && euro.get() == (byte) 0xAC, "A3 UTF-8 euro = E2 82 AC");
        // ISO-8859-1 decoding is the identity map for all 256 byte values.
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) all[i] = (byte) i;
        CharBuffer cb = StandardCharsets.ISO_8859_1.decode(ByteBuffer.wrap(all));
        boolean identity = cb.length() == 256;
        for (int i = 0; i < 256 && cb.hasRemaining(); i++) if (cb.get() != (char) i) identity = false;
        check(identity, "A3 ISO-8859-1 decode 0..255 identity");
    }

    static void testWhiteBoxA3() throws Exception {
        // Utility class: exactly one constructor, private.
        Constructor<?>[] ctors = StandardCharsets.class.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPrivate(ctors[0].getModifiers()),
              "A3 StandardCharsets single private ctor");
        // JDK-8 carries exactly the six standard constants; the UTF-32 family arrived later
        // (JDK-19+, present on the JDK-27 side which asserts it there).
        boolean hasUtf32 = false;
        for (Field f : StandardCharsets.class.getFields()) {
            if (f.getName().startsWith("UTF_32")) hasUtf32 = true;
        }
        check(!hasUtf32, "A3 JDK-8 has no UTF_32 constants");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAliasesA3();
        testAliasSingletonA3();
        testIdentityA3();
        testContainsA3();
        testBomAndLayoutA3();
        testWhiteBoxA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
