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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
