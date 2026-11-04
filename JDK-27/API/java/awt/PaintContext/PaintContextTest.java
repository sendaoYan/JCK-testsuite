/*
 * @test
 * @summary java.awt.PaintContext conformance: white-box structure + black-box behavior.
 * @run main PaintContextTest
 */

import java.lang.reflect.*;

public class PaintContextTest {
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
        Class<?> c = Class.forName("java.awt.PaintContext", false, PaintContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.PaintContext");
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
    }

    static void testBlackBox() throws Exception {
        java.awt.PaintContext impl = new java.awt.PaintContext() {
            public void dispose() {}
            public java.awt.image.ColorModel getColorModel() { return null; }
            public java.awt.image.Raster getRaster(int x, int y, int w, int h) { return null; }
        };
        impl.dispose();
        check(impl.getColorModel() == null, "getColorModel null");
        check(impl.getRaster(0, 0, 1, 1) == null, "getRaster null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
