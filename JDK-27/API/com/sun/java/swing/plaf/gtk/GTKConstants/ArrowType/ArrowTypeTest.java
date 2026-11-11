/*
 * @test
 * @requires os.family == "linux"
 * @summary com.sun.java.swing.plaf.gtk.GTKConstants.ArrowType conformance: internal enum reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.java.swing.plaf.gtk:+open
 * @run main ArrowTypeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ArrowTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.gtk.GTKConstants$ArrowType", false, ArrowTypeTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.java.swing.plaf.gtk.GTKConstants$ArrowType".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("valueOf;values".equals(methodNames(c)), "public/protected methods");
        check("DOWN;LEFT;RIGHT;UP".equals(fieldNames(c)), "public/protected fields");
        check("DOWN;LEFT;RIGHT;UP".equals(enumConstants(c)), "enum constants");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String ifaces(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return String.join(";", s);
    }

    static String methodNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return String.join(";", s);
    }

    static String fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            int fm = f.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(f.getName());
        }
        return String.join(";", s);
    }

    static String enumConstants(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
        return String.join(";", s);
    }
}
