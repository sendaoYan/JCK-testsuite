/*
 * @test
 * @summary sun.java2d.opengl.GLXGraphicsConfig conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.opengl:+open
 * @run main GLXGraphicsConfigTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GLXGraphicsConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.opengl.GLXGraphicsConfig", false, GLXGraphicsConfigTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.opengl.GLXGraphicsConfig".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.X11GraphicsConfig".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.opengl.OGLGraphicsConfig".equals(interfaces(c)), "interfaces");
        check("private native#void#initConfig(long,long);private static native#int#getOGLCapabilities(long);private static native#long#getGLXConfigInfo(int,int);public final#boolean#isCapPresent(int);public final#long#getNativeConfigInfo();public final#sun.java2d.opengl.OGLContext#getContext();public static#sun.java2d.opengl.GLXGraphicsConfig#getConfig(sun.awt.X11GraphicsDevice,int);public#java.awt.BufferCapabilities#getBufferCapabilities();public#java.awt.Image#createAcceleratedImage(java.awt.Component,int,int);public#java.awt.ImageCapabilities#getImageCapabilities();public#java.awt.image.BufferedImage#createCompatibleImage(int,int);public#java.awt.image.ColorModel#getColorModel(int);public#java.awt.image.VolatileImage#createBackBufferImage(java.awt.Component,long);public#java.awt.image.VolatileImage#createCompatibleVolatileImage(int,int,int,int);public#java.lang.String#toString();public#long#createBackBuffer(sun.awt.X11ComponentPeer,int,java.awt.BufferCapabilities);public#sun.awt.image.SurfaceManager$ProxyCache#getSurfaceDataProxyCache();public#sun.awt.image.VolatileSurfaceManager#createVolatileManager(sun.awt.image.SunVolatileImage,java.lang.Object);public#sun.java2d.SurfaceData#createManagedSurface(int,int,int);public#sun.java2d.SurfaceData#createSurfaceData(sun.awt.X11ComponentPeer);public#sun.java2d.pipe.hw.ContextCapabilities#getContextCapabilities();public#void#destroyBackBuffer(long);public#void#flip(sun.awt.X11ComponentPeer,java.awt.Component,java.awt.image.VolatileImage,int,int,int,int,java.awt.BufferCapabilities$FlipContents)".equals(methods(c)), "methods");
        check("private final#sun.awt.image.SurfaceManager$ProxyCache#surfaceDataProxyCache;private final#sun.java2d.opengl.OGLContext#context;private static#java.awt.ImageCapabilities#imageCaps;private#java.awt.BufferCapabilities#bufferCaps;private#long#pConfigInfo;private#sun.java2d.pipe.hw.ContextCapabilities#oglCaps".equals(fields(c)), "fields");
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
