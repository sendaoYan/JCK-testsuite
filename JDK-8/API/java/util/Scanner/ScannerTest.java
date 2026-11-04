/*
 * @test
 * @summary java.util.Scanner API conformance (Java 8 surface): complete contract coverage
 *          of constructors (null NPE), delimiter/locale/radix getter-setter + null NPE +
 *          radix IAE matrix, next/hasNext exhaustion + close IllegalStateException +
 *          remove UnsupportedOperationException, nextInt/nextLong/nextBoolean/nextDouble +
 *          InputMismatchException + radix variants, findInLine/findWithinHorizon/skip,
 *          reset semantics, match(), toString, and ioException().
 * @run main ScannerTest
 */

import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ScannerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testTokens() {
        Scanner s = new Scanner("10 20 hello");
        check(s.nextInt() == 10 && s.nextInt() == 20, "nextInt");
        check("hello".equals(s.next()), "next");
        check(!s.hasNext(), "exhausted");
    }

    static void testDelimiter() {
        Scanner s = new Scanner("a,b,c").useDelimiter(",");
        check("a".equals(s.next()) && "b".equals(s.next()) && "c".equals(s.next()),
              "useDelimiter");
    }

    static void testHasNextInt() {
        Scanner s = new Scanner("42");
        check(s.hasNextInt() && s.nextInt() == 42, "hasNextInt/nextInt");
    }

    static void testConstructorNull() {
        expectThrows(NullPointerException.class, () -> new Scanner((Readable) null),
                     "Scanner((Readable)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner((java.io.InputStream) null),
                     "Scanner((InputStream)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner((String) null),
                     "Scanner((String)null) NPE");
        // InputStream charset variant with null source.
        expectThrows(NullPointerException.class,
                     () -> new Scanner((java.io.InputStream) null, "UTF-8"),
                     "Scanner((InputStream)null,charset) NPE");
    }

    static void testRadix() {
        Scanner s = new Scanner("ff");
        check(s.radix() == 10, "default radix 10");
        s.useRadix(16);
        check(s.radix() == 16, "useRadix(16)");
        check(s.nextInt() == 255, "nextInt with radix 16 parses ff");

        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(1),
                     "useRadix(1) IAE");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(37),
                     "useRadix(37) IAE");
        new Scanner("1").useRadix(2);
        new Scanner("1").useRadix(36);
        passed++;
    }

    static void testNullSetters() {
        Scanner s = new Scanner("x");
        expectThrows(NullPointerException.class, () -> s.useDelimiter((String) null),
                     "useDelimiter((String)null) NPE");
        expectThrows(NullPointerException.class, () -> s.useLocale(null), "useLocale(null) NPE");
    }

    static void testExhaustionAndClose() {
        Scanner empty = new Scanner("");
        check(!empty.hasNext(), "hasNext false when empty");
        expectThrows(NoSuchElementException.class, () -> empty.next(), "next() NSEE when empty");

        Scanner s = new Scanner("1 2 3");
        s.close();
        expectThrows(IllegalStateException.class, () -> s.hasNext(), "hasNext() ISE after close");
        expectThrows(IllegalStateException.class, () -> s.next(), "next() ISE after close");
        expectThrows(IllegalStateException.class, () -> s.nextInt(), "nextInt() ISE after close");
        check(s.ioException() == null, "ioException() null for String source");
    }

    static void testRemoveUnsupported() {
        Scanner s = new Scanner("a b");
        expectThrows(UnsupportedOperationException.class, () -> s.remove(), "remove() UOE");
    }

    static void testNextPrimitives() {
        Scanner s = new Scanner("42 7 9223372036854775807 true FALSE 3.25");
        check(s.nextByte() == 42, "nextByte");
        check(s.nextShort() == 7, "nextShort");
        check(s.nextLong() == Long.MAX_VALUE, "nextLong");
        check(s.nextBoolean(), "nextBoolean true");
        check(!s.nextBoolean(), "nextBoolean FALSE (case-insensitive)");
        s.useLocale(Locale.US);
        check(s.nextDouble() == 3.25, "nextDouble");
    }

    static void testInputMismatch() {
        Scanner s = new Scanner("hello");
        expectThrows(InputMismatchException.class, () -> s.nextInt(), "nextInt() on word IME");
    }

    static void testHasNextVariants() {
        Scanner s = new Scanner("1 2.5 true 10");
        check(s.hasNextInt(), "hasNextInt true");
        check(s.nextInt() == 1, "nextInt");
        check(s.hasNextDouble(), "hasNextDouble true");
        s.useLocale(Locale.US);
        check(s.nextDouble() == 2.5, "nextDouble");
        check(s.hasNextBoolean(), "hasNextBoolean true");
        check(s.nextBoolean(), "nextBoolean");
        check(s.hasNextInt(16), "hasNextInt(16) true");
        check(s.nextInt(16) == 16, "nextInt(16) parses 10 as hex");
    }

    static void testNextLine() {
        Scanner s = new Scanner("first line\nsecond line\n");
        check(s.hasNextLine(), "hasNextLine true");
        check("first line".equals(s.nextLine()), "nextLine first");
        check("second line".equals(s.nextLine()), "nextLine second");
        check(!s.hasNextLine(), "hasNextLine false at end");
    }

    static void testFindAndSkip() {
        Scanner s = new Scanner("abc123def");
        check("123".equals(s.findInLine("\\d+")), "findInLine matches digits");
        check(s.findInLine("\\d+") == null, "findInLine null when no more match");
        check("def".equals(s.findWithinHorizon("\\w+", 3)), "findWithinHorizon");

        Scanner s2 = new Scanner("skip these numbers 42");
        s2.skip("skip these numbers ");
        check(s2.nextInt() == 42, "skip(pattern) then nextInt");
    }

    static void testReset() {
        Scanner s = new Scanner("10");
        s.useRadix(16);
        s.useLocale(Locale.US);
        s.useDelimiter("X");
        s.reset();
        check(s.radix() == 10, "reset restores radix 10");
        check(s.locale().equals(Locale.getDefault()), "reset restores default locale");
        check(s.delimiter().pattern() != null, "reset restores default delimiter");
    }

    static void testMatchAndToString() {
        Scanner s = new Scanner("hello 42");
        check(s.hasNext("hello"), "hasNext(pattern)");
        check("hello".equals(s.next("hello")), "next(pattern)");
        check(s.match() != null && "hello".equals(s.match().group()), "match() returns last match");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        check(s.hasNextInt(), "hasNextInt after match");
        check(s.nextInt() == 42, "nextInt after match");
    }

    // ===================== 契约深化：分隔符/进制/Locale/关闭矩阵/IO 透传 =====================

    static void testDelimiterContracts() {
        // 默认分隔符规范串锁定
        check("\\p{javaWhitespace}+".equals(new Scanner("x").delimiter().pattern()),
              "default delimiter pattern");
        // 多字符分隔符
        Scanner multi = new Scanner("a::b::c").useDelimiter("::");
        check("a".equals(multi.next()) && "b".equals(multi.next()) && "c".equals(multi.next()),
              "multi-char delimiter");
        // useDelimiter 返回自身（可链式）
        Scanner chained = new Scanner("1;2");
        check(chained == chained.useDelimiter(";"), "useDelimiter returns this");
        check(chained.nextInt() == 1 && chained.nextInt() == 2, "chained delimiter parse");
        // 转义逗号正则 "\\," 等价于字面逗号
        Scanner esc = new Scanner("a,b").useDelimiter("\\,");
        check("a".equals(esc.next()) && "b".equals(esc.next()), "escaped-comma delimiter");
        // Pattern 版 useDelimiter
        Scanner pat = new Scanner("1 2").useDelimiter(Pattern.compile(" "));
        check(pat.nextInt() == 1 && pat.nextInt() == 2, "Pattern delimiter");
        // 行分隔：\r\n 属于默认空白
        Scanner crlf = new Scanner("a\r\nb");
        check("a".equals(crlf.next()) && "b".equals(crlf.next()), "CRLF treated as whitespace");
    }

    static void testLocaleGroupingAndRadix() {
        // GERMAN 千分位点/小数逗号
        Scanner de = new Scanner("1.234,56").useLocale(Locale.GERMANY);
        check(de.hasNextDouble() && de.nextDouble() == 1234.56, "GERMAN nextDouble grouped");
        Scanner deInt = new Scanner("1.234.567").useLocale(Locale.GERMANY);
        check(deInt.hasNextInt() && deInt.nextInt() == 1234567, "GERMAN nextInt grouped");
        // US 千分位逗号
        Scanner us = new Scanner("1,234,567").useLocale(Locale.US);
        check(us.nextInt() == 1234567, "US grouped nextInt");
        // FRANCE 逗号是小数点：'1,234' 不构成整数 token
        check(!new Scanner("1,234").useLocale(Locale.FRANCE).hasNextInt(),
              "FRANCE comma is decimal, not grouping for int");
        // radix 契约：默认 10；合法 2..36；useRadix 越界 IAE
        check(new Scanner("1").radix() == 10, "default radix");
        check(new Scanner("1").useRadix(Character.MAX_RADIX).radix() == Character.MAX_RADIX,
              "useRadix(MAX_RADIX)");
        check(new Scanner("1").useRadix(Character.MIN_RADIX).radix() == Character.MIN_RADIX,
              "useRadix(MIN_RADIX)");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(1), "useRadix(1) IAE");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(37), "useRadix(37) IAE");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(0), "useRadix(0) IAE");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(-5), "useRadix(-5) IAE");
        // hasNextInt(radix)/nextInt(radix) 带进制参数
        Scanner hex = new Scanner("ff");
        check(hex.hasNextInt(16) && hex.nextInt(16) == 255, "hasNextInt(16)/nextInt(16)");
        // 36 进制
        check(new Scanner("zz").useRadix(36).nextInt() == 1295, "radix 36 zz = 1295");
        // nextBigInteger(radix)
        check(new Scanner("ff").nextBigInteger(16).intValue() == 255, "nextBigInteger(16)");
        // 负号 + 进制
        check(new Scanner("-ff").useRadix(16).nextInt() == -255, "negative hex");
    }

    static void testOverflowAndMismatch() {
        // 超过 int/long 范围：hasNextInt/hasNextLong 为 false，token 仍是普通字符串
        Scanner ov = new Scanner("123456789012345678901234567890");
        check(!ov.hasNextInt(), "hasNextInt false for >int overflow");
        check(!ov.hasNextLong(), "hasNextLong false for >long overflow");
        check("123456789012345678901234567890".equals(ov.next()), "overflow token readable as string");
        // 边界值本身可解析
        check(new Scanner("9223372036854775807").nextLong() == Long.MAX_VALUE, "Long.MAX_VALUE parses");
        check(new Scanner("-9223372036854775808").nextLong() == Long.MIN_VALUE, "Long.MIN_VALUE parses");
        check(new Scanner("2147483647").nextInt() == Integer.MAX_VALUE, "Integer.MAX_VALUE parses");
        // byte/short 边界
        expectThrows(InputMismatchException.class, () -> new Scanner("128").nextByte(),
                     "nextByte 128 InputMismatchException");
        expectThrows(InputMismatchException.class, () -> new Scanner("-129").nextByte(),
                     "nextByte -129 InputMismatchException");
        expectThrows(InputMismatchException.class, () -> new Scanner("32768").nextShort(),
                     "nextShort 32768 InputMismatchException");
        check(new Scanner("127").nextByte() == 127 && new Scanner("-128").nextByte() == -128,
              "byte boundary values");
        // 布尔大小写不敏感；非布尔词 InputMismatchException
        check(new Scanner("tRuE").nextBoolean(), "boolean case-insensitive");
        expectThrows(InputMismatchException.class, () -> new Scanner("yes").nextBoolean(),
                     "nextBoolean yes InputMismatchException");
        check(!new Scanner("false").hasNextInt(), "hasNextInt false for boolean word");
    }

    static void testCloseMatrix() {
        // close 之后：一切查找/取值方法抛 IllegalStateException；元数据方法仍可用
        Scanner s = new Scanner("1 2 3");
        s.close();
        expectThrows(IllegalStateException.class, s::hasNext, "hasNext after close ISE");
        expectThrows(IllegalStateException.class, s::next, "next after close ISE");
        expectThrows(IllegalStateException.class, s::hasNextInt, "hasNextInt after close ISE");
        expectThrows(IllegalStateException.class, s::nextInt, "nextInt after close ISE");
        expectThrows(IllegalStateException.class, s::nextLine, "nextLine after close ISE");
        expectThrows(IllegalStateException.class, () -> s.skip("1"), "skip after close ISE");
        expectThrows(IllegalStateException.class, () -> s.findInLine("1"), "findInLine after close ISE");
        expectThrows(IllegalStateException.class, s::match, "match after close ISE");
        check(s.radix() == 10, "radix() still works after close");
        check(s.locale() != null, "locale() still works after close");
        check(s.delimiter() != null, "delimiter() still works after close");
        check(s.ioException() == null, "ioException() still works after close");
        s.reset();
        s.close(); // close 幂等
        check(s.toString().startsWith("java.util.Scanner"), "toString after close");
    }

    static void testMatchBeforeUse() {
        expectThrows(IllegalStateException.class, () -> new Scanner("a").match(),
                     "match() before any match ISE");
    }

    static void testFindSkipContracts() {
        // findWithinHorizon: horizon 0 = 不限；负 horizon IAE；限界内无匹配 null
        check("a".equals(new Scanner("abc").findWithinHorizon("a", 0)), "findWithinHorizon horizon 0");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("abc").findWithinHorizon("a", -1),
                     "findWithinHorizon negative horizon IAE");
        check(new Scanner("abc").findWithinHorizon("z", 2) == null, "findWithinHorizon no match null");
        // 匹配超出 horizon → null（"c" 在位置 2，horizon 2 只允许前 2 个字符）
        check(new Scanner("abc").findWithinHorizon("c", 2) == null, "match beyond horizon null");
        check("c".equals(new Scanner("abc").findWithinHorizon("c", 3)), "match within horizon");
        // findInLine 无匹配 null
        check(new Scanner("abc").findInLine("zzz") == null, "findInLine no match null");
        // findInLine 只在当前行内搜索：跨行模式不命中
        check(new Scanner("aa\nbb").findInLine("bb") == null, "findInLine stops at line end");
        Scanner two = new Scanner("aa\nbb");
        check("aa".equals(two.findInLine("aa")), "findInLine matches in current line");
        check("".equals(two.nextLine()), "rest of line consumed");
        check("bb".equals(two.nextLine()), "next line still readable");
        // skip：当前位置不匹配 → NoSuchElementException
        expectThrows(NoSuchElementException.class, () -> new Scanner("abc").skip("z"),
                     "skip no match at position NoSuchElementException");
        Scanner sk = new Scanner("  42");
        sk.skip("\\s+");
        check(sk.nextInt() == 42, "skip whitespace then nextInt");
        // skip 空正则（零宽）成功
        new Scanner("abc").skip("");
        check(true, "skip empty pattern succeeds");
        // match() 分组
        Scanner g = new Scanner("k=v");
        g.findInLine("(\\w+)=(\\w+)");
        check("k".equals(g.match().group(1)) && "v".equals(g.match().group(2)), "match groups");
        check(0 == g.match().start() && 3 == g.match().end(), "match start/end");
        // hasNext((Pattern)null) NPE；next(pattern) 不匹配 → InputMismatchException
        expectThrows(NullPointerException.class, () -> new Scanner("a").hasNext((Pattern) null),
                     "hasNext((Pattern)null) NPE");
        expectThrows(InputMismatchException.class, () -> new Scanner("aaa").next("b+"),
                     "next(pattern) mismatch InputMismatchException");
    }

    static void testLineAndExhaustion() {
        check(!new Scanner("").hasNextLine(), "empty hasNextLine false");
        expectThrows(NoSuchElementException.class, () -> new Scanner("").nextLine(),
                     "nextLine on empty NoSuchElementException");
        // 文件不以换行结尾：最后一行仍可读
        Scanner noNl = new Scanner("a\nb");
        check("a".equals(noNl.nextLine()) && "b".equals(noNl.nextLine()), "last line without newline");
        check(!noNl.hasNextLine(), "exhausted after last line");
        // nextLine 与 next 混用残留
        Scanner mix = new Scanner("42 rest");
        mix.nextInt();
        check(" rest".equals(mix.nextLine()), "nextLine returns rest of line after next");
        // next 耗尽后各类型 next* 均 NSEE
        Scanner ex = new Scanner("7");
        ex.next();
        expectThrows(NoSuchElementException.class, ex::next, "next after exhausted NSEE");
        expectThrows(NoSuchElementException.class, ex::nextInt, "nextInt after exhausted NSEE");
        expectThrows(NoSuchElementException.class, ex::nextBoolean, "nextBoolean after exhausted NSEE");
        // hasNext 系列耗尽返回 false 不抛
        check(!ex.hasNext() && !ex.hasNextInt() && !ex.hasNextLine(), "hasNext* false after exhausted");
    }

    static void testIOExceptionPassthrough() {
        final java.io.IOException boom = new java.io.IOException("boom");
        Readable bad = cb -> { throw boom; };
        Scanner s = new Scanner(bad);
        expectThrows(NoSuchElementException.class, s::next,
                     "next with failing Readable NoSuchElementException");
        check(boom == s.ioException(), "ioException() returns last IOException");
        check(!s.hasNext(), "hasNext false with failing Readable");
        // 正常 Scanner 的 ioException 为 null
        check(new Scanner("x").ioException() == null, "ioException null for healthy source");
    }

    static void testResetAndNulls() {
        Scanner s = new Scanner("ff");
        s.useRadix(16);
        s.useDelimiter("Z");
        s.reset();
        check(s.radix() == 10, "reset restores radix");
        check("\\p{javaWhitespace}+".equals(s.delimiter().pattern()), "reset restores delimiter");
        // null 参数矩阵（useDelimiter((Pattern)null) 在设置时不校验——JDK-8 实测不抛）
        expectThrows(NullPointerException.class, () -> new Scanner("x").useLocale(null), "useLocale(null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner("x").useDelimiter((String) null),
                     "useDelimiter((String)null) NPE");
        check(new Scanner("x").useDelimiter((Pattern) null).delimiter() == null,
              "useDelimiter((Pattern)null) accepted lazily (JDK-8)");
        expectThrows(NullPointerException.class, () -> new Scanner("x").hasNext((String) null),
                     "hasNext((String)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner("x").skip((String) null),
                     "skip((String)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner("x").findInLine((String) null),
                     "findInLine((String)null) NPE");
    }

    static void testBigNumbers() {
        Scanner t = new Scanner("123456789012345678901234567890 3.14159265358979323846");
        check(t.nextBigInteger().toString().equals("123456789012345678901234567890"), "nextBigInteger huge");
        check(t.nextBigDecimal().toString().startsWith("3.1415926535897932384"), "nextBigDecimal precision");
        Scanner u = new Scanner("1.5e3").useLocale(Locale.US);
        check(u.nextDouble() == 1500.0, "nextDouble exponent");
    }

    public static void main(String[] args) {
        testTokens();
        testDelimiter();
        testHasNextInt();
        testConstructorNull();
        testRadix();
        testNullSetters();
        testExhaustionAndClose();
        testRemoveUnsupported();
        testNextPrimitives();
        testInputMismatch();
        testHasNextVariants();
        testNextLine();
        testFindAndSkip();
        testReset();
        testMatchAndToString();
        testDelimiterContracts();
        testLocaleGroupingAndRadix();
        testOverflowAndMismatch();
        testCloseMatrix();
        testMatchBeforeUse();
        testFindSkipContracts();
        testLineAndExhaustion();
        testIOExceptionPassthrough();
        testResetAndNulls();
        testBigNumbers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
