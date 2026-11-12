/*
 * @test
 * @summary sun.font.NativeFont conformance: reflection (no init, native/display-heavy).
 * @run main NativeFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class NativeFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.NativeFont", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.NativeFont".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check("sun.font.PhysicalFont".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[B#getPlatformNameBytes(int);#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(long,int);#sun.font.FontStrike#createStrike(sun.font.FontStrikeDesc);#sun.font.PhysicalFont#getDelegateFont();#void#getGlyphMetrics(long,int,java.awt.geom.Point2D$Float);native#float#getGlyphAdvance(long,int);native#long#getGlyphImage(long,int);native#long#getGlyphImageNoDefault(long,int);native#sun.font.StrikeMetrics#getFontMetrics(long);private native#int#countGlyphs([B,int);private static native#boolean#fontExists([B);private static native#boolean#haveBitmapFonts([B);private#void#initNames();public static#boolean#fontExists(java.lang.String);public#int#getNumGlyphs();public#java.awt.geom.GeneralPath#getGlyphOutline(long,int,float,float);public#java.awt.geom.GeneralPath#getGlyphVectorOutline(long,[I,int,float,float);public#java.awt.geom.Rectangle2D#getMaxCharBounds(java.awt.font.FontRenderContext);public#java.lang.String#toString();public#sun.font.CharToGlyphMapper#getMapper();static#boolean#hasExternalBitmaps(java.lang.String)".equals(methods(c)), "methods");
        check("#boolean#isBitmapDelegate;#java.lang.String#encoding;#sun.font.PhysicalFont#delegateFont;private#int#numGlyphs".equals(fields(c)), "fields");
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
