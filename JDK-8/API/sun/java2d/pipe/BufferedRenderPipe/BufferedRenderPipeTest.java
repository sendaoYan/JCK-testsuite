/*
 * @test
 * @summary sun.java2d.pipe.BufferedRenderPipe conformance: reflection (no init, native/display-heavy).
 * @run main BufferedRenderPipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedRenderPipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedRenderPipe", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedRenderPipe".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.ParallelogramPipe;sun.java2d.pipe.PixelDrawPipe;sun.java2d.pipe.PixelFillPipe;sun.java2d.pipe.ShapeDrawPipe".equals(interfaces(c)), "interfaces");
        check("private native#int#fillSpans(sun.java2d.pipe.RenderQueue,long,int,int,sun.java2d.pipe.SpanIterator,long,int,int);protected abstract#void#drawPoly([I,[I,int,boolean,int,int);protected abstract#void#validateContext(sun.java2d.SunGraphics2D);protected abstract#void#validateContextAA(sun.java2d.SunGraphics2D);protected#void#drawPath(sun.java2d.SunGraphics2D,java.awt.geom.Path2D$Float,int,int);protected#void#drawPoly(sun.java2d.SunGraphics2D,[I,[I,int,boolean);protected#void#fillPath(sun.java2d.SunGraphics2D,java.awt.geom.Path2D$Float,int,int);protected#void#fillSpans(sun.java2d.SunGraphics2D,sun.java2d.pipe.SpanIterator,int,int);public#sun.java2d.pipe.ParallelogramPipe#getAAParallelogramPipe();public#void#draw(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#drawArc(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#void#drawLine(sun.java2d.SunGraphics2D,int,int,int,int);public#void#drawOval(sun.java2d.SunGraphics2D,int,int,int,int);public#void#drawParallelogram(sun.java2d.SunGraphics2D,double,double,double,double,double,double,double,double,double,double,double,double);public#void#drawPolygon(sun.java2d.SunGraphics2D,[I,[I,int);public#void#drawPolyline(sun.java2d.SunGraphics2D,[I,[I,int);public#void#drawRect(sun.java2d.SunGraphics2D,int,int,int,int);public#void#drawRoundRect(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#void#fill(sun.java2d.SunGraphics2D,java.awt.Shape);public#void#fillArc(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#void#fillOval(sun.java2d.SunGraphics2D,int,int,int,int);public#void#fillParallelogram(sun.java2d.SunGraphics2D,double,double,double,double,double,double,double,double,double,double);public#void#fillPolygon(sun.java2d.SunGraphics2D,[I,[I,int);public#void#fillRect(sun.java2d.SunGraphics2D,int,int,int,int);public#void#fillRoundRect(sun.java2d.SunGraphics2D,int,int,int,int,int,int)".equals(methods(c)), "methods");
        check("#sun.java2d.pipe.ParallelogramPipe#aapgrampipe;private#sun.java2d.pipe.BufferedRenderPipe$BufferedDrawHandler#drawHandler;protected#sun.java2d.pipe.RenderBuffer#buf;protected#sun.java2d.pipe.RenderQueue#rq;static final#int#BYTES_PER_POLY_POINT;static final#int#BYTES_PER_SCANLINE;static final#int#BYTES_PER_SPAN".equals(fields(c)), "fields");
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
