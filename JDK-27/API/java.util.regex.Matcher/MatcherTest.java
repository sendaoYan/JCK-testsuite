/*
 * @test
 * @summary java.util.regex.Matcher 一致性：白盒（反射结构）+ 黑盒（find/group/region/替换/边界组合行为）。
 * @run main MatcherTest
 *
 * 白盒（结构）：Class.forName 加载；final 类、实现 MatchResult、无公开构造器；
 *   关键成员形态（matches/find/lookingAt/group/start/end/region/appendReplacement/appendTail/
 *   replaceAll/replaceFirst/reset/usePattern/quoteReplacement/results/toMatchResult/namedGroups/hasMatch）。
 * 黑盒（行为）设计案见同目录 DESIGN.md。
 */

import java.util.ArrayList;
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
        check(hasMethod(c, "appendReplacement", StringBuilder.class, String.class),
              "appendReplacement(StringBuilder,String)");
        check(hasMethod(c, "appendTail", StringBuffer.class), "appendTail(StringBuffer)");
        check(hasMethod(c, "appendTail", StringBuilder.class), "appendTail(StringBuilder)");
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
        check(hasMethod(c, "namedGroups"), "namedGroups()");
        check(hasMethod(c, "hasMatch"), "hasMatch()");
        check(hasMethod(c, "results"), "results()");
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
        check(keys.equals(List.of("a", "b", "c")), "find loop group1");
        check(vals.equals(List.of("1", "22", "333")), "find loop group2");
        check(starts.equals(List.of(0, 4, 9)), "find loop start");
        check(ends.equals(List.of(3, 8, 14)), "find loop end");
    }

    static void testMatchesLookingAtFind() {
        Matcher m = Pattern.compile("\\d+").matcher("123");
        check(m.matches(), "matches full digits");
        check(m.group().equals("123"), "group after matches");

        m = Pattern.compile("\\d+").matcher("123abc");
        check(m.lookingAt(), "lookingAt matches prefix");
        check(m.group().equals("123"), "lookingAt group");
        check(!m.matches(), "matches fails for trailing letters");

        m = Pattern.compile("\\d+").matcher("abc");
        check(!m.lookingAt(), "lookingAt fails on letters");

        m = Pattern.compile("\\d+").matcher("a1b22c");
        check(m.find(2) && m.group().equals("22") && m.start() == 3, "find(int) from index");
        m = Pattern.compile("\\d+").matcher("a1b22c");
        check(m.find(0) && m.group().equals("1"), "find(0) equals find()");
    }

    static void testGroupCountAndNamed() {
        Matcher m = Pattern.compile("(a)(b(c))").matcher("abc");
        check(m.groupCount() == 3, "groupCount nested");
        check(m.find() && m.group(1).equals("a") && m.group(2).equals("bc") && m.group(3).equals("c"),
              "nested groups capture");

        Matcher n = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})")
                .matcher("2026-11-03");
        check(n.find(), "named find");
        check("2026".equals(n.group("year")) && "11".equals(n.group("month")) && "03".equals(n.group("day")),
              "group(name)");
        check(n.start("year") == 0 && n.end("year") == 4, "start/end(name) year");
        check(n.start("month") == 5 && n.end("month") == 7, "start/end(name) month");
        check(n.namedGroups().size() == 3 && n.namedGroups().get("day") == 3, "namedGroups");
    }

    static void testOptionalGroupAndErrors() {
        Matcher m = Pattern.compile("(a)|(b)").matcher("a");
        check(m.find() && m.group(1).equals("a") && m.group(2) == null, "non-participating group null");
        check(m.start(2) == -1 && m.end(2) == -1, "non-participating group -1 bounds");

        Matcher fresh = Pattern.compile("\\d+").matcher("abc");
        expectThrows(fresh::group, IllegalStateException.class, "group() before match");
        expectThrows(fresh::start, IllegalStateException.class, "start() before match");
        expectThrows(fresh::end, IllegalStateException.class, "end() before match");

        Matcher g = Pattern.compile("(a)").matcher("a");
        g.find();
        expectThrows(() -> g.group(5), IndexOutOfBoundsException.class, "group(5) out of range");
        expectThrows(() -> g.group(-1), IndexOutOfBoundsException.class, "group(-1) out of range");
        expectThrows(() -> g.group("nope"), IllegalArgumentException.class, "unknown named group");
        expectThrows(() -> g.group((String) null), NullPointerException.class, "null group name");
    }

    static void testResetAndUsePattern() {
        Matcher m = Pattern.compile("\\d+").matcher("a1b2");
        check(m.find() && m.group().equals("1"), "first find");
        m.reset();
        check(m.find() && m.group().equals("1"), "reset() back to start");
        m.reset("x9y");
        check(m.find() && m.group().equals("9"), "reset(CharSequence) new input");

        Matcher u = Pattern.compile("\\d+").matcher("abc123");
        check(u.find() && u.group().equals("123"), "digit before usePattern");
        u.usePattern(Pattern.compile("[a-z]+"));
        check(u.pattern().pattern().equals("[a-z]+"), "usePattern swaps pattern");
        u.reset();
        check(u.find() && u.group().equals("abc"), "new pattern after reset");
        expectThrows(() -> u.usePattern(null), IllegalArgumentException.class, "usePattern(null)");
    }

    static void testRegion() {
        Matcher m = Pattern.compile("\\d+").matcher("x1y22z");
        check(m.regionStart() == 0 && m.regionEnd() == 6, "default region");
        m.region(2, 6);
        check(m.regionStart() == 2 && m.regionEnd() == 6, "region set");
        check(m.find() && m.group().equals("22") && m.start() == 3, "find confined to region");
        check(!m.find(), "no more matches in region");

        expectThrows(() -> m.region(-1, 2), IndexOutOfBoundsException.class, "region negative start");
        expectThrows(() -> m.region(0, 99), IndexOutOfBoundsException.class, "region end out of range");
        expectThrows(() -> m.region(4, 2), IndexOutOfBoundsException.class, "region start > end");
    }

    static void testBounds() {
        Matcher m = Pattern.compile("(?<=a)b").matcher("ab");
        check(!m.hasTransparentBounds(), "default opaque bounds");
        m.region(1, 2);
        check(!m.find(), "lookbehind blocked by opaque bound");
        m.useTransparentBounds(true).region(1, 2);
        check(m.hasTransparentBounds(), "hasTransparentBounds after set");
        check(m.find() && m.group().equals("b"), "lookbehind works with transparent bounds");

        Matcher a = Pattern.compile("^b").matcher("ab");
        check(a.hasAnchoringBounds(), "default anchoring bounds");
        a.region(1, 2);
        check(a.find() && a.group().equals("b"), "anchoring bounds ^ matches region start");
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
        check(sb.toString().equals("one dog two dogs in the yard"), "appendReplacement/appendTail");

        Matcher g = Pattern.compile("(\\w+)=(\\d+)").matcher("a=1 b=2");
        StringBuffer sb2 = new StringBuffer();
        while (g.find()) g.appendReplacement(sb2, "$1:$2;");
        g.appendTail(sb2);
        check(sb2.toString().equals("a:1; b:2;"), "appendReplacement group refs");

        Matcher z = Pattern.compile("\\d+").matcher("x1y");
        StringBuilder sbd = new StringBuilder();
        while (z.find()) z.appendReplacement(sbd, "[$0]");
        z.appendTail(sbd);
        check(sbd.toString().equals("x[1]y"), "appendReplacement StringBuilder + $0");
    }

    static void testReplaceAllFirst() {
        check(Pattern.compile("cat").matcher("one cat two cats").replaceAll("dog")
              .equals("one dog two dogs"), "replaceAll literal");
        check(Pattern.compile("cat").matcher("one cat two cats").replaceFirst("dog")
              .equals("one dog two cats"), "replaceFirst literal");
        check(Pattern.compile("(\\w+)=(\\d+)").matcher("a=1 b=2").replaceAll("$2 $1")
              .equals("1 a 2 b"), "replaceAll group refs");

        check(Pattern.compile("\\d+").matcher("a1b22")
              .replaceAll(mr -> String.valueOf(Integer.parseInt(mr.group()) * 2))
              .equals("a2b44"), "replaceAll(Function)");
        check(Pattern.compile("\\d+").matcher("a1b22")
              .replaceFirst(mr -> String.valueOf(Integer.parseInt(mr.group()) * 2))
              .equals("a2b22"), "replaceFirst(Function)");

        check(Pattern.compile("a").matcher("a")
              .replaceAll(Matcher.quoteReplacement("$1")).equals("$1"),
              "quoteReplacement makes literal $");
    }

    static void testQuoteReplacement() {
        check(Matcher.quoteReplacement("abc").equals("abc"), "quoteReplacement passthrough");
        check(Matcher.quoteReplacement("$1\\2").equals("\\$1\\\\2"), "quoteReplacement escapes $ and \\");
    }

    static void testResultsAndToMatchResult() {
        List<String> groups = new ArrayList<>();
        Pattern.compile("\\d+").matcher("a1b22c333").results()
                .forEach(mr -> groups.add(mr.group()));
        check(groups.equals(List.of("1", "22", "333")), "results() stream");

        Matcher m = Pattern.compile("(\\d+)").matcher("1a2");
        check(m.find() && m.group().equals("1"), "first match");
        MatchResult snap = m.toMatchResult();
        check(snap.group().equals("1") && snap.group(1).equals("1")
              && snap.start() == 0 && snap.end() == 1, "toMatchResult snapshot");
        check(m.find() && m.group().equals("2"), "second match moves");
        check(snap.group().equals("1") && snap.start() == 0, "snapshot immutable");
        check(snap.groupCount() == 1, "snapshot groupCount");
    }

    static void testHasMatchAndToString() {
        Matcher m = Pattern.compile("\\d+").matcher("a1");
        check(!m.hasMatch(), "hasMatch false before match");
        check(m.find() && m.hasMatch(), "hasMatch true after find");
        check(m.toString().startsWith("java.util.regex.Matcher["), "toString prefix");
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
        testResultsAndToMatchResult();
        testHasMatchAndToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
