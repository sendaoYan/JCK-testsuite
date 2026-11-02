/*
 * @test
 * @summary com.sun.management.HotSpotDiagnosticMXBean conformance: diagnostic options.
 * @run main HotSpotDiagnosticMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HotSpotDiagnosticMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.management.HotSpotDiagnosticMXBean");
        check(c.isInterface(), "interface");
        Set<String> m = methodNames(c);
        check(m.contains("dumpHeap"), "dumpHeap");
        check(m.contains("getDiagnosticOptions"), "getDiagnosticOptions");
        check(m.contains("getVMOption"), "getVMOption");
        check(m.contains("setVMOption"), "setVMOption");
        com.sun.management.HotSpotDiagnosticMXBean b = java.lang.management.ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
        check(b != null, "platform bean non-null");
        java.util.List<com.sun.management.VMOption> opts = b.getDiagnosticOptions();
        check(!opts.isEmpty(), "diagnostic options non-empty");
        com.sun.management.VMOption o = b.getVMOption(opts.get(0).getName());
        check(o != null, "getVMOption by name");
        check(o.getName().equals(opts.get(0).getName()), "option name matches");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
