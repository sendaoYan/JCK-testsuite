/*
 * @test
 * @summary sun.awt.X11.XkbStateNotifyEvent conformance: reflection (no init, native/display-heavy).
 * @run main XkbStateNotifyEventTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XkbStateNotifyEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XkbStateNotifyEvent", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XkbStateNotifyEvent".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_send_event();public#byte#get_compat_grab_mods();public#byte#get_compat_lookup_mods();public#byte#get_event_type();public#byte#get_grab_mods();public#byte#get_lookup_mods();public#byte#get_req_major();public#byte#get_req_minor();public#int#getDataSize();public#int#get_base_group();public#int#get_base_mods();public#int#get_changed();public#int#get_compat_state();public#int#get_device();public#int#get_group();public#int#get_keycode();public#int#get_latched_group();public#int#get_latched_mods();public#int#get_locked_group();public#int#get_locked_mods();public#int#get_mods();public#int#get_ptr_buttons();public#int#get_type();public#int#get_xkb_type();public#long#getPData();public#long#get_display();public#long#get_serial();public#long#get_time();public#void#dispose();public#void#set_base_group(int);public#void#set_base_mods(int);public#void#set_changed(int);public#void#set_compat_grab_mods(byte);public#void#set_compat_lookup_mods(byte);public#void#set_compat_state(int);public#void#set_device(int);public#void#set_display(long);public#void#set_event_type(byte);public#void#set_grab_mods(byte);public#void#set_group(int);public#void#set_keycode(int);public#void#set_latched_group(int);public#void#set_latched_mods(int);public#void#set_locked_group(int);public#void#set_locked_mods(int);public#void#set_lookup_mods(byte);public#void#set_mods(int);public#void#set_ptr_buttons(int);public#void#set_req_major(byte);public#void#set_req_minor(byte);public#void#set_send_event(boolean);public#void#set_serial(long);public#void#set_time(long);public#void#set_type(int);public#void#set_xkb_type(int)".equals(methods(c)), "methods");
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
