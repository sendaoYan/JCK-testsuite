/*
 * @test
 * @summary jdk.security.jarsigner.JarSignerException conformance: white-box structure + black-box behavior.
 * @run main JarSignerExceptionTest
 */

import jdk.security.jarsigner.*;
import java.lang.reflect.*;
import java.util.*;
public class JarSignerExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.security.jarsigner.JarSignerException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(hasMethod(c, "getMessage"), "getMessage");
        check(hasMethod(c, "getCause"), "getCause");

    }

    static void testBlackBox() throws Exception {
        Throwable c = new Throwable("cause");
        JarSignerException e = new JarSignerException("msg", c);
        check("msg".equals(e.getMessage()), "message ctor");
        check(e.getCause() == c, "cause ctor");
        check(e instanceof RuntimeException, "is RuntimeException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
