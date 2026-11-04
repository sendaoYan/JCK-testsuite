/*
 * @test
 * @summary sun.java2d.pipe.BufferedContext conformance: reflection (no init, native/display-heavy).
 * @run main BufferedContextTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedContext", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedContext".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#invalidateSurfaces();private#void#resetClip();private#void#resetComposite();private#void#resetTransform();private#void#setClip(sun.java2d.pipe.Region);private#void#setComposite(java.awt.Composite,int);private#void#setSurfaces(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface);private#void#setTransform(java.awt.geom.AffineTransform);public abstract#sun.java2d.pipe.RenderQueue#getRenderQueue();public abstract#void#restoreState();public abstract#void#saveState();public static#void#validateContext(sun.java2d.pipe.hw.AccelSurface);public static#void#validateContext(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.Region,java.awt.Composite,java.awt.geom.AffineTransform,java.awt.Paint,sun.java2d.SunGraphics2D,int);public#void#invalidateContext();public#void#validate(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.Region,java.awt.Composite,java.awt.geom.AffineTransform,java.awt.Paint,sun.java2d.SunGraphics2D,int)".equals(methods(c)), "methods");
        check("private#boolean#isValidatedPaintJustAColor;private#boolean#xformInUse;private#int#validatedFlags;private#int#validatedRGB;private#java.awt.geom.AffineTransform#transform;private#java.lang.ref.Reference#validClipRef;private#java.lang.ref.Reference#validCompRef;private#java.lang.ref.Reference#validDstDataRef;private#java.lang.ref.Reference#validPaintRef;private#java.lang.ref.Reference#validSrcDataRef;protected static#sun.java2d.pipe.BufferedContext#currentContext;protected#sun.java2d.pipe.RenderBuffer#buf;protected#sun.java2d.pipe.RenderQueue#rq;public static final#int#NO_CONTEXT_FLAGS;public static final#int#SRC_IS_OPAQUE;public static final#int#USE_MASK".equals(fields(c)), "fields");
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
