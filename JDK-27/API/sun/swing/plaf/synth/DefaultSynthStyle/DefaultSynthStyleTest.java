/*
 * @test
 * @summary sun.swing.plaf.synth.DefaultSynthStyle conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing.plaf.synth:+open
 * @run main DefaultSynthStyleTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DefaultSynthStyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.DefaultSynthStyle", false, DefaultSynthStyleTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.DefaultSynthStyle".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.plaf.synth.SynthStyle".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Cloneable".equals(interfaces(c)), "interfaces");
        check("private#java.lang.Object#getKeyFromData(java.util.Map,java.lang.Object);protected#java.awt.Color#getColorForState(javax.swing.JComponent,javax.swing.plaf.synth.Region,int,javax.swing.plaf.synth.ColorType);protected#java.awt.Color#getColorForState(javax.swing.plaf.synth.SynthContext,javax.swing.plaf.synth.ColorType);protected#java.awt.Font#getFontForState(javax.swing.JComponent,javax.swing.plaf.synth.Region,int);protected#java.awt.Font#getFontForState(javax.swing.plaf.synth.SynthContext);public#[Lsun.swing.plaf.synth.DefaultSynthStyle$StateInfo;#getStateInfo();public#boolean#isOpaque(javax.swing.plaf.synth.SynthContext);public#java.awt.Color#getColor(javax.swing.JComponent,javax.swing.plaf.synth.Region,int,javax.swing.plaf.synth.ColorType);public#java.awt.Color#getColor(javax.swing.plaf.synth.SynthContext,javax.swing.plaf.synth.ColorType);public#java.awt.Font#getFont(javax.swing.JComponent,javax.swing.plaf.synth.Region,int);public#java.awt.Font#getFont(javax.swing.plaf.synth.SynthContext);public#java.awt.Insets#getInsets(javax.swing.plaf.synth.SynthContext,java.awt.Insets);public#java.lang.Object#clone();public#java.lang.Object#get(javax.swing.plaf.synth.SynthContext,java.lang.Object);public#java.lang.Object#getDefaultValue(javax.swing.plaf.synth.SynthContext,java.lang.Object);public#java.lang.String#toString();public#java.util.Map#getData();public#javax.swing.plaf.synth.SynthGraphicsUtils#getGraphicsUtils(javax.swing.plaf.synth.SynthContext);public#javax.swing.plaf.synth.SynthPainter#getPainter(javax.swing.plaf.synth.SynthContext);public#sun.swing.plaf.synth.DefaultSynthStyle#addTo(sun.swing.plaf.synth.DefaultSynthStyle);public#sun.swing.plaf.synth.DefaultSynthStyle$StateInfo#getStateInfo(int);public#void#setData(java.util.Map);public#void#setFont(java.awt.Font);public#void#setGraphicsUtils(javax.swing.plaf.synth.SynthGraphicsUtils);public#void#setInsets(java.awt.Insets);public#void#setOpaque(boolean);public#void#setPainter(javax.swing.plaf.synth.SynthPainter);public#void#setStateInfo([Lsun.swing.plaf.synth.DefaultSynthStyle$StateInfo;)".equals(methods(c)), "methods");
        check("private static final#java.lang.Object#PENDING;private#[Lsun.swing.plaf.synth.DefaultSynthStyle$StateInfo;#states;private#boolean#opaque;private#java.awt.Font#font;private#java.awt.Insets#insets;private#java.util.Map#data;private#javax.swing.plaf.synth.SynthGraphicsUtils#synthGraphics;private#javax.swing.plaf.synth.SynthPainter#painter".equals(fields(c)), "fields");
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
