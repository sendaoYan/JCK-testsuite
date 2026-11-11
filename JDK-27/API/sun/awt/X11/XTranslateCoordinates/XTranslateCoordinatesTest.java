/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.XTranslateCoordinates conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XTranslateCoordinatesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XTranslateCoordinatesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XTranslateCoordinates", false, XTranslateCoordinatesTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XTranslateCoordinates".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#boolean#isDisposed();public#boolean#isExecuted();public#int#execute();public#int#execute(sun.awt.X11.XErrorHandler);public#int#get_dest_x();public#int#get_dest_y();public#int#get_src_x();public#int#get_src_y();public#long#get_child();public#long#get_dest_w();public#long#get_scr_w();public#void#dispose();public#void#set_child(long);public#void#set_dest_w(long);public#void#set_dest_x(int);public#void#set_dest_y(int);public#void#set_scr_w(long);public#void#set_src_x(int);public#void#set_src_y(int)".equals(methods(c)), "methods");
        check("#int#_src_x;#int#_src_y;#long#_dest_w;#long#_scr_w;#long#child_ptr;#long#dest_x_ptr;#long#dest_y_ptr;#sun.awt.X11.UnsafeXDisposerRecord#disposer;private static#jdk.internal.misc.Unsafe#unsafe;private#boolean#__executed".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
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
