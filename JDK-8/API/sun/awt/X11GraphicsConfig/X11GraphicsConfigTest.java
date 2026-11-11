/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11GraphicsConfig conformance: reflection (no init, native/display-heavy).
 * @run main X11GraphicsConfigTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11GraphicsConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11GraphicsConfig", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11GraphicsConfig".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.GraphicsConfiguration".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.image.SurfaceManager$ProxiedGraphicsConfig".equals(interfaces(c)), "interfaces");
        check("private native#boolean#isTranslucencyCapable(long);private native#double#getXResolution(int);private native#double#getYResolution(int);private native#int#getNumColors();private native#java.awt.image.ColorModel#makeColorModel();private native#long#createBackBuffer(long,int);private native#void#init(int,int);private native#void#swapBuffers(long,int);private static native#void#dispose(long);private static native#void#initIDs();private static#int#getSwapAction(java.awt.BufferCapabilities$FlipContents);public native#java.awt.Rectangle#pGetBounds(int);public native#void#destroyBackBuffer(long);public static#java.awt.image.ComponentColorModel#createABGRCCM();public static#java.awt.image.DirectColorModel#createDCM32(int,int,int,int,boolean);public static#sun.awt.X11GraphicsConfig#getConfig(sun.awt.X11GraphicsDevice,int,int,int,boolean);public static#sun.awt.X11GraphicsConfig#getConfig(sun.awt.X11GraphicsDevice,int,int,int,int);public synchronized#java.awt.image.ColorModel#getColorModel();public synchronized#sun.java2d.loops.RenderLoops#getSolidLoops(sun.java2d.loops.SurfaceType);public synchronized#sun.java2d.loops.SurfaceType#getSurfaceType();public#boolean#isDoubleBuffered();public#boolean#isTranslucencyCapable();public#int#getBitsPerPixel();public#int#getColormap();public#int#getDepth();public#int#getVisual();public#java.awt.BufferCapabilities#getBufferCapabilities();public#java.awt.GraphicsDevice#getDevice();public#java.awt.Image#createAcceleratedImage(java.awt.Component,int,int);public#java.awt.ImageCapabilities#getImageCapabilities();public#java.awt.Rectangle#getBounds();public#java.awt.geom.AffineTransform#getDefaultTransform();public#java.awt.geom.AffineTransform#getNormalizingTransform();public#java.awt.image.ColorModel#getColorModel(int);public#java.awt.image.VolatileImage#createBackBufferImage(java.awt.Component,long);public#java.lang.Object#getProxyKey();public#java.lang.String#toString();public#long#createBackBuffer(sun.awt.X11ComponentPeer,int,java.awt.BufferCapabilities);public#long#getAData();public#sun.java2d.SurfaceData#createSurfaceData(sun.awt.X11ComponentPeer);public#void#flip(sun.awt.X11ComponentPeer,java.awt.Component,java.awt.image.VolatileImage,int,int,int,int,java.awt.BufferCapabilities$FlipContents)".equals(methods(c)), "methods");
        check("#boolean#doubleBuffer;#int#colormap;#int#depth;#java.awt.image.ColorModel#colorModel;#long#aData;private static#java.awt.ImageCapabilities#imageCaps;private#java.awt.BufferCapabilities#bufferCaps;private#java.lang.Object#disposerReferent;protected#int#bitsPerPixel;protected#int#visual;protected#sun.awt.X11GraphicsDevice#screen;protected#sun.java2d.loops.SurfaceType#surfaceType;public#sun.java2d.loops.RenderLoops#solidloops".equals(fields(c)), "fields");
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
