/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XSetWindowAttributes conformance: reflection (no init, native/display-heavy).
 * @run main XSetWindowAttributesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XSetWindowAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XSetWindowAttributes", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XSetWindowAttributes".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#boolean#get_override_redirect();public#boolean#get_save_under();public#int#getDataSize();public#int#get_backing_store();public#int#get_bit_gravity();public#int#get_win_gravity();public#long#getPData();public#long#get_background_pixel();public#long#get_background_pixmap();public#long#get_backing_pixel();public#long#get_backing_planes();public#long#get_border_pixel();public#long#get_border_pixmap();public#long#get_colormap();public#long#get_cursor();public#long#get_do_not_propagate_mask();public#long#get_event_mask();public#void#dispose();public#void#set_background_pixel(long);public#void#set_background_pixmap(long);public#void#set_backing_pixel(long);public#void#set_backing_planes(long);public#void#set_backing_store(int);public#void#set_bit_gravity(int);public#void#set_border_pixel(long);public#void#set_border_pixmap(long);public#void#set_colormap(long);public#void#set_cursor(long);public#void#set_do_not_propagate_mask(long);public#void#set_event_mask(long);public#void#set_override_redirect(boolean);public#void#set_save_under(boolean);public#void#set_win_gravity(int)".equals(methods(c)), "methods");
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
