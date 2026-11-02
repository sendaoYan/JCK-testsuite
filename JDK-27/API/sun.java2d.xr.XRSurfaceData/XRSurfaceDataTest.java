/*
 * @test
 * @summary sun.java2d.xr.XRSurfaceData conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.xr:+open
 * @run main XRSurfaceDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRSurfaceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.XRSurfaceData", false, XRSurfaceDataTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.XRSurfaceData".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.java2d.x11.XSurfaceData".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#validateAsSource(java.awt.geom.AffineTransform,int,int);native#void#freeXSDOPicture(long);native#void#initXRPicture(long,int);private static native#void#initIDs();protected native#void#XRInitSurface(int,int,int,long,int);protected#boolean#isXRDrawableValid();protected#sun.java2d.loops.MaskFill#getMaskFill(sun.java2d.SunGraphics2D);protected#sun.java2d.pipe.TextPipe#getTextPipe(sun.java2d.SunGraphics2D);public abstract#boolean#canSourceSendExposures(int,int,int,int);public static#sun.java2d.loops.SurfaceType#getPixmapSurfaceType(int);public static#sun.java2d.xr.XRGraphicsConfig#getGC(sun.awt.X11ComponentPeer);public static#sun.java2d.xr.XRSurfaceData$XRPixmapSurfaceData#createData(sun.java2d.xr.XRGraphicsConfig,int,int,java.awt.image.ColorModel,java.awt.Image,long,int,boolean);public static#sun.java2d.xr.XRSurfaceData$XRWindowSurfaceData#createData(sun.awt.X11ComponentPeer);public static#void#initXRSurfaceData();public synchronized#void#makePipes();public#boolean#copyArea(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#int#getPicture();public#int#getXid();public#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.image.Raster#getRaster(int,int,int,int);public#long#getGC();public#sun.java2d.SurfaceDataProxy#makeProxyFor(sun.java2d.SurfaceData);public#sun.java2d.loops.RenderLoops#getRenderLoops(sun.java2d.SunGraphics2D);public#sun.java2d.xr.XRGraphicsConfig#getGraphicsConfig();public#void#initXRender(int);public#void#invalidate();public#void#setStaticSrcTx(java.awt.geom.AffineTransform);public#void#validateAsDestination(sun.java2d.SunGraphics2D,sun.java2d.pipe.Region);public#void#validateCopyAreaGC(sun.java2d.pipe.Region,boolean);public#void#validatePipe(sun.java2d.SunGraphics2D)".equals(methods(c)), "methods");
        check("#boolean#transformInUse;#int#validatedFilter;#int#validatedRepeat;#java.awt.geom.AffineTransform#staticSrcTx;#java.awt.geom.AffineTransform#validatedSourceTransform;#sun.awt.X11ComponentPeer#peer;#sun.java2d.xr.XRBackend#renderQueue;#sun.java2d.xr.XRGraphicsConfig#graphicsConfig;private#boolean#validatedExposures;private#int#validatedGCForegroundPixel;private#int#xid;private#long#xgc;private#sun.java2d.loops.RenderLoops#solidloops;private#sun.java2d.loops.XORComposite#validatedXorComp;private#sun.java2d.pipe.Region#validatedClip;private#sun.java2d.pipe.Region#validatedGCClip;protected#int#depth;protected#sun.java2d.pipe.PixelToShapeConverter#aaPixelToShapeConv;protected#sun.java2d.pipe.PixelToShapeConverter#xrtxpipe;protected#sun.java2d.pipe.ShapeDrawPipe#aaShapePipe;protected#sun.java2d.pipe.TextPipe#xrtextpipe;protected#sun.java2d.xr.XRDrawImage#xrDrawImage;protected#sun.java2d.xr.XRRenderer#xrpipe;public static final#java.lang.String#DESC_BYTE_A8_X11;public static final#java.lang.String#DESC_INT_ARGB_X11;public static final#java.lang.String#DESC_INT_RGB_X11;public static final#sun.java2d.loops.SurfaceType#ByteA8X11;public static final#sun.java2d.loops.SurfaceType#IntArgbPreX11;public static final#sun.java2d.loops.SurfaceType#IntRgbX11;public#int#picture;public#sun.java2d.xr.XRCompositeManager#maskBuffer".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
