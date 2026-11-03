/*
 * @test
 * @summary java.awt.TexturePaint conformance: white-box structure + black-box behavior.
 * @run main TexturePaintTest
 */

import java.lang.reflect.*;

public class TexturePaintTest {
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
        Class<?> c = Class.forName("java.awt.TexturePaint", false, TexturePaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.TexturePaint");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.Paint"), "implements Paint");
    }

    static void testBlackBox() throws Exception {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.geom.Rectangle2D anchor = new java.awt.geom.Rectangle2D.Double(0, 0, 5, 5);
        java.awt.TexturePaint tp = new java.awt.TexturePaint(img, anchor);
        check(tp.getImage() == img, "getImage same instance");
        check(tp.getAnchorRect() != null && tp.getAnchorRect().getWidth() == 5.0, "anchor rect");
        check(tp.getTransparency() == java.awt.Transparency.OPAQUE || tp.getTransparency() == java.awt.Transparency.TRANSLUCENT, "transparency valid");
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
        java.awt.Rectangle dev = new java.awt.Rectangle(0, 0, 10, 10);
        java.awt.geom.Rectangle2D user = new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10);
        java.awt.geom.AffineTransform xf = new java.awt.geom.AffineTransform();
        check(tp.createContext(cm, dev, user, xf, new java.awt.RenderingHints(null)) != null, "createContext non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
