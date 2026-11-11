/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11GraphicsConfig conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main X11GraphicsConfigTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsConfig", false, X11GraphicsConfigTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsConfig".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.GraphicsConfiguration".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.image.SurfaceManager$Factory;sun.awt.image.SurfaceManager$ProxiedGraphicsConfig".equals(interfaces(c)), "interfaces");
        check("private native#boolean#isTranslucencyCapable(long);private native#double#getXResolution(int);private native#double#getYResolution(int);private native#int#getNumColors();private native#java.awt.image.ColorModel#makeColorModel();private native#long#createBackBuffer(long,int);private native#void#init(int,int);private native#void#swapBuffers(long,int);private static native#void#dispose(long);private static native#void#initIDs();private static#int#getSwapAction(java.awt.BufferCapabilities$FlipContents);public final#java.awt.Rectangle#getBounds();public native#void#destroyBackBuffer(long);public static#java.awt.image.ComponentColorModel#createABGRCCM();public static#java.awt.image.DirectColorModel#createDCM32(int,int,int,int,boolean);public static#sun.awt.X11GraphicsConfig#getConfig(sun.awt.X11GraphicsDevice,int,int,int,boolean);public static#sun.awt.X11GraphicsConfig#getConfig(sun.awt.X11GraphicsDevice,int,int,int,int);public synchronized#java.awt.image.ColorModel#getColorModel();public synchronized#sun.java2d.loops.RenderLoops#getSolidLoops(sun.java2d.loops.SurfaceType);public synchronized#sun.java2d.loops.SurfaceType#getSurfaceType();public#boolean#isDoubleBuffered();public#boolean#isTranslucencyCapable();public#int#getBitsPerPixel();public#int#getColormap();public#int#getDepth();public#int#getScale();public#int#getVisual();public#int#scaleDown(int);public#int#scaleUp(int);public#java.awt.BufferCapabilities#getBufferCapabilities();public#java.awt.Image#createAcceleratedImage(java.awt.Component,int,int);public#java.awt.ImageCapabilities#getImageCapabilities();public#java.awt.geom.AffineTransform#getDefaultTransform();public#java.awt.geom.AffineTransform#getNormalizingTransform();public#java.awt.image.ColorModel#getColorModel(int);public#java.awt.image.VolatileImage#createBackBufferImage(java.awt.Component,long);public#java.lang.String#toString();public#long#createBackBuffer(sun.awt.X11ComponentPeer,int,java.awt.BufferCapabilities);public#long#getAData();public#sun.awt.X11GraphicsDevice#getDevice();public#sun.awt.image.SurfaceManager$ProxyCache#getSurfaceDataProxyCache();public#sun.awt.image.VolatileSurfaceManager#createVolatileManager(sun.awt.image.SunVolatileImage,java.lang.Object);public#sun.java2d.SurfaceData#createSurfaceData(sun.awt.X11ComponentPeer);public#void#flip(sun.awt.X11ComponentPeer,java.awt.Component,java.awt.image.VolatileImage,int,int,int,int,java.awt.BufferCapabilities$FlipContents)".equals(methods(c)), "methods");
        check("#boolean#doubleBuffer;#int#colormap;#int#depth;#java.awt.image.ColorModel#colorModel;#long#aData;private final#sun.awt.X11GraphicsDevice#device;private static#java.awt.ImageCapabilities#imageCaps;private#java.awt.BufferCapabilities#bufferCaps;private#java.lang.Object#disposerReferent;protected#int#bitsPerPixel;protected#int#visual;protected#sun.java2d.loops.SurfaceType#surfaceType;public#sun.java2d.loops.RenderLoops#solidloops".equals(fields(c)), "fields");
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
