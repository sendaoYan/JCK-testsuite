/*
 * @test
 * @summary sun.java2d.loops.RenderLoops conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.loops:+open
 * @run main RenderLoopsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RenderLoopsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.RenderLoops", false, RenderLoopsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.RenderLoops".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("public static final#int#primTypeID;public#sun.java2d.loops.DrawGlyphList#drawGlyphListLoop;public#sun.java2d.loops.DrawGlyphListAA#drawGlyphListAALoop;public#sun.java2d.loops.DrawGlyphListColor#drawGlyphListColorLoop;public#sun.java2d.loops.DrawGlyphListLCD#drawGlyphListLCDLoop;public#sun.java2d.loops.DrawLine#drawLineLoop;public#sun.java2d.loops.DrawParallelogram#drawParallelogramLoop;public#sun.java2d.loops.DrawPath#drawPathLoop;public#sun.java2d.loops.DrawPolygons#drawPolygonsLoop;public#sun.java2d.loops.DrawRect#drawRectLoop;public#sun.java2d.loops.FillParallelogram#fillParallelogramLoop;public#sun.java2d.loops.FillPath#fillPathLoop;public#sun.java2d.loops.FillRect#fillRectLoop;public#sun.java2d.loops.FillSpans#fillSpansLoop".equals(fields(c)), "fields");
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
