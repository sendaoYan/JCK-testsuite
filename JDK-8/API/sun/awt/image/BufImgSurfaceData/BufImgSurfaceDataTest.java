/*
 * @test
 * @summary sun.awt.image.BufImgSurfaceData conformance: reflection (no init, native/display-heavy).
 * @run main BufImgSurfaceDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BufImgSurfaceDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.BufImgSurfaceData", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.BufImgSurfaceData".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.java2d.SurfaceData".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs(java.lang.Class,java.lang.Class);protected native#void#initRaster(java.lang.Object,int,int,int,int,int,int,java.awt.image.IndexColorModel);protected#void#checkCustomComposite();public static synchronized#sun.java2d.loops.RenderLoops#getSolidLoops(sun.java2d.loops.SurfaceType);public static#sun.java2d.SurfaceData#createData(java.awt.image.BufferedImage);public static#sun.java2d.SurfaceData#createData(java.awt.image.Raster,java.awt.image.ColorModel);public static#sun.java2d.SurfaceData#createDataBC(java.awt.image.BufferedImage,sun.java2d.loops.SurfaceType,int);public static#sun.java2d.SurfaceData#createDataBP(java.awt.image.BufferedImage,sun.java2d.loops.SurfaceType);public static#sun.java2d.SurfaceData#createDataIC(java.awt.image.BufferedImage,sun.java2d.loops.SurfaceType);public static#sun.java2d.SurfaceData#createDataSC(java.awt.image.BufferedImage,sun.java2d.loops.SurfaceType,java.awt.image.IndexColorModel);public synchronized#java.awt.GraphicsConfiguration#getDeviceConfiguration();public#java.awt.Rectangle#getBounds();public#java.awt.image.Raster#getRaster(int,int,int,int);public#java.lang.Object#getDestination();public#sun.java2d.SurfaceData#getReplacement();public#sun.java2d.loops.RenderLoops#getRenderLoops(sun.java2d.SunGraphics2D);public#void#initSolidLoops()".equals(methods(c)), "methods");
        check("#java.awt.image.BufferedImage#bufImg;#sun.java2d.loops.RenderLoops#solidloops;private static final#int#CACHE_SIZE;private static final#int#DCM_4444_ALPHA_MASK;private static final#int#DCM_4444_BLUE_MASK;private static final#int#DCM_4444_GREEN_MASK;private static final#int#DCM_4444_RED_MASK;private static final#int#DCM_555X_BLUE_MASK;private static final#int#DCM_555X_GREEN_MASK;private static final#int#DCM_555X_RED_MASK;private static final#int#DCM_ARGBBM_ALPHA_MASK;private static final#int#DCM_ARGBBM_BLUE_MASK;private static final#int#DCM_ARGBBM_GREEN_MASK;private static final#int#DCM_ARGBBM_RED_MASK;private static final#int#DCM_RGBX_BLUE_MASK;private static final#int#DCM_RGBX_GREEN_MASK;private static final#int#DCM_RGBX_RED_MASK;private static#[Lsun.java2d.loops.RenderLoops;#loopcache;private static#[Lsun.java2d.loops.SurfaceType;#typecache;private#sun.awt.image.BufferedImageGraphicsConfig#graphicsConfig".equals(fields(c)), "fields");
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
