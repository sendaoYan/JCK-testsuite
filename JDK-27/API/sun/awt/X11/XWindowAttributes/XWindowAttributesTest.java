/*
 * @test
 * @summary sun.awt.X11.XWindowAttributes conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XWindowAttributesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XWindowAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XWindowAttributes", false, XWindowAttributesTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XWindowAttributes".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_map_installed();public#boolean#get_override_redirect();public#boolean#get_save_under();public#int#getDataSize();public#int#get_backing_store();public#int#get_bit_gravity();public#int#get_border_width();public#int#get_class();public#int#get_depth();public#int#get_height();public#int#get_map_state();public#int#get_width();public#int#get_win_gravity();public#int#get_x();public#int#get_y();public#long#getPData();public#long#get_all_event_masks();public#long#get_backing_pixel();public#long#get_backing_planes();public#long#get_colormap();public#long#get_do_not_propagate_mask();public#long#get_root();public#long#get_screen();public#long#get_visual();public#long#get_your_event_mask();public#sun.awt.X11.Screen#get_screen(int);public#sun.awt.X11.Visual#get_visual(int);public#void#dispose();public#void#set_all_event_masks(long);public#void#set_backing_pixel(long);public#void#set_backing_planes(long);public#void#set_backing_store(int);public#void#set_bit_gravity(int);public#void#set_border_width(int);public#void#set_class(int);public#void#set_colormap(long);public#void#set_depth(int);public#void#set_do_not_propagate_mask(long);public#void#set_height(int);public#void#set_map_installed(boolean);public#void#set_map_state(int);public#void#set_override_redirect(boolean);public#void#set_root(long);public#void#set_save_under(boolean);public#void#set_screen(long);public#void#set_visual(long);public#void#set_width(int);public#void#set_win_gravity(int);public#void#set_x(int);public#void#set_y(int);public#void#set_your_event_mask(long)".equals(methods(c)), "methods");
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
