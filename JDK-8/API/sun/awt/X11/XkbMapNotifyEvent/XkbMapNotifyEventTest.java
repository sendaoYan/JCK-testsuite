/*
 * @test
 * @summary sun.awt.X11.XkbMapNotifyEvent conformance: reflection (no init, native/display-heavy).
 * @run main XkbMapNotifyEventTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XkbMapNotifyEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XkbMapNotifyEvent", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XkbMapNotifyEvent".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_send_event();public#int#getDataSize();public#int#get_changed();public#int#get_device();public#int#get_first_key_act();public#int#get_first_key_behavior();public#int#get_first_key_explicit();public#int#get_first_key_sym();public#int#get_first_modmap_key();public#int#get_first_type();public#int#get_first_vmodmap_key();public#int#get_flags();public#int#get_max_key_code();public#int#get_min_key_code();public#int#get_num_key_acts();public#int#get_num_key_behaviors();public#int#get_num_key_explicit();public#int#get_num_key_syms();public#int#get_num_modmap_keys();public#int#get_num_types();public#int#get_num_vmodmap_keys();public#int#get_type();public#int#get_vmods();public#int#get_xkb_type();public#long#getPData();public#long#get_display();public#long#get_serial();public#long#get_time();public#void#dispose();public#void#set_changed(int);public#void#set_device(int);public#void#set_display(long);public#void#set_first_key_act(int);public#void#set_first_key_behavior(int);public#void#set_first_key_explicit(int);public#void#set_first_key_sym(int);public#void#set_first_modmap_key(int);public#void#set_first_type(int);public#void#set_first_vmodmap_key(int);public#void#set_flags(int);public#void#set_max_key_code(int);public#void#set_min_key_code(int);public#void#set_num_key_acts(int);public#void#set_num_key_behaviors(int);public#void#set_num_key_explicit(int);public#void#set_num_key_syms(int);public#void#set_num_modmap_keys(int);public#void#set_num_types(int);public#void#set_num_vmodmap_keys(int);public#void#set_send_event(boolean);public#void#set_serial(long);public#void#set_time(long);public#void#set_type(int);public#void#set_vmods(int);public#void#set_xkb_type(int)".equals(methods(c)), "methods");
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
