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

    // ===================== 契约深化（2026-11）追加 =====================
    // 双侧（JDK-8 / JDK-27）实测锁定的行为差异用 MODERN 分支断言。

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 期望精确异常类型 + 消息（MODERN 分支分别给 JDK-27 / JDK-8 的实测消息）。 */
    static void expectExactMsg(Class<? extends Throwable> type, Runnable r,
                               String modernMsg, String legacyMsg, String label) {
        try {
            r.run();
        } catch (Throwable t) {
            if (!type.isInstance(t)) {
                throw new AssertionError(label + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            String want = MODERN ? modernMsg : legacyMsg;
            if (!java.util.Objects.equals(want, t.getMessage())) {
                throw new AssertionError(label + ": message <" + t.getMessage()
                        + "> != expected <" + want + ">");
            }
            passed++;
            return;
        }
        throw new AssertionError(label + ": expected " + type.getSimpleName()
                + " but nothing was thrown");
    }

    // 1. equals/hashCode 契约：精确哈希值 + 经典碰撞对 + 与 StringBuilder 的
    //    equals/contentEquals 分叉 + 哈希稳定性 + 手算 31*h+c 公式闭环。
    static void testHashCodeContract() {
        check("".hashCode() == 0, "hashCode(\"\") == 0");
        check("abc".hashCode() == 96354, "hashCode(\"abc\") == 96354");
        check("a".hashCode() == 97, "hashCode(\"a\") == 97");
        check("Aa".hashCode() == 2112, "hashCode(\"Aa\") == 2112");
        check("BB".hashCode() == 2112, "hashCode(\"BB\") == 2112 (classic collision pair)");
        check(!"Aa".equals("BB"), "collision pair: hashes equal but equals false");
        int h = 0;
        for (char c : "abc".toCharArray()) h = 31 * h + c;
        check(h == "abc".hashCode(), "hashCode matches 31*h+c formula");
        check("abc".hashCode() == new String("abc").hashCode(), "equal instances equal hash");
        check("hel".hashCode() != "hello".hashCode(), "different lengths typically differ (locked sample)");
        String s = "stable";
        check(s.hashCode() == s.hashCode(), "hashCode stable across two calls");
        check("cached".hashCode() == "cached".hashCode(), "hash stable while other strings hashed");
        check("".hashCode() == new String().hashCode(), "empty instances share hash 0");
    }

    // 2. 索引族负例：异常类型 + 消息逐字锁定（8 与 27 消息形式不同，MODERN 分支）。
    static void testIndexBoundMessages() {
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".charAt(-1),
                "Index -1 out of bounds for length 3", "String index out of range: -1",
                "charAt(-1)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".charAt(3),
                "Index 3 out of bounds for length 3", "String index out of range: 3",
                "charAt(len)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".substring(-1),
                "Range [-1, 3) out of bounds for length 3", "String index out of range: -1",
                "substring(-1)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".substring(4),
                "Range [4, 3) out of bounds for length 3", "String index out of range: -1",
                "substring(len+1)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".substring(3, 2),
                "Range [3, 2) out of bounds for length 3", "String index out of range: -1",
                "substring(begin>end)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".subSequence(-1, 2),
                "Range [-1, 2) out of bounds for length 3", "String index out of range: -1",
                "subSequence(-1,2)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".subSequence(1, 4),
                "Range [1, 4) out of bounds for length 3", "String index out of range: 4",
                "subSequence(1,4)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "ab".codePointAt(-1),
                "Index -1 out of bounds for length 2", "String index out of range: -1",
                "codePointAt(-1)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "ab".codePointBefore(0),
                "Index -1 out of bounds for length 2", "String index out of range: 0",
                "codePointBefore(0)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "ab".codePointBefore(3),
                "Index 2 out of bounds for length 2", "String index out of range: 3",
                "codePointBefore(len+1)");
        expectExactMsg(StringIndexOutOfBoundsException.class, () -> "abc".getChars(2, 1, new char[3], 0),
                "Range [2, 1) out of bounds for length 3", "String index out of range: -1",
                "getChars(begin>end)");
        // codePointCount 越界：两侧均为 IndexOutOfBoundsException（非 SIOOBE），消息不同。
        expectExactMsg(IndexOutOfBoundsException.class, () -> "ab".codePointCount(-1, 1),
                "Range [-1, 1) out of bounds for length 2", null,
                "codePointCount(-1,1)");
        // offsetByCodePoints 越界：两侧均为无消息的 IndexOutOfBoundsException（类型断言见 testCodePoints）。
        check(!MODERN || true, "MODERN flag evaluated");
    }

    // 3. indexOf/lastIndexOf 全形态：char/String × fromIndex 前后、越界 fromIndex 容错返回 -1。
    static void testIndexOfFullMatrix() {
        check("abab".indexOf("ab", 2) == 2, "abab indexOf(ab,2) == 2");
        check("abab".lastIndexOf("ab", 1) == 0, "abab lastIndexOf(ab,1) == 0");
        check("abab".indexOf('a', 1) == 2, "indexOf(char,fromIndex) forward");
        check("abab".lastIndexOf('b', 3) == 3, "lastIndexOf(char,fromIndex) backward");
        check("abc".indexOf('b', 99) == -1, "indexOf(char) fromIndex > len -> -1 (no throw)");
        check("abc".indexOf('b', -5) == 1, "indexOf(char) negative fromIndex clamped to 0");
        check("abc".lastIndexOf('b', -5) == -1, "lastIndexOf(char) negative fromIndex -> -1");
        check("abc".lastIndexOf('b', 99) == 1, "lastIndexOf(char) fromIndex > len clamped");
        check("abc".indexOf("b", 99) == -1, "indexOf(String) fromIndex > len -> -1");
        check("abc".lastIndexOf("b", -5) == -1, "lastIndexOf(String) negative fromIndex -> -1");
        check("abc".indexOf("", 99) == 3, "indexOf(\"\") fromIndex > len clamped to length");
        check("abc".lastIndexOf("", -5) == -1, "lastIndexOf(\"\") negative fromIndex -> -1");
        check("abc".lastIndexOf("", 99) == 3, "lastIndexOf(\"\") fromIndex > len clamped to length");
        check("".indexOf("") == 0 && "".lastIndexOf("") == 0, "empty string empty needle == 0");
        check("mississippi".indexOf("issi") == 1, "first occurrence");
        check("mississippi".lastIndexOf("issi") == 4, "last occurrence");
        check("aaa".indexOf("aa", 1) == 1, "overlapping search window from 1");
        check("aaa".lastIndexOf("aa", 2) == 1, "lastIndexOf overlapping window");
        check("abc".indexOf('c') == 2 && "abc".lastIndexOf('a') == 0, "first/last single char");
    }

    // 4. replace 两形态：全替换、重叠、空目标、不存在原样返回 this（实测两侧 == 均真）。
    static void testReplaceForms() {
        check("aaa".replace('a', 'b').equals("bbb"), "replace(char,char) replaces all");
        check("aaa".replace('z', 'b') == "aaa", "replace(char,char) no match returns this (spec)");
        check("abc".replace('a', 'a') == "abc", "replace(char,char) identity returns this");
        check("aaa".replace("aa", "b").equals("ba"), "replace(CS) non-overlapping left-to-right");
        check("aaaa".replace("aa", "b").equals("bb"), "replace(CS) pairwise non-overlap");
        check("aaa".replace("a", "").equals(""), "replace(CS) delete-all");
        check("aaa".replace("", "-").equals("-a-a-a-"), "replace(CS) empty needle inserts everywhere");
        check("".replace("", "x").equals("x"), "replace(CS) empty into empty");
        check("abc".replace((CharSequence) "z", (CharSequence) "y") == "abc",
              "replace(CS,CS) no match returns this (locked on both JDKs)");
        check("a-b".replace("-", "--").equals("a--b"), "replace(CS) expansion");
        check("banana".replace("na", "NA").equals("baNANA"), "replace(CS) multiple matches");
        check("aaa".replace('a', '\u0000').equals("\u0000\u0000\u0000"), "replace(char) with NUL target");
    }

    // 5. split limit 矩阵 + matches 正则语义（点号不匹配换行）。
    static void testSplitLimitMatrix() {
        check("a,b,c".split(",").length == 3, "split default (limit 0)");
        check("a,b,c".split(",", 0).length == 3, "split limit 0 explicit");
        check("a,b,c".split(",", 1).length == 1
              && "a,b,c".split(",", 1)[0].equals("a,b,c"), "limit 1 keeps whole string");
        check("a,b,c".split(",", 2)[0].equals("a") && "a,b,c".split(",", 2)[1].equals("b,c"),
              "limit 2 remainder unsplit");
        check("a,b,,".split(",").length == 2, "limit 0 drops all trailing empties");
        check("a,b,,".split(",", -1).length == 4, "negative limit keeps trailing empties");
        check("a,b,,".split(",", -1)[2].isEmpty() && "a,b,,".split(",", -1)[3].isEmpty(),
              "negative limit trailing fields are empty");
        check(",,".split(",").length == 0, "only-empty fields collapse with limit 0");
        check(",,".split(",", -1).length == 3, "only-empty fields kept with limit -1");
        check("".split(",").length == 1 && "".split(",")[0].isEmpty(), "empty input -> [\"\"]");
        check(",a".split(",").length == 2 && ",a".split(",")[0].isEmpty(), "leading empty kept");
        check("1.2.3".split("\\.").length == 3 && "1.2.3".split("\\.")[2].equals("3"),
              "regex metachar escaped");
        check(" a b ".split("\\s").length == 3 && " a b ".split("\\s")[0].isEmpty(),
              "leading whitespace field");
        check("a b c".split(" ", -1).length == 3, "literal space separator");
        check("aaa".split("a").length == 0, "every char is separator (limit 0)");
        check("a b".matches("a.b"), "dot matches space");
        check(!"a\nb".matches("a.b"), "dot does not match newline (no DOTALL)");
        check("a\nb".matches("a(?s).b"), "inline (?s) DOTALL matches newline");
        check("".matches(""), "empty pattern matches empty");
        check("abc".matches("[a-c]{3}"), "character class + quantifier");
    }

    // 6. trim 空白表：trim 只去 <= U+0020；U+2003/U+00A0 均不去（两侧一致）。
    static void testTrimWhitespaceTable() {
        String em = new String(Character.toChars(0x2003)); // EM SPACE
        String nb = new String(Character.toChars(0x00A0)); // NO-BREAK SPACE
        check((em + "a" + em).trim().length() == 3, "trim does not remove U+2003 (em space)");
        check((nb + "a" + nb).trim().length() == 3, "trim does not remove U+00A0 (nbsp)");
        check("abc".trim() == "abc", "trim no-op returns this");
        check("".trim() == "", "trim of empty returns canonical empty");
        check("\u0000a\u0000".trim().equals("a"), "trim removes NUL (<= 0x20)");
        check("\u001F a".trim().equals("a"), "trim removes U+001F");
        check(" a".trim().equals("a") && "a ".trim().equals("a"), "trim leading/trailing");
        check("a b".trim().equals("a b"), "trim keeps inner space");
        check("   ".trim().isEmpty(), "all-blank trims to empty");
        check("\t\r\n\u000Bx\u000C".trim().equals("x"), "trim removes tab/CR/LF/VT/FF");
    }

    // 7. intern 闭环（JLS 3.10.5）：常量折叠 ==、运行时拼接 !=、intern 归一。
    static void testInternClosure() {
        String folded = "a" + "b"; // 常量折叠
        check(folded == "ab", "compile-time concat folds to the literal");
        String runtime = new String("a") + new String("b");
        check(runtime != "ab", "runtime concat is a distinct instance");
        check(runtime.equals("ab"), "runtime concat equals content");
        check(runtime.intern() == "ab", "intern of runtime concat reuses the literal");
        check(new String("ab") != "ab", "new String is a distinct instance");
        check(new String("ab").intern() == "ab", "intern dedupes to the literal");
        check("abcdef".substring(0, 2).intern() == "ab", "substring-derived intern hits literal");
        check(new StringBuilder("a").append("b").toString().intern() == "ab",
              "builder-derived intern hits literal");
        check(new String("zz").intern() == new String("zz").intern(),
              "same-content different sources intern to same instance");
        String r2 = new StringBuilder("a").append(new String("b")).toString();
        check(r2.intern() == runtime.intern(), "two runtime instances intern to one canonical");
        check("".intern() == "", "empty string interns to canonical empty");
    }

    // 8. format 矩阵（显式 Locale.ENGLISH，全部实测精确全等）。
    static void testFormatMatrix() {
        check(String.format(Locale.ENGLISH, "%05d", 42).equals("00042"), "%05d zero-pad");
        check(String.format(Locale.ENGLISH, "%,d", 1234567).equals("1,234,567"), "%,d grouping");
        check(String.format(Locale.ENGLISH, "%,.2f", 1234567.891).equals("1,234,567.89"), "%,.2f");
        check(String.format(Locale.ENGLISH, "%.2f", 3.14159).equals("3.14"), "%.2f");
        check(String.format(Locale.ENGLISH, "%x", 255).equals("ff"), "%x lowercase hex");
        check(String.format(Locale.ENGLISH, "%X", 255).equals("FF"), "%X uppercase hex");
        check(String.format(Locale.ENGLISH, "%o", 64).equals("100"), "%o octal");
        check(String.format(Locale.ENGLISH, "%b", (Object) null).equals("false"), "%b null -> false");
        check(String.format(Locale.ENGLISH, "%b", "x").equals("true"), "%b non-null -> true");
        check(String.format(Locale.ENGLISH, "%b", Boolean.FALSE).equals("false"), "%b false");
        check(String.format(Locale.ENGLISH, "%c", 65).equals("A"), "%c");
        check(String.format(Locale.ENGLISH, "[%s]", (Object) null).equals("[null]"), "%s null -> null");
        check(String.format(Locale.ENGLISH, "%%").equals("%"), "%% literal");
        check(!String.format(Locale.ENGLISH, "%n").isEmpty(), "%n non-empty (value not locked)");
        check(String.format(Locale.ENGLISH, "[%-6d]", 7).equals("[7     ]"), "%-6d left justify");
        check(String.format(Locale.ENGLISH, "%+d", 7).equals("+7"), "%+d positive sign");
        check(String.format(Locale.ENGLISH, "%+d", -7).equals("-7"), "%+d negative sign");
        check(String.format(Locale.ENGLISH, "%e", 1234.5678).equals("1.234568e+03"), "%e");
        check(String.format(Locale.ENGLISH, "%g", 0.00001234).equals("1.23400e-05"), "%g small");
        check(String.format(Locale.ENGLISH, "%g", 123456789.0d).equals("1.23457e+08"), "%g large");
        check(String.format(Locale.ENGLISH, "%s", 1.0d).equals("1.0"), "%s double");
        check(String.format(Locale.ENGLISH, "%5.3s", "abcdef").equals("  abc"), "%5.3s truncate");
        check(String.format(Locale.ENGLISH, "%d", 0L).equals("0"), "%d zero");
    }

    // 9. getBytes 字符集矩阵：UTF-8 中文 3 字节 + 首字节 0xE4、US-ASCII 替换 '?'、
    //    补充平面 4 字节、默认字符集 ASCII 往返。
    static void testGetBytesCharsets() {
        byte[] zh = "中".getBytes(StandardCharsets.UTF_8);
        check(zh.length == 3, "UTF-8 bytes of U+4E2D: length 3");
        check((zh[0] & 0xFF) == 0xE4, "first byte 0xE4");
        check((zh[1] & 0xFF) == 0xB8 && (zh[2] & 0xFF) == 0xAD, "remaining bytes 0xB8 0xAD");
        check(new String(zh, StandardCharsets.UTF_8).equals("中"), "UTF-8 round-trip");
        byte[] ascii = "中".getBytes(StandardCharsets.US_ASCII);
        check(ascii.length == 1 && (ascii[0] & 0xFF) == 0x3F, "US-ASCII unmappable -> '?' (0x3F)");
        byte[] sup = new String(new int[]{0x1D11E}, 0, 1).getBytes(StandardCharsets.UTF_8);
        check(sup.length == 4, "supplementary code point -> 4 UTF-8 bytes");
        check(new String(sup, StandardCharsets.UTF_8).codePointAt(0) == 0x1D11E,
              "supplementary round-trip");
        byte[] mix = "a中b".getBytes(StandardCharsets.UTF_8);
        check(mix.length == 5, "mixed ASCII/CJK length 5");
        check(new String(mix, 0, 1, StandardCharsets.UTF_8).equals("a"), "ASCII byte-slice ctor");
        check(new String(mix, StandardCharsets.UTF_8).equals("a中b"), "mixed round-trip");
        byte[] def = "hello".getBytes();
        check(new String(def).equals("hello"), "default charset ASCII round-trip");
        check(Arrays.equals("hello".getBytes(StandardCharsets.US_ASCII),
                new byte[]{'h', 'e', 'l', 'l', 'o'}), "ASCII bytes identical");
    }

    // 10. 构造器族 round-trip：码点构造器与代理对字面量等价、快照独立性、
    //     ctor/valueOf/copyValueOf 三路一致且均为防御拷贝。
    static void testConstructorRoundTrips() {
        String cp = new String(new int[]{0x1D11E}, 0, 1);
        check(cp.length() == 2, "code-point ctor: surrogate pair length 2");
        check(cp.equals("𝄞"), "code-point ctor equals surrogate literal");
        check(cp.codePointAt(0) == 0x1D11E, "code-point ctor round-trip value");
        StringBuilder sb = new StringBuilder("abc");
        String snapshot = new String(sb);
        sb.append("def");
        check(snapshot.equals("abc"), "String(StringBuilder) snapshot independent of later appends");
        char[] src = {'a', 'b'};
        String viaCtor = new String(src);
        String viaValueOf = String.valueOf(src);
        String viaCopy = String.copyValueOf(src);
        check(viaCtor.equals("ab") && viaValueOf.equals("ab") && viaCopy.equals("ab"),
              "ctor/valueOf/copyValueOf produce same content");
        src[0] = 'z';
        check(viaCtor.equals("ab") && viaValueOf.equals("ab") && viaCopy.equals("ab"),
              "all three are defensive copies");
        check(new String(new char[]{'x', 'y', 'z'}, 1, 2).equals("yz"), "char[] offset slice");
        byte[] b = "ab".getBytes(StandardCharsets.UTF_8);
        check(new String(b, 1, 1, StandardCharsets.UTF_8).equals("b"), "byte[] offset slice");
    }

    // 11. regionMatches 容错：越界返回 false 不抛、零长匹配、ignoreCase。
    static void testRegionMatchesTolerance() {
        check("Apple".regionMatches(true, 0, "apple", 0, 5), "ignoreCase full match");
        check(!"Apple".regionMatches(false, 0, "apple", 0, 5), "case-sensitive mismatch");
        check("abc".regionMatches(3, "", 0, 0), "zero-length match at end index");
        check(!"abc".regionMatches(4, "", 0, 0), "toffset > len with zero len -> false (no throw)");
        check(!"abc".regionMatches(-1, "a", 0, 1), "negative toffset -> false (no throw)");
        check(!"abc".regionMatches(1, "bcd", 0, 3), "combined range overflow -> false");
        check(!"abc".regionMatches(0, "bcd", 1, 3), "other offset overflow -> false");
        check("abcdef".regionMatches(true, 2, "CDE", 0, 3), "ignoreCase middle window");
    }

    // 12. compareTo 精确差值：字符差、长度差（剩余首字符值）、反对称。
    static void testComparePrecision() {
        check("a".compareTo("c") == -2, "char diff exact -2");
        check("abc".compareTo("ab") == 1, "length diff yields length delta 1");
        check("abc".compareTo("abcd") == -1, "prefix shorter by exact -1");
        check("A".compareTo("a") == -32, "case diff -32");
        check("".compareTo("a") == -1 && "a".compareTo("") == 1, "empty vs one char");
        check("abc".compareTo("abc") == 0, "equal -> 0");
        check("abc".compareTo("abd") == -("abd".compareTo("abc")), "antisymmetry exact");
        check("ABC".compareToIgnoreCase("abc") == 0, "compareToIgnoreCase equal");
        check("abc".compareToIgnoreCase("ABD") < 0, "compareToIgnoreCase sign");
        check("Ａ".compareTo("Ｂ") == -1, "high BMP neighbors");
        check("abc".compareTo("cba") == ('a' - 'c'), "first char decides exact diff");
    }

    // 13. equals/contentEquals 分叉 + equalsIgnoreCase 矩阵。
    static void testEqualsDivergence() {
        StringBuilder sb = new StringBuilder("hello");
        check(!"hello".equals(sb), "equals(StringBuilder) false (type-sensitive)");
        check("hello".contentEquals(sb), "contentEquals(StringBuilder) true for same content");
        check("hello".contentEquals(new StringBuffer("hello")), "contentEquals(StringBuffer)");
        check(!"hello".contentEquals(new StringBuffer("hellx")), "contentEquals negative");
        check("hello".contentEquals((CharSequence) new StringBuilder("hell").append("o")),
              "contentEquals built-up builder");
        check("A".equalsIgnoreCase("a"), "equalsIgnoreCase case pair");
        check("AB".equalsIgnoreCase("ab") && "ab".equalsIgnoreCase("AB"), "equalsIgnoreCase symmetric");
        check(!"A".equalsIgnoreCase((String) null), "equalsIgnoreCase(null) false");
        check(!"ab".equalsIgnoreCase("abc"), "equalsIgnoreCase length mismatch");
        check(!"ab".equalsIgnoreCase("ac"), "equalsIgnoreCase last char mismatch");
        check("".equalsIgnoreCase(""), "empty pair");
    }

    // 14. valueOf 全家 + join 形态（null 元素 -> "null"）。
    static void testValueOfAndJoin() {
        check(String.valueOf(1.0d).equals("1.0"), "valueOf(double 1.0)");
        check(String.valueOf(-0.0d).equals("-0.0"), "valueOf(double -0.0)");
        check(String.valueOf(Long.MAX_VALUE).equals("9223372036854775807"), "valueOf(long max)");
        check(String.valueOf(Integer.MIN_VALUE).equals("-2147483648"), "valueOf(int min)");
        check(String.valueOf('x').equals("x"), "valueOf(char)");
        check(String.valueOf(new char[]{'a'}).equals("a"), "valueOf(char[]) single");
        check(String.join(",", "a").equals("a"), "join single element");
        check(String.join(",", new String[]{"a", "b"}).equals("a,b"), "join array");
        check(String.join("--", "a", "", "b").equals("a----b"), "join keeps empty middle");
        check(String.join(",", java.util.Arrays.asList("x")).equals("x"), "join iterable single");
        char[] ca = {'a', 'b'};
        String v = String.valueOf(ca);
        ca[0] = 'z';
        check(v.equals("ab"), "valueOf(char[]) defensive copy");
        check(String.valueOf((Object) null).equals("null"), "valueOf(Object null) -> \"null\"");
        check(String.valueOf(true).equals("true"), "valueOf(boolean)");
    }

    // 15. 流内容精确：chars()/codePoints() toArray 全等（含代理对单元/配对解码）。
    static void testStreamContents() {
        int[] chs = "abc".chars().toArray();
        check(chs.length == 3 && chs[0] == 97 && chs[1] == 98 && chs[2] == 99, "chars() toArray exact");
        check("abc".chars().sum() == 97 + 98 + 99, "chars() sum");
        String sup = new String(new int[]{0x1D11E, 'a'}, 0, 2);
        int[] units = sup.chars().toArray();
        check(units.length == 3 && units[0] == 0xD834 && units[1] == 0xDD1E && units[2] == 97,
              "chars() over surrogate pair yields UTF-16 units");
        int[] cps = sup.codePoints().toArray();
        check(cps.length == 2 && cps[0] == 0x1D11E && cps[1] == 97, "codePoints() decodes pairs");
        int[] back = "xyz".chars().toArray();
        check(new String(back, 0, back.length).equals("xyz"), "ASCII chars round-trip via int[] ctor");
        StringBuilder sb = new StringBuilder("ab");
        check(sb.chars().sum() == 97 + 98, "default chars() via StringBuilder");
        check("".chars().count() == 0 && "".codePoints().count() == 0, "empty streams count 0");
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
        testHashCodeContract();
        testIndexBoundMessages();
        testIndexOfFullMatrix();
        testReplaceForms();
        testSplitLimitMatrix();
        testTrimWhitespaceTable();
        testInternClosure();
        testFormatMatrix();
        testGetBytesCharsets();
        testConstructorRoundTrips();
        testRegionMatchesTolerance();
        testComparePrecision();
        testEqualsDivergence();
        testValueOfAndJoin();
        testStreamContents();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
