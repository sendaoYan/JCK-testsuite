/*
 * @test
 * @summary sun.awt.geom.AreaOp conformance: reflection (no init, native/display-heavy).
 * @run main AreaOpTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AreaOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.geom.AreaOp", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.geom.AreaOp".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#addEdges(java.util.Vector,java.util.Vector,int);private#java.util.Vector#pruneEdges(java.util.Vector);public abstract#int#classify(sun.awt.geom.Edge);public abstract#int#getState();public abstract#void#newRow();public static#boolean#obstructs(double,double,int);public static#void#finalizeSubCurves(java.util.Vector,java.util.Vector);public static#void#resolveLinks(java.util.Vector,java.util.Vector,java.util.Vector);public#java.util.Vector#calculate(java.util.Vector,java.util.Vector)".equals(methods(c)), "methods");
        check("private static#[Lsun.awt.geom.ChainEnd;#EmptyChainList;private static#[Lsun.awt.geom.CurveLink;#EmptyLinkList;private static#java.util.Comparator#YXTopComparator;public static final#int#CTAG_LEFT;public static final#int#CTAG_RIGHT;public static final#int#ETAG_ENTER;public static final#int#ETAG_EXIT;public static final#int#ETAG_IGNORE;public static final#int#RSTAG_INSIDE;public static final#int#RSTAG_OUTSIDE".equals(fields(c)), "fields");
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
