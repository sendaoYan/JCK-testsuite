/*
 * @test
 * @summary java.util.regex.PatternSyntaxException API conformance: hierarchy
 *          (extends IllegalArgumentException, Serializable, not final), constructor
 *          field accessors, exact multi-line getMessage() format (index / caret),
 *          null-pattern variant, and precise description/index for JDK-8 compile
 *          errors (Unclosed group, Illegal repetition range, Dangling meta
 *          character, Unclosed character class, duplicate named group).
 * @run main PatternSyntaxExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternSyntaxExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> expected, Runnable r, String msg) {
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
        Class<?> c = Class.forName("java.util.regex.PatternSyntaxException");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");

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
        check("desc".equals(e.getDescription()), "getDescription");
        check("pattern".equals(e.getPattern()), "getPattern");
        check(e.getIndex() == 2, "getIndex");

        String sep = System.lineSeparator();
        check(("desc near index 2" + sep + "pattern" + sep + "  ^").equals(e.getMessage()),
              "getMessage full format");
    }

    static void testMessageVariants() {
        String sep = System.lineSeparator();
        PatternSyntaxException e1 = new PatternSyntaxException("d", "pattern", -1);
        check(("d" + sep + "pattern").equals(e1.getMessage()), "index -1 omits index and caret");

        PatternSyntaxException e2 = new PatternSyntaxException("d", "ab", 5);
        check(("d near index 5" + sep + "ab").equals(e2.getMessage()), "index beyond length omits caret");

        PatternSyntaxException e3 = new PatternSyntaxException("d", null, 0);
        check(e3.getPattern() == null, "null pattern preserved");
        check("d".equals(e3.getDescription()) && e3.getIndex() == 0, "null pattern desc/index");
    }

    static void testCompileErrors() {
        PatternSyntaxException e = null;
        try { Pattern.compile("("); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check("(".equals(e.getPattern()), "pattern preserved");
        check(e.getIndex() == 1, "index for unclosed group");
        check("Unclosed group".equals(e.getDescription()), "description for unclosed group");

        try { Pattern.compile("a{2,1}"); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check("a{2,1}".equals(e.getPattern()) && e.getIndex() == 5
              && "Illegal repetition range".equals(e.getDescription()),
              "illegal repetition range");

        try { Pattern.compile("*a"); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check("*a".equals(e.getPattern()) && e.getIndex() == 0
              && "Dangling meta character '*'".equals(e.getDescription()),
              "dangling meta character");

        try { Pattern.compile("["); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check("[".equals(e.getPattern()) && e.getIndex() == 0
              && "Unclosed character class".equals(e.getDescription()),
              "unclosed character class");

        try { Pattern.compile("(?<n>a)(?<n>b)"); throw new AssertionError("expected PatternSyntaxException"); }
        catch (PatternSyntaxException x) { e = x; }
        check("Named capturing group <n> is already defined".equals(e.getDescription()),
              "duplicate named group description");

        expectThrows(PatternSyntaxException.class, () -> Pattern.compile("["),
                     "unclosed char class is PatternSyntaxException");
    }

    static void testIsIllegalArgument() {
        PatternSyntaxException e = new PatternSyntaxException("d", "p", 0);
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
        check(e.getMessage().contains("p"), "getMessage contains pattern");
    }

    static void testSerialization() throws Exception {
        PatternSyntaxException e = new PatternSyntaxException("desc", "pattern", 2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        PatternSyntaxException r = (PatternSyntaxException) ois.readObject();
        check("desc".equals(r.getDescription()) && "pattern".equals(r.getPattern())
              && r.getIndex() == 2, "serialization round-trip");
        check(ObjectStreamClass.lookup(PatternSyntaxException.class).getSerialVersionUID()
              == -3864639126226059218L, "serialVersionUID stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructor();
        testMessageVariants();
        testCompileErrors();
        testIsIllegalArgument();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
