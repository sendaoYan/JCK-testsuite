/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.xr.MaskTileManager conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.xr:+open
 * @run main MaskTileManagerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MaskTileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.xr.MaskTileManager", false, MaskTileManagerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.xr.MaskTileManager".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#limitRectCoords(sun.java2d.xr.GrowableRectArray,int);protected#void#allocTiles(sun.java2d.xr.DirtyRegion);protected#void#compositeSingleTile(sun.java2d.xr.XRSurfaceData,sun.java2d.xr.MaskTile,sun.java2d.xr.DirtyRegion,boolean,int,int,sun.java2d.xr.XRColor);protected#void#tileRects();public#int#uploadMask(int,int,int,int,[B);public#sun.java2d.xr.MaskTile#getMainTile();public#void#clearUploadMask(int,int,int);public#void#fillMask(sun.java2d.xr.XRSurfaceData)".equals(methods(c)), "methods");
        check("#int#allocatedTiles;#int#maskPicture;#int#maskPixmap;#int#xTiles;#int#yTiles;#java.util.ArrayList#tileList;#long#maskGC;#sun.java2d.xr.MaskTile#mainTile;#sun.java2d.xr.XRBackend#con;#sun.java2d.xr.XRCompositeManager#xrMgr;public static final#int#MASK_SIZE".equals(fields(c)), "fields");
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
