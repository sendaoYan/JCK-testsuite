/*
 * @test
 * @summary sun.font.StandardGlyphVector conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main StandardGlyphVectorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StandardGlyphVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.StandardGlyphVector", false, StandardGlyphVectorTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.StandardGlyphVector".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.font.GlyphVector".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[I#getValidatedGlyphs([I);#boolean#needsPositions([D);#java.lang.Object#setupGlyphImages([J,[F,[D);#java.lang.StringBuffer#appendString(java.lang.StringBuffer);private static#boolean#equalNonTranslateTX(java.awt.geom.AffineTransform,java.awt.geom.AffineTransform);private static#boolean#matchTX([D,java.awt.geom.AffineTransform);private static#java.awt.geom.AffineTransform#getNonTranslateTX(java.awt.geom.AffineTransform);private#[F#internalGetGlyphPositions(int,int,int,[F);private#float#getTracking(java.awt.Font);private#java.awt.Rectangle#getGlyphsPixelBounds(java.awt.font.FontRenderContext,float,float,int,int);private#java.awt.Shape#getGlyphsOutline(int,int,float,float);private#java.awt.geom.Rectangle2D#getGlyphOutlineBounds(int);private#sun.font.StandardGlyphVector$GlyphStrike#getDefaultStrike();private#sun.font.StandardGlyphVector$GlyphStrike#getGlyphStrike(int);private#void#addFlags(int);private#void#clearCaches();private#void#clearCaches(int);private#void#clearFlags(int);private#void#init(java.awt.Font,[C,int,int,java.awt.font.FontRenderContext,int);private#void#initFontData();private#void#initPositions();private#void#resetDTX(java.awt.geom.AffineTransform);private#void#setDTX(java.awt.geom.AffineTransform);private#void#setFRCTX();private#void#setRenderTransform([D);public static#sun.font.StandardGlyphVector#getStandardGV(java.awt.font.GlyphVector,sun.java2d.loops.FontInfo);public#[F#getGlyphInfo();public#[F#getGlyphPositions([F);public#[F#getGlyphPositions(int,int,[F);public#[I#getGlyphCharIndices(int,int,[I);public#[I#getGlyphCodes(int,int,[I);public#[Ljava.awt.geom.AffineTransform;#getGlyphTransforms();public#[Ljava.awt.geom.AffineTransform;#getGlyphTransforms(int,int,[Ljava.awt.geom.AffineTransform;);public#boolean#equals(java.awt.font.GlyphVector);public#boolean#equals(java.lang.Object);public#int#getGlyphCharIndex(int);public#int#getGlyphCode(int);public#int#getLayoutFlags();public#int#getNumGlyphs();public#int#hashCode();public#java.awt.Font#getFont();public#java.awt.Rectangle#getGlyphPixelBounds(int,java.awt.font.FontRenderContext,float,float);public#java.awt.Rectangle#getPixelBounds(java.awt.font.FontRenderContext,float,float);public#java.awt.Shape#getGlyphLogicalBounds(int);public#java.awt.Shape#getGlyphOutline(int);public#java.awt.Shape#getGlyphOutline(int,float,float);public#java.awt.Shape#getGlyphVisualBounds(int);public#java.awt.Shape#getOutline();public#java.awt.Shape#getOutline(float,float);public#java.awt.font.FontRenderContext#getFontRenderContext();public#java.awt.font.GlyphJustificationInfo#getGlyphJustificationInfo(int);public#java.awt.font.GlyphMetrics#getGlyphMetrics(int);public#java.awt.geom.AffineTransform#getGlyphTransform(int);public#java.awt.geom.Point2D#getGlyphPosition(int);public#java.awt.geom.Rectangle2D#getLogicalBounds();public#java.awt.geom.Rectangle2D#getVisualBounds();public#java.lang.Object#clone();public#java.lang.String#toString();public#sun.font.StandardGlyphVector#copy();public#void#initGlyphVector(java.awt.Font,java.awt.font.FontRenderContext,[I,[F,[I,int);public#void#performDefaultLayout();public#void#setGlyphPosition(int,java.awt.geom.Point2D);public#void#setGlyphPositions([F);public#void#setGlyphPositions([F,int,int,int);public#void#setGlyphTransform(int,java.awt.geom.AffineTransform);public#void#setGlyphTransforms([Ljava.awt.geom.AffineTransform;);public#void#setGlyphTransforms([Ljava.awt.geom.AffineTransform;,int,int,int)".equals(methods(c)), "methods");
        check("private static final#int#UNINITIALIZED_FLAGS;private#[F#positions;private#[I#charIndices;private#[I#glyphs;private#[I#userGlyphs;private#int#flags;private#java.awt.Font#font;private#java.awt.font.FontRenderContext#frc;private#java.awt.geom.AffineTransform#dtx;private#java.awt.geom.AffineTransform#frctx;private#java.awt.geom.AffineTransform#ftx;private#java.awt.geom.AffineTransform#invdtx;private#java.lang.ref.SoftReference#fsref;private#java.lang.ref.SoftReference#lbcacheRef;private#java.lang.ref.SoftReference#vbcacheRef;private#sun.font.Font2D#font2D;private#sun.font.StandardGlyphVector$GlyphTransformInfo#gti;public static final#int#FLAG_USES_ALTERNATE_ORIENTATION;public static final#int#FLAG_USES_VERTICAL_BASELINE;public static final#int#FLAG_USES_VERTICAL_METRICS".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 7, "ctors");
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
