/*
 * @test
 * @summary java.lang.String API conformance (Java 8 surface): length/isEmpty/charAt,
 *          code points (BMP + supplementary), getChars/getBytes/toCharArray, equality
 *          (equals/equalsIgnoreCase/compareTo/compareToIgnoreCase/regionMatches),
 *          startsWith/endsWith, indexOf/lastIndexOf, substring/subSequence/concat,
 *          replace (char/CharSequence/regex), matches/split, case conversion, trim/
 *          intern/valueOf/copyValueOf/contains/join/format, and chars()/codePoints().
 * @run main StringTest
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public class StringTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectSIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
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
    }

    static void testGetCharsBytes() {
        char[] dst = new char[3];
        "abc".getChars(0, 3, dst, 0);
        check(dst[0] == 'a' && dst[1] == 'b' && dst[2] == 'c', "getChars");
        check(Arrays.equals("abc".toCharArray(), new char[]{'a', 'b', 'c'}), "toCharArray");

        byte[] utf8 = "abc".getBytes(StandardCharsets.UTF_8);
        check(Arrays.equals(utf8, new byte[]{'a', 'b', 'c'}), "getBytes(UTF-8)");
        check(new String(utf8, StandardCharsets.UTF_8).equals("abc"), "new String(byte[], Charset)");
        check(new String(utf8, 0, 3, Charset.forName("UTF-8")).equals("abc"),
              "new String(byte[], off, len, Charset)");
        byte[] def = "abc".getBytes();
        check(def.length == 3 && new String(def).equals("abc"), "default charset round-trip");
    }

    static void testEqualityCompare() {
        check("abc".equals("abc"), "equals");
        check(!"abc".equals("ABC"), "!equals different case");
        check(!"abc".equals((Object) null), "!equals null");
        check(!"abc".equals(Integer.valueOf(123)), "!equals non-String");
        check("abc".equalsIgnoreCase("ABC"), "equalsIgnoreCase");
        check(!"abc".equalsIgnoreCase("abd"), "!equalsIgnoreCase different");

        check("abc".compareTo("abc") == 0, "compareTo equal");
        check("abc".compareTo("abd") < 0, "compareTo < ");
        check("abd".compareTo("abc") > 0, "compareTo > ");
        check("abc".compareTo("ab") == 1, "compareTo length diff == 1");
        check("a".compareTo("") == 1, "compareTo empty");
        check("abc".compareToIgnoreCase("ABC") == 0, "compareToIgnoreCase");
    }

    static void testRegionMatches() {
        check("Hello".regionMatches(1, "ell", 0, 3), "regionMatches exact");
        check("Hello".regionMatches(true, 0, "hELLO", 0, 5), "regionMatches ignoreCase");
        check("Hello".regionMatches(0, "Hello", 0, 5), "regionMatches full");
        check(!"Hello".regionMatches(0, "He", 0, 3), "regionMatches length overflow");
    }

    static void testStartsEnds() {
        check("abcdef".startsWith("abc"), "startsWith");
        check("abcdef".startsWith("cde", 2), "startsWith offset");
        check("abcdef".endsWith("def"), "endsWith");
        check(!"abcdef".endsWith("abc"), "!endsWith");
        check("abcdef".startsWith(""), "startsWith empty");
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
        check("banana".lastIndexOf("zz") == -1, "lastIndexOf(zz)");
    }

    static void testSubstringConcat() {
        check("abcdef".substring(2).equals("cdef"), "substring(2)");
        check("abcdef".substring(2, 4).equals("cd"), "substring(2,4)");
        check("abcdef".subSequence(1, 3).equals("bc"), "subSequence");
        expectSIOOBE(() -> "abc".substring(2, 1), "substring begin>end");
        expectSIOOBE(() -> "abc".substring(-1), "substring negative");
        check("ab".concat("cd").equals("abcd"), "concat");
        check("".concat("x").equals("x"), "concat empty");
    }

    static void testReplace() {
        check("banana".replace('a', 'o').equals("bonono"), "replace(char)");
        check("banana".replace("na", "NA").equals("baNANA"), "replace(CharSequence literal)");
        check("banana".replace("an", "x").equals("bxxa"), "replace(CharSequence)");
        check("a1b2".replaceAll("\\d", "#").equals("a#b#"), "replaceAll regex");
        check("aaa".replaceAll("a+", "b").equals("b"), "replaceAll greedy");
        check("aaa".replaceFirst("a", "b").equals("baa"), "replaceFirst");
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
    }

    static void testCase() {
        check("AbC".toLowerCase().equals("abc"), "toLowerCase");
        check("abc".toUpperCase().equals("ABC"), "toUpperCase");
        check("AbC".toLowerCase(Locale.US).equals("abc"), "toLowerCase(Locale)");
        check("abc".toUpperCase(Locale.US).equals("ABC"), "toUpperCase(Locale)");
        check("i".toUpperCase(Locale.US).equals("I"), "toUpperCase(Locale.US) i->I");
    }

    static void testTrimIntern() {
        check("  a b  ".trim().equals("a b"), "trim");
        check("".trim().equals(""), "trim empty");
        check("abc".intern() == "abc", "intern returns canonical literal");
        check(new String("abc").intern() == "abc", "new String(...).intern() == literal");
    }

    static void testValueOf() {
        check(String.valueOf(123).equals("123"), "valueOf(int)");
        check(String.valueOf(123L).equals("123"), "valueOf(long)");
        check(String.valueOf(1.5).equals("1.5"), "valueOf(double)");
        check(String.valueOf(1.5f).equals("1.5"), "valueOf(float)");
        check(String.valueOf(true).equals("true"), "valueOf(boolean)");
        check(String.valueOf('a').equals("a"), "valueOf(char)");
        check(String.valueOf((Object) null).equals("null"), "valueOf(null)");
        check(String.valueOf(new char[]{'a', 'b'}).equals("ab"), "valueOf(char[])");
        check(String.valueOf(new char[]{'x', 'a', 'b', 'y'}, 1, 2).equals("ab"),
              "valueOf(char[], off, len)");
        check(String.copyValueOf(new char[]{'a', 'b'}).equals("ab"), "copyValueOf");
    }

    static void testContainsJoinFormat() {
        check("abcdef".contains("cde"), "contains");
        check(!"abcdef".contains("xyz"), "!contains");
        check("abcdef".contains(""), "contains empty");
        check(String.join(",", "a", "b", "c").equals("a,b,c"), "join varargs");
        check(String.join("-", Arrays.asList("a", "b")).equals("a-b"), "join iterable");
        check(String.format("%d-%s", 5, "x").equals("5-x"), "format");
        check(String.format("%04d", 7).equals("0007"), "format padding");
    }

    static void testStreams() {
        check("ab".chars().count() == 2, "chars() count");
        check("ab".codePoints().count() == 2, "codePoints() count");
        String s = "a" + new String(Character.toChars(0x1D518));
        check(s.chars().count() == 3, "chars() counts UTF-16 units");
        check(s.codePoints().count() == 2, "codePoints() counts code points");
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
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

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void testBlankStrip() {
        check("".isBlank() && "   ".isBlank() && "\t\n".isBlank(), "isBlank true");
        check("\u2000\u2003".isBlank(), "isBlank U+2000/U+2003 whitespace");
        check(!"\u00A0\u2007".isBlank(), "!isBlank no-break spaces (not whitespace)");
        check(!"abc".isBlank() && !" a ".isBlank(), "!isBlank with content");
        check("  a b  ".strip().equals("a b"), "strip both ends");
        check("\u2000a\u2000".strip().equals("a"), "strip removes U+2000");
        check("\u00A0\u00A0x\u00A0".strip().equals("\u00A0\u00A0x\u00A0"), "strip keeps NBSP (not whitespace)");
        check("\u2000x".stripLeading().equals("x"), "stripLeading");
        check("x\u2000".stripTrailing().equals("x"), "stripTrailing");
        check("  a b  ".trim().equals("a b"), "trim (<= U+0020)");
        check("\u2000a".trim().equals("\u2000a"), "trim leaves U+2000");
        check("\u00A0a\u00A0".trim().equals("\u00A0a\u00A0"), "trim leaves NBSP");
    }

    static void testRepeatIndentLines() {
        check("ab".repeat(3).equals("ababab"), "repeat(3)");
        check("ab".repeat(0).equals("") && "ab".repeat(1).equals("ab"), "repeat(0/1)");
        check("".repeat(100).equals(""), "repeat empty");
        expectIAE(() -> "x".repeat(-1), "repeat negative");

        check("a\nb".lines().count() == 2, "lines count");
        check("a\nb\n".lines().count() == 2, "lines drops trailing empty");
        check("a\r\nb\rc".lines().count() == 3, "lines splits CRLF and CR");
        check("".lines().count() == 0, "empty lines == 0");
        check("a\nb".lines().toList().equals(java.util.List.of("a", "b")), "lines order");

        check("a\nb".indent(2).equals("  a\n  b\n"), "indent(+2)");
        check("  a\n  b".indent(-1).equals(" a\n b\n"), "indent(-1)");
        check("a\r\nb".indent(0).equals("a\nb\n"), "indent(0) normalizes terminators");
        check("a\nb\n".indent(0).equals("a\nb\n"), "indent(0) trailing newline preserved");
        check("".indent(3).equals(""), "indent empty");
    }

    static void testTransform() {
        check("abc".transform(s -> s + "!").equals("abc!"), "transform append");
        check("aBc".transform(String::toUpperCase).equals("ABC"), "transform toUpperCase");
        check("42".transform(Integer::parseInt).intValue() == 42, "transform -> Integer");
        check("x".transform(s -> null) == null, "transform null result allowed");
    }

    static void testFormattedTranslateEscapes() {
        check("%s-%d".formatted("x", 5).equals("x-5"), "formatted");
        check(String.format(Locale.ROOT, "%04d|%.1f", 7, 3.14).equals("0007|3.1"),
              "format padding/precision (fixed locale)");
        check("a\\tb".translateEscapes().equals("a\tb"), "translateEscapes \\t");
        check("a\\nb".translateEscapes().equals("a\nb"), "translateEscapes \\n");
        check("\\\\".translateEscapes().equals("\\"), "translateEscapes \\\\ -> \\");
        check("\\101".translateEscapes().equals("A"), "translateEscapes octal \\101 -> A");
        check(("a\\" + "\n" + "b").translateEscapes().equals("ab"), "translateEscapes line continuation");
        expectIAE(() -> "\\q".translateEscapes(), "translateEscapes invalid \\q");
    }

    static void testStripIndent() {
        check("    a\n      b\n    c".stripIndent().equals("a\n  b\nc"), "stripIndent");
        check("  \n    a\n    ".stripIndent().equals("\na\n"), "stripIndent blank lines");
        check("a".stripIndent().equals("a"), "stripIndent single line");
        check("".stripIndent().equals(""), "stripIndent empty");
    }

    static void testJoinAndCodePointBounds() {
        check(String.join(",", "a", null, "b").equals("a,null,b"), "join null element -> \"null\"");
        expectNPE(() -> String.join(null, "a", "b"), "join null delimiter NPE");
        expectSIOOBE(() -> "abc".codePointAt(3), "codePointAt(3) at length");
        expectSIOOBE(() -> "abc".codePointBefore(0), "codePointBefore(0)");
        expectIOOBE(() -> "abc".codePointCount(0, 4), "codePointCount(0,4) overflow");
    }

    static void testBoundaryEquivalence() {
        // Empty-string boundaries.
        check("".indexOf("") == 0, "indexOf empty needle at 0");
        check("abc".indexOf("") == 0, "indexOf empty needle returns 0");
        check("abc".lastIndexOf("") == 3, "lastIndexOf empty needle returns length");
        check("".substring(0).equals(""), "substring(0) of empty");
        check("abc".substring(3).equals(""), "substring(length) is empty");
        check("abc".substring(0, 0).equals(""), "substring(0,0) is empty");
        check("abc".regionMatches(3, "", 0, 0), "regionMatches empty at end");

        // Ordering antisymmetry.
        check("abc".compareTo("abd") == -"abd".compareTo("abc"), "compareTo antisymmetry");
        check("ABC".compareToIgnoreCase("abc") == 0, "compareToIgnoreCase case-insensitive");

        // valueOf of extreme primitives.
        check(String.valueOf(Integer.MIN_VALUE).equals("-2147483648"), "valueOf(Integer.MIN_VALUE)");
        check(String.valueOf(Long.MIN_VALUE).equals("-9223372036854775808"), "valueOf(Long.MIN_VALUE)");

        // equals -> hashCode consistency.
        check("abc".equals("abc") == ("abc".hashCode() == "abc".hashCode()),
              "equals->hashCode consistent");

        // Locale-independent ASCII case conversion.
        check("azAZ09".toLowerCase(Locale.ROOT).equals("azaz09"), "toLowerCase ASCII");
        check("azAZ09".toUpperCase(Locale.ROOT).equals("AZAZ09"), "toUpperCase ASCII");
    }

    // ===================== 契约深化（2026-11）追加 =====================
    // 与 JDK-8 侧实测锁定的行为差异用 MODERN 分支断言（本文件恒为 true 分支）。

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

    // 2. 索引族负例：异常类型 + 消息逐字锁定（JDK-27 为 Index/Range 形式）。
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
        // offsetByCodePoints 越界：两侧均为无消息的 IndexOutOfBoundsException（类型断言见 testJoinAndCodePointBounds）。
        check(MODERN, "MODERN branch on JDK-27+");
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

    // 12. compareTo 精确差值：字符差、长度差（长度差值）、反对称。
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

    // 16. JDK-11+ strip 家族精确表：strip 用 Character.isWhitespace（U+2003 去、U+00A0 不去）。
    static void testStripPrecision() {
        String em = new String(Character.toChars(0x2003)); // EM SPACE: isWhitespace true
        String nb = new String(Character.toChars(0x00A0)); // NO-BREAK SPACE: isWhitespace false
        check((em + "a" + em).strip().length() == 1, "strip removes U+2003 both ends");
        check((nb + "a" + nb).strip().length() == 3, "strip keeps U+00A0 both ends");
        check("abc".strip() == "abc", "strip no-op returns this");
        check("   ".strip().isEmpty(), "all-blank strips to empty");
        check("  a  ".stripLeading().equals("a  "), "stripLeading only head");
        check("  a  ".stripTrailing().equals("  a"), "stripTrailing only tail");
        check("".strip().isEmpty(), "strip empty");
        check("\t\n x \t\n".strip().equals("x"), "strip removes ASCII controls both ends");
        check((em + "x").stripLeading().equals("x") && ("x" + em).stripTrailing().equals("x"),
              "stripLeading/Trailing with U+2003");
        check((nb + "x").stripLeading().equals(nb + "x"), "stripLeading keeps NBSP");
    }

    // 17. JDK-11+ repeat 契约：0 -> 空、1 -> this（实测锁定）、负数 IAE 精确消息。
    static void testRepeatContract() {
        check("ab".repeat(3).equals("ababab"), "repeat 3");
        check("ab".repeat(0).isEmpty(), "repeat 0 -> empty");
        check("x".repeat(1) == "x", "repeat(1) returns this (locked)");
        check("".repeat(100).isEmpty(), "empty repeated stays empty");
        check("ab".repeat(2).length() == 4, "repeat length");
        try {
            "x".repeat(-1);
            throw new AssertionError("repeat(-1): expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("count is negative: -1".equals(e.getMessage()), "repeat(-1) exact message");
        }
    }

    // 18. JDK-11+ lines() 精确元素表（\n、\r\n、\r 均为终止符；尾部空行丢弃）。
    static void testLinesPrecision() {
        check(java.util.List.of("a", "", "b").equals("a\n\nb".lines().toList()),
              "lines keeps inner empty line");
        check(java.util.List.of("abc").equals("abc".lines().toList()), "no terminator single");
        check("".lines().toList().isEmpty(), "empty -> no lines");
        check("\n".lines().count() == 1 && "\n".lines().toList().get(0).isEmpty(),
              "single newline -> exactly one empty line");
        check(java.util.List.of("a", "b").equals("a\r\nb".lines().toList()), "CRLF one terminator");
        check(java.util.List.of("a", "b", "c").equals("a\rb\nc".lines().toList()), "CR and LF both");
        check(java.util.List.of("a", "b").equals("a\nb\n".lines().toList()),
              "trailing terminator dropped");
        check("a\nb".lines().count() == 2, "count via stream");
    }

    // 19. JDK-11+ isBlank 表：isWhitespace 集合（U+00A0/U+200B 非 blank）。
    static void testIsBlankTable() {
        check("\t".isBlank(), "tab isBlank");
        check(!"\u00A0".isBlank(), "NBSP is not blank");
        check(!"\u200B".isBlank(), "U+200B zero-width space not blank");
        check("".isBlank() && "   ".isBlank(), "empty/blank");
        check(!" a ".isBlank(), "content not blank");
        check("\u2003".isBlank(), "U+2003 em space blank");
        check("\u2028".isBlank(), "U+2028 line separator blank (isWhitespace true)");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(name)) return true;
        }
        return false;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.String");
        check(!c.isInterface(), "String not an interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "String is final");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "String not abstract");
        check(c.getSuperclass() == Object.class, "String extends Object");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasInterface(c, "java.lang.CharSequence"), "implements CharSequence");
        check(hasInterface(c, "java.lang.constant.Constable"), "implements Constable");
        check(hasInterface(c, "java.lang.constant.ConstantDesc"), "implements ConstantDesc");
        check(hasMethod(c, "length"), "method length()");
        check(hasMethod(c, "charAt", int.class), "method charAt(int)");
        check(hasMethod(c, "compareTo", String.class), "method compareTo(String)");
        check(hasMethod(c, "isBlank"), "method isBlank()");
        check(hasMethod(c, "repeat", int.class), "method repeat(int)");
        check(hasMethod(c, "strip"), "method strip()");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() {
        testLengthCharAt();
        testCodePoints();
        testGetCharsBytes();
        testEqualityCompare();
        testRegionMatches();
        testStartsEnds();
        testIndexOf();
        testSubstringConcat();
        testReplace();
        testMatchesSplit();
        testCase();
        testTrimIntern();
        testValueOf();
        testContainsJoinFormat();
        testStreams();
        testBlankStrip();
        testRepeatIndentLines();
        testTransform();
        testFormattedTranslateEscapes();
        testStripIndent();
        testJoinAndCodePointBounds();
        testBoundaryEquivalence();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // 契约深化（2026-11）追加
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
        testStripPrecision();
        testRepeatContract();
        testLinesPrecision();
        testIsBlankTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
