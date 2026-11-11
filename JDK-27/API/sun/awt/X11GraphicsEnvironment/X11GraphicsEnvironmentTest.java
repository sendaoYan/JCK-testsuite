/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11GraphicsEnvironment conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main X11GraphicsEnvironmentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsEnvironment", false, X11GraphicsEnvironmentTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsEnvironment".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.java2d.SunGraphicsEnvironment".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private native#int#getDefaultScreenNum();private static native#boolean#initGLX();private static native#boolean#initXRender(boolean,boolean);private static native#boolean#pRunningXinerama();private static native#int#checkShmExt();private static native#java.lang.String#getDisplayString();private static native#void#initDisplay(boolean);private static native#void#initNativeData();private static#boolean#_isDisplayLocal();private static#void#initStatic();private synchronized#void#initDevices();protected native#int#getNumScreens();protected#java.awt.GraphicsDevice#makeScreenDevice(int);public static#boolean#isGLXAvailable();public static#boolean#isGLXVerbose();public static#boolean#isXRenderAvailable();public static#boolean#isXRenderVerbose();public synchronized#[Ljava.awt.GraphicsDevice;#getScreenDevices();public synchronized#java.awt.GraphicsDevice#getDefaultScreenDevice();public synchronized#java.awt.GraphicsDevice#getScreenDevice(int);public#boolean#isDisplayLocal();public#boolean#runningXinerama();public#java.lang.String#getDefaultFontFaceName();public#void#paletteChanged();public#void#rebuildDevices()".equals(methods(c)), "methods");
        check("private final#java.util.Map#devices;private static#boolean#glxAvailable;private static#boolean#glxVerbose;private static#boolean#xRenderAvailable;private static#boolean#xRenderVerbose;private#int#mainScreen;private#java.lang.Boolean#isDisplayLocal;private#java.util.List#oldDevices".equals(fields(c)), "fields");
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
