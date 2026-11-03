/*
 * @test
 * @summary javax.swing.text.BadLocationException conformance: white-box structure + black-box behavior.
 * @run main BadLocationExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.BadLocationException;

public class BadLocationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.BadLocationException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BadLocationException e = new BadLocationException("offset out of range", 42);
        check(e.getMessage().equals("offset out of range"), "ctor message");
        check(e.offsetRequested() == 42, "offsetRequested returns ctor offset");
        check(e instanceof Exception, "instanceof Exception");
        check(e instanceof Throwable, "instanceof Throwable");
        Throwable asThrowable = e;
        check(!(asThrowable instanceof RuntimeException), "checked exception, not RuntimeException");

        BadLocationException zero = new BadLocationException("", 0);
        check(zero.getMessage().equals(""), "empty message roundtrip");
        check(zero.offsetRequested() == 0, "zero offset roundtrip");

        BadLocationException max = new BadLocationException(null, Integer.MAX_VALUE);
        check(max.getMessage() == null, "null message preserved");
        check(max.offsetRequested() == Integer.MAX_VALUE, "Integer.MAX_VALUE offset preserved");

        BadLocationException neg = new BadLocationException("x", -1);
        check(neg.offsetRequested() == -1, "negative offset passed through");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
