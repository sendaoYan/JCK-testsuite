/*
 * @test
 * @summary java.awt.print.Book conformance: white-box structure + black-box behavior.
 * @run main BookTest
 */

import java.lang.reflect.*;

public class BookTest {
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
        Class<?> c = Class.forName("java.awt.print.Book", false, BookTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.print.Book");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.awt.print.Pageable"), "implements Pageable");
        check(hasMethod(c, "append"), "declares append");
        check(hasMethod(c, "setPage"), "declares setPage");
    }

    static void testBlackBox() throws Exception {
        java.awt.print.Book book = new java.awt.print.Book();
        check(book.getNumberOfPages() == 0, "empty book 0 pages");
        java.awt.print.Printable pr = (g, pf, idx) -> java.awt.print.Printable.PAGE_EXISTS;
        java.awt.print.PageFormat pf = new java.awt.print.PageFormat();
        book.append(pr, pf);
        check(book.getNumberOfPages() == 1, "append one page");
        book.append(pr, pf, 3);
        check(book.getNumberOfPages() == 4, "append with numPages=3 -> 4 total");
        check(book.getPageFormat(0) == pf, "getPageFormat same instance");
        check(book.getPrintable(0) == pr, "getPrintable same instance");
        book.setPage(2, pr, pf);
        check(book.getPrintable(2) == pr, "setPage");
        boolean iob = false;
        try { book.getPageFormat(99); } catch (IndexOutOfBoundsException e) { iob = true; }
        check(iob, "getPageFormat bad index IOB");
        boolean iob2 = false;
        try { book.setPage(99, pr, pf); } catch (IndexOutOfBoundsException e) { iob2 = true; }
        check(iob2, "setPage bad index IOB");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
