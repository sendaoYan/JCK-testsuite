/*
 * @test
 * @summary java.util.regex.PatternSyntaxException 一致性：白盒（反射结构）+ 黑盒（构造/查询/编译异常组合行为）。
 * @run main PatternSyntaxExceptionTest
 *
 * 白盒（结构）：Class.forName 加载；继承 IllegalArgumentException、实现 Serializable；非 final；
 *   构造器 PatternSyntaxException(String,String,int)；方法 getIndex/getDescription/getPattern/getMessage。
 * 黑盒（行为）设计案见同目录 DESIGN.md。
 */

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternSyntaxExceptionTest {
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
        Class<?> c = Class.forName("java.util.regex.PatternSyntaxException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");

        check(c.getConstructor(String.class, String.class, int.class) != null,
              "constructor (String,String,int)");
        check(hasMethod(c, "getIndex"), "getIndex()");
        check(hasMethod(c, "getDescription"), "getDescription()");
        check(hasMethod(c, "getPattern"), "getPattern()");
        check(hasMethod(c, "getMessage"), "getMessage()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstructor() {
        PatternSyntaxException e = new PatternSyntaxException("desc", "pattern", 2);
        check(e.getDescription().equals("desc"), "getDescription");
        check(e.getPattern().equals("pattern"), "getPattern");
        check(e.getIndex() == 2, "getIndex");

        String sep = System.lineSeparator();
        check(e.getMessage().equals("desc near index 2" + sep + "pattern" + sep + "  ^"),
              "getMessage full format");
    }

    static void testMessageVariants() {
        String sep = System.lineSeparator();
        PatternSyntaxException e1 = new PatternSyntaxException("d", "pattern", -1);
        check(e1.getMessage().equals("d" + sep + "pattern"), "index -1 omits index and caret");

        PatternSyntaxException e2 = new PatternSyntaxException("d", "ab", 5);
        check(e2.getMessage().equals("d near index 5" + sep + "ab"), "index beyond length omits caret");

        PatternSyntaxException e3 = new PatternSyntaxException("d", null, 0);
        check(e3.getPattern() == null, "null pattern preserved");
        check(e3.getDescription().equals("d") && e3.getIndex() == 0, "null pattern desc/index");
    }

    static void testCompileErrors() {
        PatternSyntaxException e = null;
        try { Pattern.compile("("); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check(e.getPattern().equals("("), "pattern preserved");
        check(e.getIndex() == 1, "index for unclosed group");
        check(e.getDescription().equals("Unclosed group"), "description for unclosed group");

        try { Pattern.compile("a{2,1}"); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check(e.getPattern().equals("a{2,1}") && e.getIndex() == 5
              && e.getDescription().equals("Illegal repetition range"),
              "illegal repetition range");

        try { Pattern.compile("*a"); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check(e.getPattern().equals("*a") && e.getIndex() == 0
              && e.getDescription().equals("Dangling meta character '*'"),
              "dangling meta character");

        // 它是 IllegalArgumentException 子类，且 getMessage 非空
        expectThrows(() -> Pattern.compile("["), PatternSyntaxException.class,
                     "unclosed char class is PatternSyntaxException");
    }

    static void testIsIllegalArgument() {
        PatternSyntaxException e = new PatternSyntaxException("d", "p", 0);
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
        check(e.getMessage().contains("p"), "getMessage contains pattern");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructor();
        testMessageVariants();
        testCompileErrors();
        testIsIllegalArgument();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
