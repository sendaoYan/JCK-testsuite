/*
 * @test
 * @summary sun.font.TextLineComponent conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main TextLineComponentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TextLineComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.TextLineComponent", false, TextLineComponentTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.TextLineComponent".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#caretAtOffsetIsValid(int);public abstract#boolean#isSimple();public abstract#float#getAdvance();public abstract#float#getAdvanceBetween(int,int);public abstract#float#getCharAdvance(int);public abstract#float#getCharX(int);public abstract#float#getCharY(int);public abstract#int#getLineBreakIndex(int,float);public abstract#int#getNumCharacters();public abstract#int#getNumJustificationInfos();public abstract#java.awt.Rectangle#getPixelBounds(java.awt.font.FontRenderContext,float,float);public abstract#java.awt.Shape#getOutline(float,float);public abstract#java.awt.geom.AffineTransform#getBaselineTransform();public abstract#java.awt.geom.Rectangle2D#getCharVisualBounds(int);public abstract#java.awt.geom.Rectangle2D#getItalicBounds();public abstract#java.awt.geom.Rectangle2D#getLogicalBounds();public abstract#java.awt.geom.Rectangle2D#getVisualBounds();public abstract#sun.font.CoreMetrics#getCoreMetrics();public abstract#sun.font.TextLineComponent#applyJustificationDeltas([F,int,[Z);public abstract#sun.font.TextLineComponent#getSubset(int,int,int);public abstract#void#draw(java.awt.Graphics2D,float,float);public abstract#void#getJustificationInfos([Ljava.awt.font.GlyphJustificationInfo;,int,int,int)".equals(methods(c)), "methods");
        check("public static final#int#LEFT_TO_RIGHT;public static final#int#RIGHT_TO_LEFT;public static final#int#UNCHANGED".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 0, "ctors");
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
