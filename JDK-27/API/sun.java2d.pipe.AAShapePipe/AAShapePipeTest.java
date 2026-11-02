/*
 * @test
 * @summary sun.java2d.pipe.AAShapePipe conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main AAShapePipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AAShapePipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.AAShapePipe", false, AAShapePipeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.AAShapePipe".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.ParallelogramPipe;sun.java2d.pipe.ShapeDrawPipe".equals(interfaces(c)), "interfaces");
        check("public#void#draw(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#drawParallelogram(sun.java2d.SunGraphics2D,double,double,double,double,double,double,double,double,double,double,double,double);public#void#fill(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#fillParallelogram(sun.java2d.SunGraphics2D,double,double,double,double,double,double,double,double,double,double);public#void#renderPath(sun.java2d.SunGraphics2D,java.awt.Shape,java.awt.BasicStroke);public#void#renderTiles(sun.java2d.SunGraphics2D,java.awt.Shape,sun.java2d.pipe.AATileGenerator,[I,sun.java2d.pipe.AAShapePipe$TileState)".equals(methods(c)), "methods");
        check("final#sun.java2d.pipe.CompositePipe#outpipe;private static final#boolean#DO_RENDER;private static final#sun.java2d.ReentrantContextProvider#TILE_STATE_PROVIDER;static final#sun.java2d.pipe.RenderingEngine#RDR_ENGINE".equals(fields(c)), "fields");
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
