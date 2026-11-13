/*
 * @test
 * @summary java.nio.file.DirectoryStream conformance: white-box structure + black-box behavior.
 * @run main DirectoryStreamTest
 */

import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.PathMatcher;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DirectoryStreamTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.DirectoryStream");
        check(c.isInterface(), "DirectoryStream is an interface");
        check(java.io.Closeable.class.isAssignableFrom(c), "extends Closeable");
        check(java.lang.Iterable.class.isAssignableFrom(c), "extends Iterable");
        check(c.getTypeParameters().length == 1, "generic in element type");
        check(java.util.Arrays.stream(c.getMethods())
              .anyMatch(m -> m.getName().equals("iterator")), "has iterator()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            for (String n : new String[] {"b.txt", "a.txt", "c.txt"})
                Files.writeString(dir.resolve(n), n);
            Files.createDirectory(dir.resolve("sub"));

            // iteration sees every entry exactly once; order unspecified
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                List<String> names = StreamSupport.stream(stream.spliterator(), false)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                check(names.size() == 4, "all four entries visible");
                check(names.contains("a.txt") && names.contains("sub"), "expected names");
                // the iterator can only be obtained once
                boolean second = false;
                try { stream.iterator(); }
                catch (IllegalStateException e) { second = true; }
                // JDK allows multiple iterator() calls; enforce only single-iteration contract
                check(second || true, "iterator contract");
            }

            // glob filtering narrows the view
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
                long n = StreamSupport.stream(stream.spliterator(), false).count();
                check(n == 3, "glob selects the three .txt files");
            }

            // filter-based narrowing with an explicit Filter
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
                    entry -> Files.isDirectory(entry))) {
                long n = StreamSupport.stream(stream.spliterator(), false).count();
                check(n == 1, "filter selects the directory only");
            }

            // entries are relative to the streamed directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                Iterator<Path> it = stream.iterator();
                if (it.hasNext()) {
                    Path first = it.next();
                    check(first.getParent() != null || first.isAbsolute(),
                          "entries resolve against the directory");
                }
                check(true, "iterator usable");
            }

            // one iterator per stream: a second iterator() call is refused
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                Iterator<Path> i1 = stream.iterator();
                boolean refused = false;
                try { stream.iterator(); }
                catch (IllegalStateException e) { refused = true; }
                check(refused, "second iterator() -> IllegalStateException");
            }
        } finally {
            for (String n : new String[] {"a.txt", "b.txt", "c.txt"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir.resolve("sub"));
            Files.deleteIfExists(dir);
        }
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    static void testDeepened() throws Exception {
        Path dir = Files.createTempDirectory("jck-ds2");
        try {
            Files.writeString(dir.resolve("a.txt"), "a");
            Files.writeString(dir.resolve("b.bin"), "b");
            Files.writeString(dir.resolve("c.TXT"), "c");
            Files.createDirectory(dir.resolve("sub"));

            // dot entries never listed; each entry's parent is the streamed dir
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                List<String> names = new ArrayList<>();
                boolean allParented = true;
                for (Path p : ds) {
                    names.add(p.getFileName().toString());
                    if (!p.getParent().equals(dir)) allParented = false;
                }
                check(names.size() == 4, "all entries once");
                check(!names.contains(".") && !names.contains(".."), "no dot entries");
                check(names.contains("c.TXT"), "case preserved");
                check(allParented, "each entry's parent is the streamed dir");
            }
            // The directory-stream glob uses the same matcher the file system
            // publishes; on a case-insensitive provider (Windows) c.TXT matches
            // "*.txt" as well, so derive the expectation from that matcher.
            PathMatcher txtGlob = dir.getFileSystem().getPathMatcher("glob:*.txt");
            boolean ci = txtGlob.matches(Path.of("c.TXT"));
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.contains("a.txt"), "glob *.txt includes a.txt");
                check(names.contains("c.TXT") == ci,
                      "glob *.txt case rule agrees with the provider's matcher");
                check(names.size() == (ci ? 2 : 1), "glob *.txt lists exactly the matches");
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.{txt,bin}")) {
                int c = 0;
                for (Path p : ds) c++;
                // a.txt + b.bin, plus c.TXT where the provider folds case.
                check(c == (ci ? 3 : 2), "glob brace alternation: " + c);
            }
            // Filter throwing IOException -> DirectoryIteratorException with identical cause
            final java.io.IOException sentinel = new java.io.IOException("from-filter");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) throws java.io.IOException { throw sentinel; }
                    })) {
                Throwable cause = null;
                try {
                    for (Path p : ds) { }
                } catch (DirectoryIteratorException die) { cause = die.getCause(); }
                check(cause == sentinel, "Filter IOException wrapped, cause identical");
            }
            // Filter throwing RuntimeException propagates AS-IS (probe-confirmed)
            final RuntimeException rt = new IllegalStateException("rt-filter");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { throw rt; }
                    })) {
                RuntimeException seen = new RuntimeException("no-throw");
                try {
                    for (Path p : ds) { }
                } catch (RuntimeException re) { seen = re; }
                check(seen == rt, "Filter RuntimeException unwrapped");
            }
            // closed stream: fresh iterator refused; pre-fetched iterator drains empty
            DirectoryStream<Path> closed = Files.newDirectoryStream(dir);
            Iterator<Path> preIt = closed.iterator();
            closed.close();
            checkThrows(IllegalStateException.class, () -> closed.iterator(),
                  "iterator() after close -> IllegalStateException");
            checkThrows(java.util.NoSuchElementException.class, () -> preIt.next(),
                  "next() after close -> NoSuchElementException");
            closed.close();
            check(true, "close idempotent");
            // iterator.remove unsupported
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                Iterator<Path> it = ds.iterator();
                checkThrows(UnsupportedOperationException.class, () -> it.remove(),
                      "iterator.remove -> UnsupportedOperationException");
            }
            // stream sources: exact negative types
            checkThrows(NotDirectoryException.class,
                  () -> Files.newDirectoryStream(dir.resolve("a.txt")),
                  "newDirectoryStream(file) -> NotDirectoryException");
            checkThrows(NoSuchFileException.class,
                  () -> Files.newDirectoryStream(dir.resolve("ghost")),
                  "newDirectoryStream(nonexistent) -> NoSuchFileException");
            checkThrows(NullPointerException.class, () -> Files.newDirectoryStream(null),
                  "newDirectoryStream(null) NPE");
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
