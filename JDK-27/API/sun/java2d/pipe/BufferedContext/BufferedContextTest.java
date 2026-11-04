/*
 * @test
 * @summary sun.java2d.pipe.BufferedContext conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main BufferedContextTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedContext", false, BufferedContextTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedContext".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#boolean#stateChanged(java.lang.ref.Reference,java.lang.Object);private static#java.lang.ref.Reference#wrapState(java.lang.Object);private#void#resetClip();private#void#resetComposite();private#void#resetTransform();private#void#setClip(sun.java2d.pipe.Region);private#void#setComposite(java.awt.Composite,int);private#void#setSurfaces(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface);private#void#setTransform(java.awt.geom.AffineTransform);private#void#validate(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.Region,java.awt.Composite,java.awt.geom.AffineTransform,java.awt.Paint,sun.java2d.SunGraphics2D,int);public final#sun.java2d.pipe.RenderQueue#getRenderQueue();public final#void#invalidateContext();public static#void#validateContext(sun.java2d.pipe.hw.AccelSurface);public static#void#validateContext(sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.hw.AccelSurface,sun.java2d.pipe.Region,java.awt.Composite,java.awt.geom.AffineTransform,java.awt.Paint,sun.java2d.SunGraphics2D,int)".equals(methods(c)), "methods");
        check("private final#sun.java2d.pipe.RenderBuffer#buf;private final#sun.java2d.pipe.RenderQueue#rq;private#boolean#isValidatedPaintJustAColor;private#boolean#xformInUse;private#int#validatedFlags;private#int#validatedRGB;private#java.awt.geom.AffineTransform#transform;private#java.lang.ref.Reference#validClipRef;private#java.lang.ref.Reference#validCompRef;private#java.lang.ref.Reference#validDstDataRef;private#java.lang.ref.Reference#validPaintRef;private#java.lang.ref.Reference#validSrcDataRef;protected static#sun.java2d.pipe.BufferedContext#currentContext;public static final#int#NO_CONTEXT_FLAGS;public static final#int#SRC_IS_OPAQUE;public static final#int#USE_MASK".equals(fields(c)), "fields");
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
