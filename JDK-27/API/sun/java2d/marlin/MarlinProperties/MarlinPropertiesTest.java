/*
 * @test
 * @summary sun.java2d.marlin.MarlinProperties conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.marlin:+open
 * @run main MarlinPropertiesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MarlinPropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.MarlinProperties", false, MarlinPropertiesTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.MarlinProperties".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#boolean#isDoChecks();public static#boolean#isDoClip();public static#boolean#isDoClipAtRuntime();public static#boolean#isDoClipRuntimeFlag();public static#boolean#isDoClipSubdivider();public static#boolean#isDoMonitors();public static#boolean#isDoStats();public static#boolean#isForceNoRLE();public static#boolean#isForceRLE();public static#boolean#isLogCreateContext();public static#boolean#isLogUnsafeMalloc();public static#boolean#isLoggingEnabled();public static#boolean#isProfileQuality();public static#boolean#isSkipRenderTiles();public static#boolean#isSkipRenderer();public static#boolean#isUseDPQS();public static#boolean#isUseLogger();public static#boolean#isUsePathSimplifier();public static#boolean#isUseSimplifier();public static#boolean#isUseThreadLocal();public static#boolean#isUseTileFlags();public static#boolean#isUseTileFlagsWithHeuristics();public static#double#getDouble(java.lang.String,double,double,double);public static#float#getCubicDecD2();public static#float#getCubicIncD1();public static#float#getCurveLengthError();public static#float#getFloat(java.lang.String,float,float,float);public static#float#getPathSimplifierPixelTolerance();public static#float#getQuadDecD2();public static#float#getStrokerJoinError();public static#float#getSubdividerMinLength();public static#int#getBlockSize_Log2();public static#int#getInitialEdges();public static#int#getInitialPixelHeight();public static#int#getInitialPixelWidth();public static#int#getRLEMinWidth();public static#int#getStrokerJoinStyle();public static#int#getSubPixel_Log2_X();public static#int#getSubPixel_Log2_Y();public static#int#getTileSize_Log2();public static#int#getTileWidth_Log2();static#boolean#getBoolean(java.lang.String,java.lang.String);static#int#align(int,int);static#int#getInteger(java.lang.String,int,int,int);static#java.lang.String#getString(java.lang.String,java.lang.String)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
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
