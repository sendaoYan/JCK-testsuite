/*
 * @test
 * @summary sun.java2d.SurfaceData conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d:+open
 * @run main SurfaceDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SurfaceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.SurfaceData", false, SurfaceDataTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.SurfaceData".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.Transparency;sun.java2d.DisposerTarget;sun.java2d.StateTrackable;sun.java2d.Surface".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();private static#sun.java2d.loops.CompositeType#getFillCompositeType(sun.java2d.SunGraphics2D);private static#sun.java2d.loops.SurfaceType#getPaintSurfaceType(sun.java2d.SunGraphics2D);private static#sun.java2d.pipe.PixelToParallelogramConverter#makeConverter(sun.java2d.pipe.AAShapePipe);private static#sun.java2d.pipe.PixelToParallelogramConverter#makeConverter(sun.java2d.pipe.AAShapePipe,sun.java2d.pipe.ParallelogramPipe);private#sun.java2d.pipe.TextPipe#getTextPipe(sun.java2d.SunGraphics2D,boolean);protected static native#boolean#isOpaqueGray(java.awt.image.IndexColorModel);protected#sun.java2d.loops.MaskFill#getMaskFill(sun.java2d.SunGraphics2D);protected#void#setBlitProxyCache(sun.awt.image.SurfaceManager$ProxyCache);public abstract#java.awt.GraphicsConfiguration#getDeviceConfiguration();public abstract#java.awt.Rectangle#getBounds();public abstract#java.awt.image.Raster#getRaster(int,int,int,int);public abstract#java.lang.Object#getDestination();public abstract#sun.java2d.SurfaceData#getReplacement();public final#boolean#isValid();public final#java.awt.image.ColorModel#getColorModel();public final#sun.java2d.loops.SurfaceType#getSurfaceType();public final#void#markDirty();public static#boolean#isNull(sun.java2d.SurfaceData);public static#java.lang.Object#convertTo(java.lang.Class,sun.java2d.SurfaceData);public static#sun.java2d.SurfaceData#getPrimarySurfaceData(java.awt.Image);public static#sun.java2d.SurfaceData#restoreContents(java.awt.Image);public static#sun.java2d.loops.RenderLoops#makeRenderLoops(sun.java2d.loops.SurfaceType,sun.java2d.loops.CompositeType,sun.java2d.loops.SurfaceType);public#boolean#canRenderLCDText(sun.java2d.SunGraphics2D);public#boolean#canRenderParallelograms(sun.java2d.SunGraphics2D);public#boolean#copyArea(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#boolean#isSurfaceLost();public#boolean#useTightBBoxes();public#double#getDefaultScaleX();public#double#getDefaultScaleY();public#int#getTransparency();public#int#pixelFor(int);public#int#pixelFor(java.awt.Color);public#int#rgbFor(int);public#java.lang.Object#getDisposerReferent();public#long#getNativeOps();public#sun.java2d.StateTrackable$State#getState();public#sun.java2d.StateTracker#getStateTracker();public#sun.java2d.SurfaceData#getSourceSurfaceData(java.awt.Image,int,sun.java2d.loops.CompositeType,java.awt.Color);public#sun.java2d.SurfaceDataProxy#makeProxyFor(sun.java2d.SurfaceData);public#sun.java2d.loops.RenderLoops#getRenderLoops(sun.java2d.SunGraphics2D);public#void#flush();public#void#invalidate();public#void#setSurfaceLost(boolean);public#void#validatePipe(sun.java2d.SunGraphics2D)".equals(methods(c)), "methods");
        check("#int#haveLCDLoop;#int#havePgramSolidLoop;#int#havePgramXORLoop;private static#sun.java2d.loops.RenderCache#loopcache;private#boolean#surfaceLost;private#boolean#valid;private#java.awt.image.ColorModel#colorModel;private#java.lang.Object#disposerReferent;private#long#pData;private#sun.awt.image.SurfaceManager$ProxyCache#blitProxyCache;private#sun.java2d.StateTrackableDelegate#stateDelegate;private#sun.java2d.loops.SurfaceType#surfaceType;protected static final#sun.java2d.pipe.AAShapePipe#AAClipColorShape;protected static final#sun.java2d.pipe.AAShapePipe#AAClipCompShape;protected static final#sun.java2d.pipe.AAShapePipe#AAClipPaintShape;protected static final#sun.java2d.pipe.AAShapePipe#AAColorShape;protected static final#sun.java2d.pipe.AAShapePipe#AACompShape;protected static final#sun.java2d.pipe.AAShapePipe#AAPaintShape;protected static final#sun.java2d.pipe.AlphaColorPipe#colorPipe;protected static final#sun.java2d.pipe.CompositePipe#clipColorPipe;protected static final#sun.java2d.pipe.CompositePipe#clipCompPipe;protected static final#sun.java2d.pipe.CompositePipe#clipPaintPipe;protected static final#sun.java2d.pipe.CompositePipe#compPipe;protected static final#sun.java2d.pipe.CompositePipe#paintPipe;protected static final#sun.java2d.pipe.DrawImagePipe#imagepipe;protected static final#sun.java2d.pipe.LoopPipe#colorPrimitives;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAClipColorViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAClipCompViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAClipPaintViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAColorViaPgram;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAColorViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AACompViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#AAPaintViaShape;protected static final#sun.java2d.pipe.PixelToParallelogramConverter#colorViaPgram;protected static final#sun.java2d.pipe.PixelToShapeConverter#colorViaShape;protected static final#sun.java2d.pipe.PixelToShapeConverter#compViaShape;protected static final#sun.java2d.pipe.PixelToShapeConverter#paintViaShape;protected static final#sun.java2d.pipe.SpanShapeRenderer#compShape;protected static final#sun.java2d.pipe.SpanShapeRenderer#paintShape;protected static final#sun.java2d.pipe.TextPipe#clipColorText;protected static final#sun.java2d.pipe.TextPipe#clipCompText;protected static final#sun.java2d.pipe.TextPipe#clipPaintText;protected static final#sun.java2d.pipe.TextPipe#colorText;protected static final#sun.java2d.pipe.TextPipe#compText;protected static final#sun.java2d.pipe.TextPipe#paintText;public static final#sun.java2d.pipe.TextPipe#aaTextRenderer;public static final#sun.java2d.pipe.TextPipe#lcdTextRenderer;public static final#sun.java2d.pipe.TextPipe#outlineTextRenderer;public static final#sun.java2d.pipe.TextPipe#solidTextRenderer;static final#int#LOOP_FOUND;static final#int#LOOP_NOTFOUND;static final#int#LOOP_UNKNOWN".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 4, "ctors");
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
