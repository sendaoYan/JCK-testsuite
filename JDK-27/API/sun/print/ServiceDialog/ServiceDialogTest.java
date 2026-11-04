/*
 * @test
 * @summary sun.print.ServiceDialog conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main ServiceDialogTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ServiceDialogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.ServiceDialog", false, ServiceDialogTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.ServiceDialog".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JDialog".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.event.ActionListener".equals(interfaces(c)), "interfaces");
        check("#void#initPageDialog(int,int,javax.print.PrintService,javax.print.DocFlavor,javax.print.attribute.PrintRequestAttributeSet);#void#initPrintDialog(int,int,[Ljavax.print.PrintService;,int,javax.print.DocFlavor,javax.print.attribute.PrintRequestAttributeSet);private static#char#getMnemonic(java.lang.String);private static#int#getVKMnemonic(java.lang.String);private static#java.lang.String#removeMnemonics(java.lang.String);private static#java.net.URL#getImageResource(java.lang.String);private static#javax.swing.JButton#createButton(java.lang.String,java.awt.event.ActionListener);private static#javax.swing.JButton#createExitButton(java.lang.String,java.awt.event.ActionListener);private static#javax.swing.JCheckBox#createCheckBox(java.lang.String,java.awt.event.ActionListener);private static#javax.swing.JRadioButton#createRadioButton(java.lang.String,java.awt.event.ActionListener);private static#void#addToBG(javax.swing.AbstractButton,java.awt.Container,javax.swing.ButtonGroup);private static#void#addToGB(java.awt.Component,java.awt.Container,java.awt.GridBagLayout,java.awt.GridBagConstraints);private#boolean#showFileChooser();private#void#handleEscKey(javax.swing.JButton);private#void#updatePanels();public static#java.lang.String#getMsg(java.lang.String);public static#void#initResource();public static#void#showNoPrintService(java.awt.GraphicsConfiguration);public#int#getStatus();public#javax.print.PrintService#getPrintService();public#javax.print.attribute.PrintRequestAttributeSet#getAttributes();public#void#actionPerformed(java.awt.event.ActionEvent);public#void#dispose(int)".equals(methods(c)), "methods");
        check("private static final#java.awt.Insets#compInsets;private static final#java.awt.Insets#panelInsets;private static final#java.lang.String#strBundle;private static#java.util.ResourceBundle#messageRB;private#[Ljavax.print.PrintService;#services;private#boolean#isAWT;private#int#defaultServiceIndex;private#int#status;private#javax.print.DocFlavor#docFlavor;private#javax.print.PrintService#psCurrent;private#javax.print.attribute.HashPrintRequestAttributeSet#asCurrent;private#javax.print.attribute.PrintRequestAttributeSet#asOriginal;private#javax.swing.JButton#btnApprove;private#javax.swing.JButton#btnCancel;private#javax.swing.JTabbedPane#tpTabs;private#sun.print.ServiceDialog$AppearancePanel#pnlAppearance;private#sun.print.ServiceDialog$GeneralPanel#pnlGeneral;private#sun.print.ServiceDialog$PageSetupPanel#pnlPageSetup;private#sun.print.ServiceDialog$ValidatingFileChooser#jfc;public static final#int#APPROVE;public static final#int#CANCEL;public static final#int#WAITING;static#java.lang.Class#_keyEventClazz".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
