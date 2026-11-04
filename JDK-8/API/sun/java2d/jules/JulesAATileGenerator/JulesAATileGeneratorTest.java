/*
 * @test
 * @summary sun.java2d.jules.JulesAATileGenerator conformance: reflection (no init, native/display-heavy).
 * @run main JulesAATileGeneratorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class JulesAATileGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.jules.JulesAATileGenerator", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.jules.JulesAATileGenerator".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.AATileGenerator".equals(interfaces(c)), "interfaces");
        check("private static native#long#rasterizeTrapezoidsNative(long,[I,[I,int,[B,int,int);private static native#void#freePixmanImgPtr(long);private#void#bucketSortTraps();private#void#calculateArea([I);protected#boolean#doTrapsCoverTile(sun.java2d.xr.GrowableIntArray,int);protected#int#getXPos(int);protected#int#getYPos(int);protected#sun.java2d.jules.JulesTile#rasterizeTile(int,sun.java2d.jules.JulesTile);public#int#getTileCount();public#int#getTileHeight();public#int#getTileWidth();public#int#getTypicalAlpha();public#sun.java2d.jules.TileTrapContainer#getTrapContainer(int);public#void#calculateTypicalAlpha();public#void#dispose();public#void#getAlpha([B,int,int);public#void#nextTile()".equals(methods(c)), "methods");
        check("#[Lsun.java2d.jules.TileTrapContainer;#tiledTrapArray;#boolean#threaded;#int#bottom;#int#currTilePos;#int#height;#int#left;#int#leftFP;#int#rasterTileCnt;#int#right;#int#tileCnt;#int#tilesX;#int#tilesY;#int#top;#int#topFP;#int#width;#sun.java2d.jules.IdleTileCache#tileCache;#sun.java2d.jules.JulesTile#mainTile;#sun.java2d.jules.TileWorker#worker;#sun.java2d.jules.TrapezoidList#traps;static final#boolean#ENABLE_THREADING;static final#int#CPU_CNT;static final#int#THREAD_BEGIN;static final#int#THREAD_MIN;static final#int#TILE_SIZE;static final#int#TILE_SIZE_FP;static final#java.util.concurrent.ExecutorService#rasterThreadPool".equals(fields(c)), "fields");
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
