/*
 * @test
 * @summary sun.swing.SwingAccessor conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing:+open
 * @run main SwingAccessorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SwingAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.SwingAccessor", false, SwingAccessorTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.SwingAccessor".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#ensureClassInitialized(java.lang.Class);public static#java.lang.Boolean#getAllowHTMLObject();public static#sun.swing.SwingAccessor$AccessibleComponentAccessor#getAccessibleComponentAccessor();public static#sun.swing.SwingAccessor$JComponentAccessor#getJComponentAccessor();public static#sun.swing.SwingAccessor$JLightweightFrameAccessor#getJLightweightFrameAccessor();public static#sun.swing.SwingAccessor$JTextComponentAccessor#getJTextComponentAccessor();public static#sun.swing.SwingAccessor$KeyStrokeAccessor#getKeyStrokeAccessor();public static#sun.swing.SwingAccessor$LAFStateAccessor#getLAFStateAccessor();public static#sun.swing.SwingAccessor$PopupFactoryAccessor#getPopupFactoryAccessor();public static#sun.swing.SwingAccessor$RepaintManagerAccessor#getRepaintManagerAccessor();public static#sun.swing.SwingAccessor$UIDefaultsAccessor#getUIDefaultsAccessor();public static#void#setAccessibleComponentAccessor(sun.swing.SwingAccessor$AccessibleComponentAccessor);public static#void#setAllowHTMLObject(java.lang.Boolean);public static#void#setJComponentAccessor(sun.swing.SwingAccessor$JComponentAccessor);public static#void#setJLightweightFrameAccessor(sun.swing.SwingAccessor$JLightweightFrameAccessor);public static#void#setJTextComponentAccessor(sun.swing.SwingAccessor$JTextComponentAccessor);public static#void#setKeyStrokeAccessor(sun.swing.SwingAccessor$KeyStrokeAccessor);public static#void#setLAFStateAccessor(sun.swing.SwingAccessor$LAFStateAccessor);public static#void#setPopupFactoryAccessor(sun.swing.SwingAccessor$PopupFactoryAccessor);public static#void#setRepaintManagerAccessor(sun.swing.SwingAccessor$RepaintManagerAccessor);public static#void#setUIDefaultsAccessor(sun.swing.SwingAccessor$UIDefaultsAccessor)".equals(methods(c)), "methods");
        check("private static#java.lang.ThreadLocal#tlObj;private static#sun.swing.SwingAccessor$AccessibleComponentAccessor#accessibleComponentAccessor;private static#sun.swing.SwingAccessor$JComponentAccessor#jComponentAccessor;private static#sun.swing.SwingAccessor$JLightweightFrameAccessor#jLightweightFrameAccessor;private static#sun.swing.SwingAccessor$JTextComponentAccessor#jtextComponentAccessor;private static#sun.swing.SwingAccessor$KeyStrokeAccessor#keyStrokeAccessor;private static#sun.swing.SwingAccessor$LAFStateAccessor#lafStateAccessor;private static#sun.swing.SwingAccessor$PopupFactoryAccessor#popupFactoryAccessor;private static#sun.swing.SwingAccessor$RepaintManagerAccessor#repaintManagerAccessor;private static#sun.swing.SwingAccessor$UIDefaultsAccessor#uiDefaultsAccessor".equals(fields(c)), "fields");
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
