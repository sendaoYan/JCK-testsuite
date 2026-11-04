/*
 * @test
 * @summary javax.imageio.IIOException conformance: white-box structure + black-box behavior.
 * @run main IIOExceptionTest
 */

import java.lang.reflect.Modifier;

public class IIOExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOException");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        check(c.getConstructors().length == 2, "two public constructors");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // single-arg constructor carries the message, no cause
        javax.imageio.IIOException e = new javax.imageio.IIOException("boom");
        check(e instanceof java.io.IOException, "is IOException");
        check("boom".equals(e.getMessage()), "message round-trip");
        check(e.getCause() == null, "no cause for single-arg");

        // null message is preserved
        javax.imageio.IIOException eNull = new javax.imageio.IIOException(null);
        check(eNull.getMessage() == null, "null message preserved");
        check(eNull.getCause() == null, "null message has no cause");

        // two-arg constructor sets message and cause
        java.io.IOException cause = new java.io.IOException("root");
        javax.imageio.IIOException e2 = new javax.imageio.IIOException("wrap", cause);
        check("wrap".equals(e2.getMessage()), "two-arg message");
        check(e2.getCause() == cause, "two-arg cause identity");
        check(e2 instanceof java.io.Serializable, "serializable");

        // two-arg with null message still records the cause
        javax.imageio.IIOException e3 = new javax.imageio.IIOException(null, cause);
        check(e3.getMessage() == null, "two-arg null message");
        check(e3.getCause() == cause, "two-arg null message cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
