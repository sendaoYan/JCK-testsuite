/*
 * @test
 * @summary sun.java2d.pipe.DrawImagePipe conformance: reflection (no init, native/display-heavy).
 * @run main DrawImagePipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DrawImagePipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.DrawImagePipe", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.DrawImagePipe".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.getSuperclass() == null, "superclass null");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#copyImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#scaleImage(sun.java2d.SunGraphics2D,java.awt.Image,int,int,int,int,java.awt.Color,java.awt.image.ImageObserver);public abstract#boolean#transformImage(sun.java2d.SunGraphics2D,java.awt.Image,java.awt.geom.AffineTransform,java.awt.image.ImageObserver);public abstract#void#transformImage(sun.java2d.SunGraphics2D,java.awt.image.BufferedImage,java.awt.image.BufferedImageOp,int,int)".equals(methods(c)), "methods");
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
