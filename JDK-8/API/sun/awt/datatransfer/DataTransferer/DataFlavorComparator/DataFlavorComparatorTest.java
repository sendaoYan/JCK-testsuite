/*
 * @test
 * @summary sun.awt.datatransfer.DataTransferer.DataFlavorComparator conformance: reflection (no init, native/display-heavy).
 * @run main DataFlavorComparatorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DataFlavorComparatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.datatransfer.DataTransferer$DataFlavorComparator", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.datatransfer.DataTransferer$DataFlavorComparator".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.datatransfer.DataTransferer$IndexedComparator".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public#int#compare(java.lang.Object,java.lang.Object)".equals(methods(c)), "methods");
        check("private final#sun.awt.datatransfer.DataTransferer$CharsetComparator#charsetComparator;private static final#java.lang.Integer#UNKNOWN_OBJECT_LOSES;private static final#java.lang.Integer#UNKNOWN_OBJECT_WINS;private static final#java.lang.Long#UNKNOWN_OBJECT_LOSES_L;private static final#java.lang.Long#UNKNOWN_OBJECT_WINS_L;private static final#java.util.Map#decodedTextRepresentations;private static final#java.util.Map#encodedTextRepresentations;private static final#java.util.Map#exactTypes;private static final#java.util.Map#nonTextRepresentations;private static final#java.util.Map#primaryTypes;private static final#java.util.Map#textTypes".equals(fields(c)), "fields");
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
