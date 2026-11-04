/*
 * @test
 * @summary sun.java2d.loops.BlitBg conformance: reflection (no init, native/display-heavy).
 * @run main BlitBgTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BlitBgTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.BlitBg", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.BlitBg".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.loops.GraphicsPrimitive".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public native#void#BlitBg(sun.java2d.SurfaceData,sun.java2d.SurfaceData,java.awt.Composite,sun.java2d.pipe.Region,int,int,int,int,int,int,int);public static#sun.java2d.loops.BlitBg#getFromCache(sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public static#sun.java2d.loops.BlitBg#locate(sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public#sun.java2d.loops.GraphicsPrimitive#makePrimitive(sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public#sun.java2d.loops.GraphicsPrimitive#traceWrap()".equals(methods(c)), "methods");
        check("private static#sun.java2d.loops.RenderCache#blitcache;public static final#int#primTypeID;public static final#java.lang.String#methodSignature".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
