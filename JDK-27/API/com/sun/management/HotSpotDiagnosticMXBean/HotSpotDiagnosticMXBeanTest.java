/*
 * @test
 * @summary com.sun.management.HotSpotDiagnosticMXBean conformance: white-box structure + black-box behavior.
 * @run main HotSpotDiagnosticMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HotSpotDiagnosticMXBeanTest {
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
        Class<?> c = load("com.sun.management.HotSpotDiagnosticMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.lang.management.PlatformManagedObject"),
            "extends PlatformManagedObject");
        Set<String> m = methodNames(c);
        for (String n : new String[] {"dumpHeap","getDiagnosticOptions","getVMOption","setVMOption","dumpThreads"})
            check(m.contains(n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.management.HotSpotDiagnosticMXBean b =
            ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
        check(b != null, "platform bean non-null");

        // Diagnostic options list is non-empty and self-consistent.
        List<com.sun.management.VMOption> opts = b.getDiagnosticOptions();
        check(opts != null && !opts.isEmpty(), "diagnostic options non-empty");
        boolean sawHeapDumpOnOOM = false;
        for (com.sun.management.VMOption o : opts) {
            check(o.getName() != null, "option name non-null");
            check(o.getValue() != null, "option value non-null");
            check(o.getOrigin() != null, "option origin non-null");
            if ("HeapDumpOnOutOfMemoryError".equals(o.getName())) sawHeapDumpOnOOM = true;
        }
        check(sawHeapDumpOnOOM, "diagnostic options contain HeapDumpOnOutOfMemoryError");

        // getVMOption on a known option matches its name.
        com.sun.management.VMOption printGC = b.getVMOption("PrintGC");
        check(printGC != null, "getVMOption(PrintGC) non-null");
        check("PrintGC".equals(printGC.getName()), "getVMOption name matches");
        check(printGC.getValue() != null, "getVMOption value non-null");

        // getVMOption on an unknown option throws IllegalArgumentException.
        checkThrowsIae(new Runnable() {
            public void run() { b.getVMOption("NoSuchVmOption_12345"); }
        }, "getVMOption unknown option throws IllegalArgumentException");

        // setVMOption round-trips for a writable option and restores the original
        // value in finally (no global side effects left behind).
        com.sun.management.VMOption heapOom = b.getVMOption("HeapDumpOnOutOfMemoryError");
        check(heapOom != null, "HeapDumpOnOutOfMemoryError non-null");
        check(heapOom.isWriteable(), "HeapDumpOnOutOfMemoryError writeable");
        String original = heapOom.getValue();
        String target = "true".equalsIgnoreCase(original) ? "false" : "true";
        try {
            b.setVMOption("HeapDumpOnOutOfMemoryError", target);
            com.sun.management.VMOption after = b.getVMOption("HeapDumpOnOutOfMemoryError");
            check(after != null, "getVMOption after set non-null");
            check(target.equalsIgnoreCase(after.getValue()), "setVMOption value reflected");
        } finally {
            b.setVMOption("HeapDumpOnOutOfMemoryError", original);
        }

        // dumpHeap writes a non-empty file to an isolated temp dir; cleaned in finally.
        dumpHeap(b);
    }

    static void dumpHeap(com.sun.management.HotSpotDiagnosticMXBean b) throws Exception {
        Path dir = Files.createTempDirectory("jck-hsdiag");
        try {
            Path file = dir.resolve("hotspot-diagnostic.hprof");
            b.dumpHeap(file.toString(), true);
            check(Files.exists(file) && Files.isRegularFile(file), "dumpHeap file exists");
            check(Files.size(file) > 0, "dumpHeap file non-empty");
        } finally {
            deleteRecursively(dir);
        }
    }

    static void deleteRecursively(Path p) throws IOException {
        if (p == null || !Files.exists(p)) return;
        if (Files.isDirectory(p)) {
            try (java.util.stream.Stream<Path> s = Files.list(p)) {
                for (Path child : (Iterable<Path>) s::iterator) deleteRecursively(child);
            }
        }
        Files.deleteIfExists(p);
    }

    static void checkThrowsIae(Runnable r, String m) {
        try {
            r.run();
            throw new AssertionError(m + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, m);
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
