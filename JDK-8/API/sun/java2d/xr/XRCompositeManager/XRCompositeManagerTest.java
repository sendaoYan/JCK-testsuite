/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.xr.XRCompositeManager conformance: reflection (no init, native/display-heavy).
 * @run main XRCompositeManagerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRCompositeManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.XRCompositeManager", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.XRCompositeManager".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#setComposite(java.awt.Composite);protected#sun.java2d.xr.XRSurfaceData#getCurrentSource();public static synchronized#sun.java2d.xr.XRCompositeManager#getInstance(sun.java2d.xr.XRSurfaceData);public#boolean#isSolidPaintActive();public#boolean#isTexturePaintActive();public#boolean#maskRequired();public#byte#getCompRule();public#float#getExtraAlpha();public#int#getExtraAlphaMask();public#sun.font.XRTextRenderer#getTextRenderer();public#sun.java2d.xr.MaskTileManager#getMaskBuffer();public#sun.java2d.xr.XRBackend#getBackend();public#sun.java2d.xr.XRColor#getAlphaColor();public#sun.java2d.xr.XRColor#getMaskColor();public#sun.java2d.xr.XRMaskImage#getMaskImage();public#void#XRComposite(int,int,int,int,int,int,int,int,int,int,int);public#void#XRCompositeRectangles(sun.java2d.xr.XRSurfaceData,sun.java2d.xr.GrowableRectArray);public#void#XRCompositeTraps(int,int,int,sun.java2d.jules.TrapezoidList);public#void#XRRenderRectangles(sun.java2d.xr.XRSurfaceData,sun.java2d.xr.GrowableRectArray);public#void#XRResetPaint();public#void#compositeBlit(sun.java2d.xr.XRSurfaceData,sun.java2d.xr.XRSurfaceData,int,int,int,int,int,int);public#void#compositeText(sun.java2d.xr.XRSurfaceData,int,int,int,int,sun.java2d.xr.GrowableEltArray);public#void#initResources(sun.java2d.xr.XRSurfaceData);public#void#setForeground(int);public#void#setGradientPaint(sun.java2d.xr.XRSurfaceData);public#void#setTexturePaint(sun.java2d.xr.XRSurfaceData);public#void#validateCompositeState(java.awt.Composite,java.awt.geom.AffineTransform,java.awt.Paint,sun.java2d.SunGraphics2D)".equals(methods(c)), "methods");
        check("#boolean#xorEnabled;#byte#compRule;#float#extraAlpha;#float#validatedExtraAlpha;#int#alphaMask;#int#alphaMaskPict;#int#gradCachePicture;#int#gradCachePixmap;#int#srcType;#int#validatedPixel;#java.awt.Composite#validatedComp;#java.awt.Paint#validatedPaint;#sun.font.XRTextRenderer#textRenderer;#sun.java2d.xr.MaskTileManager#maskBuffer;#sun.java2d.xr.XRBackend#con;#sun.java2d.xr.XRColor#alphaColor;#sun.java2d.xr.XRColor#solidColor;#sun.java2d.xr.XRMaskImage#maskImage;#sun.java2d.xr.XRSolidSrcPict#solidSrc32;#sun.java2d.xr.XRSurfaceData#gradient;#sun.java2d.xr.XRSurfaceData#solidSrcPict;#sun.java2d.xr.XRSurfaceData#texture;private static final#int#GRADIENT;private static final#int#SOLID;private static final#int#TEXTURE;private static#boolean#enableGradCache;private static#sun.java2d.xr.XRCompositeManager#instance".equals(fields(c)), "fields");
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
