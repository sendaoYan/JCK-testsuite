/*
 * @test
 * @summary javax.print.ServiceUIFactory conformance: white-box structure + black-box behavior.
 * @run main ServiceUIFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ServiceUIFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.ServiceUIFactory");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "public abstract class");
        check(hasMethod(c, "getUI") && hasMethod(c, "getUIClassNamesForRole"), "abstract methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check("javax.swing.JComponent".equals(ServiceUIFactory.JCOMPONENT_UI), "JCOMPONENT_UI");
        check("java.awt.Panel".equals(ServiceUIFactory.PANEL_UI), "PANEL_UI");
        check("java.awt.Dialog".equals(ServiceUIFactory.DIALOG_UI), "DIALOG_UI");
        check("javax.swing.JDialog".equals(ServiceUIFactory.JDIALOG_UI), "JDIALOG_UI");
        check(ServiceUIFactory.ABOUT_UIROLE == 1 && ServiceUIFactory.ADMIN_UIROLE == 2 && ServiceUIFactory.MAIN_UIROLE == 3 && ServiceUIFactory.RESERVED_UIROLE == 99, "UI role constants");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
