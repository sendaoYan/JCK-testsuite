/*
 * @test
 * @summary java.awt.print.Printable conformance: interface contract (headless-safe):
 *          shape, PAGE_EXISTS/NO_SUCH_PAGE constants, result-code semantics, and
 *          polymorphic use including a printing-null-graphics probe.
 * @run main PrintableTest
 */

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PrintableTest {
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

    // Records arguments and returns PAGE_EXISTS for page 0, NO_SUCH_PAGE otherwise.
    static class FirstPageOnly implements Printable {
        PageFormat lastFormat;
        int lastIndex = -2;
        public int print(java.awt.Graphics g, PageFormat pf, int pageIndex) {
            lastFormat = pf;
            lastIndex = pageIndex;
            return pageIndex == 0 ? PAGE_EXISTS : NO_SUCH_PAGE;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.print.Printable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "print"), "method print");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.size() == 1, "single-method interface, got " + names.size() + ": " + names);
        Method m = c.getMethod("print", java.awt.Graphics.class, PageFormat.class, int.class);
        check(Modifier.isAbstract(m.getModifiers()), "print abstract");
        check(m.getReturnType() == int.class, "print returns int");
        check(m.getExceptionTypes().length == 1
                && m.getExceptionTypes()[0] == java.awt.print.PrinterException.class,
                "print declares PrinterException");

        // Result-code constants.
        Field fe = c.getField("PAGE_EXISTS");
        check(fe.getInt(null) == 0, "PAGE_EXISTS == 0");
        check(Modifier.isStatic(fe.getModifiers()) && Modifier.isFinal(fe.getModifiers()), "constant modifiers");
        check(c.getField("NO_SUCH_PAGE").getInt(null) == 1, "NO_SUCH_PAGE == 1");

        // Known consumers of the interface.
        Class<?> pageable = Class.forName("java.awt.print.Pageable");
        check(pageable.getMethod("getPrintable", int.class).getReturnType() == c,
                "Pageable serves Printable instances");

        // Polymorphic contract: the interface method receives all three arguments
        // and honors its documented result codes. A null Graphics is allowed for
        // implementations that do not draw.
        FirstPageOnly impl = new FirstPageOnly();
        Printable p = impl;
        check(p.print(null, null, 0) == Printable.PAGE_EXISTS, "page 0 exists");
        check(p.print(null, null, 1) == Printable.NO_SUCH_PAGE, "page 1 does not exist");
        check(impl.lastIndex == 1 && impl.lastFormat == null, "arguments delivered verbatim");
        // The constant values flow through the interface reference too.
        check(Printable.PAGE_EXISTS == 0 && Printable.NO_SUCH_PAGE == 1, "constants through interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
