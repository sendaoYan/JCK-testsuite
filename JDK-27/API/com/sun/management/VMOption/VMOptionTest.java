/*
 * @test
 * @summary com.sun.management.VMOption conformance: white-box structure + black-box behavior.
 * @run main VMOptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;

public class VMOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.VMOption");
        check(!c.isInterface(), "not an interface");
        Set<String> m = methodNames(c);
        for (String n : new String[] {"getName","getValue","getOrigin","isWriteable","toString","from"})
            check(m.contains(n), "method " + n);
        Class<?> origin = load("com.sun.management.VMOption$Origin");
        check(origin.isEnum(), "Origin is enum");
        check(Enum.class.isAssignableFrom(origin), "Origin extends Enum");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor + getters + toString.
        com.sun.management.VMOption o = new com.sun.management.VMOption(
            "PrintGC", "false", true, com.sun.management.VMOption.Origin.DEFAULT);
        check(o.getName().equals("PrintGC"), "name");
        check(o.getValue().equals("false"), "value");
        check(o.isWriteable(), "writeable");
        check(o.getOrigin() == com.sun.management.VMOption.Origin.DEFAULT, "origin");
        String s = o.toString();
        check(s != null, "toString non-null");
        check(s.contains("PrintGC"), "toString contains name");
        check(s.contains("false"), "toString contains value");
        check(s.contains("(read-write)"), "toString marks read-write");

        // read-only VMOption renders differently.
        com.sun.management.VMOption ro = new com.sun.management.VMOption(
            "X", "y", false, com.sun.management.VMOption.Origin.OTHER);
        check(ro.toString().contains("(read-only)"), "toString marks read-only");

        // Origin enum: fixed set and valueOf round-trip.
        com.sun.management.VMOption.Origin[] all = com.sun.management.VMOption.Origin.values();
        check(all.length == 8, "Origin has 8 constants");
        String[] expected = {"DEFAULT","VM_CREATION","ENVIRON_VAR","CONFIG_FILE","MANAGEMENT","ERGONOMIC","ATTACH_ON_DEMAND","OTHER"};
        for (int i = 0; i < expected.length; i++) {
            check(all[i].name().equals(expected[i]), "Origin[" + i + "] == " + expected[i]);
            check(com.sun.management.VMOption.Origin.valueOf(expected[i]) == all[i],
                "valueOf(" + expected[i] + ") round-trip");
        }

        // from(null) == null.
        check(com.sun.management.VMOption.from(null) == null, "from(null) == null");

        // Real VMOption objects from HotSpotDiagnosticMXBean are well-formed.
        com.sun.management.HotSpotDiagnosticMXBean diag =
            ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
        List<com.sun.management.VMOption> opts = diag.getDiagnosticOptions();
        check(opts != null && !opts.isEmpty(), "real diagnostic options non-empty");
        for (com.sun.management.VMOption vo : opts) {
            check(vo.getName() != null && !vo.getName().isEmpty(), "real option name non-empty");
            check(vo.getValue() != null, "real option value non-null");
            check(vo.getOrigin() != null, "real option origin non-null");
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
