/*
 * @test
 * @summary sun.font.GlyphLayout conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main GlyphLayoutTest
 */

import java.util.*;
import java.lang.reflect.*;

public class GlyphLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.GlyphLayout", false, GlyphLayoutTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.GlyphLayout".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#init(int);private#void#nextEngineRecord(int,int,int,sun.font.Font2D,int);public static#sun.font.GlyphLayout#get();public static#void#done(sun.font.GlyphLayout);public#sun.font.StandardGlyphVector#layout(java.awt.Font,java.awt.font.FontRenderContext,[C,int,int,int,sun.font.StandardGlyphVector)".equals(methods(c)), "methods");
        check("private static volatile#sun.font.GlyphLayout#cache;private#[F#_mat;private#int#_ercount;private#int#_offset;private#int#_typo_flags;private#java.awt.geom.Point2D$Float#_pt;private#java.util.ArrayList#_erecords;private#sun.font.FontRunIterator#_fontRuns;private#sun.font.FontStrikeDesc#_sd;private#sun.font.GlyphLayout$GVData#_gvdata;private#sun.font.ScriptRun#_scriptRuns;private#sun.font.TextRecord#_textRecord".equals(fields(c)), "fields");
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
