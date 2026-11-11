/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11GraphicsDevice conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main X11GraphicsDeviceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsDeviceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsDevice", false, X11GraphicsDeviceTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsDevice".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.awt.GraphicsDevice".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.DisplayChangedListener".equals(interfaces(c)), "interfaces");
        check("private native#int#getConfigColormap(int,int);private native#int#getConfigDepth(int,int);private native#java.awt.Rectangle#pGetBounds(int);private native#void#getDoubleBufferVisuals(int);private static native#boolean#initXrandrExtension(boolean);private static native#double#getNativeScaleFactor(int);private static native#java.awt.DisplayMode#getCurrentDisplayMode(int);private static native#void#configDisplayMode(int,int,int,int);private static native#void#enterFullScreenExclusive(long);private static native#void#enumDisplayModes(int,java.util.ArrayList);private static native#void#exitFullScreenExclusive(long);private static synchronized#boolean#isXrandrExtensionSupported();private static#void#enterFullScreenExclusive(java.awt.Window);private static#void#exitFullScreenExclusive(java.awt.Window);private synchronized#java.awt.DisplayMode#getMatchingDisplayMode(java.awt.DisplayMode);private#int#initScaleFactor();private#java.awt.DisplayMode#getDefaultDisplayMode();private#java.awt.Rectangle#getBoundsImpl();private#void#addDoubleBufferVisual(int);private#void#makeConfigurations();private#void#makeDefaultConfiguration();public native#int#getConfigVisualId(int,int);public native#int#getNumConfigs(int);public native#long#getDisplay();public synchronized#[Ljava.awt.DisplayMode;#getDisplayModes();public synchronized#java.awt.DisplayMode#getDisplayMode();public synchronized#void#displayChanged();public synchronized#void#setDisplayMode(java.awt.DisplayMode);public synchronized#void#setFullScreenWindow(java.awt.Window);public#[Ljava.awt.GraphicsConfiguration;#getConfigurations();public#boolean#isDisplayChangeSupported();public#boolean#isFullScreenSupported();public#int#getNativeScale();public#int#getScaleFactor();public#int#getScreen();public#int#getType();public#int#scaleDown(int);public#int#scaleUp(int);public#java.awt.GraphicsConfiguration#getDefaultConfiguration();public#java.awt.Insets#getInsets();public#java.awt.Rectangle#getBounds();public#java.lang.String#getIDstring();public#java.lang.String#toString();public#sun.awt.image.SurfaceManager$ProxyCache#getProxyCacheFor(sun.java2d.loops.SurfaceType);public#void#addDisplayChangedListener(sun.awt.DisplayChangedListener);public#void#invalidate(sun.awt.X11GraphicsDevice);public#void#paletteChanged();public#void#removeDisplayChangedListener(sun.awt.DisplayChangedListener);public#void#resetInsets();public#void#setInsets(java.awt.Insets);static native#boolean#isDBESupported()".equals(methods(c)), "methods");
        check("#[Ljava.awt.GraphicsConfiguration;#configs;#java.awt.GraphicsConfiguration#defaultConfig;#java.util.HashSet#doubleBufferVisuals;#java.util.Map#x11ProxyCacheMap;private static#java.lang.Boolean#xrandrExtSupported;private volatile#int#screen;private volatile#java.awt.DisplayMode#xwlCurrentDisplayMode;private volatile#java.awt.Insets#insets;private volatile#java.awt.Rectangle#bounds;private#boolean#shutdownHookRegistered;private#int#scale;private#java.awt.DisplayMode#origDisplayMode;private#sun.awt.SunDisplayChanger#topLevels".equals(fields(c)), "fields");
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
