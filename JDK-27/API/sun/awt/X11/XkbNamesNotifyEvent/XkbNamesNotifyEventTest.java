/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XkbNamesNotifyEvent conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XkbNamesNotifyEventTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XkbNamesNotifyEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XkbNamesNotifyEvent", false, XkbNamesNotifyEventTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XkbNamesNotifyEvent".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_send_event();public#int#getDataSize();public#int#get_changed();public#int#get_changed_groups();public#int#get_changed_indicators();public#int#get_changed_vmods();public#int#get_device();public#int#get_first_key();public#int#get_first_lvl();public#int#get_first_type();public#int#get_num_aliases();public#int#get_num_keys();public#int#get_num_lvls();public#int#get_num_radio_groups();public#int#get_num_types();public#int#get_type();public#int#get_xkb_type();public#long#getPData();public#long#get_display();public#long#get_serial();public#long#get_time();public#void#dispose();public#void#set_changed(int);public#void#set_changed_groups(int);public#void#set_changed_indicators(int);public#void#set_changed_vmods(int);public#void#set_device(int);public#void#set_display(long);public#void#set_first_key(int);public#void#set_first_lvl(int);public#void#set_first_type(int);public#void#set_num_aliases(int);public#void#set_num_keys(int);public#void#set_num_lvls(int);public#void#set_num_radio_groups(int);public#void#set_num_types(int);public#void#set_send_event(boolean);public#void#set_serial(long);public#void#set_time(long);public#void#set_type(int);public#void#set_xkb_type(int)".equals(methods(c)), "methods");
        check("#long#pData;private final#boolean#should_free_memory;private#jdk.internal.misc.Unsafe#unsafe".equals(fields(c)), "fields");
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
