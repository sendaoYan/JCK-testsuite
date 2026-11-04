/*
 * @test
 * @summary sun.java2d.pipe.BufferedPaints conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main BufferedPaintsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedPaintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedPaints", false, BufferedPaintsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedPaints".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#[I#convertToIntArgbPrePixels([Ljava.awt.Color;,boolean);private static#int#colorToIntArgbPrePixel(java.awt.Color,boolean);private static#void#setColor(sun.java2d.pipe.RenderQueue,int);private static#void#setGradientPaint(sun.java2d.pipe.RenderQueue,java.awt.geom.AffineTransform,java.awt.Color,java.awt.Color,java.awt.geom.Point2D,java.awt.geom.Point2D,boolean,boolean);private static#void#setGradientPaint(sun.java2d.pipe.RenderQueue,sun.java2d.SunGraphics2D,java.awt.GradientPaint,boolean);private static#void#setLinearGradientPaint(sun.java2d.pipe.RenderQueue,sun.java2d.SunGraphics2D,java.awt.LinearGradientPaint,boolean);private static#void#setRadialGradientPaint(sun.java2d.pipe.RenderQueue,sun.java2d.SunGraphics2D,java.awt.RadialGradientPaint,boolean);private static#void#setTexturePaint(sun.java2d.pipe.RenderQueue,sun.java2d.SunGraphics2D,java.awt.TexturePaint,boolean);public static#int#convertSRGBtoLinearRGB(int);static#void#resetPaint(sun.java2d.pipe.RenderQueue);static#void#setPaint(sun.java2d.pipe.RenderQueue,sun.java2d.SunGraphics2D,java.awt.Paint,int)".equals(methods(c)), "methods");
        check("public static final#int#MULTI_MAX_FRACTIONS".equals(fields(c)), "fields");
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
