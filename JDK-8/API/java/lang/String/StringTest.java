/*
 * @test
 * @summary java.lang.String API conformance (Java 8 surface): constructors + boundary
 *          exceptions (NPE/SIOOBE/IOOBE/IAE/UnsupportedEncodingException), null-argument
 *          semantics matrix, equals/hashCode/compareTo contracts (reflexive/symmetric/
 *          transitive/consistent/null), code points (BMP + supplementary + surrogate
 *          boundaries), getChars/getBytes/toCharArray, contentEquals, startsWith/endsWith,
 *          indexOf/lastIndexOf (incl. surrogate + boundary), substring/subSequence/concat,
 *          replace (char/CharSequence/regex), matches/split (limit semantics), case
 *          conversion (Locale + Turkish-I edge), trim/intern/valueOf/copyValueOf, join/
 *          format, and chars()/codePoints() streams.
 * @run main StringTest
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Locale;

public class StringTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void expectSIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- constructors & factories ----

    static void testConstructorMatrix() {
        check("abc".equals(new String("abc")), "String(String)");
        check("ab".equals(new String(new char[]{'a', 'b'})), "String(char[])");
        check("bc".equals(new String(new char[]{'a', 'b', 'c'}, 1, 2)), "String(char[],off,len)");
        check("".equals(new String(new char[0])), "String(char[0])");
        check("".equals(new String(new char[]{'a'}, 0, 0)), "String(char[],0,0) empty");

        // offset/length == length exactly is valid.
        check("ab".equals(new String(new char[]{'a', 'b'}, 0, 2)), "String(char[],0,2) full");
        // copy is defensive: mutating source array must not affect the string.
        char[] src = {'x', 'y'};
        String s = new String(src);
        src[0] = 'z';
        check("xy".equals(s), "String(char[]) copies (defensive)");

        // int[] code points (supplementary spans two chars).
        String cp = new String(new int[]{'a', 0x1D518, 'b'}, 0, 3);
        check(cp.length() == 4, "String(int[],off,len) supplementary length == 4");
        check(cp.codePointAt(1) == 0x1D518, "String(int[],off,len) code point value");

        check("abc".equals(new String(new StringBuilder("abc"))), "String(StringBuilder)");
        check("abc".equals(new String(new StringBuffer("abc"))), "String(StringBuffer)");

        check("abc".equals(new String("abc".getBytes(StandardCharsets.UTF_8), 0, 3,
              StandardCharsets.UTF_8)), "String(byte[],off,len,Charset)");

        // exact exception matrix
        checkThrows(NullPointerException.class, () -> new String((String) null),
                    "String((String)null)");
        checkThrows(NullPointerException.class, () -> new String((char[]) null),
                    "String((char[])null)");
        checkThrows(NullPointerException.class, () -> new String((char[]) null, 0, 0),
                    "String((char[])null,off,len)");
        checkThrows(NullPointerException.class, () -> new String((byte[]) null),
                    "String((byte[])null)");
        checkThrows(NullPointerException.class, () -> new String((byte[]) null, 0, 0,
                    StandardCharsets.UTF_8), "String((byte[])null,off,len,Charset)");
        checkThrows(NullPointerException.class, () -> new String((byte[]) null, "UTF-8"),
                    "String((byte[])null,charsetName)");
        checkThrows(NullPointerException.class, () -> new String((StringBuilder) null),
                    "String((StringBuilder)null)");
        checkThrows(NullPointerException.class, () -> new String((StringBuffer) null),
                    "String((StringBuffer)null)");
        checkThrows(NullPointerException.class, () -> new String((int[]) null, 0, 0),
                    "String((int[])null,off,len)");

        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> new String(new char[]{'a'}, -1, 1), "String(char[],-1,1)");
        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> new String(new char[]{'a'}, 0, 2), "String(char[],0,2)");
        // offset == length with count == 0 is legal (empty).
        check("".equals(new String(new char[]{'a'}, 1, 0)), "String(char[],1,0) empty valid");
        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> new String(new char[]{'a'}, 2, 0), "String(char[],2,0)");

        checkThrows(IndexOutOfBoundsException.class,
                    () -> new String(new byte[]{'a'}, -1, 1, StandardCharsets.UTF_8),
                    "String(byte[],-1,1,Charset)");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new String(new byte[]{'a'}, 0, 2, StandardCharsets.UTF_8),
                    "String(byte[],0,2,Charset)");

        checkThrows(IndexOutOfBoundsException.class,
                    () -> new String(new int[]{'a'}, -1, 1), "String(int[],-1,1)");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new String(new int[]{'a'}, 0, 2), "String(int[],0,2)");
        checkThrows(IllegalArgumentException.class,
                    () -> new String(new int[]{Character.MAX_CODE_POINT + 1}, 0, 1),
                    "String(int[],invalid codepoint)");

        // unknown charset name (String) -> UnsupportedEncodingException (checked)
        try {
            new String(new byte[]{'a'}, "jck-no-such-charset");
            throw new AssertionError("String(byte[],badName): expected UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            passed++;
        }
    }

    static void testLengthCharAt() {
        check("".length() == 0 && "".isEmpty(), "empty string");
        check("abc".length() == 3 && !"abc".isEmpty(), "abc length 3");
        check("abc".charAt(0) == 'a' && "abc".charAt(2) == 'c', "charAt");
        expectSIOOBE(() -> "abc".charAt(-1), "charAt(-1)");
        expectSIOOBE(() -> "abc".charAt(3), "charAt(3)");
        expectSIOOBE(() -> "abc".charAt(4), "charAt(4)");
        // surrogate unit is returned verbatim by charAt (no combining).
        String s = new String(Character.toChars(0x1D518));
        check(s.charAt(0) == Character.highSurrogate(0x1D518), "charAt(high surrogate)");
        check(s.charAt(1) == Character.lowSurrogate(0x1D518), "charAt(low surrogate)");
    }

    static void testCodePoints() {
        // U+1D518 is a supplementary code point (surrogate pair).
        String s = "a" + new String(Character.toChars(0x1D518)) + "b";
        check(s.length() == 4, "supplementary length == 4");
        check(s.codePointAt(0) == 'a', "codePointAt(0)");
        check(s.codePointAt(1) == 0x1D518, "codePointAt(1) == 0x1D518");
        check(s.codePointAt(3) == 'b', "codePointAt(3)");
        check(s.codePointBefore(1) == 'a', "codePointBefore(1)");
        check(s.codePointBefore(3) == 0x1D518, "codePointBefore(3)");
        check(s.codePointBefore(4) == 'b', "codePointBefore(4)");
        check(s.codePointCount(0, 4) == 3, "codePointCount == 3");
        check(s.offsetByCodePoints(0, 1) == 1, "offsetByCodePoints(0,1)");
        check(s.offsetByCodePoints(0, 2) == 3, "offsetByCodePoints(0,2)");
        check(s.offsetByCodePoints(3, 1) == 4, "offsetByCodePoints(3,1)");
        // codePointAt at a low-surrogate index does not combine backwards.
        check(s.codePointAt(2) == Character.lowSurrogate(0x1D518),
              "codePointAt(low-surrogate index) returns surrogate unit");

        expectSIOOBE(() -> "ab".codePointAt(-1), "codePointAt(-1)");
        expectSIOOBE(() -> "ab".codePointAt(2), "codePointAt(len)");
        expectSIOOBE(() -> "ab".codePointBefore(0), "codePointBefore(0)");
        expectSIOOBE(() -> "ab".codePointBefore(3), "codePointBefore(len+1)");
        expectIOOBE(() -> "ab".codePointCount(-1, 1), "codePointCount(-1,1)");
        expectIOOBE(() -> "ab".codePointCount(1, 0), "codePointCount(begin>end)");
        expectIOOBE(() -> "ab".offsetByCodePoints(0, 3), "offsetByCodePoints past end");
        expectIOOBE(() -> "ab".offsetByCodePoints(0, -1), "offsetByCodePoints negative");
    }

    static void testGetCharsBytes() {
        char[] dst = new char[3];
        "abc".getChars(0, 3, dst, 0);
        check(dst[0] == 'a' && dst[1] == 'b' && dst[2] == 'c', "getChars");
        // getChars does not null-terminate; remainder of dst untouched.
        char[] dst2 = new char[4];
        dst2[3] = 'x';
        "abc".getChars(0, 3, dst2, 0);
        check(dst2[3] == 'x', "getChars leaves trailing destination untouched");
        check(Arrays.equals("abc".toCharArray(), new char[]{'a', 'b', 'c'}), "toCharArray");
        // toCharArray is a fresh copy.
        String s = "abc";
        char[] ca = s.toCharArray();
        ca[0] = 'z';
        check("abc".equals(s), "toCharArray returns a fresh copy");

        expectSIOOBE(() -> "abc".getChars(-1, 3, new char[3], 0), "getChars(-1,3)");
        expectSIOOBE(() -> "abc".getChars(0, 4, new char[3], 0), "getChars(0,4)");
        expectSIOOBE(() -> "abc".getChars(2, 1, new char[3], 0), "getChars(2,1)");
        expectIOOBE(() -> "abc".getChars(0, 3, new char[2], 0), "getChars dest too small");
        expectIOOBE(() -> "abc".getChars(0, 3, new char[3], 1), "getChars dest offset overflow");

        byte[] utf8 = "abc".getBytes(StandardCharsets.UTF_8);
        check(Arrays.equals(utf8, new byte[]{'a', 'b', 'c'}), "getBytes(UTF-8)");
        check(new String(utf8, StandardCharsets.UTF_8).equals("abc"), "new String(byte[], Charset)");
        check(new String(utf8, 0, 3, Charset.forName("UTF-8")).equals("abc"),
              "new String(byte[], off, len, Charset)");
        byte[] def = "abc".getBytes();
        check(def.length == 3 && new String(def).equals("abc"), "default charset round-trip");
        // getBytes(String) with a valid name and an unknown name.
        try {
            check(Arrays.equals("abc".getBytes("UTF-8"), utf8), "getBytes(\"UTF-8\")");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        try {
            "abc".getBytes("jck-no-such-charset");
            throw new AssertionError("getBytes(badName): expected UnsupportedEncodingException");
        } catch (java.io.UnsupportedEncodingException expected) {
            passed++;
        }
        // getBytes(Charset) with null Charset -> NPE.
        expectNPE(() -> "abc".getBytes((Charset) null), "getBytes((Charset)null)");
        checkThrows(NullPointerException.class, () -> "abc".getBytes((String) null),
                    "getBytes((String)null)");
    }

    static void testEqualityCompare() {
        check("abc".equals("abc"), "equals");
        check(!"abc".equals("ABC"), "!equals different case");
        check(!"abc".equals((Object) null), "!equals null");
        check(!"abc".equals(Integer.valueOf(123)), "!equals non-String");
        check("abc".equalsIgnoreCase("ABC"), "equalsIgnoreCase");
        check(!"abc".equalsIgnoreCase("abd"), "!equalsIgnoreCase different");
        check(!"abc".equalsIgnoreCase(""), "!equalsIgnoreCase empty false");
        check("".equals(""), "empty equals empty");

        // equals contract: reflexive, symmetric, transitive, consistent.
        check("abc".equals("abc"), "equals reflexive");
        String a = new String("abc"), b = new String("abc"), c = new String("abc");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.equals(b) && b.equals(c) && a.equals(c), "equals transitive");
        check(a.equals(b) && a.equals(b), "equals consistent");
        check(a.hashCode() == b.hashCode(), "equal strings have equal hashCode");
        check(a.hashCode() == a.hashCode(), "hashCode stable");

        // equalsIgnoreCase(null) -> false (not NPE).
        check(!"abc".equalsIgnoreCase(null), "equalsIgnoreCase(null) == false");
        check("".equalsIgnoreCase(""), "empty equalsIgnoreCase empty");

        check("abc".compareTo("abc") == 0, "compareTo equal");
        check("abc".compareTo("abd") < 0, "compareTo < ");
        check("abd".compareTo("abc") > 0, "compareTo > ");
        check("abc".compareTo("ab") == 1, "compareTo length diff == 1");
        check("a".compareTo("") == 1, "compareTo empty");
        check("".compareTo("a") == -1, "compareTo from empty");
        check("abc".compareToIgnoreCase("ABC") == 0, "compareToIgnoreCase");
        // compareTo(null) -> NPE; equals(null) -> false.
        expectNPE(() -> "abc".compareTo(null), "compareTo(null)");
        expectNPE(() -> "abc".compareToIgnoreCase(null), "compareToIgnoreCase(null)");
    }

    static void testRegionMatches() {
        check("Hello".regionMatches(1, "ell", 0, 3), "regionMatches exact");
        check("Hello".regionMatches(true, 0, "hELLO", 0, 5), "regionMatches ignoreCase");
        check("Hello".regionMatches(0, "Hello", 0, 5), "regionMatches full");
        check(!"Hello".regionMatches(0, "He", 0, 3), "regionMatches length overflow");
        check("Hello".regionMatches(0, "", 0, 0), "regionMatches empty len 0");
        check("abc".regionMatches(true, 0, "ABC", 0, 3), "regionMatches ignoreCase uppercase");

        expectNPE(() -> "Hello".regionMatches(0, null, 0, 1), "regionMatches(null)");
        expectNPE(() -> "Hello".regionMatches(true, 0, null, 0, 1),
                  "regionMatches(true,null)");
    }

    static void testStartsEnds() {
        check("abcdef".startsWith("abc"), "startsWith");
        check("abcdef".startsWith("cde", 2), "startsWith offset");
        check("abcdef".endsWith("def"), "endsWith");
        check(!"abcdef".endsWith("abc"), "!endsWith");
        check("abcdef".startsWith(""), "startsWith empty");
        check("abcdef".endsWith(""), "endsWith empty");
        check("abc".startsWith("abc"), "startsWith self");
        check("abc".endsWith("abc"), "endsWith self");
        // boundary: offset == length is valid only for empty prefix.
        check("abc".startsWith("", 3), "startsWith(\"\",len) true");
        check(!"abc".startsWith("a", 3), "startsWith(\"a\",len) false");

        expectNPE(() -> "abc".startsWith(null), "startsWith(null)");
        expectNPE(() -> "abc".startsWith(null, 0), "startsWith(null,0)");
        expectNPE(() -> "abc".endsWith(null), "endsWith(null)");
    }

    static void testIndexOf() {
        check("banana".indexOf('a') == 1, "indexOf('a')");
        check("banana".indexOf('a', 2) == 3, "indexOf('a',2)");
        check("banana".indexOf('z') == -1, "indexOf('z')");
        check("banana".indexOf("na") == 2, "indexOf(na)");
        check("banana".indexOf("na", 3) == 4, "indexOf(na,3)");
        check("banana".indexOf("zz") == -1, "indexOf(zz)");
        check("banana".lastIndexOf('a') == 5, "lastIndexOf('a')");
        check("banana".lastIndexOf('a', 2) == 1, "lastIndexOf('a',2)");
        check("banana".lastIndexOf("na") == 4, "lastIndexOf(na)");
        check("banana".lastIndexOf("na", 3) == 2, "lastIndexOf(na,3)");
        check("banana".lastIndexOf("zz") == -1, "lastIndexOf(zz)");
        check("".indexOf('a') == -1 && "".indexOf("") == 0, "empty indexOf");
        check("abc".indexOf("") == 0, "indexOf empty == 0");
        check("abc".lastIndexOf("") == 3, "lastIndexOf empty == length");
        // indexOf(int) treats the int as a full code point and finds a supplementary
        // surrogate pair; a truncated char does not match the pair.
        String sp = "a" + new String(Character.toChars(0x1D518));
        check(sp.indexOf(0x1D518) == 1, "indexOf(code point) finds supplementary pair");
        check(sp.indexOf((char) 0x1D518) == -1, "indexOf(truncated char) misses pair");
        check(sp.indexOf(new String(Character.toChars(0x1D518))) == 1, "indexOf(surrogate pair string)");
        check(sp.lastIndexOf(0x1D518) == 1, "lastIndexOf(code point) finds supplementary pair");

        expectNPE(() -> "abc".indexOf((String) null), "indexOf((String)null)");
        expectNPE(() -> "abc".indexOf((String) null, 0), "indexOf((String)null,0)");
        expectNPE(() -> "abc".lastIndexOf((String) null), "lastIndexOf((String)null)");
        expectNPE(() -> "abc".lastIndexOf((String) null, 0), "lastIndexOf((String)null,0)");
        // negative fromIndex is treated as 0 (not an error).
        check("abc".indexOf('b', -5) == 1, "indexOf('b',negative) == from 0");
    }

    static void testSubstringConcat() {
        check("abcdef".substring(2).equals("cdef"), "substring(2)");
        check("abcdef".substring(2, 4).equals("cd"), "substring(2,4)");
        check("abcdef".substring(0).equals("abcdef"), "substring(0)");
        check("abcdef".substring(6).equals(""), "substring(len) empty");
        check("abcdef".subSequence(1, 3).equals("bc"), "subSequence");
        expectSIOOBE(() -> "abc".substring(2, 1), "substring begin>end");
        expectSIOOBE(() -> "abc".substring(-1), "substring negative");
        expectSIOOBE(() -> "abc".substring(4), "substring past end");
        expectSIOOBE(() -> "abc".substring(0, 4), "substring(0,4) past end");
        expectSIOOBE(() -> "abc".subSequence(-1, 1), "subSequence(-1,1)");

        check("ab".concat("cd").equals("abcd"), "concat");
        check("".concat("x").equals("x"), "concat empty");
        check("x".concat("").equals("x"), "concat to empty");
        expectNPE(() -> "ab".concat(null), "concat(null)");
    }

    static void testContentEquals() {
        check("abc".contentEquals("abc"), "contentEquals(CharSequence)");
        check("abc".contentEquals(new StringBuilder("abc")), "contentEquals(StringBuilder)");
        check("abc".contentEquals(new StringBuffer("abc")), "contentEquals(StringBuffer)");
        check(!"abc".contentEquals("abd"), "!contentEquals different");
        check("".contentEquals(""), "contentEquals empty");
        check("".contentEquals(new StringBuilder()), "contentEquals empty builder");
        expectNPE(() -> "abc".contentEquals((CharSequence) null), "contentEquals(null)");
        expectNPE(() -> "abc".contentEquals((StringBuffer) null), "contentEquals((StringBuffer)null)");
    }

    static void testReplace() {
        check("banana".replace('a', 'o').equals("bonono"), "replace(char)");
        check("banana".replace('z', 'o').equals("banana"), "replace(char) no-op");
        check("banana".replace("na", "NA").equals("baNANA"), "replace(CharSequence literal)");
        check("banana".replace("an", "x").equals("bxxa"), "replace(CharSequence)");
        check("a1b2".replaceAll("\\d", "#").equals("a#b#"), "replaceAll regex");
        check("aaa".replaceAll("a+", "b").equals("b"), "replaceAll greedy");
        check("aaa".replaceFirst("a", "b").equals("baa"), "replaceFirst");
        check("".replace("", "x").equals("x"), "replace empty needle at empty");

        check("abc".replace('a', (char) 0).equals("\0bc"), "replace(char) with NUL");
        checkThrows(NullPointerException.class, () -> "abc".replace((CharSequence) null, "x"),
                    "replace((CharSequence)null)");
        checkThrows(NullPointerException.class, () -> "abc".replace("a", null),
                    "replace(a,null)");
        checkThrows(NullPointerException.class, () -> "abc".replaceAll(null, "x"),
                    "replaceAll(null)");
        checkThrows(NullPointerException.class, () -> "abc".replaceAll("a", null),
                    "replaceAll(a,null)");
        checkThrows(NullPointerException.class, () -> "abc".replaceFirst(null, "x"),
                    "replaceFirst(null)");
    }

    static void testMatchesSplit() {
        check("abc".matches("a.c"), "matches .");
        check("123".matches("\\d+"), "matches \\d+");
        check(!"abc".matches("\\d+"), "!matches \\d+");
        check(Arrays.equals("a,b,c".split(","), new String[]{"a", "b", "c"}), "split basic");
        check(Arrays.equals("a,,b".split(","), new String[]{"a", "", "b"}), "split inner empty");
        check(Arrays.equals("a,b,".split(","), new String[]{"a", "b"}), "split drops trailing empty");
        check(Arrays.equals("a,b,".split(",", -1), new String[]{"a", "b", ""}), "split -1 keeps trailing");
        check(Arrays.equals("a,b,c".split(",", 2), new String[]{"a", "b,c"}), "split limit 2");
        check(Arrays.equals(",a".split(","), new String[]{"", "a"}), "split keeps leading empty");
        check(Arrays.equals("".split(","), new String[]{""}), "split empty string -> [\"\"]");
        check(Arrays.equals("a b c".split("\\s+"), new String[]{"a", "b", "c"}), "split whitespace");

        expectNPE(() -> "abc".split(null), "split(null)");
        expectNPE(() -> "abc".split(null, 1), "split(null,1)");
        expectNPE(() -> "abc".matches(null), "matches(null)");
    }

    static void testCase() {
        check("AbC".toLowerCase().equals("abc"), "toLowerCase");
        check("abc".toUpperCase().equals("ABC"), "toUpperCase");
        check("AbC".toLowerCase(Locale.US).equals("abc"), "toLowerCase(Locale)");
        check("abc".toUpperCase(Locale.US).equals("ABC"), "toUpperCase(Locale)");
        check("i".toUpperCase(Locale.US).equals("I"), "toUpperCase(Locale.US) i->I");
        // Turkish-I edge: Locale.forLanguageTag("tr") dotless vs dotted I.
        check("i".toUpperCase(Locale.forLanguageTag("tr")).equals("İ"),
              "toUpperCase(tr) i -> U+0130");
        check("I".toLowerCase(Locale.forLanguageTag("tr")).equals("ı"),
              "toLowerCase(tr) I -> dotless i");
        check("strasse".toUpperCase(Locale.US).equals("STRASSE"), "toUpperCase ascii");

        expectNPE(() -> "abc".toLowerCase((Locale) null), "toLowerCase((Locale)null)");
        expectNPE(() -> "abc".toUpperCase((Locale) null), "toUpperCase((Locale)null)");
    }

    static void testTrimIntern() {
        check("  a b  ".trim().equals("a b"), "trim");
        check("".trim().equals(""), "trim empty");
        check("\t\n abc \t\n".trim().equals("abc"), "trim removes <= 0x20");
        check(" abc ".trim().equals(" abc "), "trim keeps > 0x20 spaces");
        check("abc".intern() == "abc", "intern returns canonical literal");
        check(new String("abc").intern() == "abc", "new String(...).intern() == literal");
        check("".intern() == "", "empty intern");
    }

    static void testValueOf() {
        check(String.valueOf(123).equals("123"), "valueOf(int)");
        check(String.valueOf(123L).equals("123"), "valueOf(long)");
        check(String.valueOf(1.5).equals("1.5"), "valueOf(double)");
        check(String.valueOf(1.5f).equals("1.5"), "valueOf(float)");
        check(String.valueOf(true).equals("true"), "valueOf(boolean)");
        check(String.valueOf('a').equals("a"), "valueOf(char)");
        check(String.valueOf((Object) null).equals("null"), "valueOf(null)");
        Object obj = new Object();
        check(String.valueOf(obj).equals(obj.toString()), "valueOf(Object) via toString");
        check(String.valueOf(new char[]{'a', 'b'}).equals("ab"), "valueOf(char[])");
        check(String.valueOf(new char[]{'x', 'a', 'b', 'y'}, 1, 2).equals("ab"),
              "valueOf(char[], off, len)");
        check(String.copyValueOf(new char[]{'a', 'b'}).equals("ab"), "copyValueOf");
        check(String.copyValueOf(new char[]{'x', 'a', 'b', 'y'}, 1, 2).equals("ab"),
              "copyValueOf(char[],off,len)");

        expectNPE(() -> String.valueOf((char[]) null), "valueOf((char[])null)");
        expectNPE(() -> String.valueOf((char[]) null, 0, 0), "valueOf((char[])null,off,len)");
        expectNPE(() -> String.copyValueOf((char[]) null), "copyValueOf((char[])null)");
        expectNPE(() -> String.copyValueOf((char[]) null, 0, 0), "copyValueOf((char[])null,off,len)");
        expectIOOBE(() -> String.valueOf(new char[]{'a'}, -1, 1), "valueOf(char[],-1,1)");
        expectIOOBE(() -> String.valueOf(new char[]{'a'}, 0, 2), "valueOf(char[],0,2)");
        expectIOOBE(() -> String.copyValueOf(new char[]{'a'}, 0, 2), "copyValueOf(char[],0,2)");
    }

    static void testContainsJoinFormat() {
        check("abcdef".contains("cde"), "contains");
        check(!"abcdef".contains("xyz"), "!contains");
        check("abcdef".contains(""), "contains empty");
        check(String.join(",", "a", "b", "c").equals("a,b,c"), "join varargs");
        check(String.join("-", Arrays.asList("a", "b")).equals("a-b"), "join iterable");
        check(String.join(",", new String[0]).equals(""), "join empty array");
        check(String.join(",", Arrays.asList()).equals(""), "join empty iterable");
        check(String.format("%d-%s", 5, "x").equals("5-x"), "format");
        check(String.format("%04d", 7).equals("0007"), "format padding");
        check(String.format(Locale.US, "%.2f", 1.5).equals("1.50"), "format locale");

        expectNPE(() -> "abc".contains(null), "contains(null)");
        expectNPE(() -> String.join(null, "a"), "join(null delimiter)");
        expectNPE(() -> String.join(",", (Iterable<? extends CharSequence>) null),
                  "join(null iterable)");
        expectNPE(() -> String.join(",", (CharSequence[]) null), "join(null array)");
        expectNPE(() -> String.format(null, 1), "format(null)");
        expectNPE(() -> String.format(Locale.US, null, 1), "format(Locale,null)");
    }

    static void testStreams() {
        check("ab".chars().count() == 2, "chars() count");
        check("ab".codePoints().count() == 2, "codePoints() count");
        String s = "a" + new String(Character.toChars(0x1D518));
        check(s.chars().count() == 3, "chars() counts UTF-16 units");
        check(s.codePoints().count() == 2, "codePoints() counts code points");
    }

    public static void main(String[] args) {
        testConstructorMatrix();
        testLengthCharAt();
        testCodePoints();
        testGetCharsBytes();
        testEqualityCompare();
        testRegionMatches();
        testStartsEnds();
        testIndexOf();
        testSubstringConcat();
        testContentEquals();
        testReplace();
        testMatchesSplit();
        testCase();
        testTrimIntern();
        testValueOf();
        testContainsJoinFormat();
        testStreams();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
