/*
 * @test
 * @summary sun.java2d.pipe.hw.ContextCapabilities conformance: reflection (no init, native/display-heavy).
 * @run main ContextCapabilitiesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ContextCapabilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.hw.ContextCapabilities", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.hw.ContextCapabilities".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#int#getCaps();public#java.lang.String#getAdapterId();public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("protected final#int#caps;protected final#java.lang.String#adapterId;protected static final#int#FIRST_PRIVATE_CAP;public static final#int#CAPS_EMPTY;public static final#int#CAPS_MULTITEXTURE;public static final#int#CAPS_PS20;public static final#int#CAPS_PS30;public static final#int#CAPS_RT_PLAIN_ALPHA;public static final#int#CAPS_RT_TEXTURE_ALPHA;public static final#int#CAPS_RT_TEXTURE_OPAQUE;public static final#int#CAPS_TEXNONPOW2;public static final#int#CAPS_TEXNONSQUARE".equals(fields(c)), "fields");
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
