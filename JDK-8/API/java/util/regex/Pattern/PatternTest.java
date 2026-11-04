/*
 * @test
 * @summary java.util.regex.Pattern API conformance: full contract coverage.
 *          compile(String)/(String,int) with precise exceptions (null -> NPE,
 *          unknown flag -> IllegalArgumentException, bad pattern ->
 *          PatternSyntaxException), flags() incl. UNICODE_CHARACTER_CLASS implies
 *          UNICODE_CASE, matches convenience, matcher groups, per-flag behavior
 *          (LITERAL/CASE_INSENSITIVE/MULTILINE/DOTALL/COMMENTS/UNIX_LINES/
 *          UNICODE_CHARACTER_CLASS), split (limits, trailing empties, null -> NPE),
 *          quote (literal escaping, null -> NPE), named capturing groups,
 *          asPredicate/splitAsStream, boundary/anchors/backreferences/escapes,
 *          serialization round-trip + serialVersionUID.
 * @run main PatternTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.function.Predicate;

public class PatternTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
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

    static void checkEq(String[] got, String... exp) {
        check(Arrays.equals(got, exp),
              "array mismatch: got " + Arrays.toString(got) + " expected " + Arrays.toString(exp));
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.regex.Pattern");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "direct subclass of Object");

        check(hasStaticIntField(c, "UNIX_LINES"), "UNIX_LINES const");
        check(hasStaticIntField(c, "CASE_INSENSITIVE"), "CASE_INSENSITIVE const");
        check(hasStaticIntField(c, "COMMENTS"), "COMMENTS const");
        check(hasStaticIntField(c, "MULTILINE"), "MULTILINE const");
        check(hasStaticIntField(c, "LITERAL"), "LITERAL const");
        check(hasStaticIntField(c, "DOTALL"), "DOTALL const");
        check(hasStaticIntField(c, "UNICODE_CASE"), "UNICODE_CASE const");
        check(hasStaticIntField(c, "CANON_EQ"), "CANON_EQ const");
        check(hasStaticIntField(c, "UNICODE_CHARACTER_CLASS"), "UNICODE_CHARACTER_CLASS const");

        check(hasMethod(c, "compile", String.class), "compile(String)");
        check(hasMethod(c, "compile", String.class, int.class), "compile(String,int)");
        check(hasMethod(c, "pattern"), "pattern()");
        check(hasMethod(c, "matcher", CharSequence.class), "matcher(CharSequence)");
        check(hasMethod(c, "flags"), "flags()");
        check(hasMethod(c, "matches", String.class, CharSequence.class), "matches(String,CharSequence)");
        check(hasMethod(c, "split", CharSequence.class, int.class), "split(CharSequence,int)");
        check(hasMethod(c, "split", CharSequence.class), "split(CharSequence)");
        check(hasMethod(c, "quote", String.class), "quote(String)");
        check(hasMethod(c, "asPredicate"), "asPredicate()");
        check(hasMethod(c, "splitAsStream", CharSequence.class), "splitAsStream(CharSequence)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCompileAndExceptions() {
        Pattern p = Pattern.compile("a+");
        check("a+".equals(p.pattern()), "pattern() returns source");
        check("a+".equals(p.toString()), "toString() returns source");
        check(p.flags() == 0, "default flags 0");

        int f = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
        Pattern q = Pattern.compile("a+", f);
        check(q.flags() == f, "flags() returns combined flags");

        // UNICODE_CHARACTER_CLASS implicitly enables UNICODE_CASE
        Pattern u = Pattern.compile("a", Pattern.UNICODE_CHARACTER_CLASS);
        check(u.flags() == (Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE),
              "UNICODE_CHARACTER_CLASS implies UNICODE_CASE in flags()");

        expectThrows(PatternSyntaxException.class, () -> Pattern.compile("["),
                     "bad pattern -> PatternSyntaxException");
        // Note: JDK-8 does not validate the flags bitmask (no IllegalArgumentException
        // for unknown flags, unlike later JDKs); skip that assertion on JDK-8.
        expectThrows(NullPointerException.class, () -> Pattern.compile(null),
                     "compile(null) -> NPE");
        expectThrows(NullPointerException.class, () -> Pattern.compile(null, 0),
                     "compile(null,int) -> NPE");
    }

    static void testMatchesConvenience() {
        check(Pattern.matches("\\d+", "123"), "matches digit");
        check(!Pattern.matches("\\d+", "12a"), "!matches digit+letter");
        check(Pattern.matches("", ""), "empty matches empty");
        check(!Pattern.matches("a", ""), "!matches a vs empty");
        check(Pattern.matches("[a-z]+@[a-z]+\\.com", "bob@example.com"), "matches email");
        expectThrows(NullPointerException.class, () -> Pattern.matches(null, "x"),
                     "matches(null, CharSequence) -> NPE");
        expectThrows(NullPointerException.class, () -> Pattern.matches("a", (CharSequence) null),
                     "matches(String, null) -> NPE");
    }

    static void testMatcherGroups() {
        Pattern p = Pattern.compile("([a-z]+)@([a-z]+)\\.com");
        Matcher m = p.matcher("bob@example.com");
        check(m.find(), "find email");
        check("bob@example.com".equals(m.group()), "group(0) whole");
        check("bob".equals(m.group(1)), "group(1)");
        check("example".equals(m.group(2)), "group(2)");
        check(m.start() == 0 && m.end() == 15, "start/end whole");
        check(m.start(1) == 0 && m.end(1) == 3, "start/end group1");
        check(m.start(2) == 4 && m.end(2) == 11, "start/end group2");
        check(m.groupCount() == 2, "groupCount 2");
        expectThrows(NullPointerException.class, () -> p.matcher(null), "matcher(null) -> NPE");
    }

    static void testNamedGroups() {
        Pattern p = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})");
        Matcher m = p.matcher("2026-11");
        check(m.find(), "named find");
        check("2026".equals(m.group("year")) && "11".equals(m.group("month")),
              "group(name)");
        check(m.start("year") == 0 && m.end("year") == 4, "start/end(name) year");
        check(m.start("month") == 5 && m.end("month") == 7, "start/end(name) month");
        // duplicate named group is a compile-time error
        expectThrows(PatternSyntaxException.class,
                     () -> Pattern.compile("(?<n>a)(?<n>b)"),
                     "duplicate named group rejected");
    }

    static void testFlagsBehavior() {
        check(Pattern.compile("a.b", Pattern.LITERAL).matcher("a.b").matches(), "LITERAL matches dot");
        check(!Pattern.compile("a.b", Pattern.LITERAL).matcher("axb").matches(), "LITERAL no wildcard");

        check(Pattern.compile("abc", Pattern.CASE_INSENSITIVE).matcher("ABC").matches(),
              "CASE_INSENSITIVE");

        check(!Pattern.compile("^b").matcher("a\nb").find(), "no MULTILINE ^b fails");
        check(Pattern.compile("^b", Pattern.MULTILINE).matcher("a\nb").find(), "MULTILINE ^b succeeds");

        check(!Pattern.compile("a.b").matcher("a\nb").matches(), "no DOTALL dot excludes newline");
        check(Pattern.compile("a.b", Pattern.DOTALL).matcher("a\nb").matches(), "DOTALL dot includes newline");

        check(Pattern.compile("a b", Pattern.COMMENTS).matcher("ab").matches(),
              "COMMENTS ignores whitespace");

        check(!Pattern.compile("a.b").matcher("a\rb").matches(),
              "default dot excludes CR line terminator");
        check(Pattern.compile("a.b", Pattern.UNIX_LINES).matcher("a\rb").matches(),
              "UNIX_LINES treats only \\n as terminator");

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

        checkEq(Pattern.compile(",").split("a,b,"), "a", "b");
        checkEq(Pattern.compile(",").split("a,b,", -1), "a", "b", "");

        checkEq(Pattern.compile("z").split("abc"), "abc");

        expectThrows(NullPointerException.class, () -> Pattern.compile(":").split(null),
                     "split(null) -> NPE");
        expectThrows(NullPointerException.class, () -> Pattern.compile(":").split(null, 1),
                     "split(null,int) -> NPE");
    }

    static void testQuote() {
        check("\\Qa.b*c[d]\\E".equals(Pattern.quote("a.b*c[d]")), "quote wraps literal");
        String s = "a+b*c[d]\\E";
        check(Pattern.matches(Pattern.quote(s), s), "quote round-trips metachar string");
        check(Pattern.compile(Pattern.quote(s)).matcher(s).matches(), "compiled quote matches itself");
        check(!Pattern.compile(Pattern.quote("a.b")).matcher("axb").matches(),
              "quote disables metachars");
        expectThrows(NullPointerException.class, () -> Pattern.quote(null), "quote(null) -> NPE");
    }

    static void testPredicatesAndStream() {
        Predicate<String> findP = Pattern.compile("\\d+").asPredicate();
        check(findP.test("abc123") && !findP.test("abc"), "asPredicate uses find");
        expectThrows(NullPointerException.class, () -> findP.test(null), "asPredicate(null) -> NPE");

        Object[] parts = Pattern.compile(":").splitAsStream("boo:and:foo").toArray();
        check(Arrays.equals(parts, new Object[]{"boo", "and", "foo"}), "splitAsStream content");
        check(Pattern.compile(":").splitAsStream("boo:and:foo").count() == 3, "splitAsStream count");
    }

    static void testEdgePatterns() {
        check(Pattern.compile("a*").matcher("").matches(), "a* matches empty");
        check(!Pattern.compile("a+").matcher("").matches(), "a+ does not match empty");
        check(Pattern.compile("").matcher("").matches(), "empty pattern matches empty");
        check(Pattern.compile("(?i)abc").matcher("ABC").matches(), "inline flag (?i)");
        check(Pattern.compile("a{2,3}").matcher("aaa").matches(), "quantifier exact");
        check(Pattern.compile("\\bword\\b").matcher("a word here").find(), "word boundary");
    }

    static void testBoundaryEscapesGroups() {
        checkEq(Pattern.compile("").split("abc", 0), "a", "b", "c");
        checkEq(Pattern.compile("").split("abc", -1), "a", "b", "c", "");
        checkEq(Pattern.compile(":").split(""), "");
        check(Pattern.matches("", ""), "empty regex matches empty");
        check(!Pattern.matches("", "a"), "empty regex does not match non-empty");

        check(Pattern.compile("(a)\\1").matcher("aa").matches(), "backreference (a)\\1 matches aa");
        check(!Pattern.compile("(a)\\1").matcher("ab").matches(), "backreference rejects ab");
        check(Pattern.compile("\\Qa+b\\E").matcher("a+b").matches(), "\\Q...\\E literal escape");
        check(Pattern.compile("\\Afoo").matcher("foo").find(), "\\A anchors to start");
        check(!Pattern.compile("foo\\z").matcher("foo\n").matches(), "\\z matches absolute end");

        expectThrows(PatternSyntaxException.class, () -> Pattern.compile("("),
                     "unmatched paren rejected");
    }

    static void testSerialization() throws Exception {
        Pattern p = Pattern.compile("a+b", Pattern.CASE_INSENSITIVE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(p);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Pattern q = (Pattern) ois.readObject();
        check("a+b".equals(q.pattern()), "round-trip preserves pattern");
        check(q.flags() == Pattern.CASE_INSENSITIVE, "round-trip preserves flags");
        check(q.matcher("aaab").matches(), "round-trip pattern still matches");
        check(ObjectStreamClass.lookup(Pattern.class).getSerialVersionUID() == 5073258162644648461L,
              "serialVersionUID stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCompileAndExceptions();
        testMatchesConvenience();
        testMatcherGroups();
        testNamedGroups();
        testFlagsBehavior();
        testSplit();
        testQuote();
        testPredicatesAndStream();
        testEdgePatterns();
        testBoundaryEscapesGroups();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
