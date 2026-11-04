/*
 * @test
 * @summary java.util.regex.Matcher API conformance: full contract coverage.
 *          find loop, matches/lookingAt/find(int) anchoring differences, nested and
 *          named groups, non-participating groups (null/-1), IllegalStateException
 *          before a match, IndexOutOfBoundsException/IllegalArgumentException/NPE
 *          on group index/name errors, reset/usePattern reuse, region (valid/invalid),
 *          transparent/anchoring bounds, hitEnd/requireEnd, appendReplacement/appendTail
 *          (StringBuffer), replaceAll/replaceFirst with $ group refs, quoteReplacement,
 *          toMatchResult immutable snapshot, and zero-width/backreference edge cases.
 * @run main MatcherTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.regex.Matcher");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(MatchResult.class.isAssignableFrom(c), "implements MatchResult");
        check(c.getConstructors().length == 0, "no public constructor");

        check(hasMethod(c, "pattern"), "pattern()");
        check(hasMethod(c, "toMatchResult"), "toMatchResult()");
        check(hasMethod(c, "usePattern", Pattern.class), "usePattern(Pattern)");
        check(hasMethod(c, "reset"), "reset()");
        check(hasMethod(c, "reset", CharSequence.class), "reset(CharSequence)");
        check(hasMethod(c, "start"), "start()");
        check(hasMethod(c, "start", int.class), "start(int)");
        check(hasMethod(c, "start", String.class), "start(String)");
        check(hasMethod(c, "end"), "end()");
        check(hasMethod(c, "end", int.class), "end(int)");
        check(hasMethod(c, "end", String.class), "end(String)");
        check(hasMethod(c, "group"), "group()");
        check(hasMethod(c, "group", int.class), "group(int)");
        check(hasMethod(c, "group", String.class), "group(String)");
        check(hasMethod(c, "groupCount"), "groupCount()");
        check(hasMethod(c, "matches"), "matches()");
        check(hasMethod(c, "find"), "find()");
        check(hasMethod(c, "find", int.class), "find(int)");
        check(hasMethod(c, "lookingAt"), "lookingAt()");
        check(hasMethod(c, "quoteReplacement", String.class), "quoteReplacement(String)");
        check(hasMethod(c, "appendReplacement", StringBuffer.class, String.class),
              "appendReplacement(StringBuffer,String)");
        check(hasMethod(c, "appendTail", StringBuffer.class), "appendTail(StringBuffer)");
        check(hasMethod(c, "replaceAll", String.class), "replaceAll(String)");
        check(hasMethod(c, "replaceFirst", String.class), "replaceFirst(String)");
        check(hasMethod(c, "region", int.class, int.class), "region(int,int)");
        check(hasMethod(c, "regionStart"), "regionStart()");
        check(hasMethod(c, "regionEnd"), "regionEnd()");
        check(hasMethod(c, "useTransparentBounds", boolean.class), "useTransparentBounds(boolean)");
        check(hasMethod(c, "hasTransparentBounds"), "hasTransparentBounds()");
        check(hasMethod(c, "useAnchoringBounds", boolean.class), "useAnchoringBounds(boolean)");
        check(hasMethod(c, "hasAnchoringBounds"), "hasAnchoringBounds()");
        check(hasMethod(c, "hitEnd"), "hitEnd()");
        check(hasMethod(c, "requireEnd"), "requireEnd()");
        check(hasMethod(c, "toString"), "toString()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testFindLoop() {
        Matcher m = Pattern.compile("(\\w+)=(\\d+)").matcher("a=1 b=22 c=333");
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        while (m.find()) {
            keys.add(m.group(1));
            vals.add(m.group(2));
            starts.add(m.start());
            ends.add(m.end());
        }
        check(keys.equals(Arrays.asList("a", "b", "c")), "find loop group1");
        check(vals.equals(Arrays.asList("1", "22", "333")), "find loop group2");
        check(starts.equals(Arrays.asList(0, 4, 9)), "find loop start");
        check(ends.equals(Arrays.asList(3, 8, 14)), "find loop end");
    }

    static void testMatchesLookingAtFind() {
        Matcher m = Pattern.compile("\\d+").matcher("123");
        check(m.matches(), "matches full digits");
        check("123".equals(m.group()), "group after matches");

        m = Pattern.compile("\\d+").matcher("123abc");
        check(m.lookingAt(), "lookingAt matches prefix");
        check("123".equals(m.group()), "lookingAt group");
        check(!m.matches(), "matches fails for trailing letters");

        m = Pattern.compile("\\d+").matcher("abc");
        check(!m.lookingAt(), "lookingAt fails on letters");

        m = Pattern.compile("\\d+").matcher("a1b22c");
        check(m.find(2) && "22".equals(m.group()) && m.start() == 3, "find(int) from index");
        m = Pattern.compile("\\d+").matcher("a1b22c");
        check(m.find(0) && "1".equals(m.group()), "find(0) equals find()");
    }

    static void testGroupCountAndNamed() {
        Matcher m = Pattern.compile("(a)(b(c))").matcher("abc");
        check(m.groupCount() == 3, "groupCount nested");
        check(m.find() && "a".equals(m.group(1)) && "bc".equals(m.group(2)) && "c".equals(m.group(3)),
              "nested groups capture");

        Matcher n = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})")
                .matcher("2026-11-03");
        check(n.find(), "named find");
        check("2026".equals(n.group("year")) && "11".equals(n.group("month")) && "03".equals(n.group("day")),
              "group(name)");
        check(n.start("year") == 0 && n.end("year") == 4, "start/end(name) year");
        check(n.start("month") == 5 && n.end("month") == 7, "start/end(name) month");
    }

    static void testOptionalGroupAndErrors() {
        Matcher m = Pattern.compile("(a)|(b)").matcher("a");
        check(m.find() && "a".equals(m.group(1)) && m.group(2) == null, "non-participating group null");
        check(m.start(2) == -1 && m.end(2) == -1, "non-participating group -1 bounds");

        final Matcher fresh = Pattern.compile("\\d+").matcher("abc");
        expectThrows(IllegalStateException.class, fresh::group, "group() before match");
        expectThrows(IllegalStateException.class, fresh::start, "start() before match");
        expectThrows(IllegalStateException.class, fresh::end, "end() before match");

        final Matcher g = Pattern.compile("(a)").matcher("a");
        g.find();
        expectThrows(IndexOutOfBoundsException.class, () -> g.group(5), "group(5) out of range");
        expectThrows(IndexOutOfBoundsException.class, () -> g.group(-1), "group(-1) out of range");
        expectThrows(IllegalArgumentException.class, () -> g.group("nope"), "unknown named group");
        expectThrows(NullPointerException.class, () -> g.group((String) null), "null group name");
        expectThrows(IllegalArgumentException.class, () -> g.start("nope"), "unknown named start");
        expectThrows(NullPointerException.class, () -> g.end((String) null), "null named end");
    }

    static void testResetAndUsePattern() {
        Matcher m = Pattern.compile("\\d+").matcher("a1b2");
        check(m.find() && "1".equals(m.group()), "first find");
        m.reset();
        check(m.find() && "1".equals(m.group()), "reset() back to start");
        m.reset("x9y");
        check(m.find() && "9".equals(m.group()), "reset(CharSequence) new input");

        Matcher u = Pattern.compile("\\d+").matcher("abc123");
        check(u.find() && "123".equals(u.group()), "digit before usePattern");
        u.usePattern(Pattern.compile("[a-z]+"));
        check("[a-z]+".equals(u.pattern().pattern()), "usePattern swaps pattern");
        u.reset();
        check(u.find() && "abc".equals(u.group()), "new pattern after reset");
        expectThrows(IllegalArgumentException.class, () -> u.usePattern(null), "usePattern(null)");
        expectThrows(NullPointerException.class, () -> u.reset(null), "reset(null)");
    }

    static void testRegion() {
        Matcher m = Pattern.compile("\\d+").matcher("x1y22z");
        check(m.regionStart() == 0 && m.regionEnd() == 6, "default region");
        m.region(2, 6);
        check(m.regionStart() == 2 && m.regionEnd() == 6, "region set");
        check(m.find() && "22".equals(m.group()) && m.start() == 3, "find confined to region");
        check(!m.find(), "no more matches in region");

        expectThrows(IndexOutOfBoundsException.class, () -> m.region(-1, 2), "region negative start");
        expectThrows(IndexOutOfBoundsException.class, () -> m.region(0, 99), "region end out of range");
        expectThrows(IndexOutOfBoundsException.class, () -> m.region(4, 2), "region start > end");
    }

    static void testBounds() {
        Matcher m = Pattern.compile("(?<=a)b").matcher("ab");
        check(!m.hasTransparentBounds(), "default opaque bounds");
        m.region(1, 2);
        check(!m.find(), "lookbehind blocked by opaque bound");
        m.useTransparentBounds(true).region(1, 2);
        check(m.hasTransparentBounds(), "hasTransparentBounds after set");
        check(m.find() && "b".equals(m.group()), "lookbehind works with transparent bounds");

        Matcher a = Pattern.compile("^b").matcher("ab");
        check(a.hasAnchoringBounds(), "default anchoring bounds");
        a.region(1, 2);
        check(a.find() && "b".equals(a.group()), "anchoring bounds ^ matches region start");
        a.useAnchoringBounds(false).region(1, 2);
        check(!a.find(), "non-anchoring bounds ^ does not match region start");
    }

    static void testHitEndRequireEnd() {
        Matcher m = Pattern.compile("a$").matcher("a");
        check(m.find(), "find a$");
        check(m.hitEnd(), "hitEnd after matching to end");
        check(m.requireEnd(), "requireEnd for a$");

        m = Pattern.compile("a").matcher("a");
        check(m.find(), "find a");
        check(!m.hitEnd(), "hitEnd false: match found without reaching end");
        check(!m.requireEnd(), "requireEnd false for plain a");

        m = Pattern.compile("abc").matcher("ab");
        check(!m.find(), "abc vs ab no match");
        check(m.hitEnd(), "hitEnd true after failed match reaching end");
    }

    static void testAppendReplace() {
        Matcher m = Pattern.compile("cat").matcher("one cat two cats in the yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) m.appendReplacement(sb, "dog");
        m.appendTail(sb);
        check("one dog two dogs in the yard".equals(sb.toString()), "appendReplacement/appendTail");

        Matcher g = Pattern.compile("(\\w+)=(\\d+)").matcher("a=1 b=2");
        StringBuffer sb2 = new StringBuffer();
        while (g.find()) g.appendReplacement(sb2, "$1:$2;");
        g.appendTail(sb2);
        check("a:1; b:2;".equals(sb2.toString()), "appendReplacement group refs");

        Matcher z = Pattern.compile("\\d+").matcher("x1y");
        StringBuffer sbd = new StringBuffer();
        while (z.find()) z.appendReplacement(sbd, "[$0]");
        z.appendTail(sbd);
        check("x[1]y".equals(sbd.toString()), "appendReplacement $0");
    }

    static void testReplaceAllFirst() {
        check("one dog two dogs".equals(Pattern.compile("cat").matcher("one cat two cats").replaceAll("dog")),
              "replaceAll literal");
        check("one dog two cats".equals(Pattern.compile("cat").matcher("one cat two cats").replaceFirst("dog")),
              "replaceFirst literal");
        check("1 a 2 b".equals(Pattern.compile("(\\w+)=(\\d+)").matcher("a=1 b=2").replaceAll("$2 $1")),
              "replaceAll group refs");
        check("$1".equals(Pattern.compile("a").matcher("a").replaceAll(Matcher.quoteReplacement("$1"))),
              "quoteReplacement makes literal $");
    }

    static void testQuoteReplacement() {
        check("abc".equals(Matcher.quoteReplacement("abc")), "quoteReplacement passthrough");
        check("\\$1\\\\2".equals(Matcher.quoteReplacement("$1\\2")), "quoteReplacement escapes $ and \\");
        check("\\\\".equals(Matcher.quoteReplacement("\\")), "quoteReplacement single backslash");
    }

    static void testToMatchResult() {
        Matcher m = Pattern.compile("(\\d+)").matcher("1a2");
        check(m.find() && "1".equals(m.group()), "first match");
        MatchResult snap = m.toMatchResult();
        check("1".equals(snap.group()) && "1".equals(snap.group(1))
              && snap.start() == 0 && snap.end() == 1, "toMatchResult snapshot");
        check(m.find() && "2".equals(m.group()), "second match moves");
        check("1".equals(snap.group()) && snap.start() == 0, "snapshot immutable");
        check(snap.groupCount() == 1, "snapshot groupCount");
    }

    static void testBoundaryEmptyAndEscapes() {
        Matcher m = Pattern.compile("a*").matcher("");
        check(m.matches() && "".equals(m.group()) && m.start() == 0 && m.end() == 0,
              "a* matches empty with zero-width group");
        Matcher le = Pattern.compile("").matcher("abc");
        check(le.lookingAt() && "".equals(le.group()), "empty pattern lookingAt zero-width");
        check("abc".equals(Pattern.compile("x*").matcher("abc").replaceAll("")),
              "empty replacement over zero-width matches");
        check("b".equals(Pattern.compile("a").matcher("aba").replaceAll("")), "replaceAll empty removes matches");
        Matcher e = Pattern.compile("a*").matcher("abc");
        e.region(0, 0);
        check(e.regionStart() == 0 && e.regionEnd() == 0, "region(0,0) empty region");
        check(e.matches(), "a* matches empty region");
        check(Pattern.compile("(\\d)\\1").matcher("11").matches(), "backreference matcher");
        check(Pattern.compile("\\d+").matcher("a1").toString().startsWith("java.util.regex.Matcher["),
              "toString prefix");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFindLoop();
        testMatchesLookingAtFind();
        testGroupCountAndNamed();
        testOptionalGroupAndErrors();
        testResetAndUsePattern();
        testRegion();
        testBounds();
        testHitEndRequireEnd();
        testAppendReplace();
        testReplaceAllFirst();
        testQuoteReplacement();
        testToMatchResult();
        testBoundaryEmptyAndEscapes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
