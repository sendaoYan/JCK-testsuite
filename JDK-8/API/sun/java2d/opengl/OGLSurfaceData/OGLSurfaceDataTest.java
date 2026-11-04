/*
 * @test
 * @summary sun.java2d.opengl.OGLSurfaceData conformance: reflection (no init, native/display-heavy).
 * @run main OGLSurfaceDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class OGLSurfaceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.opengl.OGLSurfaceData", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.opengl.OGLSurfaceData".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.SurfaceData".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.hw.AccelSurface".equals(interfaces(c)), "interfaces");
        check("#boolean#isOnScreen();#boolean#isTexNonPow2Available();#boolean#isTexRectAvailable();final#sun.java2d.opengl.OGLGraphicsConfig#getOGLGraphicsConfig();private native#int#getTextureID(long);private native#int#getTextureTarget(long);private static#sun.java2d.loops.SurfaceType#getCustomSurfaceType(int);private#boolean#canHandleComposite(java.awt.Composite);private#void#initSurfaceNow(int,int);protected native#boolean#initFBObject(long,boolean,boolean,boolean,int,int);protected native#boolean#initFlipBackbuffer(long);protected native#boolean#initTexture(long,boolean,boolean,boolean,int,int);protected#sun.java2d.loops.MaskFill#getMaskFill(sun.java2d.SunGraphics2D);protected#void#initSurface(int,int);public final#int#getTextureID();public final#int#getTextureTarget();public final#int#getType();public final#sun.java2d.opengl.OGLContext#getContext();public#boolean#canRenderLCDText(sun.java2d.SunGraphics2D);public#boolean#copyArea(sun.java2d.SunGraphics2D,int,int,int,int,int,int);public#java.awt.Rectangle#getNativeBounds();public#java.awt.image.Raster#getRaster(int,int,int,int);public#long#getNativeResource(int);public#sun.java2d.SurfaceDataProxy#makeProxyFor(sun.java2d.SurfaceData);public#void#flush();public#void#validatePipe(sun.java2d.SunGraphics2D);static#void#dispose(long,sun.java2d.opengl.OGLGraphicsConfig);static#void#swapBuffers(long)".equals(methods(c)), "methods");
        check("private static final#java.lang.String#DESC_OPENGL_SURFACE;private static final#java.lang.String#DESC_OPENGL_SURFACE_RTT;private static final#java.lang.String#DESC_OPENGL_TEXTURE;private static#boolean#isBIOpShaderEnabled;private static#boolean#isFBObjectEnabled;private static#boolean#isGradShaderEnabled;private static#boolean#isLCDShaderEnabled;private#int#nativeHeight;private#int#nativeWidth;private#sun.java2d.opengl.OGLGraphicsConfig#graphicsConfig;protected static#sun.java2d.opengl.OGLDrawImage#oglImagePipe;protected static#sun.java2d.opengl.OGLRenderer#oglRenderPipe;protected static#sun.java2d.opengl.OGLTextRenderer#oglTextPipe;protected static#sun.java2d.pipe.ParallelogramPipe#oglAAPgramPipe;protected static#sun.java2d.pipe.PixelToParallelogramConverter#oglTxRenderPipe;protected#int#type;public static final#int#FBOBJECT;public static final#int#PF_3BYTE_BGR;public static final#int#PF_BYTE_GRAY;public static final#int#PF_INT_ARGB;public static final#int#PF_INT_ARGB_PRE;public static final#int#PF_INT_BGR;public static final#int#PF_INT_BGRX;public static final#int#PF_INT_RGB;public static final#int#PF_INT_RGBX;public static final#int#PF_USHORT_555_RGB;public static final#int#PF_USHORT_555_RGBX;public static final#int#PF_USHORT_565_RGB;public static final#int#PF_USHORT_GRAY;static final#sun.java2d.loops.SurfaceType#OpenGLSurface;static final#sun.java2d.loops.SurfaceType#OpenGLSurfaceRTT;static final#sun.java2d.loops.SurfaceType#OpenGLTexture".equals(fields(c)), "fields");
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
