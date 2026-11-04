/*
 * @test
 * @summary java.awt.Graphics conformance: abstract class reflection.
 * @run main GraphicsTest
 */

public class GraphicsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Graphics");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "translate"), "method translate");
        check(hasMethod(c, "getColor"), "method getColor");
        check(hasMethod(c, "setColor"), "method setColor");
        check(hasMethod(c, "setPaintMode"), "method setPaintMode");
        check(hasMethod(c, "setXORMode"), "method setXORMode");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "getClipBounds"), "method getClipBounds");
        check(hasMethod(c, "clipRect"), "method clipRect");
        check(hasMethod(c, "setClip"), "method setClip");
        check(hasMethod(c, "getClip"), "method getClip");
        check(hasMethod(c, "copyArea"), "method copyArea");
        check(hasMethod(c, "drawLine"), "method drawLine");
        check(hasMethod(c, "fillRect"), "method fillRect");
        check(hasMethod(c, "drawRect"), "method drawRect");
        check(hasMethod(c, "clearRect"), "method clearRect");
        check(hasMethod(c, "drawRoundRect"), "method drawRoundRect");
        check(hasMethod(c, "fillRoundRect"), "method fillRoundRect");
        check(hasMethod(c, "draw3DRect"), "method draw3DRect");
        check(hasMethod(c, "fill3DRect"), "method fill3DRect");
        check(hasMethod(c, "drawOval"), "method drawOval");
        check(hasMethod(c, "fillOval"), "method fillOval");
        check(hasMethod(c, "drawArc"), "method drawArc");
        check(hasMethod(c, "fillArc"), "method fillArc");
        check(hasMethod(c, "drawPolyline"), "method drawPolyline");
        check(hasMethod(c, "drawPolygon"), "method drawPolygon");
        check(hasMethod(c, "fillPolygon"), "method fillPolygon");
        check(hasMethod(c, "drawString"), "method drawString");
        check(hasMethod(c, "drawChars"), "method drawChars");
        check(hasMethod(c, "drawBytes"), "method drawBytes");
        check(hasMethod(c, "drawImage"), "method drawImage");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "finalize"), "method finalize");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getClipRect"), "method getClipRect");
        check(hasMethod(c, "hitClip"), "method hitClip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
