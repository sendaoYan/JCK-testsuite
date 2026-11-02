/*
 * @test
 * @summary sun.font.FontDesignMetrics conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main FontDesignMetricsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontDesignMetricsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontDesignMetrics", false, FontDesignMetricsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.FontDesignMetrics".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.awt.FontMetrics".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#java.awt.font.FontRenderContext#getDefaultFrc();private#float#getLatinCharWidth(char);private#float#handleCharWidth(int);private#void#initAdvCache();private#void#initMatrixAndMetrics();private#void#readObject(java.io.ObjectInputStream);private#void#writeObject(java.io.ObjectOutputStream);public static#sun.font.FontDesignMetrics#getMetrics(java.awt.Font);public static#sun.font.FontDesignMetrics#getMetrics(java.awt.Font,java.awt.font.FontRenderContext);public#[I#getWidths();public#int#charWidth(char);public#int#charWidth(int);public#int#charsWidth([C,int,int);public#int#getAscent();public#int#getDescent();public#int#getHeight();public#int#getLeading();public#int#getMaxAdvance();public#int#stringWidth(java.lang.String);public#java.awt.font.FontRenderContext#getFontRenderContext();public#java.awt.geom.Rectangle2D#getSimpleBounds([C,int,int)".equals(methods(c)), "methods");
        check("private static final#[Lsun.font.FontDesignMetrics;#recentMetrics;private static final#float#UNKNOWN_WIDTH;private static final#int#CURRENT_VERSION;private static final#int#MAXRECENT;private static final#java.util.concurrent.ConcurrentHashMap#metricsCache;private static final#long#serialVersionUID;private static#float#roundingUpValue;private static#int#recentIndex;private static#java.awt.font.FontRenderContext#DEFAULT_FRC;private transient#[D#devmatrix;private transient#[F#advCache;private transient#int#height;private transient#java.awt.font.FontRenderContext#frc;private transient#sun.font.FontStrike#fontStrike;private#[D#matrix;private#[I#cache;private#boolean#isAntiAliased;private#boolean#usesFractionalMetrics;private#float#ascent;private#float#descent;private#float#leading;private#float#maxAdvance;private#int#serVersion;private#java.awt.Font#font;private#java.awt.geom.AffineTransform#frcTx".equals(fields(c)), "fields");
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
