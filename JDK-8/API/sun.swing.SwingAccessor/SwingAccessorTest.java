/*
 * @test
 * @summary sun.swing.SwingAccessor conformance: reflection (no init, native/display-heavy).
 * @run main SwingAccessorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SwingAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.SwingAccessor", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.swing.SwingAccessor".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#java.lang.Boolean#getAllowHTMLObject();public static#sun.swing.SwingAccessor$JLightweightFrameAccessor#getJLightweightFrameAccessor();public static#sun.swing.SwingAccessor$JTextComponentAccessor#getJTextComponentAccessor();public static#sun.swing.SwingAccessor$RepaintManagerAccessor#getRepaintManagerAccessor();public static#void#setAllowHTMLObject(java.lang.Boolean);public static#void#setJLightweightFrameAccessor(sun.swing.SwingAccessor$JLightweightFrameAccessor);public static#void#setJTextComponentAccessor(sun.swing.SwingAccessor$JTextComponentAccessor);public static#void#setRepaintManagerAccessor(sun.swing.SwingAccessor$RepaintManagerAccessor)".equals(methods(c)), "methods");
        check("private static final#sun.misc.Unsafe#unsafe;private static#java.lang.ThreadLocal#tlObj;private static#sun.swing.SwingAccessor$JLightweightFrameAccessor#jLightweightFrameAccessor;private static#sun.swing.SwingAccessor$JTextComponentAccessor#jtextComponentAccessor;private static#sun.swing.SwingAccessor$RepaintManagerAccessor#repaintManagerAccessor".equals(fields(c)), "fields");
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
            s.append(Modifier.toString(m.getModifiers())).append('#');
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
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
