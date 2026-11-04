/*
 * @test
 * @summary sun.java2d.Disposer conformance: reflection (no init, native/display-heavy).
 * @run main DisposerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DisposerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.Disposer", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.Disposer".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();private static#void#clearDeferredRecords();public static#java.lang.ref.ReferenceQueue#getQueue();public static#void#addObjectRecord(java.lang.Object,sun.java2d.DisposerRecord);public static#void#addRecord(java.lang.Object,long,long);public static#void#addRecord(java.lang.Object,sun.java2d.DisposerRecord);public static#void#addReference(java.lang.ref.Reference,sun.java2d.DisposerRecord);public static#void#pollRemove();public#void#run();synchronized#void#add(java.lang.Object,sun.java2d.DisposerRecord)".equals(methods(c)), "methods");
        check("private static final#java.lang.ref.ReferenceQueue#queue;private static final#java.util.Hashtable#records;private static#java.util.ArrayList#deferredRecords;private static#sun.java2d.Disposer#disposerInstance;public static final#int#PHANTOM;public static final#int#WEAK;public static volatile#boolean#pollingQueue;public static#int#refType".equals(fields(c)), "fields");
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
