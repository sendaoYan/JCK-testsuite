/*
 * @test
 * @summary sun.java2d.pipe.BufferedBufImgOps conformance: reflection (no init, native/display-heavy).
 * @run main BufferedBufImgOpsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufferedBufImgOpsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.BufferedBufImgOps", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.BufferedBufImgOps".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#disableConvolveOp(sun.java2d.pipe.RenderQueue);private static#void#disableLookupOp(sun.java2d.pipe.RenderQueue);private static#void#disableRescaleOp(sun.java2d.pipe.RenderQueue);private static#void#enableConvolveOp(sun.java2d.pipe.RenderQueue,sun.java2d.SurfaceData,java.awt.image.ConvolveOp);private static#void#enableLookupOp(sun.java2d.pipe.RenderQueue,sun.java2d.SurfaceData,java.awt.image.BufferedImage,java.awt.image.LookupOp);private static#void#enableRescaleOp(sun.java2d.pipe.RenderQueue,sun.java2d.SurfaceData,java.awt.image.BufferedImage,java.awt.image.RescaleOp);public static#boolean#isConvolveOpValid(java.awt.image.ConvolveOp);public static#boolean#isLookupOpValid(java.awt.image.LookupOp,java.awt.image.BufferedImage);public static#boolean#isRescaleOpValid(java.awt.image.RescaleOp,java.awt.image.BufferedImage);public static#void#disableBufImgOp(sun.java2d.pipe.RenderQueue,java.awt.image.BufferedImageOp);public static#void#enableBufImgOp(sun.java2d.pipe.RenderQueue,sun.java2d.SurfaceData,java.awt.image.BufferedImage,java.awt.image.BufferedImageOp)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
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
