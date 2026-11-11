/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.x11.X11Renderer.X11TracingRenderer conformance: reflection (no init, native/display-heavy).
 * @run main X11TracingRendererTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11TracingRendererTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.x11.X11Renderer$X11TracingRenderer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.x11.X11Renderer$X11TracingRenderer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.x11.X11Renderer".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#XDoPath(sun.java2d.SunGraphics2D,long,long,int,int,java.awt.geom.Path2D$Float,boolean);#void#XDrawArc(long,long,int,int,int,int,int,int);#void#XDrawLine(long,long,int,int,int,int);#void#XDrawOval(long,long,int,int,int,int);#void#XDrawPoly(long,long,int,int,[I,[I,int,boolean);#void#XDrawRect(long,long,int,int,int,int);#void#XDrawRoundRect(long,long,int,int,int,int,int,int);#void#XFillArc(long,long,int,int,int,int,int,int);#void#XFillOval(long,long,int,int,int,int);#void#XFillPoly(long,long,int,int,[I,[I,int);#void#XFillRect(long,long,int,int,int,int);#void#XFillRoundRect(long,long,int,int,int,int,int,int);#void#XFillSpans(long,long,sun.java2d.pipe.SpanIterator,long,int,int);#void#devCopyArea(long,long,int,int,int,int,int,int)".equals(methods(c)), "methods");
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
