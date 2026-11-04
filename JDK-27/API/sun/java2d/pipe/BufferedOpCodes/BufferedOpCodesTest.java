/*
 * @test
 * @summary sun.java2d.pipe.BufferedOpCodes conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main BufferedOpCodesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedOpCodesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedOpCodes", false, BufferedOpCodesTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedOpCodes".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("public static final#int#BEGIN_SHAPE_CLIP;public static final#int#BLIT;public static final#int#COPY_AREA;public static final#int#DISABLE_CONVOLVE_OP;public static final#int#DISABLE_LOOKUP_OP;public static final#int#DISABLE_RESCALE_OP;public static final#int#DISPOSE_CONFIG;public static final#int#DISPOSE_SURFACE;public static final#int#DRAW_AAPARALLELOGRAM;public static final#int#DRAW_GLYPH_LIST;public static final#int#DRAW_LINE;public static final#int#DRAW_PARALLELOGRAM;public static final#int#DRAW_PIXEL;public static final#int#DRAW_POLY;public static final#int#DRAW_RECT;public static final#int#DRAW_SCANLINES;public static final#int#ENABLE_CONVOLVE_OP;public static final#int#ENABLE_LOOKUP_OP;public static final#int#ENABLE_RESCALE_OP;public static final#int#END_SHAPE_CLIP;public static final#int#FILL_AAPARALLELOGRAM;public static final#int#FILL_PARALLELOGRAM;public static final#int#FILL_RECT;public static final#int#FILL_SPANS;public static final#int#FLUSH_SURFACE;public static final#int#INVALIDATE_CONTEXT;public static final#int#MASK_BLIT;public static final#int#MASK_FILL;public static final#int#NOOP;public static final#int#RESET_CLIP;public static final#int#RESET_COMPOSITE;public static final#int#RESET_PAINT;public static final#int#RESET_TRANSFORM;public static final#int#RESTORE_DEVICES;public static final#int#SET_ALPHA_COMPOSITE;public static final#int#SET_COLOR;public static final#int#SET_GRADIENT_PAINT;public static final#int#SET_LINEAR_GRADIENT_PAINT;public static final#int#SET_RADIAL_GRADIENT_PAINT;public static final#int#SET_RECT_CLIP;public static final#int#SET_SCRATCH_SURFACE;public static final#int#SET_SHAPE_CLIP_SPANS;public static final#int#SET_SURFACES;public static final#int#SET_TEXTURE_PAINT;public static final#int#SET_TRANSFORM;public static final#int#SET_XOR_COMPOSITE;public static final#int#SURFACE_TO_SW_BLIT;public static final#int#SWAP_BUFFERS;public static final#int#SYNC".equals(fields(c)), "fields");
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
