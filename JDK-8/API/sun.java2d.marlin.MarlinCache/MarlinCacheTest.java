/*
 * @test
 * @summary sun.java2d.marlin.MarlinCache conformance: reflection (no init, native/display-heavy).
 * @run main MarlinCacheTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MarlinCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.MarlinCache", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.MarlinCache".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.marlin.MarlinConst".equals(interfaces(c)), "interfaces");
        check("#int#alphaSumInTile(int);#long#startRLERow(int,int,int);#void#clearAARow(int);#void#copyAARowNoRLE([I,int,int,int);#void#copyAARowRLE_WithBlockFlags([I,[I,int,int,int);#void#dispose();#void#init(int,int,int,int,int);#void#resetTileLine(int);private static#[B#buildAlphaMap(int);private#void#expandRowAAChunk(long);private#void#touchTile(int,int,int,int,[I);public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("#[I#touchedTile;#boolean#useRLE;#int#bboxX0;#int#bboxX1;#int#bboxY0;#int#bboxY1;#int#tileMax;#int#tileMin;#long#rowAAChunkPos;final#[I#rowAAEnc;final#[I#rowAAx0;final#[I#rowAAx1;final#[I#touchedTile_initial;final#[J#rowAAChunkIndex;final#[J#rowAALen;final#[J#rowAAPos;final#sun.java2d.marlin.OffHeapArray#rowAAChunk;final#sun.java2d.marlin.RendererContext#rdrCtx;static final#[B#ALPHA_MAP;static final#boolean#FORCE_NO_RLE;static final#boolean#FORCE_RLE;static final#int#RLE_MAX_WIDTH;static final#int#RLE_MIN_WIDTH;static final#long#INITIAL_CHUNK_ARRAY;static final#sun.java2d.marlin.OffHeapArray#ALPHA_MAP_UNSAFE".equals(fields(c)), "fields");
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
