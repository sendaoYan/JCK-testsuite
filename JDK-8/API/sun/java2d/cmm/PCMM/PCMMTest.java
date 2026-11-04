/*
 * @test
 * @summary sun.java2d.cmm.PCMM conformance: reflection (no init, native/display-heavy).
 * @run main PCMMTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PCMMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.cmm.PCMM", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.cmm.PCMM".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.getSuperclass() == null, "superclass null");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#int#getProfileSize(sun.java2d.cmm.Profile);public abstract#int#getTagSize(sun.java2d.cmm.Profile,int);public abstract#sun.java2d.cmm.ColorTransform#createTransform([Lsun.java2d.cmm.ColorTransform;);public abstract#sun.java2d.cmm.ColorTransform#createTransform(java.awt.color.ICC_Profile,int,int);public abstract#sun.java2d.cmm.Profile#loadProfile([B);public abstract#void#freeProfile(sun.java2d.cmm.Profile);public abstract#void#getProfileData(sun.java2d.cmm.Profile,[B);public abstract#void#getTagData(sun.java2d.cmm.Profile,int,[B);public abstract#void#setTagData(sun.java2d.cmm.Profile,int,[B)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 0, "ctors");
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
