/*
 * @test
 * @summary java.awt.Paint conformance: white-box structure + black-box behavior.
 * @run main PaintTest
 */

import java.lang.reflect.*;

public class PaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Paint", false, PaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Paint");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
        check(hasInterface(c, "java.awt.Transparency"), "extends Transparency");
    }

    static void testBlackBox() throws Exception {
        java.awt.Paint impl = new java.awt.Paint() {
            public java.awt.PaintContext createContext(java.awt.image.ColorModel cm, java.awt.Rectangle dev, java.awt.geom.Rectangle2D user, java.awt.geom.AffineTransform xf, java.awt.RenderingHints hints) { return null; }
            public int getTransparency() { return java.awt.Transparency.OPAQUE; }
        };
        check(impl.createContext(null, null, null, null, null) == null, "createContext null");
        check(impl.getTransparency() == java.awt.Transparency.OPAQUE, "transparency OPAQUE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Paint.class.isInterface(), "Paint is an interface");
        // Paint extends Transparency and declares createContext
        check(java.awt.Transparency.class.isAssignableFrom(java.awt.Paint.class), "Paint extends Transparency");
        boolean hasCreate = false;
        for (java.lang.reflect.Method m : java.awt.Paint.class.getMethods())
            if (m.getName().equals("createContext")) hasCreate = true;
        check(hasCreate, "Paint declares createContext");
        // implementations: Color, GradientPaint, LinearGradientPaint, RadialGradientPaint, TexturePaint
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.geom.Rectangle2D ub = new java.awt.geom.Rectangle2D.Double(0, 0, 2, 2);
        java.awt.RenderingHints none = new java.awt.RenderingHints(null);
        java.awt.Paint[] paints = {
            java.awt.Color.red,
            new java.awt.GradientPaint(0f, 0f, java.awt.Color.red, 1f, 1f, java.awt.Color.blue),
            new java.awt.LinearGradientPaint(0f, 0f, 1f, 0f, new float[]{0f, 1f},
                    new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}),
            new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(1f, 1f), 2f,
                    new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue})};
        String[] pn = {"Color", "GradientPaint", "LinearGradientPaint", "RadialGradientPaint"};
        for (int i = 0; i < paints.length; i++) {
            check(paints[i].createContext(bi.getColorModel(), new java.awt.Rectangle(0, 0, 2, 2), ub,
                    new java.awt.geom.AffineTransform(), none) != null, pn[i] + " createContext headless");
            check(paints[i].getTransparency() == java.awt.Transparency.OPAQUE, pn[i] + " opaque transparency");
        }
        // translucent variant
        java.awt.Paint tr = new java.awt.Color(1, 2, 3, 100);
        check(tr.getTransparency() == java.awt.Transparency.TRANSLUCENT, "translucent Color paint");
    }

}
