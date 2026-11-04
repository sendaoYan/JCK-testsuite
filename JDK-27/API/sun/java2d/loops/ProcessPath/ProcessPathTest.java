/*
 * @test
 * @summary sun.java2d.loops.ProcessPath conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.loops:+open
 * @run main ProcessPathTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ProcessPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.ProcessPath", false, ProcessPathTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.ProcessPath".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#boolean#IS_CLIPPED(int);private static#boolean#doProcessPath(sun.java2d.loops.ProcessPath$ProcessHandler,java.awt.geom.Path2D$Float,float,float);private static#float#CLIP(float,float,float,float,double);private static#int#CLIP(int,int,int,int,double);private static#int#CLIPCLAMP(float,float,[F,int,int,int,int,int,int);private static#int#CLIPCLAMP(int,int,[I,int,int,int,int,int,int);private static#int#TESTANDCLIP(float,float,[F,int,int,int,int);private static#int#TESTANDCLIP(int,int,[I,int,int,int,int);private static#void#DrawMonotonicCubic(sun.java2d.loops.ProcessPath$ProcessHandler,[F,boolean,[I);private static#void#DrawMonotonicQuad(sun.java2d.loops.ProcessPath$ProcessHandler,[F,boolean,[I);private static#void#FillPolygon(sun.java2d.loops.ProcessPath$FillProcessHandler,int);private static#void#ProcessCubic(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I);private static#void#ProcessFirstMonotonicPartOfCubic(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I,float);private static#void#ProcessFirstMonotonicPartOfQuad(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I,float);private static#void#ProcessLine(sun.java2d.loops.ProcessPath$ProcessHandler,float,float,float,float,[I);private static#void#ProcessMonotonicCubic(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I);private static#void#ProcessMonotonicQuad(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I);private static#void#ProcessQuad(sun.java2d.loops.ProcessPath$ProcessHandler,[F,[I);public static#boolean#drawPath(sun.java2d.loops.ProcessPath$DrawHandler,java.awt.geom.Path2D$Float,int,int);public static#boolean#drawPath(sun.java2d.loops.ProcessPath$DrawHandler,sun.java2d.loops.ProcessPath$EndSubPathHandler,java.awt.geom.Path2D$Float,int,int);public static#boolean#fillPath(sun.java2d.loops.ProcessPath$DrawHandler,java.awt.geom.Path2D$Float,int,int)".equals(methods(c)), "methods");
        check("private static final#float#CALC_LBND;private static final#float#CALC_UBND;private static final#float#LOWER_BND;private static final#float#UPPER_BND;private static final#int#CRES_INVISIBLE;private static final#int#CRES_MAX_CLIPPED;private static final#int#CRES_MIN_CLIPPED;private static final#int#CRES_NOT_CLIPPED;private static final#int#CUB_A_MDP_MULT;private static final#int#CUB_A_SHIFT;private static final#int#CUB_B_MDP_MULT;private static final#int#CUB_B_SHIFT;private static final#int#CUB_C_MDP_MULT;private static final#int#CUB_C_SHIFT;private static final#int#DF_CUB_COUNT;private static final#int#DF_CUB_DEC_BND;private static final#int#DF_CUB_INC_BND;private static final#int#DF_CUB_SHIFT;private static final#int#DF_CUB_STEPS;private static final#int#DF_MAX_POINT;private static final#int#DF_QUAD_COUNT;private static final#int#DF_QUAD_DEC_BND;private static final#int#DF_QUAD_INC_BND;private static final#int#DF_QUAD_SHIFT;private static final#int#DF_QUAD_STEPS;private static final#int#FWD_PREC;private static final#int#LOWER_OUT_BND;private static final#int#MAX_CUB_SIZE;private static final#int#MAX_QUAD_SIZE;private static final#int#MDP_F_MASK;private static final#int#MDP_HALF_MULT;private static final#int#MDP_MULT;private static final#int#MDP_PREC;private static final#int#MDP_W_MASK;private static final#int#QUAD_A_MDP_MULT;private static final#int#QUAD_A_SHIFT;private static final#int#QUAD_B_MDP_MULT;private static final#int#QUAD_B_SHIFT;private static final#int#UPPER_OUT_BND;public static final#float#EPSF;public static final#int#EPSFX;public static final#int#PH_MODE_DRAW_CLIP;public static final#int#PH_MODE_FILL_CLIP;public static#sun.java2d.loops.ProcessPath$EndSubPathHandler#noopEndSubPathHandler".equals(fields(c)), "fields");
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
