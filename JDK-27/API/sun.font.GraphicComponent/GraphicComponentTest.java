/*
 * @test
 * @summary sun.font.GraphicComponent conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main GraphicComponentTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GraphicComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.GraphicComponent", false, GraphicComponentTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.GraphicComponent".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.font.Decoration$Label;sun.font.TextLineComponent".equals(interfaces(c)), "interfaces");
        check("private#void#initLocalOrdering([I,[B,int,int);public static#sun.font.CoreMetrics#createCoreMetrics(java.awt.font.GraphicAttribute);public#boolean#caretAtOffsetIsValid(int);public#boolean#isSimple();public#float#getAdvance();public#float#getAdvanceBetween(int,int);public#float#getCharAdvance(int);public#float#getCharX(int);public#float#getCharY(int);public#float#getItalicAngle();public#int#getLineBreakIndex(int,float);public#int#getNumCharacters();public#int#getNumJustificationInfos();public#java.awt.Rectangle#getPixelBounds(java.awt.font.FontRenderContext,float,float);public#java.awt.Shape#getOutline(float,float);public#java.awt.Shape#handleGetOutline(float,float);public#java.awt.geom.AffineTransform#getBaselineTransform();public#java.awt.geom.Rectangle2D#getCharVisualBounds(int);public#java.awt.geom.Rectangle2D#getItalicBounds();public#java.awt.geom.Rectangle2D#getLogicalBounds();public#java.awt.geom.Rectangle2D#getVisualBounds();public#java.awt.geom.Rectangle2D#handleGetCharVisualBounds(int);public#java.awt.geom.Rectangle2D#handleGetVisualBounds();public#java.lang.String#toString();public#sun.font.CoreMetrics#getCoreMetrics();public#sun.font.TextLineComponent#applyJustificationDeltas([F,int,[Z);public#sun.font.TextLineComponent#getSubset(int,int,int);public#void#draw(java.awt.Graphics2D,float,float);public#void#getJustificationInfos([Ljava.awt.font.GlyphJustificationInfo;,int,int,int);public#void#handleDraw(java.awt.Graphics2D,float,float)".equals(methods(c)), "methods");
        check("private#[B#levels;private#[I#charsLtoV;private#float#graphicAdvance;private#int#graphicCount;private#java.awt.font.GraphicAttribute#graphic;private#java.awt.geom.AffineTransform#baseTx;private#java.awt.geom.Rectangle2D#visualBounds;private#sun.font.CoreMetrics#cm;private#sun.font.Decoration#decorator;public static final#float#GRAPHIC_LEADING".equals(fields(c)), "fields");
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
