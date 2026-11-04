/*
 * @test
 * @summary sun.java2d.loops.GraphicsPrimitive conformance: reflection (no init, native/display-heavy).
 * @run main GraphicsPrimitiveTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GraphicsPrimitiveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.GraphicsPrimitive", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.GraphicsPrimitive".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("final#boolean#satisfiesSameAs(sun.java2d.loops.GraphicsPrimitive);private static#java.io.PrintStream#getTraceOutputFile();protected static#sun.java2d.SurfaceData#convertFrom(sun.java2d.loops.Blit,sun.java2d.SurfaceData,int,int,int,int,sun.java2d.SurfaceData);protected static#sun.java2d.SurfaceData#convertFrom(sun.java2d.loops.Blit,sun.java2d.SurfaceData,int,int,int,int,sun.java2d.SurfaceData,int);protected static#sun.java2d.loops.Blit#createConverter(sun.java2d.loops.SurfaceType,sun.java2d.loops.SurfaceType);protected static#sun.java2d.loops.GraphicsPrimitive#getGeneralOp(int,sun.java2d.loops.CompositeType);protected static#void#convertTo(sun.java2d.loops.Blit,sun.java2d.SurfaceData,sun.java2d.SurfaceData,sun.java2d.pipe.Region,int,int,int,int);protected#void#setupGeneralBinaryOp(sun.java2d.loops.GraphicsPrimitive$GeneralBinaryOp);protected#void#setupGeneralUnaryOp(sun.java2d.loops.GraphicsPrimitive$GeneralUnaryOp);public abstract#sun.java2d.loops.GraphicsPrimitive#makePrimitive(sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public abstract#sun.java2d.loops.GraphicsPrimitive#traceWrap();public final#boolean#satisfies(java.lang.String,sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public final#int#getPrimTypeID();public final#int#getUniqueID();public final#java.lang.String#getSignature();public final#long#getNativePrim();public final#sun.java2d.loops.CompositeType#getCompositeType();public final#sun.java2d.loops.SurfaceType#getDestType();public final#sun.java2d.loops.SurfaceType#getSourceType();public static final synchronized#int#makePrimTypeID();public static final synchronized#int#makeUniqueID(int,sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public static synchronized#void#tracePrimitive(java.lang.Object);public static#boolean#tracingEnabled();public static#java.lang.String#simplename([Ljava.lang.reflect.Field;,java.lang.Object);public static#java.lang.String#simplename(sun.java2d.loops.CompositeType);public static#java.lang.String#simplename(sun.java2d.loops.SurfaceType);public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("private static#int#unusedPrimID;private#int#uniqueID;private#java.lang.String#cachedname;private#java.lang.String#methodSignature;private#long#pNativePrim;private#sun.java2d.loops.CompositeType#compositeType;private#sun.java2d.loops.SurfaceType#destType;private#sun.java2d.loops.SurfaceType#sourceType;public static final#int#TRACECOUNTS;public static final#int#TRACELOG;public static final#int#TRACETIMESTAMP;public static#int#traceflags;public static#java.io.PrintStream#traceout;public static#java.lang.String#tracefile;static#java.util.HashMap#traceMap".equals(fields(c)), "fields");
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
