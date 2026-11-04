/*
 * @test
 * @summary sun.awt.X11.MWMConstants conformance: reflection (no init, native/display-heavy).
 * @run main MWMConstantsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MWMConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.MWMConstants", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.MWMConstants".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("static final#int#MWM_DECOR_ALL;static final#int#MWM_DECOR_BORDER;static final#int#MWM_DECOR_MAXIMIZE;static final#int#MWM_DECOR_MENU;static final#int#MWM_DECOR_MINIMIZE;static final#int#MWM_DECOR_RESIZEH;static final#int#MWM_DECOR_TITLE;static final#int#MWM_FUNC_ALL;static final#int#MWM_FUNC_CLOSE;static final#int#MWM_FUNC_MAXIMIZE;static final#int#MWM_FUNC_MINIMIZE;static final#int#MWM_FUNC_MOVE;static final#int#MWM_FUNC_RESIZE;static final#int#MWM_HINTS_DECORATIONS;static final#int#MWM_HINTS_FUNCTIONS;static final#int#MWM_HINTS_INPUT_MODE;static final#int#MWM_HINTS_STATUS;static final#int#MWM_INPUT_FULL_APPLICATION_MODAL;static final#int#MWM_INPUT_MODELESS;static final#int#MWM_INPUT_PRIMARY_APPLICATION_MODAL;static final#int#MWM_INPUT_SYSTEM_MODAL;static final#int#PROP_MOTIF_WM_INFO_ELEMENTS;static final#int#PROP_MWM_HINTS_ELEMENTS;static final#int#PROP_MWM_INFO_ELEMENTS;static final#java.lang.String#MWM_HINTS_ATOM_NAME".equals(fields(c)), "fields");
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
