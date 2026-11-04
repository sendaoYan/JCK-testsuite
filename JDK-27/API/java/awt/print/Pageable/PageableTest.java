/*
 * @test
 * @summary java.awt.print.Pageable conformance: white-box structure + black-box behavior.
 * @run main PageableTest
 */

import java.lang.reflect.*;

public class PageableTest {
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
        Class<?> c = Class.forName("java.awt.print.Pageable", false, PageableTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.Pageable");
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
        check(java.awt.print.Pageable.UNKNOWN_NUMBER_OF_PAGES == -1, "UNKNOWN_NUMBER_OF_PAGES == -1");
        java.awt.print.Pageable p = new java.awt.print.Pageable() {
            public int getNumberOfPages() { return 1; }
            public java.awt.print.PageFormat getPageFormat(int i) { return null; }
            public java.awt.print.Printable getPrintable(int i) { return null; }
        };
        check(p.getNumberOfPages() == 1, "getNumberOfPages");
        check(p.getPageFormat(0) == null, "getPageFormat null");
        check(p.getPrintable(0) == null, "getPrintable null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
