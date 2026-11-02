/*
 * @test
 * @summary sun.swing.plaf.synth.Paint9Painter conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.swing.plaf.synth:+open
 * @run main Paint9PainterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class Paint9PainterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.plaf.synth.Paint9Painter", false, Paint9PainterTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.swing.plaf.synth.Paint9Painter".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.swing.CachedPainter".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#drawChunk(java.awt.Image,java.awt.Graphics,boolean,int,int,int,int,int,int,int,int,boolean);private#void#drawImage(java.awt.Image,java.awt.Graphics,int,int,int,int,int,int,int,int);protected#java.awt.Image#createImage(java.awt.Component,int,int,java.awt.GraphicsConfiguration,[Ljava.lang.Object;);protected#void#paint9(java.awt.Graphics,int,int,int,int,java.awt.Image,java.awt.Insets,java.awt.Insets,sun.swing.plaf.synth.Paint9Painter$PaintType,int);protected#void#paintToImage(java.awt.Component,java.awt.Image,java.awt.Graphics,int,int,[Ljava.lang.Object;);public static#boolean#validImage(java.awt.Image);public#void#paint(java.awt.Component,java.awt.Graphics,int,int,int,int,java.awt.Image,java.awt.Insets,java.awt.Insets,sun.swing.plaf.synth.Paint9Painter$PaintType,int)".equals(methods(c)), "methods");
        check("private static final#java.awt.Insets#EMPTY_INSETS;public static final#int#PAINT_ALL;public static final#int#PAINT_BOTTOM;public static final#int#PAINT_BOTTOM_LEFT;public static final#int#PAINT_BOTTOM_RIGHT;public static final#int#PAINT_CENTER;public static final#int#PAINT_LEFT;public static final#int#PAINT_RIGHT;public static final#int#PAINT_TOP;public static final#int#PAINT_TOP_LEFT;public static final#int#PAINT_TOP_RIGHT".equals(fields(c)), "fields");
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
