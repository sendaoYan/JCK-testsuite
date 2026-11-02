/*
 * @test
 * @summary sun.font.FontStrikeDesc conformance: reflection (no init, native/display-heavy).
 * @run main FontStrikeDescTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontStrikeDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontStrikeDesc", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FontStrikeDesc".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#int#getAAHintIntVal(java.lang.Object,sun.font.Font2D,int);public static#int#getAAHintIntVal(sun.font.Font2D,java.awt.Font,java.awt.font.FontRenderContext);public static#int#getFMHintIntVal(java.lang.Object);public#boolean#equals(java.lang.Object);public#int#hashCode();public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("#int#aaHint;#int#fmHint;#int#style;#java.awt.geom.AffineTransform#devTx;#java.awt.geom.AffineTransform#glyphTx;private#int#hashCode;private#int#valuemask;static final#int#AA_LCD_H;static final#int#AA_LCD_V;static final#int#AA_ON;static final#int#FRAC_METRICS_ON;static final#int#FRAC_METRICS_SP".equals(fields(c)), "fields");
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
