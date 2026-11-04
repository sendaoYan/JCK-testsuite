/*
 * @test
 * @summary java.awt.print.PageFormat conformance: white-box structure + black-box behavior.
 * @run main PageFormatTest
 */

import java.lang.reflect.*;

public class PageFormatTest {
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
        Class<?> c = Class.forName("java.awt.print.PageFormat", false, PageFormatTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.PageFormat");
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
        check(hasMethod(c, "setOrientation"), "declares setOrientation");
        check(hasMethod(c, "getMatrix"), "declares getMatrix");
    }

    static void testBlackBox() throws Exception {
        java.awt.print.PageFormat pf = new java.awt.print.PageFormat();
        check(pf.getWidth() == 612.0 && pf.getHeight() == 792.0, "default LETTER 612x792");
        check(pf.getOrientation() == java.awt.print.PageFormat.PORTRAIT, "default PORTRAIT");
        check(pf.getImageableX() >= 0.0, "imageableX >= 0");
        check(pf.getImageableWidth() > 0.0, "imageableWidth > 0");
        check(pf.getImageableHeight() > 0.0, "imageableHeight > 0");
        pf.setOrientation(java.awt.print.PageFormat.LANDSCAPE);
        check(pf.getOrientation() == java.awt.print.PageFormat.LANDSCAPE, "setOrientation LANDSCAPE");
        pf.setOrientation(java.awt.print.PageFormat.REVERSE_LANDSCAPE);
        check(pf.getOrientation() == java.awt.print.PageFormat.REVERSE_LANDSCAPE, "setOrientation REVERSE_LANDSCAPE");
        pf.setOrientation(java.awt.print.PageFormat.PORTRAIT);
        check(pf.getOrientation() == java.awt.print.PageFormat.PORTRAIT, "setOrientation PORTRAIT");
        boolean iae = false;
        try { pf.setOrientation(999); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid orientation IAE");
        double[] m = pf.getMatrix();
        check(m != null && m.length == 6, "getMatrix 6 doubles");
        java.awt.print.PageFormat cl = (java.awt.print.PageFormat) pf.clone();
        check(cl != pf && cl.getWidth() == pf.getWidth(), "clone equal value");
        java.awt.print.Paper paper = pf.getPaper();
        check(paper != null, "getPaper non-null");
        pf.setPaper(paper);
        java.awt.print.Paper got = pf.getPaper();
        check(got != paper, "setPaper clones");
        check(got.getWidth() == paper.getWidth() && got.getHeight() == paper.getHeight(), "setPaper equal value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
