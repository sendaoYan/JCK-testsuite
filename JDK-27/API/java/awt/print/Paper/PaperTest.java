/*
 * @test
 * @summary java.awt.print.Paper conformance: white-box structure + black-box behavior.
 * @run main PaperTest
 */

import java.lang.reflect.*;

public class PaperTest {
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
        Class<?> c = Class.forName("java.awt.print.Paper", false, PaperTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.Paper");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
    }

    static void testBlackBox() throws Exception {
        java.awt.print.Paper p = new java.awt.print.Paper();
        check(p.getWidth() == 612.0 && p.getHeight() == 792.0, "default LETTER");
        p.setSize(500.0, 700.0);
        check(p.getWidth() == 500.0 && p.getHeight() == 700.0, "setSize");
        p.setImageableArea(10.0, 20.0, 300.0, 400.0);
        check(p.getImageableX() == 10.0 && p.getImageableY() == 20.0, "imageable x/y");
        check(p.getImageableWidth() == 300.0 && p.getImageableHeight() == 400.0, "imageable w/h");
        java.awt.print.Paper cl = (java.awt.print.Paper) p.clone();
        check(cl != p && cl.getWidth() == p.getWidth(), "clone equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
