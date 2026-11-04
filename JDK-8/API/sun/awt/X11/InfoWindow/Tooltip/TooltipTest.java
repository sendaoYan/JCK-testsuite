/*
 * @test
 * @summary sun.awt.X11.InfoWindow.Tooltip conformance: reflection (no init, native/display-heavy).
 * @run main TooltipTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TooltipTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.InfoWindow$Tooltip", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.InfoWindow$Tooltip".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.InfoWindow".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#boolean#isPointerOverTrayIcon(java.awt.Rectangle);private#void#display();public#void#enter();public#void#exit()".equals(methods(c)), "methods");
        check("private final#java.awt.Label#textLabel;private final#java.lang.Object#target;private final#java.lang.Runnable#starter;private final#sun.awt.X11.InfoWindow$Tooltip$LiveArguments#liveArguments;private static final#int#TOOLTIP_MAX_LENGTH;private static final#int#TOOLTIP_MOUSE_CURSOR_INDENT;private static final#int#TOOLTIP_SHOW_TIME;private static final#int#TOOLTIP_START_DELAY_TIME;private static final#java.awt.Color#TOOLTIP_BACKGROUND_COLOR;private static final#java.awt.Font#TOOLTIP_TEXT_FONT".equals(fields(c)), "fields");
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
