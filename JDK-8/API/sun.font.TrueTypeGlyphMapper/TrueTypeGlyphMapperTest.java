/*
 * @test
 * @summary sun.font.TrueTypeGlyphMapper conformance: reflection (no init, native/display-heavy).
 * @run main TrueTypeGlyphMapperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TrueTypeGlyphMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.TrueTypeGlyphMapper", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.TrueTypeGlyphMapper".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.font.CharToGlyphMapper".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#hasSupplementaryChars();private final#char#remapJAChar(char);private final#int#remapJAIntChar(int);private#char#getGlyphFromCMAP(int);private#void#handleBadCMAP();public#boolean#charsToGlyphsNS(int,[C,[I);public#int#charToGlyph(char);public#int#charToGlyph(int);public#int#getNumGlyphs();public#void#charsToGlyphs(int,[C,[I);public#void#charsToGlyphs(int,[I,[I)".equals(methods(c)), "methods");
        check("#int#numGlyphs;#sun.font.CMap#cmap;#sun.font.TrueTypeFont#font;private final#boolean#needsJAremapping;private#boolean#remapJAWaveDash;static final#boolean#isJAlocale;static final#char#JA_FULLWIDTH_TILDE_CHAR;static final#char#JA_WAVE_DASH_CHAR;static final#char#JA_YEN;static final#char#REVERSE_SOLIDUS".equals(fields(c)), "fields");
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
