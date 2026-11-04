/*
 * @test
 * @summary java.awt.geom.IllegalPathStateException conformance: white-box structure plus black-box behavior.
 * @run main IllegalPathStateExceptionTest
 */

import java.awt.geom.GeneralPath;
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

        // every segment-adding operation requires a preceding moveTo
        boolean quadThrew = false;
        try { new Path2D.Double().quadTo(1, 2, 3, 4); }
        catch (IllegalPathStateException ex) { quadThrew = true; }
        check(quadThrew, "Path2D.quadTo before moveTo throws IllegalPathStateException");

        boolean curveThrew = false;
        try { new Path2D.Double().curveTo(1, 2, 3, 4, 5, 6); }
        catch (IllegalPathStateException ex) { curveThrew = true; }
        check(curveThrew, "Path2D.curveTo before moveTo throws IllegalPathStateException");

        boolean gpThrew = false;
        try { new GeneralPath().lineTo(1, 2); }
        catch (IllegalPathStateException ex) { gpThrew = true; }
        check(gpThrew, "GeneralPath.lineTo before moveTo throws IllegalPathStateException");

        // after reset the state is "no moveTo" again
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0, 0);
        gp.reset();
        boolean afterResetThrew = false;
        try { gp.lineTo(1, 1); }
        catch (IllegalPathStateException ex) { afterResetThrew = true; }
        check(afterResetThrew, "lineTo after reset throws IllegalPathStateException");

        // once moveTo is supplied the same sequence succeeds
        GeneralPath ok = new GeneralPath();
        ok.moveTo(0, 0);
        ok.lineTo(1, 2);
        ok.quadTo(1, 2, 3, 4);
        ok.curveTo(1, 2, 3, 4, 5, 6);
        check(ok.getBounds2D().getWidth() >= 0, "same sequence after moveTo succeeds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
