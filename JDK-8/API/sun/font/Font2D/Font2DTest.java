/*
 * @test
 * @summary sun.font.Font2D conformance: reflection (no init, native/display-heavy).
 * @run main Font2DTest
 */

import java.util.*;
import java.lang.reflect.*;

public class Font2DTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.Font2D", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.Font2D".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#supportsEncoding(java.lang.String);#int#getRank();#sun.font.FontStrike#getStrike(sun.font.FontStrikeDesc);#void#setRank(int);#void#setUseWeakRefs(boolean,int);#void#updateLastStrikeRef(sun.font.FontStrike);abstract#sun.font.CharToGlyphMapper#getMapper();abstract#sun.font.FontStrike#createStrike(sun.font.FontStrikeDesc);private#sun.font.FontStrike#getStrike(sun.font.FontStrikeDesc,boolean);protected#[B#getTableBytes(int);protected#int#getValidatedGlyphCode(int);protected#long#getLayoutTableCache();protected#long#getUnitsPerEm();protected#void#setStyle();public#boolean#canDisplay(char);public#boolean#canDisplay(int);public#boolean#canDoStyle(int);public#boolean#hasSupplementaryChars();public#boolean#useAAForPtSize(int);public#byte#getBaselineFor(char);public#float#getItalicAngle(java.awt.Font,java.awt.geom.AffineTransform,java.lang.Object,java.lang.Object);public#int#charToGlyph(int);public#int#getMissingGlyphCode();public#int#getNumGlyphs();public#int#getStyle();public#int#getWeight();public#int#getWidth();public#java.lang.String#getFamilyName(java.util.Locale);public#java.lang.String#getFontName(java.util.Locale);public#java.lang.String#getPostscriptName();public#sun.font.FontStrike#getStrike(java.awt.Font);public#sun.font.FontStrike#getStrike(java.awt.Font,java.awt.font.FontRenderContext);public#sun.font.FontStrike#getStrike(java.awt.Font,java.awt.geom.AffineTransform,int,int);public#sun.font.FontStrike#getStrike(java.awt.Font,java.awt.geom.AffineTransform,java.awt.geom.AffineTransform,int,int);public#void#getFontMetrics(java.awt.Font,java.awt.font.FontRenderContext,[F);public#void#getFontMetrics(java.awt.Font,java.awt.geom.AffineTransform,java.lang.Object,java.lang.Object,[F);public#void#getStyleMetrics(float,[F,int)".equals(methods(c)), "methods");
        check("private static final#[Ljava.lang.String;#boldItalicNames;private static final#[Ljava.lang.String;#boldNames;private static final#[Ljava.lang.String;#italicNames;private static final#java.awt.font.FontRenderContext#DEFAULT_FRC;private#boolean#useWeak;private#int#strikeCacheMax;protected#int#fontRank;protected#int#style;protected#java.lang.String#familyName;protected#java.lang.String#fullName;protected#java.lang.ref.Reference#lastFontStrike;protected#java.util.concurrent.ConcurrentHashMap#strikeCache;protected#sun.font.CharToGlyphMapper#mapper;protected#sun.font.FontFamily#family;public static final#int#DEFAULT_RANK;public static final#int#FONT_CONFIG_RANK;public static final#int#FWEIGHT_BOLD;public static final#int#FWEIGHT_NORMAL;public static final#int#FWIDTH_NORMAL;public static final#int#JRE_RANK;public static final#int#NATIVE_RANK;public static final#int#TTF_RANK;public static final#int#TYPE1_RANK;public static final#int#UNKNOWN_RANK;public#sun.font.Font2DHandle#handle".equals(fields(c)), "fields");
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
