/*
 * @test
 * @summary java.util.regex.MatchResult API conformance: interface shape (7 abstract
 *          methods) + behavior of start/end/group/groupCount via a custom
 *          implementation, via Matcher, and via toMatchResult() immutable snapshot.
 * @run main MatchResultTest
 */

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.lang.reflect.Method method(Class<?> c, String name, Class<?>... p) {
        try { return c.getMethod(name, p); }
        catch (NoSuchMethodException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.regex.MatchResult");
        check(c.isInterface(), "is interface");

        check(java.lang.reflect.Modifier.isAbstract(method(c, "start").getModifiers()), "abstract start()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "start", int.class).getModifiers()), "abstract start(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "end").getModifiers()), "abstract end()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "end", int.class).getModifiers()), "abstract end(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "group").getModifiers()), "abstract group()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "group", int.class).getModifiers()), "abstract group(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "groupCount").getModifiers()), "abstract groupCount()");

        // JDK-8 MatchResult has exactly these seven abstract methods (no default methods)
        int abstractCount = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 7, "exactly 7 abstract methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static MatchResult customResult() {
        return new MatchResult() {
            public int start() { return 0; }
            public int start(int g) { return g == 1 ? 0 : g == 2 ? 4 : -1; }
            public int end() { return 9; }
            public int end(int g) { return g == 1 ? 3 : g == 2 ? 9 : -1; }
            public String group() { return "foo=bar"; }
            public String group(int g) { return g == 1 ? "foo" : g == 2 ? "bar" : null; }
            public int groupCount() { return 2; }
        };
    }

    static void testCustomImplementation() {
        MatchResult r = customResult();
        check(r.start() == 0 && r.end() == 9, "start/end whole");
        check("foo=bar".equals(r.group()) && r.groupCount() == 2, "group/groupCount");
        check(r.start(1) == 0 && r.end(1) == 3 && "foo".equals(r.group(1)), "group by index 1");
        check(r.start(2) == 4 && r.end(2) == 9 && "bar".equals(r.group(2)), "group by index 2");
        check(r.start(3) == -1 && r.end(3) == -1 && r.group(3) == null, "absent group -1/null");
    }

    static void testMatcherAsMatchResult() {
        Matcher m = Pattern.compile("(\\w+)").matcher("hello world");
        check(m instanceof MatchResult, "Matcher implements MatchResult");
        MatchResult r = m;
        check(m.find() && "hello".equals(r.group()), "MatchResult.group via Matcher");
        check(r.start() == 0 && r.end() == 5, "MatchResult.start/end via Matcher");
        check(r.groupCount() == 1, "MatchResult.groupCount via Matcher");
        check(r.start(1) == 0 && r.end(1) == 5 && "hello".equals(r.group(1)),
              "MatchResult.group(int) via Matcher");

        MatchResult snap = m.toMatchResult();
        check("hello".equals(snap.group()) && snap.start() == 0 && snap.end() == 5,
              "toMatchResult returns MatchResult snapshot");
        check(m.find() && "world".equals(m.group()), "second match moves");
        check("hello".equals(snap.group()) && snap.start() == 0, "snapshot immutable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCustomImplementation();
        testMatcherAsMatchResult();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
