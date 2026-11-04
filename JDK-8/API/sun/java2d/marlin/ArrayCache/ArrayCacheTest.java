/*
 * @test
 * @summary sun.java2d.marlin.ArrayCache conformance: reflection (no init, native/display-heavy).
 * @run main ArrayCacheTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ArrayCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.ArrayCache", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.ArrayCache".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.marlin.MarlinConst".equals(interfaces(c)), "interfaces");
        check("public static#int#getNewSize(int,int);public static#long#getNewLargeSize(long,long);static synchronized#void#incOversize();static synchronized#void#incResizeDirtyByte();static synchronized#void#incResizeDirtyFloat();static synchronized#void#incResizeDirtyInt();static synchronized#void#incResizeInt();static#int#getBucket(int);static#int#getBucketDirtyBytes(int);static#void#dumpStats()".equals(methods(c)), "methods");
        check("private static#int#oversize;private static#int#resizeDirtyByte;private static#int#resizeDirtyFloat;private static#int#resizeDirtyInt;private static#int#resizeInt;static final#[I#ARRAY_SIZES;static final#[I#DIRTY_BYTE_ARRAY_SIZES;static final#int#BUCKETS;static final#int#MASK_CLR_1;static final#int#MAX_ARRAY_SIZE;static final#int#MAX_DIRTY_BYTE_ARRAY_SIZE;static final#int#MIN_ARRAY_SIZE;static final#int#MIN_DIRTY_BYTE_ARRAY_SIZE;static final#int#THRESHOLD_ARRAY_SIZE;static final#long#THRESHOLD_HUGE_ARRAY_SIZE;static final#long#THRESHOLD_LARGE_ARRAY_SIZE".equals(fields(c)), "fields");
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
