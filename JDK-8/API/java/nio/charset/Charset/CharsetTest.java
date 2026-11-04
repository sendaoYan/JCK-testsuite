/*
 * @test
 * @summary java.nio.charset.Charset conformance: white-box structure (abstract, implements
 *          Comparable, static factories) plus black-box lookup (forName/isSupported/
 *          availableCharsets), identity, contains, encode/decode roundtrip
 *          incl. CJK and surrogate pairs, and exceptions.
 * @run main CharsetTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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
        padded.put((byte) 'X');
        ByteBuffer okBytes = utf8.encode("ok");
        padded.put(okBytes);
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

    // ============ A3 batch appended: contract deepening (lookup matrix / contains table /
    // ============ convenience REPLACE semantics / identity / ordering / manual loop) ============

    static final Charset[] SIX = {
        StandardCharsets.US_ASCII, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,
        StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16,
    };

    static void checkThrowsExact(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t.getClass() == type) { passed++; return; }
            throw new AssertionError(msg + " (expected exactly " + type.getName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void testAliasMatrixA3() {
        String[][] m = {
            {"UTF8", "UTF-8"}, {"utf-8", "UTF-8"}, {"unicode-1-1-utf-8", "UTF-8"},
            {"ISO8859_1", "ISO-8859-1"}, {"latin1", "ISO-8859-1"}, {"ISO_8859_1", "ISO-8859-1"},
            {"8859_1", "ISO-8859-1"},
            {"ascii", "US-ASCII"}, {"ANSI_X3.4-1968", "US-ASCII"}, {"ISO646-US", "US-ASCII"},
            {"UnicodeBigUnmarked", "UTF-16BE"}, {"UnicodeLittleUnmarked", "UTF-16LE"},
            {"UnicodeBig", "UTF-16"}, {"unicode", "UTF-16"}, {"utf16", "UTF-16"},
        };
        for (String[] p : m) {
            Charset cs = Charset.forName(p[0]);
            check(p[1].equals(cs.name()), "A3 alias " + p[0] + " -> " + p[1]);
            check(cs == Charset.forName(p[1]), "A3 alias " + p[0] + " resolves to canonical singleton");
        }
    }

    static void testIllegalAndUnknownNamesA3() {
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName(""), "A3 forName(\"\")");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName(" "), "A3 forName(space)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("bad name"), "A3 forName(bad name)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("-bad"), "A3 forName(-bad)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("a#b"), "A3 forName(a#b)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("a/b"), "A3 forName(a/b)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("a?"), "A3 forName(a?)");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.forName("a%b"), "A3 forName(a%b)");
        // IllegalCharsetNameException carries the offending name for round-trip inspection.
        try {
            Charset.forName("bad name");
            check(false, "A3 forName(bad name) must throw");
        } catch (IllegalCharsetNameException e) {
            check("bad name".equals(e.getCharsetName()), "A3 ICE getCharsetName round-trip");
        }
        // An illegal name makes isSupported throw the same ICE (verified on JDK-8 and JDK-27).
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.isSupported(""), "A3 isSupported(\"\") throws ICE");
        checkThrowsExact(IllegalCharsetNameException.class, () -> Charset.isSupported("a#b"), "A3 isSupported(a#b) throws ICE");
        // Legal-but-unknown names (RFC-2278 charset naming allows letters/digits/'-'/':'/'_'/'.'):
        // ':' and a leading digit are legal name characters, so these are unknown, not illegal.
        checkThrowsExact(UnsupportedCharsetException.class, () -> Charset.forName("a:b"), "A3 forName(a:b) unknown");
        checkThrowsExact(UnsupportedCharsetException.class, () -> Charset.forName("1abc"), "A3 forName(1abc) unknown");
        check(!Charset.isSupported("a:b"), "A3 isSupported(a:b) false");
        check(!Charset.isSupported("1abc"), "A3 isSupported(1abc) false");
        try {
            Charset.forName("zzz-no-such-a3");
            check(false, "A3 forName(unknown) must throw");
        } catch (UnsupportedCharsetException e) {
            check("zzz-no-such-a3".equals(e.getCharsetName()), "A3 UCE getCharsetName round-trip");
        }
        // null name -> plain IllegalArgumentException (not ICE), verified on both sides.
        checkThrowsExact(IllegalArgumentException.class, () -> Charset.forName(null), "A3 forName(null) plain IAE");
        checkThrowsExact(IllegalArgumentException.class, () -> Charset.isSupported(null), "A3 isSupported(null) plain IAE");
    }

    static void testAvailableCharsetsA3() {
        SortedMap<String, Charset> avail = Charset.availableCharsets();
        // JDK-8 = 171, JDK-27 = 173 on this toolchain; assert a stable lower bound only.
        check(avail.size() >= 150, "A3 availableCharsets size >= 150 (JDK-8=171/JDK-27=173)");
        for (Charset c : SIX) {
            check(avail.containsKey(c.name()), "A3 avail contains " + c.name());
            check(avail.get(c.name()) == Charset.forName(c.name()), "A3 avail value is singleton for " + c.name());
        }
        // The map is a case-insensitive sorted map: iteration is ascending ignoring key case.
        String prev = null;
        boolean ciAscending = true;
        for (String k : avail.keySet()) {
            if (prev != null && prev.compareToIgnoreCase(k) >= 0) ciAscending = false;
            prev = k;
        }
        check(ciAscending, "A3 availableCharsets iterates case-insensitively ascending");
        // Case-insensitive ordering implies no two keys differing only by case can coexist.
        Set<String> seen = new HashSet<>();
        boolean caseDuplicate = false;
        for (String k : avail.keySet()) {
            if (!seen.add(k.toLowerCase(Locale.ROOT))) caseDuplicate = true;
        }
        check(!caseDuplicate, "A3 no two keys differing only by case");
    }

    static void testContainsMatrixA3() {
        // Probed contains() table of the six standard charsets (identical on JDK-8 and JDK-27).
        boolean[][] exp = {
            //            ASCII   ISO    UTF-8  UTF16BE LE     UTF-16
            /* ASCII  */ {true,  false, false, false, false, false},
            /* ISO    */ {true,  true,  false, false, false, false},
            /* UTF-8  */ {true,  true,  true,  true,  true,  true},
            /* UTF16BE*/ {true,  true,  true,  true,  true,  true},
            /* LE     */ {true,  true,  true,  true,  true,  true},
            /* UTF-16 */ {true,  true,  true,  true,  true,  true},
        };
        for (int i = 0; i < SIX.length; i++)
            for (int j = 0; j < SIX.length; j++)
                check(SIX[i].contains(SIX[j]) == exp[i][j],
                      "A3 contains " + SIX[i].name() + " contains " + SIX[j].name()
                      + " == " + exp[i][j]);
    }

    static void testConvenienceCodingA3() throws Exception {
        // Charset.decode(ByteBuffer) convenience decodes with REPLACE by default: malformed -> U+FFFD.
        CharBuffer c = Charset.forName("UTF-8")
                .decode(ByteBuffer.wrap(new byte[]{0x41, (byte) 0xC3, 0x28, 0x42}));
        check(c.length() == 4, "A3 conv decode malformed length 4");
        check(c.charAt(0) == 'A' && c.charAt(1) == '�' && c.charAt(2) == '(' && c.charAt(3) == 'B',
              "A3 conv decode malformed -> A U+FFFD ( B");
        // Charset.encode(String) convenience encodes with REPLACE by default: unmappable -> '?'.
        ByteBuffer e = StandardCharsets.US_ASCII.encode("a中b");
        check(e.remaining() == 3 && e.get() == 'a' && e.get() == '?' && e.get() == 'b',
              "A3 conv encode unmappable -> a?b");
        // Manual encoder loop with a small output buffer must equal the convenience bytes.
        // The tiny buffer must cover the encoder's atomic write unit (UTF-8 surrogate pair = 4
        // bytes; UTF-16 BOM unit = 2 bytes), otherwise OVERFLOW makes no progress and loops forever.
        String s = "Aé中😀ok";
        for (String cn : new String[] {"UTF-8", "US-ASCII", "UTF-16"}) {
            Charset cs = Charset.forName(cn);
            ByteBuffer conv = cs.encode(s);
            byte[] convArr = new byte[conv.remaining()];
            conv.get(convArr);
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
            byte[] manArr = new byte[accum.remaining()];
            accum.get(manArr);
            check(Arrays.equals(convArr, manArr), "A3 manual loop encode == convenience for " + cn);
        }
        // UTF-8 exact byte layout for the mixed string (1+2+3+4 = 10 bytes).
        ByteBuffer mix = StandardCharsets.UTF_8.encode("Aé中😀");
        byte[] mixArr = new byte[mix.remaining()];
        mix.get(mixArr);
        byte[] exp8 = {0x41, (byte) 0xC3, (byte) 0xA9, (byte) 0xE4, (byte) 0xB8, (byte) 0xAD,
                       (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x80};
        check(mixArr.length == 10, "A3 UTF-8 mixed length 10 (1+2+3+4)");
        check(Arrays.equals(mixArr, exp8), "A3 UTF-8 mixed exact bytes");
        // UTF-16 variants: BE/LE emit no BOM; default-named UTF-16 emits FE FF first.
        ByteBuffer be = StandardCharsets.UTF_16BE.encode("A");
        check(be.remaining() == 2 && be.get() == 0x00 && be.get() == 0x41, "A3 UTF-16BE 'A' = 00 41 (no BOM)");
        ByteBuffer le = StandardCharsets.UTF_16LE.encode("A");
        check(le.remaining() == 2 && le.get() == 0x41 && le.get() == 0x00, "A3 UTF-16LE 'A' = 41 00 (no BOM)");
        ByteBuffer d16 = StandardCharsets.UTF_16.encode("A");
        check(d16.remaining() == 4, "A3 UTF-16 'A' = 4 bytes with BOM");
        check(d16.get() == (byte) 0xFE && d16.get() == (byte) 0xFF && d16.get() == 0x00 && d16.get() == 0x41,
              "A3 UTF-16 'A' = FE FF 00 41");
    }

    static void testIdentityAndDisplayA3() {
        // displayName ignores the locale for standard charsets (and tolerates null).
        check("UTF-8".equals(StandardCharsets.UTF_8.displayName(Locale.FRENCH)), "A3 displayName(FRENCH)");
        check("UTF-16".equals(StandardCharsets.UTF_16.displayName(Locale.ROOT)), "A3 displayName(ROOT)");
        check("UTF-8".equals(StandardCharsets.UTF_8.displayName(null)), "A3 displayName(null) tolerated");
        for (Charset c : SIX) {
            check(c.canEncode(), "A3 canEncode " + c.name());
            check(c.isRegistered(), "A3 isRegistered " + c.name());
            check(c.toString().equals(c.name()), "A3 toString == name " + c.name());
        }
        // equals/hashCode normalize name case and aliases.
        Charset byCase = Charset.forName("utf-8");
        check(StandardCharsets.UTF_8.equals(byCase) && byCase.equals(StandardCharsets.UTF_8),
              "A3 equals symmetric across case");
        check(StandardCharsets.UTF_8.hashCode() == byCase.hashCode(), "A3 hashCode case-normalized");
        Charset byAlias = Charset.forName("unicode");
        check(StandardCharsets.UTF_16.equals(byAlias) && byAlias.hashCode() == StandardCharsets.UTF_16.hashCode(),
              "A3 equals/hashCode alias-normalized");
        check(!StandardCharsets.UTF_8.equals(null), "A3 equals(null) false");
        check(!StandardCharsets.UTF_8.equals("UTF-8"), "A3 equals(String) false");
        // compareTo orders by canonical name (signs match the name String comparison).
        check(StandardCharsets.ISO_8859_1.compareTo(StandardCharsets.US_ASCII) < 0, "A3 ISO-8859-1 < US-ASCII");
        check(StandardCharsets.US_ASCII.compareTo(StandardCharsets.UTF_8) < 0, "A3 US-ASCII < UTF-8");
        check(StandardCharsets.UTF_8.compareTo(StandardCharsets.US_ASCII) > 0, "A3 UTF-8 > US-ASCII");
        check(Integer.signum(StandardCharsets.UTF_16BE.compareTo(StandardCharsets.UTF_16LE))
                      == Integer.signum("UTF-16BE".compareTo("UTF-16LE")),
              "A3 compareTo sign matches canonical-name order");
        // Factory methods return a fresh coder object on every call, defaulting to REPORT.
        check(StandardCharsets.UTF_8.newDecoder() != StandardCharsets.UTF_8.newDecoder(), "A3 newDecoder fresh object");
        check(StandardCharsets.UTF_8.newEncoder() != StandardCharsets.UTF_8.newEncoder(), "A3 newEncoder fresh object");
        CharsetDecoder nd = StandardCharsets.UTF_8.newDecoder();
        check(nd.malformedInputAction() == CodingErrorAction.REPORT
              && nd.unmappableCharacterAction() == CodingErrorAction.REPORT, "A3 newDecoder defaults REPORT");
        CharsetEncoder ne = StandardCharsets.UTF_8.newEncoder();
        check(ne.malformedInputAction() == CodingErrorAction.REPORT
              && ne.unmappableCharacterAction() == CodingErrorAction.REPORT, "A3 newEncoder defaults REPORT");
        // defaultCharset resolves back through the lookup machinery as a singleton.
        Charset dc = Charset.defaultCharset();
        check(dc != null && Charset.isSupported(dc.name()), "A3 defaultCharset name is supported");
        check(dc == Charset.forName(dc.name()), "A3 defaultCharset is singleton");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAliasMatrixA3();
        testIllegalAndUnknownNamesA3();
        testAvailableCharsetsA3();
        testContainsMatrixA3();
        testConvenienceCodingA3();
        testIdentityAndDisplayA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
