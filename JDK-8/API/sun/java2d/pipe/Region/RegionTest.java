/*
 * @test
 * @summary sun.java2d.pipe.Region conformance: reflection (no init, native/display-heavy).
 * @run main RegionTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RegionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.Region", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.Region".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();private#sun.java2d.pipe.Region#getSafeTranslatedRegion(int,int);private#void#appendSpan([I);private#void#calcBBox();private#void#endRow([I);private#void#filterSpans(sun.java2d.pipe.Region,sun.java2d.pipe.Region,int);private#void#needSpace(int);public final#int#getHeight();public final#int#getHiX();public final#int#getHiY();public final#int#getLoX();public final#int#getLoY();public final#int#getWidth();public static#int#clipAdd(int,int);public static#int#clipScale(int,double);public static#int#dimAdd(int,int);public static#sun.java2d.pipe.Region#getInstance([I);public static#sun.java2d.pipe.Region#getInstance(java.awt.Rectangle);public static#sun.java2d.pipe.Region#getInstance(java.awt.Shape,java.awt.geom.AffineTransform);public static#sun.java2d.pipe.Region#getInstance(sun.java2d.pipe.Region,boolean,java.awt.Shape,java.awt.geom.AffineTransform);public static#sun.java2d.pipe.Region#getInstance(sun.java2d.pipe.Region,java.awt.Shape,java.awt.geom.AffineTransform);public static#sun.java2d.pipe.Region#getInstanceXYWH(int,int,int,int);public static#sun.java2d.pipe.Region#getInstanceXYXY(int,int,int,int);public#boolean#contains(int,int);public#boolean#encompasses(sun.java2d.pipe.Region);public#boolean#encompassesXYWH(int,int,int,int);public#boolean#encompassesXYXY(int,int,int,int);public#boolean#equals(java.lang.Object);public#boolean#intersectsQuickCheck(sun.java2d.pipe.Region);public#boolean#intersectsQuickCheckXYXY(int,int,int,int);public#boolean#isEmpty();public#boolean#isInsideQuickCheck(sun.java2d.pipe.Region);public#boolean#isInsideXYWH(int,int,int,int);public#boolean#isInsideXYXY(int,int,int,int);public#boolean#isRectangular();public#int#hashCode();public#java.lang.String#toString();public#sun.java2d.pipe.Region#getBoundsIntersection(java.awt.Rectangle);public#sun.java2d.pipe.Region#getBoundsIntersection(sun.java2d.pipe.Region);public#sun.java2d.pipe.Region#getBoundsIntersectionXYWH(int,int,int,int);public#sun.java2d.pipe.Region#getBoundsIntersectionXYXY(int,int,int,int);public#sun.java2d.pipe.Region#getDifference(sun.java2d.pipe.Region);public#sun.java2d.pipe.Region#getExclusiveOr(sun.java2d.pipe.Region);public#sun.java2d.pipe.Region#getIntersection(java.awt.Rectangle);public#sun.java2d.pipe.Region#getIntersection(sun.java2d.pipe.Region);public#sun.java2d.pipe.Region#getIntersectionXYWH(int,int,int,int);public#sun.java2d.pipe.Region#getIntersectionXYXY(int,int,int,int);public#sun.java2d.pipe.Region#getScaledRegion(double,double);public#sun.java2d.pipe.Region#getTranslatedRegion(int,int);public#sun.java2d.pipe.Region#getUnion(sun.java2d.pipe.Region);public#sun.java2d.pipe.RegionIterator#getIterator();public#sun.java2d.pipe.SpanIterator#filter(sun.java2d.pipe.SpanIterator);public#sun.java2d.pipe.SpanIterator#getSpanIterator();public#sun.java2d.pipe.SpanIterator#getSpanIterator([I);public#void#appendSpans(sun.java2d.pipe.SpanIterator);public#void#clipBoxToBounds([I);public#void#getBounds([I);public#void#setOutputArea([I);public#void#setOutputArea(java.awt.Rectangle);public#void#setOutputAreaXYWH(int,int,int,int);public#void#setOutputAreaXYXY(int,int,int,int);static#sun.java2d.pipe.Region#getInstance(int,int,int,int,[I)".equals(methods(c)), "methods");
        check("#[I#bands;#int#endIndex;#int#hix;#int#hiy;#int#lox;#int#loy;public static final#sun.java2d.pipe.Region#EMPTY_REGION;public static final#sun.java2d.pipe.Region#WHOLE_REGION;static final#int#GROW_SIZE;static final#int#INCLUDE_A;static final#int#INCLUDE_B;static final#int#INCLUDE_COMMON;static final#int#INIT_SIZE".equals(fields(c)), "fields");
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
