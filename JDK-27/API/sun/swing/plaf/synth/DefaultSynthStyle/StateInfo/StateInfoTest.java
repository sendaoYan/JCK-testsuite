/*
 * @test
 * @summary sun.swing.plaf.synth.DefaultSynthStyle.StateInfo conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing.plaf.synth:+open
 * @run main StateInfoTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StateInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.DefaultSynthStyle$StateInfo", false, StateInfoTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.DefaultSynthStyle$StateInfo".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#[Ljava.awt.Color;#getColors();public#int#getComponentState();public#java.awt.Color#getColor(javax.swing.plaf.synth.ColorType);public#java.awt.Font#getFont();public#java.lang.Object#clone();public#java.lang.String#toString();public#java.util.Map#getData();public#sun.swing.plaf.synth.DefaultSynthStyle$StateInfo#addTo(sun.swing.plaf.synth.DefaultSynthStyle$StateInfo);public#void#setColors([Ljava.awt.Color;);public#void#setComponentState(int);public#void#setData(java.util.Map);public#void#setFont(java.awt.Font)".equals(methods(c)), "methods");
        check("private#[Ljava.awt.Color;#colors;private#int#state;private#java.awt.Font#font;private#java.util.Map#data".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 3, "ctors");
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
