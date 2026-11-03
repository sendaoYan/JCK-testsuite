/*
 * @test
 * @summary java.util.regex.Pattern 一致性：白盒（反射结构）+ 黑盒（编译/匹配/拆分/谓词/流组合行为）。
 * @run main PatternTest
 *
 * 白盒（结构）：Class.forName 加载；final 类、实现 Serializable；静态标志常量；
 *   关键成员形态（compile/pattern/matcher/flags/matches/split/splitWithDelimiters/quote/
 *   namedGroups/asPredicate/asMatchPredicate/splitAsStream）。
 * 黑盒（行为）设计案见同目录 DESIGN.md。
 */

import java.util.Map;
import java.util.regex.Pattern;
import java.util.function.Predicate;

public class PatternTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasStaticIntField(Class<?> c, String name) {
        try {
            java.lang.reflect.Field f = c.getField(name);
            int m = f.getModifiers();
            return java.lang.reflect.Modifier.isPublic(m)
                   && java.lang.reflect.Modifier.isStatic(m)
                   && java.lang.reflect.Modifier.isFinal(m)
                   && f.getType() == int.class;
        } catch (NoSuchFieldException e) { return false; }
    }

    static void checkEq(String[] got, String... exp) {
        check(java.util.Arrays.equals(got, exp),
              "array mismatch: got " + java.util.Arrays.toString(got)
              + " expected " + java.util.Arrays.toString(exp));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.regex.Pattern");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "direct subclass of Object");

        // 静态标志常量
        check(hasStaticIntField(c, "UNIX_LINES"), "UNIX_LINES const");
        check(hasStaticIntField(c, "CASE_INSENSITIVE"), "CASE_INSENSITIVE const");
        check(hasStaticIntField(c, "COMMENTS"), "COMMENTS const");
        check(hasStaticIntField(c, "MULTILINE"), "MULTILINE const");
        check(hasStaticIntField(c, "LITERAL"), "LITERAL const");
        check(hasStaticIntField(c, "DOTALL"), "DOTALL const");
        check(hasStaticIntField(c, "UNICODE_CASE"), "UNICODE_CASE const");
        check(hasStaticIntField(c, "CANON_EQ"), "CANON_EQ const");
        check(hasStaticIntField(c, "UNICODE_CHARACTER_CLASS"), "UNICODE_CHARACTER_CLASS const");

        // 关键成员形态
        check(hasMethod(c, "compile", String.class), "compile(String)");
        check(hasMethod(c, "compile", String.class, int.class), "compile(String,int)");
        check(hasMethod(c, "pattern"), "pattern()");
        check(hasMethod(c, "matcher", CharSequence.class), "matcher(CharSequence)");
        check(hasMethod(c, "flags"), "flags()");
        check(hasMethod(c, "matches", String.class, CharSequence.class), "matches(String,CharSequence)");
        check(hasMethod(c, "split", CharSequence.class, int.class), "split(CharSequence,int)");
        check(hasMethod(c, "splitWithDelimiters", CharSequence.class, int.class),
              "splitWithDelimiters(CharSequence,int)");
        check(hasMethod(c, "split", CharSequence.class), "split(CharSequence)");
        check(hasMethod(c, "quote", String.class), "quote(String)");
        check(hasMethod(c, "namedGroups"), "namedGroups()");
        check(hasMethod(c, "asPredicate"), "asPredicate()");
        check(hasMethod(c, "asMatchPredicate"), "asMatchPredicate()");
        check(hasMethod(c, "splitAsStream", CharSequence.class), "splitAsStream(CharSequence)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCompileFlags() {
        Pattern p = Pattern.compile("a+");
        check(p.pattern().equals("a+"), "pattern() returns source");
        check(p.toString().equals("a+"), "toString() returns source");
        check(p.flags() == 0, "default flags 0");

        int f = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
        Pattern q = Pattern.compile("a+", f);
        check(q.flags() == f, "flags() returns combined flags");

        // UNICODE_CHARACTER_CLASS 隐式开启 UNICODE_CASE
        Pattern u = Pattern.compile("a", Pattern.UNICODE_CHARACTER_CLASS);
        check(u.flags() == (Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE),
              "UNICODE_CHARACTER_CLASS implies UNICODE_CASE in flags()");

        // 未知标志位抛 IllegalArgumentException
        expectThrows(() -> Pattern.compile("a", 0x400), IllegalArgumentException.class,
                     "unknown flag rejected");
    }

    static void testMatchesConvenience() {
        check(Pattern.matches("\\d+", "123"), "matches digit");
        check(!Pattern.matches("\\d+", "12a"), "!matches digit+letter");
        check(Pattern.matches("", ""), "empty matches empty");
        check(!Pattern.matches("a", ""), "!matches a vs empty");
        check(Pattern.matches("[a-z]+@[a-z]+\\.com", "bob@example.com"), "matches email");
    }

    static void testMatcherGroups() {
        Pattern p = Pattern.compile("([a-z]+)@([a-z]+)\\.com");
        java.util.regex.Matcher m = p.matcher("bob@example.com");
        check(m.find(), "find email");
        check(m.group().equals("bob@example.com"), "group(0) whole");
        check(m.group(1).equals("bob"), "group(1)");
        check(m.group(2).equals("example"), "group(2)");
        check(m.start() == 0 && m.end() == 15, "start/end whole");
        check(m.start(1) == 0 && m.end(1) == 3, "start/end group1");
        check(m.start(2) == 4 && m.end(2) == 11, "start/end group2");
        check(m.groupCount() == 2, "groupCount 2");
    }

    static void testFlagsBehavior() {
        // LITERAL
        check(Pattern.compile("a.b", Pattern.LITERAL).matcher("a.b").matches(), "LITERAL matches dot");
        check(!Pattern.compile("a.b", Pattern.LITERAL).matcher("axb").matches(), "LITERAL no wildcard");

        // CASE_INSENSITIVE
        check(Pattern.compile("abc", Pattern.CASE_INSENSITIVE).matcher("ABC").matches(),
              "CASE_INSENSITIVE");

        // MULTILINE
        check(!Pattern.compile("^b").matcher("a\nb").find(), "no MULTILINE ^b fails");
        check(Pattern.compile("^b", Pattern.MULTILINE).matcher("a\nb").find(), "MULTILINE ^b succeeds");

        // DOTALL
        check(!Pattern.compile("a.b").matcher("a\nb").matches(), "no DOTALL dot excludes newline");
        check(Pattern.compile("a.b", Pattern.DOTALL).matcher("a\nb").matches(), "DOTALL dot includes newline");

        // COMMENTS
        check(Pattern.compile("a b", Pattern.COMMENTS).matcher("ab").matches(),
              "COMMENTS ignores whitespace");

        // UNIX_LINES: 仅 \n 作为行终止符（默认 . 不匹配 \r，UNIX_LINES 下 \r 视为普通字符）
        check(!Pattern.compile("a.b").matcher("a\rb").matches(),
              "default dot excludes CR line terminator");
        check(Pattern.compile("a.b", Pattern.UNIX_LINES).matcher("a\rb").matches(),
              "UNIX_LINES treats only \\n as terminator");

        // UNICODE_CHARACTER_CLASS: \d 覆盖非 ASCII 数字
        check(!Pattern.compile("\\d").matcher("\u0661").matches(), "default \\d excludes Arabic-Indic digit");
        check(Pattern.compile("\\d", Pattern.UNICODE_CHARACTER_CLASS).matcher("\u0661").matches(),
              "UNICODE_CHARACTER_CLASS \\d includes Arabic-Indic digit");
    }

    static void testSplit() {
        checkEq(Pattern.compile(":").split("boo:and:foo"), "boo", "and", "foo");
        checkEq(Pattern.compile(":").split("boo:and:foo", 2), "boo", "and:foo");
        checkEq(Pattern.compile(":").split("boo:and:foo", 5), "boo", "and", "foo");
        checkEq(Pattern.compile(":").split("boo:and:foo", -1), "boo", "and", "foo");
        checkEq(Pattern.compile("o").split("boo:and:foo"), "b", "", ":and:f");

        // trailing empty：limit 0 丢弃，limit 负保留
        checkEq(Pattern.compile(",").split("a,b,"), "a", "b");
        checkEq(Pattern.compile(",").split("a,b,", -1), "a", "b", "");

        // 无匹配返回单元素数组
        checkEq(Pattern.compile("z").split("abc"), "abc");
    }

    static void testSplitWithDelimiters() {
        checkEq(Pattern.compile(":+").splitWithDelimiters("boo:::and::foo", 2),
                "boo", ":::", "and::foo");
        checkEq(Pattern.compile(":+").splitWithDelimiters("boo:::and::foo", 5),
                "boo", ":::", "and", "::", "foo");
        checkEq(Pattern.compile(":+").splitWithDelimiters("boo:::and::foo", -1),
                "boo", ":::", "and", "::", "foo");
        // limit 0 丢弃尾部空串（最后一个分隔符之后无剩余子串）
        checkEq(Pattern.compile("o").splitWithDelimiters("boo:::and::foo", 0),
                "b", "o", "", "o", ":::and::f", "o", "", "o");
    }

    static void testQuote() {
        check(Pattern.quote("a.b*c[d]").equals("\\Qa.b*c[d]\\E"), "quote wraps literal");
        String s = "a+b*c[d]\\E";
        check(Pattern.matches(Pattern.quote(s), s), "quote round-trips metachar string");
        check(Pattern.compile(Pattern.quote(s)).matcher(s).matches(), "compiled quote matches itself");
        check(!Pattern.compile(Pattern.quote("a.b")).matcher("axb").matches(),
              "quote disables metachars");
    }

    static void testNamedGroups() {
        Map<String, Integer> ng = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})").namedGroups();
        check(ng.size() == 2 && ng.get("year") == 1 && ng.get("month") == 2,
              "namedGroups maps names to numbers");
        check(Pattern.compile("abc").namedGroups().isEmpty(), "no named groups -> empty map");
        expectThrows(() -> Pattern.compile("(?<year>\\d{4})").namedGroups().put("x", 9),
                     UnsupportedOperationException.class, "namedGroups unmodifiable");
    }

    static void testPredicatesAndStream() {
        Predicate<String> findP = Pattern.compile("\\d+").asPredicate();
        check(findP.test("abc123") && !findP.test("abc"), "asPredicate uses find");

        Predicate<String> matchP = Pattern.compile("\\d+").asMatchPredicate();
        check(matchP.test("123") && !matchP.test("abc123"), "asMatchPredicate uses matches");

        Object[] parts = Pattern.compile(":").splitAsStream("boo:and:foo").toArray();
        check(java.util.Arrays.equals(parts, new Object[]{"boo", "and", "foo"}),
              "splitAsStream content");
        check(Pattern.compile(":").splitAsStream("boo:and:foo").count() == 3,
              "splitAsStream count");
    }

    static void testEdgePatterns() {
        check(Pattern.compile("a*").matcher("").matches(), "a* matches empty");
        check(!Pattern.compile("a+").matcher("").matches(), "a+ does not match empty");
        check(Pattern.compile("").matcher("").matches(), "empty pattern matches empty");
        check(Pattern.compile("(?i)abc").matcher("ABC").matches(), "inline flag (?i)");
        check(Pattern.compile("a{2,3}").matcher("aaa").matches(), "quantifier exact");
        check(Pattern.compile("\\bword\\b").matcher("a word here").find(), "word boundary");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCompileFlags();
        testMatchesConvenience();
        testMatcherGroups();
        testFlagsBehavior();
        testSplit();
        testSplitWithDelimiters();
        testQuote();
        testNamedGroups();
        testPredicatesAndStream();
        testEdgePatterns();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
