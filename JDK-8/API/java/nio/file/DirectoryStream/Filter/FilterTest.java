/*
 * @test
 * @summary java.nio.file.DirectoryStream.Filter conformance: white-box structure + black-box
 *          behavior (lambda/anonymous predicate, narrowing, exception wiring).
 * @run main FilterTest
 */

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static DirectoryStream.Filter<Path> txtFilter = new DirectoryStream.Filter<Path>() {
        public boolean accept(Path entry) {
            return entry.getFileName().toString().endsWith(".txt");
        }
    };

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.DirectoryStream$Filter");
        check(c.isInterface(), "Filter is an interface");
        check(c.getEnclosingClass() == DirectoryStream.class, "nested in DirectoryStream");
        check(c.getTypeParameters().length == 1, "generic in entry type");
        check(c.getTypeParameters()[0].getName().equals("T"), "type parameter T");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("accept")), "has accept");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("accept")
                  && m.getExceptionTypes().length > 0), "accept declares IOException");
        check(c.isAnnotationPresent(FunctionalInterface.class),
              "@FunctionalInterface (lambda target)");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // standalone predicate behaviour on arbitrary paths
        check(txtFilter.accept(Paths.get("a.txt")), "accept .txt");
        check(!txtFilter.accept(Paths.get("a.bin")), "reject .bin");
        check(!txtFilter.accept(Paths.get("txt")), "suffix only, not substring");
        check(txtFilter.accept(Paths.get("/deep/dir/x.txt")), "accept on absolute path");
        // lambda form (functional interface)
        DirectoryStream.Filter<Path> lambda = p -> p.getNameCount() > 1;
        check(lambda.accept(Paths.get("a/b")), "lambda accept true");
        check(!lambda.accept(Paths.get("a")), "lambda accept false");

        Path dir = Files.createTempDirectory("jck-flt");
        try {
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.write(dir.resolve("b.bin"), new byte[] {2});
            Files.write(dir.resolve("c.txt"), new byte[] {3});
            Files.createDirectory(dir.resolve("sub.dir"));
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                int count = 0;
                for (Path p : ds) count++;
                check(count == 2, "glob filter yields two entries");
            }
            // Filter via newDirectoryStream: exact selected set
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, txtFilter)) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.size() == 2, "txt filter excludes .bin and dir");
                check(names.contains("a.txt") && names.contains("c.txt"), "selected names exact");
            }
            // accept invoked once per candidate entry
            final int[] calls = {0};
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { calls[0]++; return false; }
                    })) {
                int seen = 0;
                for (Path p : ds) seen++;
                check(seen == 0, "accept=false for all -> empty iteration");
                check(calls[0] == 4, "accept invoked once per entry (4 entries), got " + calls[0]);
            }
            // entries passed to accept are resolved against the directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { return p.getParent().equals(dir); }
                    })) {
                int c = 0;
                for (Path p : ds) c++;
                check(c == 4, "accept receives dir-resolved entries");
            }
            // IOException from accept wrapped as DirectoryIteratorException, cause identical
            final IOException sentinel = new IOException("filter-io");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) throws IOException { throw sentinel; }
                    })) {
                Throwable cause = null;
                try {
                    for (Path p : ds) { }
                } catch (DirectoryIteratorException die) { cause = die.getCause(); }
                check(cause == sentinel, "IOException wrapped in DirectoryIteratorException");
            }
            // RuntimeException from accept propagates unwrapped (probe-confirmed)
            final RuntimeException rt = new IllegalStateException("filter-rt");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { throw rt; }
                    })) {
                RuntimeException seen = new RuntimeException("no-throw");
                try {
                    for (Path p : ds) { }
                } catch (RuntimeException re) { seen = re; }
                check(seen == rt, "RuntimeException propagates unwrapped");
            }
            // DirectoryIteratorException is an unchecked RuntimeException
            check(java.lang.RuntimeException.class.isAssignableFrom(DirectoryIteratorException.class),
                  "DirectoryIteratorException is a RuntimeException");
            java.io.IOException wrapped = new java.io.IOException("x");
            DirectoryIteratorException die = new DirectoryIteratorException(wrapped);
            check(die.getCause() == wrapped, "constructor stores cause");
            check(die.getMessage().contains("x"), "message derived from cause");
        } finally {
            try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList()))
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
