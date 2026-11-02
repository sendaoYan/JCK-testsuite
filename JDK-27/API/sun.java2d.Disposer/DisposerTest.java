/*
 * @test
 * @summary sun.java2d.Disposer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d:+open
 * @run main DisposerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DisposerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.Disposer", false, DisposerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.Disposer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();private static#void#clearDeferredRecords();private static#void#safeDispose(sun.java2d.DisposerRecord);public static#java.lang.ref.ReferenceQueue#getQueue();public static#void#addObjectRecord(java.lang.Object,sun.java2d.DisposerRecord);public static#void#addRecord(java.lang.Object,long,long);public static#void#addRecord(java.lang.Object,sun.java2d.DisposerRecord);public static#void#addReference(java.lang.ref.Reference,sun.java2d.DisposerRecord);public static#void#pollRemove();public#void#run();synchronized#void#add(java.lang.Object,sun.java2d.DisposerRecord)".equals(methods(c)), "methods");
        check("private static final#java.lang.ref.ReferenceQueue#queue;private static final#java.util.Hashtable#records;private static final#sun.java2d.Disposer#disposerInstance;private static#java.util.concurrent.ConcurrentLinkedDeque#deferredRecords;public static volatile#boolean#pollingQueue".equals(fields(c)), "fields");
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
