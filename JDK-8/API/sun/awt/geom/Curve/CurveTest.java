/*
 * @test
 * @summary sun.awt.geom.Curve conformance: reflection (no init, native/display-heavy).
 * @run main CurveTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CurveTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.geom.Curve", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.geom.Curve".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#double#TforY(double);public abstract#double#XforT(double);public abstract#double#XforY(double);public abstract#double#YforT(double);public abstract#double#dXforT(double,int);public abstract#double#dYforT(double,int);public abstract#double#getX0();public abstract#double#getX1();public abstract#double#getXBot();public abstract#double#getXMax();public abstract#double#getXMin();public abstract#double#getXTop();public abstract#double#getY0();public abstract#double#getY1();public abstract#double#getYBot();public abstract#double#getYTop();public abstract#double#nextVertical(double,double);public abstract#int#getOrder();public abstract#int#getSegment([D);public abstract#sun.awt.geom.Curve#getReversedCurve();public abstract#sun.awt.geom.Curve#getSubCurve(double,double,int);public abstract#void#enlarge(java.awt.geom.Rectangle2D);public final#int#getDirection();public final#sun.awt.geom.Curve#getWithDirection(int);public static#double#next(double);public static#double#prev(double);public static#double#round(double);public static#int#orderof(double,double);public static#int#pointCrossingsForCubic(double,double,double,double,double,double,double,double,double,double,int);public static#int#pointCrossingsForLine(double,double,double,double,double,double);public static#int#pointCrossingsForPath(java.awt.geom.PathIterator,double,double);public static#int#pointCrossingsForQuad(double,double,double,double,double,double,double,double,int);public static#int#rectCrossingsForCubic(int,double,double,double,double,double,double,double,double,double,double,double,double,int);public static#int#rectCrossingsForLine(int,double,double,double,double,double,double,double,double);public static#int#rectCrossingsForPath(java.awt.geom.PathIterator,double,double,double,double);public static#int#rectCrossingsForQuad(int,double,double,double,double,double,double,double,double,double,double,int);public static#long#diffbits(double,double);public static#long#signeddiffbits(double,double);public static#void#insertCubic(java.util.Vector,double,double,[D);public static#void#insertLine(java.util.Vector,double,double,double,double);public static#void#insertMove(java.util.Vector,double,double);public static#void#insertQuad(java.util.Vector,double,double,[D);public#boolean#accumulateCrossings(sun.awt.geom.Crossings);public#boolean#fairlyClose(double,double);public#boolean#findIntersect(sun.awt.geom.Curve,[D,double,int,int,double,double,double,double,double,double,double,double,double,double,double,double);public#double#refineTforY(double,double,double);public#int#compareTo(sun.awt.geom.Curve,[D);public#int#crossingsFor(double,double);public#java.lang.String#controlPointString();public#java.lang.String#toString();public#sun.awt.geom.Curve#getSubCurve(double,double)".equals(methods(c)), "methods");
        check("protected#int#direction;public static final#double#TMIN;public static final#int#DECREASING;public static final#int#INCREASING;public static final#int#RECT_INTERSECTS".equals(fields(c)), "fields");
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
