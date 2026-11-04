/*
 * @test
 * @summary sun.java2d.pipe.DrawImagePipe conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main DrawImagePipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DrawImagePipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.DrawImagePipe", false, DrawImagePipeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.DrawImagePipe".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#transformImage(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public abstract#void#transformImage(sun.java2d.SunGraphics2D,java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
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
