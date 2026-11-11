/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.font.XRGlyphCache conformance: reflection (no init, native/display-heavy).
 * @run main XRGlyphCacheTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRGlyphCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.XRGlyphCache", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.XRGlyphCache".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.font.GlyphDisposedListener".equals(interfaces(c)), "interfaces");
        check("private#void#freeGlyphs(sun.java2d.xr.GrowableIntArray);protected#[B#generateGlyphImageStream(java.util.List);protected#[Ljava.util.List;#seperateGlyphTypes(java.util.List,boolean);protected#boolean#containsLCDGlyphs(java.util.List);protected#int#getFreeGlyphID();protected#sun.font.XRGlyphCacheEntry#getEntryForPointer(long);protected#void#clearCache([Lsun.font.XRGlyphCacheEntry;);protected#void#uploadGlyphs([Lsun.font.XRGlyphCacheEntry;,java.util.ArrayList,sun.font.GlyphList,[I);public#[Lsun.font.XRGlyphCacheEntry;#cacheGlyphs(sun.font.GlyphList);public#void#glyphDisposed(java.util.ArrayList)".equals(methods(c)), "methods");
        check("#int#cachedPixels;#int#grayGlyphSet;#int#lcdGlyphSet;#int#nextID;#int#time;#java.util.ArrayList#freeGlyphIDs;#java.util.HashMap#cacheMap;#sun.java2d.xr.MutableInteger#tmp;#sun.java2d.xr.XRBackend#con;#sun.java2d.xr.XRCompositeManager#maskBuffer;static final#boolean#batchGlyphUpload;static final#int#MAX_CACHED_PIXELS".equals(fields(c)), "fields");
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
