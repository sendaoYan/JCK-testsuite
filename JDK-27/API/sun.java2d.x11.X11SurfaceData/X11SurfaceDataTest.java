/*
 * @test
 * @summary sun.java2d.x11.X11SurfaceData conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.x11:+open
 * @run main X11SurfaceDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11SurfaceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.x11.X11SurfaceData", false, X11SurfaceDataTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.x11.X11SurfaceData".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.java2d.x11.XSurfaceData".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("final#long#getGC(sun.java2d.pipe.Region,int,java.awt.Composite,int,boolean);private static native#boolean#isShmPMAvailable();private static native#void#XSetCopyMode(long);private static native#void#XSetForeground(long,int);private static native#void#XSetXorMode(long);private static native#void#initIDs(java.lang.Class);protected native#void#initSurface(int,int,int,long);public abstract#boolean#canSourceSendExposures(int,int,int,int);public final#long#getBlitGC(sun.java2d.pipe.Region,boolean);public final#long#getRenderGC(sun.java2d.pipe.Region,int,java.awt.Composite,int);public static#boolean#isAccelerationEnabled();public static#sun.awt.X11GraphicsConfig#getGC(sun.awt.X11ComponentPeer);public static#sun.java2d.loops.SurfaceType#getSurfaceType(sun.awt.X11GraphicsConfig,int);public static#sun.java2d.loops.SurfaceType#getSurfaceType(sun.awt.X11GraphicsConfig,int,boolean);public static#sun.java2d.x11.X11SurfaceData$X11PixmapSurfaceData#createData(sun.awt.X11GraphicsConfig,int,int,java.awt.image.ColorModel,java.awt.Image,long,int,boolean);public static#sun.java2d.x11.X11SurfaceData$X11WindowSurfaceData#createData(sun.awt.X11ComponentPeer);public synchronized#void#makePipes();public#boolean#copyArea(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.image.Raster#getRaster(int,int,int,int);public#sun.java2d.SurfaceDataProxy#makeProxyFor(sun.java2d.SurfaceData);public#sun.java2d.loops.RenderLoops#getRenderLoops(sun.java2d.SunGraphics2D);public#void#invalidate();public#void#validatePipe(sun.java2d.SunGraphics2D)".equals(methods(c)), "methods");
        check("#sun.awt.X11ComponentPeer#peer;#sun.awt.X11GraphicsConfig#graphicsConfig;private static#java.lang.Boolean#accelerationEnabled;private static#sun.java2d.x11.X11SurfaceData$LazyPipe#lazypipe;private#boolean#validatedExposures;private#int#validatedPixel;private#int#xorpixelmod;private#long#xgc;private#sun.java2d.loops.RenderLoops#solidloops;private#sun.java2d.loops.XORComposite#validatedXorComp;private#sun.java2d.pipe.Region#validatedClip;protected static#sun.java2d.pipe.TextPipe#x11textpipe;protected#int#depth;protected#sun.java2d.pipe.PixelToShapeConverter#x11txpipe;protected#sun.java2d.x11.X11Renderer#x11pipe;public static final#java.lang.String#DESC_3BYTE_BGR_X11;public static final#java.lang.String#DESC_3BYTE_BGR_X11_BM;public static final#java.lang.String#DESC_3BYTE_RGB_X11;public static final#java.lang.String#DESC_3BYTE_RGB_X11_BM;public static final#java.lang.String#DESC_4BYTE_ABGR_PRE_X11;public static final#java.lang.String#DESC_BYTE_GRAY_X11;public static final#java.lang.String#DESC_BYTE_GRAY_X11_BM;public static final#java.lang.String#DESC_BYTE_IND_OPQ_X11;public static final#java.lang.String#DESC_BYTE_IND_X11_BM;public static final#java.lang.String#DESC_INDEX8_GRAY_X11;public static final#java.lang.String#DESC_INDEX8_GRAY_X11_BM;public static final#java.lang.String#DESC_INT_ARGB_PRE_X11;public static final#java.lang.String#DESC_INT_BGR_X11;public static final#java.lang.String#DESC_INT_BGR_X11_BM;public static final#java.lang.String#DESC_INT_RGB_X11;public static final#java.lang.String#DESC_INT_RGB_X11_BM;public static final#java.lang.String#DESC_USHORT_555_RGB_X11;public static final#java.lang.String#DESC_USHORT_555_RGB_X11_BM;public static final#java.lang.String#DESC_USHORT_565_RGB_X11;public static final#java.lang.String#DESC_USHORT_565_RGB_X11_BM;public static final#java.lang.String#DESC_USHORT_INDEXED_X11;public static final#java.lang.String#DESC_USHORT_INDEXED_X11_BM;public static final#sun.java2d.loops.SurfaceType#ByteGrayX11;public static final#sun.java2d.loops.SurfaceType#ByteGrayX11_BM;public static final#sun.java2d.loops.SurfaceType#ByteIndexedOpaqueX11;public static final#sun.java2d.loops.SurfaceType#ByteIndexedX11_BM;public static final#sun.java2d.loops.SurfaceType#FourByteAbgrPreX11;public static final#sun.java2d.loops.SurfaceType#Index8GrayX11;public static final#sun.java2d.loops.SurfaceType#Index8GrayX11_BM;public static final#sun.java2d.loops.SurfaceType#IntArgbPreX11;public static final#sun.java2d.loops.SurfaceType#IntBgrX11;public static final#sun.java2d.loops.SurfaceType#IntBgrX11_BM;public static final#sun.java2d.loops.SurfaceType#IntRgbX11;public static final#sun.java2d.loops.SurfaceType#IntRgbX11_BM;public static final#sun.java2d.loops.SurfaceType#ThreeByteBgrX11;public static final#sun.java2d.loops.SurfaceType#ThreeByteBgrX11_BM;public static final#sun.java2d.loops.SurfaceType#ThreeByteRgbX11;public static final#sun.java2d.loops.SurfaceType#ThreeByteRgbX11_BM;public static final#sun.java2d.loops.SurfaceType#UShort555RgbX11;public static final#sun.java2d.loops.SurfaceType#UShort555RgbX11_BM;public static final#sun.java2d.loops.SurfaceType#UShort565RgbX11;public static final#sun.java2d.loops.SurfaceType#UShort565RgbX11_BM;public static final#sun.java2d.loops.SurfaceType#UShortIndexedX11;public static final#sun.java2d.loops.SurfaceType#UShortIndexedX11_BM".equals(fields(c)), "fields");
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
