/*
 * @test
 * @summary sun.awt.X11.Screen conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main ScreenTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ScreenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.Screen", false, ScreenTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.Screen".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_save_unders();public#int#getDataSize();public#int#get_backing_store();public#int#get_height();public#int#get_max_maps();public#int#get_mheight();public#int#get_min_maps();public#int#get_mwidth();public#int#get_ndepths();public#int#get_root_depth();public#int#get_width();public#long#getPData();public#long#get_black_pixel();public#long#get_cmap();public#long#get_default_gc();public#long#get_depths();public#long#get_display();public#long#get_display(int);public#long#get_ext_data();public#long#get_root();public#long#get_root_input_mask();public#long#get_root_visual();public#long#get_white_pixel();public#sun.awt.X11.Depth#get_depths(int);public#sun.awt.X11.Visual#get_root_visual(int);public#sun.awt.X11.XExtData#get_ext_data(int);public#void#dispose();public#void#set_backing_store(int);public#void#set_black_pixel(long);public#void#set_cmap(long);public#void#set_default_gc(long);public#void#set_depths(long);public#void#set_display(long);public#void#set_ext_data(long);public#void#set_height(int);public#void#set_max_maps(int);public#void#set_mheight(int);public#void#set_min_maps(int);public#void#set_mwidth(int);public#void#set_ndepths(int);public#void#set_root(long);public#void#set_root_depth(int);public#void#set_root_input_mask(long);public#void#set_root_visual(long);public#void#set_save_unders(boolean);public#void#set_white_pixel(long);public#void#set_width(int)".equals(methods(c)), "methods");
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
