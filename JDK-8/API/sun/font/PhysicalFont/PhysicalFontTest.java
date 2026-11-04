/*
 * @test
 * @summary sun.font.PhysicalFont conformance: reflection (no init, native/display-heavy).
 * @run main PhysicalFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PhysicalFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.PhysicalFont", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.PhysicalFont".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.font.Font2D".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.awt.geom.Point2D$Float#getGlyphPoint(long,int,int);abstract#float#getGlyphAdvance(long,int);abstract#java.awt.geom.GeneralPath#getGlyphOutline(long,int,float,float);abstract#java.awt.geom.GeneralPath#getGlyphVectorOutline(long,[I,int,float,float);abstract#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(long,int);abstract#long#getGlyphImage(long,int);abstract#sun.font.StrikeMetrics#getFontMetrics(long);abstract#void#getGlyphMetrics(long,int,java.awt.geom.Point2D$Float);public#boolean#equals(java.lang.Object);public#int#hashCode()".equals(methods(c)), "methods");
        check("protected#java.lang.Object#nativeNames;protected#java.lang.String#platName".equals(fields(c)), "fields");
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
