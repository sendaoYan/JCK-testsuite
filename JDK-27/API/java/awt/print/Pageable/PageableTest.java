/*
 * @test
 * @summary java.awt.print.Pageable conformance: interface contract (headless-safe):
 *          shape, UNKNOWN_NUMBER_OF_PAGES constant, and polymorphic use via Book
 *          and a custom implementation.
 * @run main PageableTest
 */

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PageableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static final Printable NOP = new Printable() {
        public int print(java.awt.Graphics g, PageFormat pf, int pageIndex) {
            return Printable.NO_SUCH_PAGE;
        }
    };

    // Fixed two-page implementation used to exercise the interface reference.
    static class TwoPages implements Pageable {
        public int getNumberOfPages() { return 2; }
        public PageFormat getPageFormat(int pageIndex) {
            if (pageIndex < 0 || pageIndex > 1) throw new ArrayIndexOutOfBoundsException(pageIndex);
            return new PageFormat();
        }
        public Printable getPrintable(int pageIndex) {
            if (pageIndex < 0 || pageIndex > 1) throw new ArrayIndexOutOfBoundsException(pageIndex);
            return NOP;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.print.Pageable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getNumberOfPages"), "method getNumberOfPages");
        check(hasMethod(c, "getPageFormat"), "method getPageFormat");
        check(hasMethod(c, "getPrintable"), "method getPrintable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.size() == 3, "exactly three methods, got " + names.size() + ": " + names);
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }
        check(c.getMethod("getNumberOfPages").getReturnType() == int.class, "getNumberOfPages returns int");
        check(c.getMethod("getPageFormat", int.class).getReturnType() == PageFormat.class,
                "getPageFormat returns PageFormat");
        check(c.getMethod("getPrintable", int.class).getReturnType() == Printable.class,
                "getPrintable returns Printable");

        // The unknown-count sentinel.
        java.lang.reflect.Field f = c.getField("UNKNOWN_NUMBER_OF_PAGES");
        check(f.getInt(null) == -1, "UNKNOWN_NUMBER_OF_PAGES == -1");
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), "constant modifiers");

        // Book is the standard implementation.
        check(Book.class.getInterfaces()[0] == c, "Book implements Pageable");

        // Polymorphic contract through the interface reference.
        Pageable p = new TwoPages();
        check(p.getNumberOfPages() == 2, "getNumberOfPages through interface");
        check(p.getPageFormat(0) != null && p.getPageFormat(1) != null, "getPageFormat through interface");
        check(p.getPrintable(0) == NOP, "getPrintable through interface");
        boolean threw = false;
        try { p.getPageFormat(2); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "implementations police their own ranges");

        // A Book behaves identically through the same reference.
        Book b = new Book();
        b.append(NOP, new PageFormat());
        Pageable pb = b;
        check(pb.getNumberOfPages() == 1, "Book through interface");
        check(pb.getPrintable(0) == NOP, "Book printable through interface");
        check(pb.getPageFormat(0).getClass() == PageFormat.class, "Book format through interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
