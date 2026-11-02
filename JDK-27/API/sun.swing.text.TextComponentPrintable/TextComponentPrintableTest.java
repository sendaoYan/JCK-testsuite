/*
 * @test
 * @summary sun.swing.text.TextComponentPrintable conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing.text:+open
 * @run main TextComponentPrintableTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TextComponentPrintableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.text.TextComponentPrintable", false, TextComponentPrintableTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.text.TextComponentPrintable".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.swing.text.CountingPrintable".equals(interfaces(c)), "interfaces");
        check("private static#boolean#isFrameSetDocument(javax.swing.text.Document);private static#java.util.List#getFrames(javax.swing.JEditorPane);private static#void#createFrames(javax.swing.JEditorPane);private static#void#getFrames(java.awt.Container,java.util.List);private#int#printOnEDT(java.awt.Graphics,java.awt.print.PageFormat,int);private#javax.swing.text.JTextComponent#createPrintShell(javax.swing.text.JTextComponent);private#javax.swing.text.JTextComponent#createPrintShellOnEDT(javax.swing.text.JTextComponent);private#void#acquireReadLock();private#void#calculateRowsMetrics();private#void#layout(int);private#void#layoutOnEDT(int);private#void#releaseReadLock();private#void#updatePagesMetrics(int,int);public static#java.awt.print.Printable#getPrintable(javax.swing.text.JTextComponent,java.text.MessageFormat,java.text.MessageFormat);public#int#getNumberOfPages();public#int#print(java.awt.Graphics,java.awt.print.PageFormat,int)".equals(methods(c)), "methods");
        check("private final#java.awt.Font#footerFont;private final#java.awt.Font#headerFont;private final#java.text.MessageFormat#footerFormat;private final#java.text.MessageFormat#headerFormat;private final#java.util.List#pagesMetrics;private final#java.util.List#rowsMetrics;private final#javax.swing.text.JTextComponent#printShell;private final#javax.swing.text.JTextComponent#textComponentToPrint;private static final#float#FOOTER_FONT_SIZE;private static final#float#HEADER_FONT_SIZE;private static final#int#LIST_SIZE;private volatile#java.awt.font.FontRenderContext#frc;private#boolean#isLayouted;private#boolean#needReadLock".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
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
