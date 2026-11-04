/*
 * @test
 * @summary sun.font.FontScaler conformance: reflection (no init, native/display-heavy).
 * @run main FontScalerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontScalerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontScaler", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FontScaler".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.DisposerRecord".equals(interfaces(c)), "interfaces");
        check("abstract#float#getGlyphAdvance(long,int);abstract#int#getGlyphCode(char);abstract#int#getMissingGlyphCode();abstract#int#getNumGlyphs();abstract#java.awt.geom.GeneralPath#getGlyphOutline(long,int,float,float);abstract#java.awt.geom.GeneralPath#getGlyphVectorOutline(long,[I,int,float,float);abstract#java.awt.geom.Point2D$Float#getGlyphPoint(long,int,int);abstract#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(long,int);abstract#long#createScalerContext([D,int,int,float,float,boolean);abstract#long#getGlyphImage(long,int);abstract#long#getLayoutTableCache();abstract#long#getUnitsPerEm();abstract#sun.font.StrikeMetrics#getFontMetrics(long);abstract#void#getGlyphMetrics(long,int,java.awt.geom.Point2D$Float);abstract#void#invalidateScalerContext(long);public static synchronized#sun.font.FontScaler#getNullScaler();public static#sun.font.FontScaler#getScaler(sun.font.Font2D,int,boolean,int);public#void#dispose();public#void#disposeScaler()".equals(methods(c)), "methods");
        check("private static#java.lang.reflect.Constructor#scalerConstructor;private static#sun.font.FontScaler#nullScaler;protected#boolean#disposed;protected#java.lang.ref.WeakReference#font;protected#long#nativeScaler".equals(fields(c)), "fields");
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
