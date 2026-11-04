/*
 * @test
 * @summary java.awt.Graphics2D conformance: abstract class reflection.
 * @run main Graphics2DTest
 */

public class Graphics2DTest {
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
        Class<?> c = Class.forName("java.awt.Graphics2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.Graphics"), "superclass java.awt.Graphics");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "draw3DRect"), "method draw3DRect");
        check(hasMethod(c, "fill3DRect"), "method fill3DRect");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "drawImage"), "method drawImage");
        check(hasMethod(c, "drawRenderedImage"), "method drawRenderedImage");
        check(hasMethod(c, "drawRenderableImage"), "method drawRenderableImage");
        check(hasMethod(c, "drawString"), "method drawString");
        check(hasMethod(c, "drawGlyphVector"), "method drawGlyphVector");
        check(hasMethod(c, "fill"), "method fill");
        check(hasMethod(c, "hit"), "method hit");
        check(hasMethod(c, "getDeviceConfiguration"), "method getDeviceConfiguration");
        check(hasMethod(c, "setComposite"), "method setComposite");
        check(hasMethod(c, "setPaint"), "method setPaint");
        check(hasMethod(c, "setStroke"), "method setStroke");
        check(hasMethod(c, "setRenderingHint"), "method setRenderingHint");
        check(hasMethod(c, "getRenderingHint"), "method getRenderingHint");
        check(hasMethod(c, "setRenderingHints"), "method setRenderingHints");
        check(hasMethod(c, "addRenderingHints"), "method addRenderingHints");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
        check(hasMethod(c, "translate"), "method translate");
        check(hasMethod(c, "rotate"), "method rotate");
        check(hasMethod(c, "scale"), "method scale");
        check(hasMethod(c, "shear"), "method shear");
        check(hasMethod(c, "transform"), "method transform");
        check(hasMethod(c, "setTransform"), "method setTransform");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "getPaint"), "method getPaint");
        check(hasMethod(c, "getComposite"), "method getComposite");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "getStroke"), "method getStroke");
        check(hasMethod(c, "clip"), "method clip");
        check(hasMethod(c, "getFontRenderContext"), "method getFontRenderContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
