/*
 * @test
 * @summary sun.font.LayoutPathImpl.SegmentPath conformance: reflection (no init, native/display-heavy).
 * @run main SegmentPathTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SegmentPathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.LayoutPathImpl$SegmentPath", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.LayoutPathImpl$SegmentPath".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.font.LayoutPathImpl".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#double#getClosedAdvance(double,boolean);private#int#getSegmentIndexForAdvance(double,boolean);private#int#locateAndGetIndex(java.awt.geom.Point2D,boolean,java.awt.geom.Point2D);private#void#calcoffset(int,boolean,java.awt.geom.Point2D);private#void#map(int,double,double,java.awt.geom.Point2D);public static transient#sun.font.LayoutPathImpl$SegmentPath#get(sun.font.LayoutPathImpl$EndType,[D);public#boolean#pointToPath(java.awt.geom.Point2D,java.awt.geom.Point2D);public#double#end();public#double#length();public#double#start();public#java.awt.Shape#mapShape(java.awt.Shape);public#java.lang.String#toString();public#void#pathToPoint(java.awt.geom.Point2D,boolean,java.awt.geom.Point2D)".equals(methods(c)), "methods");
        check("#sun.font.LayoutPathImpl$EndType#etype;private#[D#data".equals(fields(c)), "fields");
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
