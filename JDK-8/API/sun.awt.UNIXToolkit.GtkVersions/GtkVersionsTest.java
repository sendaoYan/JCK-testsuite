/*
 * @test
 * @summary sun.awt.UNIXToolkit.GtkVersions conformance: reflection (no init, native/display-heavy).
 * @run main GtkVersionsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GtkVersionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.UNIXToolkit$GtkVersions", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.UNIXToolkit$GtkVersions".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#[Lsun.awt.UNIXToolkit$GtkVersions;#values();public static#sun.awt.UNIXToolkit$GtkVersions#getVersion(int);public static#sun.awt.UNIXToolkit$GtkVersions#valueOf(java.lang.String);public#int#getNumber()".equals(methods(c)), "methods");
        check("final#int#number;public static final#sun.awt.UNIXToolkit$GtkVersions#ANY;public static final#sun.awt.UNIXToolkit$GtkVersions#GTK2;public static final#sun.awt.UNIXToolkit$GtkVersions#GTK3".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        check("ANY;GTK2;GTK3".equals(enumConstants(c)), "enum constants");
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
