/*
 * @test
 * @summary com.sun.java.swing.plaf.gtk.GTKConstants.StateType conformance: internal enum reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.java.swing.plaf.gtk:+open
 * @run main StateTypeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StateTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.gtk.GTKConstants$StateType", false, StateTypeTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.java.swing.plaf.gtk.GTKConstants$StateType".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && "java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("valueOf;values".equals(methodNames(c)), "public/protected methods");
        check("ACTIVE;INSENSITIVE;NORMAL;PRELIGHT;SELECTED".equals(fieldNames(c)), "public/protected fields");
        check("ACTIVE;INSENSITIVE;NORMAL;PRELIGHT;SELECTED".equals(enumConstants(c)), "enum constants");
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
