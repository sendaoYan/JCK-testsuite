/*
 * @test
 * @summary sun.java2d.pipe.BufferedTextPipe conformance: reflection (no init, native/display-heavy).
 * @run main BufferedTextPipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedTextPipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedTextPipe", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedTextPipe".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.pipe.GlyphListPipe".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#int#createPackedParams(sun.java2d.SunGraphics2D,sun.font.GlyphList);private#void#enqueueGlyphList(sun.java2d.SunGraphics2D,sun.font.GlyphList);protected abstract#void#drawGlyphList(int,boolean,boolean,boolean,int,float,float,[J,[F);protected abstract#void#validateContext(sun.java2d.SunGraphics2D,java.awt.Composite);protected#void#drawGlyphList(sun.java2d.SunGraphics2D,sun.font.GlyphList)".equals(methods(c)), "methods");
        check("private static final#int#BYTES_PER_GLYPH_IMAGE;private static final#int#BYTES_PER_GLYPH_POSITION;private static final#int#OFFSET_CONTRAST;private static final#int#OFFSET_POSITIONS;private static final#int#OFFSET_RGBORDER;private static final#int#OFFSET_SUBPIXPOS;protected final#sun.java2d.pipe.RenderQueue#rq".equals(fields(c)), "fields");
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
