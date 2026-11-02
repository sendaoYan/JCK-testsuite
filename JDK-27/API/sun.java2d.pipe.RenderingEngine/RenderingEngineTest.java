/*
 * @test
 * @summary sun.java2d.pipe.RenderingEngine conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main RenderingEngineTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RenderingEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.RenderingEngine", false, RenderingEngineTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.RenderingEngine".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#float#getMinimumAAPenSize();public abstract#java.awt.Shape#createStrokedShape(java.awt.Shape,float,int,int,float,[F,float);public abstract#sun.java2d.pipe.AATileGenerator#getAATileGenerator(double,double,double,double,double,double,double,double,sun.java2d.pipe.Region,[I);public abstract#sun.java2d.pipe.AATileGenerator#getAATileGenerator(java.awt.Shape,java.awt.geom.AffineTransform,sun.java2d.pipe.Region,java.awt.BasicStroke,boolean,boolean,[I);public abstract#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,java.awt.BasicStroke,boolean,boolean,boolean,sun.awt.geom.PathConsumer2D);public static synchronized#sun.java2d.pipe.RenderingEngine#getInstance();public static#void#feedConsumer(java.awt.geom.PathIterator,sun.awt.geom.PathConsumer2D);public#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,sun.java2d.pipe.Region,java.awt.BasicStroke,boolean,boolean,boolean,sun.awt.geom.PathConsumer2D)".equals(methods(c)), "methods");
        check("private static#sun.java2d.pipe.RenderingEngine#reImpl".equals(fields(c)), "fields");
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
