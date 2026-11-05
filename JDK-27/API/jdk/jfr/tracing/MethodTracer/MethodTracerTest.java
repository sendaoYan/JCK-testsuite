/*
 * @test
 * @summary jdk.jfr.tracing.MethodTracer conformance: internal package reflection.
 * @modules jdk.jfr/jdk.jfr.tracing:+open
 * @run main MethodTracerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodTracerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    // ---- deepening: behavioral contract of the static tracing surface ----
    static void testBehavior(Class<?> c) throws Exception {
        Method timestamp = c.getDeclaredMethod("timestamp");
        check(Modifier.isStatic(timestamp.getModifiers()), "timestamp is static");
        check(timestamp.getReturnType() == long.class, "timestamp returns long");
        long t1 = (Long) timestamp.invoke(null);
        long t2 = (Long) timestamp.invoke(null);
        check(t1 >= 0L, "timestamp() >= 0 (was " + t1 + ")");
        check(t2 >= t1, "timestamp() monotonic non-decreasing");
        // all trace/timing entry points take (long, long) and return void; must be callable without throwing
        String[] names = {"trace", "timing", "traceTiming", "traceObjectInit", "timingObjectInit", "traceTimingObjectInit"};
        for (String n : names) {
            Method m = c.getDeclaredMethod(n, long.class, long.class);
            check(Modifier.isStatic(m.getModifiers()), n + " is static");
            check(m.getReturnType() == void.class, n + " returns void");
            m.invoke(null, 1L, 2L);
            check(true, n + "(long,long) callable without throwing");
            m.invoke(null, 0L, 0L);
            check(true, n + "(0,0) tolerated");
        }
        // the package is not exported by jdk.jfr (qualified contains); reflection path is the only access
        check(c.getModule() != null && "jdk.jfr".equals(c.getModule().getName()), "lives in module jdk.jfr");
        check(!c.getModule().isExported("jdk.jfr.tracing"), "jdk.jfr.tracing is not exported (contains-only)");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.tracing.MethodTracer", false, MethodTracerTest.class.getClassLoader());
        check(c.getName().equals("jdk.jfr.tracing.MethodTracer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"timestamp","timing","timingObjectInit","trace","traceObjectInit","traceTiming","traceTimingObjectInit"}), "methods");
        check(Arrays.equals(fields(c), new String[]{}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        testBehavior(c);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
