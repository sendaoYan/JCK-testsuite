/*
 * @test
 * @summary sun.swing.plaf.synth.SynthIcon conformance: reflection (no init, native/display-heavy).
 * @run main SynthIconTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SynthIconTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.SynthIcon", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.SynthIcon".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("javax.swing.Icon".equals(interfaces(c)), "interfaces");
        check("public abstract#int#getIconHeight(javax.swing.plaf.synth.SynthContext);public abstract#int#getIconWidth(javax.swing.plaf.synth.SynthContext);public abstract#void#paintIcon(javax.swing.plaf.synth.SynthContext,java.awt.Graphics,int,int,int,int);public static#int#getIconHeight(javax.swing.Icon,javax.swing.plaf.synth.SynthContext);public static#int#getIconWidth(javax.swing.Icon,javax.swing.plaf.synth.SynthContext);public static#void#paintIcon(javax.swing.Icon,javax.swing.plaf.synth.SynthContext,java.awt.Graphics,int,int,int,int);public#int#getIconHeight();public#int#getIconWidth();public#void#paintIcon(java.awt.Component,java.awt.Graphics,int,int)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
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
