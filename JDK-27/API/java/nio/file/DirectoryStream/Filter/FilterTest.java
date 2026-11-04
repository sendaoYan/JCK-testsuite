/*
 * @test
 * @summary java.nio.file.DirectoryStream.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.DirectoryStream$Filter");
        check(c.isInterface(), "Filter is a functional interface");
        check(c.getTypeParameters().length == 1, "generic in entry type");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("accept")), "has accept()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Files.writeString(dir.resolve("keep.log"), "k");
            Files.writeString(dir.resolve("skip.tmp"), "s");
            Files.writeString(dir.resolve("also.log"), "a");

            // lambda accept selects by name
            DirectoryStream.Filter<Path> logOnly = p ->
                    p.getFileName().toString().endsWith(".log");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, logOnly)) {
                List<String> names = StreamSupport.stream(stream.spliterator(), false)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                check(names.size() == 2 && !names.contains("skip.tmp"),
                      "filter admits only .log entries");
            }

            // a filter accepting everything keeps all entries
            try (DirectoryStream<Path> all = Files.newDirectoryStream(dir, p -> true)) {
                check(StreamSupport.stream(all.spliterator(), false).count() == 3,
                      "accept-true keeps all");
            }
            // a filter accepting nothing yields an empty stream
            try (DirectoryStream<Path> none = Files.newDirectoryStream(dir, p -> false)) {
                check(StreamSupport.stream(none.spliterator(), false).count() == 0,
                      "accept-false keeps none");
            }

            // filter failures surface as DirectoryIteratorException during iteration
            try (DirectoryStream<Path> boom = Files.newDirectoryStream(dir, p -> {
                throw new java.io.IOException("no");
            })) {
                boolean wrapped = false;
                try {
                    StreamSupport.stream(boom.spliterator(), false).count();
                } catch (java.nio.file.DirectoryIteratorException e) {
                    wrapped = e.getCause() instanceof java.io.IOException;
                }
                check(wrapped, "filter IOException wrapped at iteration");
            }
        } finally {
            for (String n : new String[] {"keep.log", "skip.tmp", "also.log"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir);
        }
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    static void testDeepened() throws Exception {
        Class<?> c = Class.forName("java.nio.file.DirectoryStream$Filter");
        check(c.getEnclosingClass() == DirectoryStream.class, "nested in DirectoryStream");
        check(c.getTypeParameters()[0].getName().equals("T"), "type parameter T");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("accept")
                  && m.getExceptionTypes().length > 0), "accept declares IOException");
        check(c.isAnnotationPresent(FunctionalInterface.class),
              "@FunctionalInterface (lambda target)");
        check(c.getDeclaredMethods().length == 1, "single abstract method");

        // standalone predicate on arbitrary paths
        DirectoryStream.Filter<Path> txt = p -> p.getFileName().toString().endsWith(".txt");
        check(txt.accept(Path.of("a.txt")), "accept .txt");
        check(!txt.accept(Path.of("a.bin")), "reject .bin");
        check(!txt.accept(Path.of("txt")), "suffix only, not substring");
        check(txt.accept(Path.of("/deep/dir/x.txt")), "accept on absolute path");

        Path dir = Files.createTempDirectory("jck-flt2");
        try {
            Files.writeString(dir.resolve("a.txt"), "a");
            Files.writeString(dir.resolve("b.bin"), "b");
            Files.writeString(dir.resolve("c.txt"), "c");
            Files.createDirectory(dir.resolve("sub.dir"));
            // exact selected set
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, txt)) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.size() == 2, "txt filter excludes .bin and dir");
                check(names.contains("a.txt") && names.contains("c.txt"), "selected names exact");
            }
            // accept invoked once per entry
            final int[] calls = {0};
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, p -> { calls[0]++; return false; })) {
                int seen = 0;
                for (Path p : ds) seen++;
                check(seen == 0, "accept=false -> empty iteration");
                check(calls[0] == 4, "accept invoked once per entry (4), got " + calls[0]);
            }
            // entries passed to accept are resolved against the directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    p -> p.getParent().equals(dir))) {
                int cnt = 0;
                for (Path p : ds) cnt++;
                check(cnt == 4, "accept receives dir-resolved entries");
            }
            // IOException -> DirectoryIteratorException with identical cause object
            final IOException sentinel = new IOException("filter-io");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, p -> { throw sentinel; })) {
                Throwable cause = null;
                try {
                    for (Path p : ds) { }
                } catch (DirectoryIteratorException die) { cause = die.getCause(); }
                check(cause == sentinel, "IOException wrapped, cause identical");
            }
            // RuntimeException propagates unwrapped (probe-confirmed)
            final RuntimeException rt = new IllegalStateException("filter-rt");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, p -> { throw rt; })) {
                RuntimeException seen = new RuntimeException("no-throw");
                try {
                    for (Path p : ds) { }
                } catch (RuntimeException re) { seen = re; }
                check(seen == rt, "RuntimeException propagates unwrapped");
            }
            // DirectoryIteratorException shape
            check(java.lang.RuntimeException.class.isAssignableFrom(DirectoryIteratorException.class),
                  "DirectoryIteratorException is a RuntimeException");
            IOException wrapped = new IOException("x");
            DirectoryIteratorException die = new DirectoryIteratorException(wrapped);
            check(die.getCause() == wrapped, "constructor stores cause");
            check(die.getMessage().contains("x"), "message derived from cause");
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
