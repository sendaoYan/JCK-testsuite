/*
 * @test
 * @summary sun.java2d.marlin.MarlinRenderingEngine conformance: reflection (no init, native/display-heavy).
 * @run main MarlinRenderingEngineTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MarlinRenderingEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.MarlinRenderingEngine", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.MarlinRenderingEngine".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.pipe.RenderingEngine".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.marlin.MarlinConst".equals(interfaces(c)), "interfaces");
        check("#java.awt.geom.PathIterator#getNormalizingPathIterator(sun.java2d.marlin.RendererContext,sun.java2d.marlin.MarlinRenderingEngine$NormMode,java.awt.geom.PathIterator);final#void#strokeTo(sun.java2d.marlin.RendererContext,java.awt.Shape,java.awt.geom.AffineTransform,float,sun.java2d.marlin.MarlinRenderingEngine$NormMode,int,int,float,[F,float,sun.awt.geom.PathConsumer2D);final#void#strokeTo(sun.java2d.marlin.RendererContext,java.awt.Shape,java.awt.geom.AffineTransform,java.awt.BasicStroke,boolean,sun.java2d.marlin.MarlinRenderingEngine$NormMode,boolean,sun.awt.geom.PathConsumer2D);private final#float#userSpaceLineWidth(java.awt.geom.AffineTransform,float);private static#boolean#nearZero(double);private static#void#logSettings(java.lang.String);private static#void#pathTo(sun.java2d.marlin.RendererContext,java.awt.geom.PathIterator,sun.awt.geom.PathConsumer2D);private static#void#pathToLoop([F,java.awt.geom.PathIterator,sun.awt.geom.PathConsumer2D);public final#sun.java2d.pipe.AATileGenerator#getAATileGenerator(double,double,double,double,double,double,double,double,sun.java2d.pipe.Region,[I);public#float#getMinimumAAPenSize();public#java.awt.Shape#createStrokedShape(java.awt.Shape,float,int,int,float,[F,float);public#sun.java2d.pipe.AATileGenerator#getAATileGenerator(java.awt.Shape,java.awt.geom.AffineTransform,sun.java2d.pipe.Region,java.awt.BasicStroke,boolean,boolean,[I);public#void#strokeTo(java.awt.Shape,java.awt.geom.AffineTransform,java.awt.BasicStroke,boolean,boolean,boolean,sun.awt.geom.PathConsumer2D);static#sun.java2d.marlin.RendererContext#getRendererContext();static#void#returnRendererContext(sun.java2d.marlin.RendererContext)".equals(methods(c)), "methods");
        check("private static final#boolean#useThreadLocal;private static final#float#MIN_PEN_SIZE;private static final#sun.java2d.ReentrantContextProvider#rdrCtxProvider;private static#boolean#settingsLogged;static final#int#REF_TYPE".equals(fields(c)), "fields");
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
