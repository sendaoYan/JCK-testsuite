/*
 * @test
 * @summary sun.swing.DefaultLayoutStyle conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main DefaultLayoutStyleTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DefaultLayoutStyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.DefaultLayoutStyle", false, DefaultLayoutStyleTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.DefaultLayoutStyle".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.LayoutStyle".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#boolean#isLeftAligned(javax.swing.AbstractButton,int);private#boolean#isRightAligned(javax.swing.AbstractButton,int);private#int#getInset(java.awt.Insets,int);private#int#getInset(javax.swing.JComponent,int);private#javax.swing.Icon#getIcon(javax.swing.AbstractButton);private#void#checkPosition(int);protected#boolean#isLabelAndNonlabel(javax.swing.JComponent,javax.swing.JComponent,int);protected#int#flipDirection(int);protected#int#getButtonGap(javax.swing.JComponent,int,int);protected#int#getButtonGap(javax.swing.JComponent,javax.swing.JComponent,int,int);protected#int#getIndent(javax.swing.JComponent,int);public static#javax.swing.LayoutStyle#getInstance();public#int#getButtonGap(javax.swing.JComponent,int);public#int#getContainerGap(javax.swing.JComponent,int,java.awt.Container);public#int#getPreferredGap(javax.swing.JComponent,javax.swing.JComponent,javax.swing.LayoutStyle$ComponentPlacement,int,java.awt.Container)".equals(methods(c)), "methods");
        check("private static final#sun.swing.DefaultLayoutStyle#INSTANCE".equals(fields(c)), "fields");
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
