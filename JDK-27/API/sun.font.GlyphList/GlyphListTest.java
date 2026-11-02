/*
 * @test
 * @summary sun.font.GlyphList conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main GlyphListTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GlyphListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.GlyphList", false, GlyphListTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.GlyphList".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#ensureCapacity(int);private#boolean#mapChars(sun.java2d.loops.FontInfo,int);private#void#fillBounds([I,int);public static#boolean#canContainColorGlyphs();public static#sun.font.GlyphList#getInstance();public#[B#getGrayBits();public#[F#getPositions();public#[I#getBounds(int);public#[I#getMetrics();public#[J#getImages();public#boolean#isColorGlyph(int);public#boolean#isRGBOrder();public#boolean#isSubPixPos();public#boolean#setFromChars(sun.java2d.loops.FontInfo,[C,int,int,float,float);public#boolean#setFromString(sun.java2d.loops.FontInfo,java.lang.String,float,float);public#boolean#usePositions();public#float#getX();public#float#getY();public#int#getNumGlyphs();public#java.lang.Object#getStrike();public#sun.java2d.SurfaceData#getColorGlyphData();public#void#dispose();public#void#setFromGlyphVector(sun.java2d.loops.FontInfo,java.awt.font.GlyphVector,float,float);public#void#setGlyphIndex(int);public#void#startGlyphIteration()".equals(methods(c)), "methods");
        check("#[B#graybits;#[C#chData;#[F#positions;#[I#glyphData;#[I#metrics;#[J#images;#boolean#lcdRGBOrder;#boolean#lcdSubPixPos;#boolean#usePositions;#float#gposx;#float#gposy;#float#x;#float#y;#int#glyphindex;#int#len;#int#maxLen;#int#maxPosLen;#java.lang.Object#strikelist;private static final#int#DEFAULT_LENGTH;private static final#int#MAXGRAYLENGTH;private static final#int#MINGRAYLENGTH;private static final#java.util.concurrent.atomic.AtomicBoolean#inUse;private static final#sun.font.GlyphList#reusableGL;private#sun.font.ColorGlyphSurfaceData#glyphSurfaceData".equals(fields(c)), "fields");
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
