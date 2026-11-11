/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.xr.XRUtils conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.xr:+open
 * @run main XRUtilsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.XRUtils", false, XRUtilsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.XRUtils".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#[B#ATransOpToXRQualityName(int);public static#[B#getFilterName(int);public static#[I#convertFloatsToFixed([F);public static#boolean#isDoubleInShortRange(double);public static#boolean#isMaskEvaluated(byte);public static#boolean#isPointCoordInShortRange(java.awt.geom.Point2D);public static#boolean#isTransformQuadrantRotated(java.awt.geom.AffineTransform);public static#byte#j2dAlphaCompToXR(int);public static#double#XFixedToDouble(int);public static#int#ATransOpToXRQuality(int);public static#int#XDoubleToFixed(double);public static#int#clampToUShort(int);public static#int#getPictureFormatForTransparency(int);public static#int#getRepeatForCycleMethod(java.awt.MultipleGradientPaint$CycleMethod);public static#long#intToULong(int);public static#short#clampToShort(int);public static#sun.java2d.loops.SurfaceType#getXRSurfaceTypeForTransparency(int)".equals(methods(c)), "methods");
        check("public static final#[B#BEST_NAME;public static final#[B#FAST_NAME;public static final#[B#GOOD_NAME;public static final#byte#PictOpAdd;public static final#byte#PictOpAtop;public static final#byte#PictOpAtopReverse;public static final#byte#PictOpClear;public static final#byte#PictOpDst;public static final#byte#PictOpIn;public static final#byte#PictOpInReverse;public static final#byte#PictOpOut;public static final#byte#PictOpOutReverse;public static final#byte#PictOpOver;public static final#byte#PictOpOverReverse;public static final#byte#PictOpSaturate;public static final#byte#PictOpSrc;public static final#byte#PictOpXor;public static final#int#BEST;public static final#int#FAST;public static final#int#GOOD;public static final#int#None;public static final#int#PictStandardA1;public static final#int#PictStandardA4;public static final#int#PictStandardA8;public static final#int#PictStandardARGB32;public static final#int#PictStandardRGB24;public static final#int#RepeatNone;public static final#int#RepeatNormal;public static final#int#RepeatPad;public static final#int#RepeatReflect".equals(fields(c)), "fields");
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
