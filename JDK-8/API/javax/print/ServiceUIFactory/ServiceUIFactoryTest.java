/*
 * @test
 * @summary javax.print.ServiceUIFactory conformance: service UI factory.
 * @run main ServiceUIFactoryTest
 */

import java.lang.reflect.*;


public class ServiceUIFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.ServiceUIFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(hasMethod(c, "getUI"), "declares getUI");
        check(hasMethod(c, "getUIClassNamesForRole"), "declares getUIClassNamesForRole");
        check("javax.swing.JComponent".equals(javax.print.ServiceUIFactory.JCOMPONENT_UI), "JCOMPONENT_UI");
        check("java.awt.Panel".equals(javax.print.ServiceUIFactory.PANEL_UI), "PANEL_UI");
        check("java.awt.Dialog".equals(javax.print.ServiceUIFactory.DIALOG_UI), "DIALOG_UI");
        check("javax.swing.JDialog".equals(javax.print.ServiceUIFactory.JDIALOG_UI), "JDIALOG_UI");
        check(javax.print.ServiceUIFactory.ABOUT_UIROLE == 1, "ABOUT_UIROLE");
        check(javax.print.ServiceUIFactory.ADMIN_UIROLE == 2, "ADMIN_UIROLE");
        check(javax.print.ServiceUIFactory.MAIN_UIROLE == 3, "MAIN_UIROLE");
        check(javax.print.ServiceUIFactory.RESERVED_UIROLE == 99, "RESERVED_UIROLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
