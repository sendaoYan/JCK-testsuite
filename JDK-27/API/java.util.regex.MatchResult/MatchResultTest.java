/*
 * @test
 * @summary java.util.regex.MatchResult 一致性：白盒（接口形态）+ 黑盒（抽象方法与默认方法组合行为）。
 * @run main MatchResultTest
 *
 * 白盒（结构）：Class.forName 加载；接口；抽象方法 start/end/group/groupCount；
 *   默认方法 start(String)/end(String)/group(String)/namedGroups()/hasMatch()。
 * 黑盒（行为）设计案见同目录 DESIGN.md。
 */

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchResultTest {
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

    static java.lang.reflect.Method method(Class<?> c, String name, Class<?>... p) {
        try { return c.getMethod(name, p); }
        catch (NoSuchMethodException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.regex.MatchResult");
        check(c.isInterface(), "is interface");

        // 抽象方法（非 default）
        check(java.lang.reflect.Modifier.isAbstract(method(c, "start").getModifiers()), "abstract start()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "start", int.class).getModifiers()), "abstract start(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "end").getModifiers()), "abstract end()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "end", int.class).getModifiers()), "abstract end(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "group").getModifiers()), "abstract group()");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "group", int.class).getModifiers()), "abstract group(int)");
        check(java.lang.reflect.Modifier.isAbstract(method(c, "groupCount").getModifiers()), "abstract groupCount()");

        // 默认方法
        check(method(c, "start", String.class).isDefault(), "default start(String)");
        check(method(c, "end", String.class).isDefault(), "default end(String)");
        check(method(c, "group", String.class).isDefault(), "default group(String)");
        check(method(c, "namedGroups").isDefault(), "default namedGroups()");
        check(method(c, "hasMatch").isDefault(), "default hasMatch()");
    }

    // ---- Part 2: black-box (behavioral) ----
    // 自定义实现：抽象方法 + namedGroups()，验证三个默认方法按名称→编号委托。
    static MatchResult customResult() {
        return new MatchResult() {
            public int start() { return 0; }
            public int start(int g) { return g == 1 ? 0 : g == 2 ? 4 : -1; }
            public int end() { return 9; }
            public int end(int g) { return g == 1 ? 3 : g == 2 ? 9 : -1; }
            public String group() { return "foo=bar"; }
            public String group(int g) { return g == 1 ? "foo" : g == 2 ? "bar" : null; }
            public int groupCount() { return 2; }
            public Map<String, Integer> namedGroups() { return Map.of("name", 1, "value", 2); }
        };
    }

    static void testDefaultNamedMethods() {
        MatchResult r = customResult();
        check(r.start() == 0 && r.end() == 9, "start/end whole");
        check(r.group().equals("foo=bar") && r.groupCount() == 2, "group/groupCount");
        check(r.start(1) == 0 && r.end(1) == 3 && r.group(1).equals("foo"), "group by index 1");
        check(r.start(2) == 4 && r.end(2) == 9 && r.group(2).equals("bar"), "group by index 2");

        // 默认方法通过 namedGroups() 委托
        check(r.start("name") == 0 && r.end("name") == 3 && r.group("name").equals("foo"),
              "default start/end/group(name)");
        check(r.start("value") == 4 && r.end("value") == 9 && r.group("value").equals("bar"),
              "default start/end/group(value)");

        // 错误语义
        expectThrows(() -> r.group("missing"), IllegalArgumentException.class, "unknown name");
        expectThrows(() -> r.start("missing"), IllegalArgumentException.class, "unknown name start");
        expectThrows(() -> r.group((String) null), NullPointerException.class, "null name");
    }

    static void testBareDefaults() {
        MatchResult bare = new MatchResult() {
            public int start() { return 0; }
            public int start(int g) { return -1; }
            public int end() { return 0; }
            public int end(int g) { return -1; }
            public String group() { return ""; }
            public String group(int g) { return null; }
            public int groupCount() { return 0; }
        };
        expectThrows(bare::namedGroups, UnsupportedOperationException.class,
                     "default namedGroups throws UOE");
        expectThrows(bare::hasMatch, UnsupportedOperationException.class,
                     "default hasMatch throws UOE");
    }

    static void testMatcherAsMatchResult() {
        Matcher m = Pattern.compile("(?<w>\\w+)").matcher("hello world");
        check(m instanceof MatchResult, "Matcher implements MatchResult");
        MatchResult r = m;
        check(m.find() && r.group().equals("hello"), "MatchResult.group via Matcher");
        check(r.start() == 0 && r.end() == 5, "MatchResult.start/end via Matcher");
        check(r.start("w") == 0 && r.end("w") == 5 && r.group("w").equals("hello"),
              "named methods via Matcher");

        MatchResult snap = m.toMatchResult();
        check(snap.group().equals("hello") && snap.start() == 0 && snap.end() == 5,
              "toMatchResult returns MatchResult");
    }

    
    static void testBlackBox() throws Exception {
        testDefaultNamedMethods();
        testBareDefaults();
        testMatcherAsMatchResult();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
