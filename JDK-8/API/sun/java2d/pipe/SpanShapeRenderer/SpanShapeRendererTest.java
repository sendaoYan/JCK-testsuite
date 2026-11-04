/*
 * @test
 * @summary sun.java2d.pipe.SpanShapeRenderer conformance: reflection (no init, native/display-heavy).
 * @run main SpanShapeRendererTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SpanShapeRendererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.SpanShapeRenderer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.SpanShapeRenderer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.ShapeDrawPipe".equals(interfaces(c)), "interfaces");
        check("public abstract#java.lang.Object#startSequence(sun.java2d.SunGraphics2D,java.awt.Shape,java.awt.Rectangle,[I);public abstract#void#endSequence(java.lang.Object);public abstract#void#renderBox(java.lang.Object,int,int,int,int);public#void#draw(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#fill(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#renderRect(sun.java2d.SunGraphics2D,java.awt.geom.Rectangle2D);public#void#renderSpans(sun.java2d.SunGraphics2D,sun.java2d.pipe.Region,java.awt.Shape,sun.java2d.pipe.ShapeSpanIterator);public#void#spanClipLoop(java.lang.Object,sun.java2d.pipe.SpanIterator,sun.java2d.pipe.Region,[I)".equals(methods(c)), "methods");
        check("public static final#int#NON_RECTILINEAR_TRANSFORM_MASK;static final#sun.java2d.pipe.RenderingEngine#RenderEngine".equals(fields(c)), "fields");
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
