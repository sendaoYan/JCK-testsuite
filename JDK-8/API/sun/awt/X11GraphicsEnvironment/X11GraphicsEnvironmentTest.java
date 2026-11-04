/*
 * @test
 * @summary sun.awt.X11GraphicsEnvironment conformance: reflection (no init, native/display-heavy).
 * @run main X11GraphicsEnvironmentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsEnvironment", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsEnvironment".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.SunGraphicsEnvironment".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#boolean#initGLX();private static native#boolean#initXRender(boolean,boolean);private static native#boolean#pRunningXinerama();private static native#int#checkShmExt();private static native#java.awt.Point#getXineramaCenterPoint();private static native#java.lang.String#getDisplayString();private static native#void#initDisplay(boolean);private static#boolean#_isDisplayLocal();protected native#int#getDefaultScreenNum();protected native#int#getNumScreens();protected#java.awt.GraphicsDevice#makeScreenDevice(int);protected#java.awt.Rectangle#getXineramaWindowBounds();public static#boolean#isGLXAvailable();public static#boolean#isGLXVerbose();public static#boolean#isXRenderAvailable();public static#boolean#isXRenderVerbose();public#boolean#isDisplayLocal();public#boolean#runningXinerama();public#java.awt.GraphicsDevice#getDefaultScreenDevice();public#java.awt.Point#getCenterPoint();public#java.awt.Rectangle#getMaximumWindowBounds();public#java.lang.String#getDefaultFontFaceName();public#void#paletteChanged()".equals(methods(c)), "methods");
        check("private static final#sun.util.logging.PlatformLogger#log;private static final#sun.util.logging.PlatformLogger#screenLog;private static#boolean#glxAvailable;private static#boolean#glxVerbose;private static#boolean#xRenderAvailable;private static#boolean#xRenderVerbose;private static#java.lang.Boolean#xinerState;private#java.lang.Boolean#isDisplayLocal".equals(fields(c)), "fields");
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
