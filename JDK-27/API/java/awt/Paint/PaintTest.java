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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
