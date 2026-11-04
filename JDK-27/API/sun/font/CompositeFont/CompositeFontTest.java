/*
 * @test
 * @summary sun.font.CompositeFont conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main CompositeFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CompositeFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.CompositeFont", false, CompositeFontTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.CompositeFont".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.font.Font2D".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#sun.font.FontStrike#createStrike(sun.font.FontStrikeDesc);#void#replaceComponentFont(sun.font.PhysicalFont,sun.font.PhysicalFont);private#void#doDeferredInitialisation(int);protected#int#getValidatedGlyphCode(int);public#boolean#canDisplay(char);public#boolean#hasSupplementaryChars();public#boolean#isExcludedChar(int,int);public#boolean#isStdComposite();public#boolean#useAAForPtSize(int);public#int#getMissingGlyphCode();public#int#getNumGlyphs();public#int#getNumSlots();public#java.lang.String#toString();public#sun.font.CharToGlyphMapper#getMapper();public#sun.font.PhysicalFont#getSlotFont(int);public#void#getStyleMetrics(float,[F,int)".equals(methods(c)), "methods");
        check("#[I#exclusionRanges;#[I#maxIndices;#[Ljava.lang.String;#componentFileNames;#[Ljava.lang.String;#componentNames;#boolean#isStdComposite;#int#localeSlot;#int#numGlyphs;#int#numMetricsSlots;#int#numSlots;private#[Lsun.font.PhysicalFont;#components;private#[Z#deferredInitialisation".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 3, "ctors");
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
