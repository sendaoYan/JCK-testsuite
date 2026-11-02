/*
 * @test
 * @summary sun.font.PhysicalStrike conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main PhysicalStrikeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PhysicalStrikeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.PhysicalStrike", false, PhysicalStrikeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.PhysicalStrike".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.font.FontStrike".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#float#getCodePointAdvance(int);#int#getSlot0GlyphImagePtrs([I,[J,int);#java.awt.geom.Point2D$Float#getCharMetrics(char);#java.awt.geom.Point2D$Float#getGlyphPoint(int,int);#sun.font.StrikeMetrics#getFontMetrics();protected#void#adjustPoint(java.awt.geom.Point2D$Float);public#int#getNumGlyphs()".equals(methods(c)), "methods");
        check("#java.util.concurrent.ConcurrentHashMap#glyphPointMapCache;private#sun.font.PhysicalFont#physicalFont;protected static final#int#complexTX;protected#[I#intGlyphImages;protected#[J#longGlyphImages;protected#boolean#getImageWithAdvance;protected#long#pScalerContext;protected#sun.font.CharToGlyphMapper#mapper;static final#long#INTMASK;static#boolean#longAddresses".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
