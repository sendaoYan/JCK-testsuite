/*
 * @test
 * @summary sun.awt.X11GraphicsDevice conformance: reflection (no init, native/display-heavy).
 * @run main X11GraphicsDeviceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsDeviceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsDevice", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsDevice".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.GraphicsDevice".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.DisplayChangedListener".equals(interfaces(c)), "interfaces");
        check("private native#void#getDoubleBufferVisuals(int);private static native#boolean#initXrandrExtension();private static native#java.awt.DisplayMode#getCurrentDisplayMode(int);private static native#void#configDisplayMode(int,int,int,int);private static native#void#enterFullScreenExclusive(long);private static native#void#enumDisplayModes(int,java.util.ArrayList);private static native#void#exitFullScreenExclusive(long);private static native#void#initIDs();private static native#void#resetNativeData(int);private static synchronized#boolean#isXrandrExtensionSupported();private static#void#enterFullScreenExclusive(java.awt.Window);private static#void#exitFullScreenExclusive(java.awt.Window);private synchronized#java.awt.DisplayMode#getMatchingDisplayMode(java.awt.DisplayMode);private#java.awt.DisplayMode#getDefaultDisplayMode();private#void#addDoubleBufferVisual(int);private#void#makeConfigurations();private#void#makeDefaultConfiguration();public native#int#getConfigColormap(int,int);public native#int#getConfigDepth(int,int);public native#int#getConfigVisualId(int,int);public native#int#getNumConfigs(int);public native#long#getDisplay();public static native#boolean#isDBESupported();public synchronized#[Ljava.awt.DisplayMode;#getDisplayModes();public synchronized#java.awt.DisplayMode#getDisplayMode();public synchronized#void#displayChanged();public synchronized#void#setDisplayMode(java.awt.DisplayMode);public synchronized#void#setFullScreenWindow(java.awt.Window);public#[Ljava.awt.GraphicsConfiguration;#getConfigurations();public#boolean#isDisplayChangeSupported();public#boolean#isFullScreenSupported();public#int#getScreen();public#int#getType();public#java.awt.GraphicsConfiguration#getDefaultConfiguration();public#java.lang.Object#getProxyKeyFor(sun.java2d.loops.SurfaceType);public#java.lang.String#getIDstring();public#java.lang.String#toString();public#void#addDisplayChangedListener(sun.awt.DisplayChangedListener);public#void#paletteChanged();public#void#removeDisplayChangedListener(sun.awt.DisplayChangedListener)".equals(methods(c)), "methods");
        check("#[Ljava.awt.GraphicsConfiguration;#configs;#int#screen;#java.awt.GraphicsConfiguration#defaultConfig;#java.util.HashMap#x11ProxyKeyMap;#java.util.HashSet#doubleBufferVisuals;private final#java.lang.Object#configLock;private static#java.awt.AWTPermission#fullScreenExclusivePermission;private static#java.lang.Boolean#xrandrExtSupported;private#boolean#shutdownHookRegistered;private#java.awt.DisplayMode#origDisplayMode;private#sun.awt.SunDisplayChanger#topLevels".equals(fields(c)), "fields");
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
