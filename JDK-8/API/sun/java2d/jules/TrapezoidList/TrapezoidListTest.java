/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.java2d.jules.TrapezoidList conformance: reflection (no init, native/display-heavy).
 * @run main TrapezoidListTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TrapezoidListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.jules.TrapezoidList", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.jules.TrapezoidList".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private final#int#getTrapStartAddresse(int);public final#[I#getTrapArray();public final#int#getBottom();public final#int#getBottom(int);public final#int#getLeft();public final#int#getP1XLeft(int);public final#int#getP1XRight(int);public final#int#getP1YLeft(int);public final#int#getP1YRight(int);public final#int#getP2XLeft(int);public final#int#getP2XRight(int);public final#int#getP2YLeft(int);public final#int#getP2YRight(int);public final#int#getRight();public final#int#getSize();public final#int#getTop();public final#int#getTop(int);public final#void#setSize(int)".equals(methods(c)), "methods");
        check("#[I#trapArray;public static final#int#TRAP_SIZE;public static final#int#TRAP_START_INDEX".equals(fields(c)), "fields");
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
