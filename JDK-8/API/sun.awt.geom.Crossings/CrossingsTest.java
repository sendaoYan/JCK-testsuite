/*
 * @test
 * @summary sun.awt.geom.Crossings conformance: reflection (no init, native/display-heavy).
 * @run main CrossingsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CrossingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.geom.Crossings", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.geom.Crossings".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#boolean#covers(double,double);public abstract#void#record(double,double,int);public final#boolean#isEmpty();public final#double#getXHi();public final#double#getXLo();public final#double#getYHi();public final#double#getYLo();public static#sun.awt.geom.Crossings#findCrossings(java.awt.geom.PathIterator,double,double,double,double);public static#sun.awt.geom.Crossings#findCrossings(java.util.Vector,double,double,double,double);public#boolean#accumulateCubic(double,double,[D);public#boolean#accumulateLine(double,double,double,double);public#boolean#accumulateLine(double,double,double,double,int);public#boolean#accumulateQuad(double,double,[D);public#void#print()".equals(methods(c)), "methods");
        check("#[D#yranges;#double#xhi;#double#xlo;#double#yhi;#double#ylo;#int#limit;private#java.util.Vector#tmp;public static final#boolean#debug".equals(fields(c)), "fields");
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
