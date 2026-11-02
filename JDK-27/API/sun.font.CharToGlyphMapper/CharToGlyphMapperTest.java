/*
 * @test
 * @summary sun.font.CharToGlyphMapper conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main CharToGlyphMapperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CharToGlyphMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.CharToGlyphMapper", false, CharToGlyphMapperTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.CharToGlyphMapper".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#charsToGlyphsNS(int,[C,[I);public abstract#int#charToGlyphRaw(int);public abstract#int#getNumGlyphs();public abstract#void#charsToGlyphs(int,[C,[I);public abstract#void#charsToGlyphs(int,[I,[I);public static#boolean#isVariationSelector(int);public#boolean#canDisplay(char);public#boolean#canDisplay(int);public#int#charToGlyph(char);public#int#charToGlyph(int);public#int#charToVariationGlyph(int,int);public#int#charToVariationGlyphRaw(int,int);public#int#getMissingGlyphCode()".equals(methods(c)), "methods");
        check("protected#int#missingGlyph;public static final#int#HI_SURROGATE_END;public static final#int#HI_SURROGATE_START;public static final#int#INVISIBLE_GLYPHS;public static final#int#INVISIBLE_GLYPH_ID;public static final#int#LO_SURROGATE_END;public static final#int#LO_SURROGATE_START;public static final#int#UNINITIALIZED_GLYPH;public static final#int#VSS_END;public static final#int#VSS_START;public static final#int#VS_END;public static final#int#VS_START".equals(fields(c)), "fields");
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
