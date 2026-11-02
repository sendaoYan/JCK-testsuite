/*
 * @test
 * @summary sun.awt.X11.XGlobalCursorManager conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XGlobalCursorManagerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XGlobalCursorManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XGlobalCursorManager", false, XGlobalCursorManagerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XGlobalCursorManager".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.GlobalCursorManager".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#updateGrabbedCursor(java.awt.Cursor);private#java.awt.Cursor#getCapableCursor(java.awt.Component);protected#java.awt.Component#findHeavyweightUnderCursor();protected#java.awt.Component#findHeavyweightUnderCursor(boolean);protected#java.awt.Point#getLocationOnScreen(java.awt.Component);protected#void#getCursorPos(java.awt.Point);protected#void#setCursor(java.awt.Component,java.awt.Cursor,boolean);protected#void#updateCursorOutOfJava();static#long#getCursor(java.awt.Cursor);static#sun.awt.GlobalCursorManager#getCursorManager();static#void#nativeUpdateCursor(java.awt.Component);static#void#setPData(java.awt.Cursor,long)".equals(methods(c)), "methods");
        check("private static#sun.awt.X11.XGlobalCursorManager#manager;private#java.lang.ref.WeakReference#nativeContainer".equals(fields(c)), "fields");
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
