/*
 * @test
 * @summary java.awt.geom.IllegalPathStateException conformance: white-box structure plus black-box behavior.
 * @run main IllegalPathStateExceptionTest
 */

import java.awt.geom.IllegalPathStateException;
import java.awt.geom.Path2D;
import java.lang.reflect.*;

public class IllegalPathStateExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.IllegalPathStateException");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(!Exception.class.isAssignableFrom(c) || RuntimeException.class.isAssignableFrom(c), "unchecked");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "String constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalPathStateException e1 = new IllegalPathStateException();
        check(e1.getMessage() == null, "no-arg ctor message null");
        check(e1.getCause() == null, "no-arg ctor cause null");
        check(e1 instanceof RuntimeException, "instanceof RuntimeException");

        IllegalPathStateException e2 = new IllegalPathStateException("missing initial moveto");
        check("missing initial moveto".equals(e2.getMessage()), "String ctor message preserved");

        boolean threw = false;
        try {
            new Path2D.Double().lineTo(1.0, 2.0); // lineTo before moveTo
        } catch (IllegalPathStateException ex) {
            threw = true;
            check(ex.getMessage() != null && ex.getMessage().contains("moveto"),
                  "real throw carries 'moveto' message");
        }
        check(threw, "Path2D.lineTo before moveTo throws IllegalPathStateException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
