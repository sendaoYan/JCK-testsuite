/*
 * @test
 * @summary sun.print.PeekMetrics conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.print:+open
 * @run main PeekMetricsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PeekMetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PeekMetrics", false, PeekMetricsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.print.PeekMetrics".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#checkAlpha(java.awt.Composite);private#void#checkDrawingMode(java.awt.Graphics2D);private#void#checkPaint(java.awt.Paint);public#boolean#hasCompositing();public#boolean#hasImages();public#boolean#hasNonSolidColors();public#boolean#hasText();public#void#clear(java.awt.Graphics2D);public#void#draw(java.awt.Graphics2D);public#void#drawImage(java.awt.Graphics2D,java.awt.Image);public#void#drawImage(java.awt.Graphics2D,java.awt.image.RenderedImage);public#void#drawImage(java.awt.Graphics2D,java.awt.image.renderable.RenderableImage);public#void#drawText(java.awt.Graphics2D);public#void#drawText(java.awt.Graphics2D,java.awt.font.TextLayout);public#void#fill(java.awt.Graphics2D)".equals(methods(c)), "methods");
        check("private#boolean#mHasCompositing;private#boolean#mHasImages;private#boolean#mHasNonSolidColors;private#boolean#mHasText".equals(fields(c)), "fields");
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
