/*
 * @test
 * @summary sun.awt.X11.XkbEvent conformance: reflection (no init, native/display-heavy).
 * @run main XkbEventTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XkbEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XkbEvent", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XkbEvent".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#int#getDataSize();public#int#get_type();public#long#getPData();public#sun.awt.X11.XEvent#get_core();public#sun.awt.X11.XkbAccessXNotifyEvent#get_accessx();public#sun.awt.X11.XkbActionMessageEvent#get_message();public#sun.awt.X11.XkbAnyEvent#get_any();public#sun.awt.X11.XkbBellNotifyEvent#get_bell();public#sun.awt.X11.XkbCompatMapNotifyEvent#get_compat();public#sun.awt.X11.XkbControlsNotifyEvent#get_ctrls();public#sun.awt.X11.XkbExtensionDeviceNotifyEvent#get_device();public#sun.awt.X11.XkbIndicatorNotifyEvent#get_indicators();public#sun.awt.X11.XkbMapNotifyEvent#get_map();public#sun.awt.X11.XkbNamesNotifyEvent#get_names();public#sun.awt.X11.XkbNewKeyboardNotifyEvent#get_new_kbd();public#sun.awt.X11.XkbStateNotifyEvent#get_state();public#void#dispose();public#void#set_type(int)".equals(methods(c)), "methods");
        check("#long#pData;private final#boolean#should_free_memory;private#sun.misc.Unsafe#unsafe".equals(fields(c)), "fields");
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
