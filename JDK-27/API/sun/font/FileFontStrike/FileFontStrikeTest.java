/*
 * @test
 * @summary sun.font.FileFontStrike conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main FileFontStrikeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FileFontStrikeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FileFontStrike", false, FileFontStrikeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.FileFontStrike".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.font.PhysicalStrike".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#float#getCodePointAdvance(int);#float#getGlyphAdvance(int);#int#getSlot0GlyphImagePtrs([I,[J,int);#java.awt.geom.GeneralPath#getGlyphOutline(int,float,float);#java.awt.geom.Point2D$Float#getCharMetrics(char);#java.awt.geom.Point2D$Float#getGlyphMetrics(int);#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(int);#long#getCachedGlyphPtr(int);#long#getGlyphImageFromNative(int);#long#getGlyphImageFromWindows(int);#long#getGlyphImageFromX11(int);#long#getGlyphImagePtr(int);#sun.font.StrikeMetrics#getFontMetrics();#void#getGlyphImageBounds(int,java.awt.geom.Point2D$Float,java.awt.Rectangle);#void#getGlyphImagePtrs([I,[J,int);private native#long#_getGlyphImageFromWindows(java.lang.String,int,int,int,boolean,int);private synchronized#long#setCachedGlyphPtr(int,long);private synchronized#void#initGlyphCache();private#float#getGlyphAdvance(int,boolean);private#int#getGlyphImageMinX(long,int);private#java.awt.geom.Point2D$Float#getGlyphMetrics(int,boolean);private#long#getCachedGlyphPtrInternal(int);private#long#setCachedGlyphPtrInternal(int,long);protected#void#adjustPoint(java.awt.geom.Point2D$Float);public#int#getNumGlyphs();public#java.awt.geom.Rectangle2D#getOutlineBounds(int)".equals(methods(c)), "methods");
        check("#[Lsun.font.NativeStrike;#nativeStrikes;#boolean#useNatives;#java.awt.geom.AffineTransform#invertDevTx;#java.lang.ref.SoftReference#glyphMetricsMapRef;#java.util.concurrent.ConcurrentHashMap#boundsMap;private static final#int#INTARRAY;private static final#int#LONGARRAY;private static final#int#SEGINTARRAY;private static final#int#SEGLONGARRAY;private static final#int#SEGSHIFT;private static final#int#SEGSIZE;private static final#int#UNINITIALISED;private volatile#int#glyphCacheFormat;private#[F#horizontalAdvances;private#[[F#segHorizontalAdvances;private#[[I#segIntGlyphImages;private#[[J#segLongGlyphImages;private#boolean#segmentedCache;private#int#intPtSize;private#java.lang.ref.WeakReference#outlineMapRef;private#sun.font.FileFont#fileFont;static final#int#INVISIBLE_GLYPHS;static final#int#MAX_IMAGE_SIZE".equals(fields(c)), "fields");
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
