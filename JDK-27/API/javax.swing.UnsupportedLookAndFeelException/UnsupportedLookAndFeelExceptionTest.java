/*
 * @test
 * @summary javax.swing.UnsupportedLookAndFeelException conformance: white-box structure + black-box behavior.
 * @run main UnsupportedLookAndFeelExceptionTest
 */

import java.lang.reflect.Modifier;

import javax.swing.UnsupportedLookAndFeelException;

public class UnsupportedLookAndFeelExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.UnsupportedLookAndFeelException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UnsupportedLookAndFeelException e = new UnsupportedLookAndFeelException("message");
        check(e instanceof Exception, "is an Exception");
        check("message".equals(e.getMessage()), "getMessage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
