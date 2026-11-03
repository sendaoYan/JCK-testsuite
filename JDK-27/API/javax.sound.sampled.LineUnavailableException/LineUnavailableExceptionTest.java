/*
 * @test
 * @summary javax.sound.sampled.LineUnavailableException conformance: white-box structure + black-box behavior.
 * @run main LineUnavailableExceptionTest
 */

import java.lang.reflect.Modifier;

public class LineUnavailableExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.LineUnavailableException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Exception e = new javax.sound.sampled.LineUnavailableException();
        check(e instanceof Exception, "is Exception");
        check(!(e instanceof RuntimeException), "checked, not RuntimeException");
        check(e.getMessage() == null, "default message null");
        check(e.getCause() == null, "default cause null");

        javax.sound.sampled.LineUnavailableException e2 = new javax.sound.sampled.LineUnavailableException("no line available");
        check("no line available".equals(e2.getMessage()), "message round-trip");
        check(e2.getCause() == null, "String constructor cause null");
        check(e2.toString().contains("no line available"), "toString contains message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
