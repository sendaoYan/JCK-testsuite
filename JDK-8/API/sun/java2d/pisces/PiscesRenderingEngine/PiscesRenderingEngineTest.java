/*
 * @test
 * @summary sun.java2d.pisces.PiscesRenderingEngine conformance: reflection (no init, native/display-heavy).
 * @run main PiscesRenderingEngineTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PiscesRenderingEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pisces.PiscesRenderingEngine", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pisces.PiscesRenderingEngine".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.pipe.RenderingEngine".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,float,sun.java2d.pisces.PiscesRenderingEngine$NormMode,int,int,float,[F,float,sun.awt.geom.PathConsumer2D);#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,java.awt.BasicStroke,boolean,sun.java2d.pisces.PiscesRenderingEngine$NormMode,boolean,sun.awt.geom.PathConsumer2D);private static#boolean#nearZero(double,int);private#float#userSpaceLineWidth(java.awt.geom.AffineTransform,float);public#float#getMinimumAAPenSize();public#java.awt.Shape#createStrokedShape(java.awt.Shape,float,int,int,float,[F,float);public#sun.java2d.pipe.AATileGenerator#getAATileGenerator(double,double,double,double,double,double,double,double,sun.java2d.pipe.Region,[I);public#sun.java2d.pipe.AATileGenerator#getAATileGenerator(java.awt.Shape,java.awt.geom.AffineTransform,sun.java2d.pipe.Region,java.awt.BasicStroke,boolean,boolean,[I);public#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,java.awt.BasicStroke,boolean,boolean,boolean,sun.awt.geom.PathConsumer2D);static#void#pathTo(java.awt.geom.PathIterator,sun.awt.geom.PathConsumer2D)".equals(methods(c)), "methods");
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
