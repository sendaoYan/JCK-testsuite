/*
 * @test
 * @summary sun.awt.RepaintArea conformance: reflection (no init, native/display-heavy).
 * @run main RepaintAreaTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RepaintAreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.RepaintArea", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.RepaintArea".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private synchronized#sun.awt.RepaintArea#cloneAndReset();protected#void#paintComponent(java.awt.Component,java.awt.Graphics);protected#void#updateComponent(java.awt.Component,java.awt.Graphics);public synchronized#void#add(java.awt.Rectangle,int);public synchronized#void#constrain(int,int,int,int);public synchronized#void#subtract(int,int,int,int);public#boolean#isEmpty();public#java.lang.String#toString();public#void#paint(java.lang.Object,boolean);static#boolean#subtract(java.awt.Rectangle,java.awt.Rectangle)".equals(methods(c)), "methods");
        check("private static final#int#HORIZONTAL;private static final#int#MAX_BENEFIT_RATIO;private static final#int#RECT_COUNT;private static final#int#UPDATE;private static final#int#VERTICAL;private#[Ljava.awt.Rectangle;#paintRects".equals(fields(c)), "fields");
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
