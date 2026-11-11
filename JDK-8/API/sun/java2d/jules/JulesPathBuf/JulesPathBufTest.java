/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.jules.JulesPathBuf conformance: reflection (no init, native/display-heavy).
 * @run main JulesPathBufTest
 */

import java.util.*;
import java.lang.reflect.*;

public class JulesPathBufTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.jules.JulesPathBuf", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.jules.JulesPathBuf".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#[I#tesselateFillNative([I,[B,int,int,[I,int,int,int,int,int,int);private static native#[I#tesselateStrokeNative([I,[B,int,int,[I,int,double,int,int,double,[D,int,double,double,double,double,double,double,double,int,int,int,int);private static#int#DoubleToCairoFixed(double);private static#int#getCairoWindingRule(int);protected#[D#floatToDoubleArray([F);protected#int#convertPathData(java.awt.Shape,java.awt.geom.AffineTransform);public static#boolean#isCairoAvailable();public#sun.java2d.jules.TrapezoidList#tesselateFill(java.awt.Shape,java.awt.geom.AffineTransform,sun.java2d.pipe.Region);public#sun.java2d.jules.TrapezoidList#tesselateStroke(java.awt.Shape,java.awt.BasicStroke,boolean,boolean,boolean,java.awt.geom.AffineTransform,sun.java2d.pipe.Region);public#void#clear()".equals(methods(c)), "methods");
        check("#[I#xTrapArray;#sun.java2d.xr.GrowableByteArray#ops;#sun.java2d.xr.GrowablePointArray#points;private static final#boolean#isCairoAvailable;private static final#byte#CAIRO_PATH_OP_CLOSE_PATH;private static final#byte#CAIRO_PATH_OP_CURVE_TO;private static final#byte#CAIRO_PATH_OP_LINE_TO;private static final#byte#CAIRO_PATH_OP_MOVE_TO;private static final#int#CAIRO_FILL_RULE_EVEN_ODD;private static final#int#CAIRO_FILL_RULE_WINDING;static final#[D#emptyDash".equals(fields(c)), "fields");
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
