/*
 * @test
 * @summary sun.swing.MenuItemLayoutHelper.LayoutResult conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main LayoutResultTest
 */

import java.util.*;
import java.lang.reflect.*;

public class LayoutResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.MenuItemLayoutHelper$LayoutResult", false, LayoutResultTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.MenuItemLayoutHelper$LayoutResult".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#java.awt.Rectangle#getAccRect();public#java.awt.Rectangle#getArrowRect();public#java.awt.Rectangle#getCheckRect();public#java.awt.Rectangle#getIconRect();public#java.awt.Rectangle#getLabelRect();public#java.awt.Rectangle#getTextRect();public#java.util.Map#getAllRects();public#void#setAccRect(java.awt.Rectangle);public#void#setArrowRect(java.awt.Rectangle);public#void#setCheckRect(java.awt.Rectangle);public#void#setIconRect(java.awt.Rectangle);public#void#setLabelRect(java.awt.Rectangle);public#void#setTextRect(java.awt.Rectangle)".equals(methods(c)), "methods");
        check("private#java.awt.Rectangle#accRect;private#java.awt.Rectangle#arrowRect;private#java.awt.Rectangle#checkRect;private#java.awt.Rectangle#iconRect;private#java.awt.Rectangle#labelRect;private#java.awt.Rectangle#textRect".equals(fields(c)), "fields");
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
