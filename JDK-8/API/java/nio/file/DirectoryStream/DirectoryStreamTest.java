/*
 * @test
 * @summary java.nio.file.DirectoryStream conformance: white-box structure + black-box behavior
 *          (iteration contract, glob/filter narrowing, close semantics, exception wiring).
 * @run main DirectoryStreamTest
 */

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        check(c.getTypeParameters()[0].getName().equals("T"), "type parameter T");
        check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals("iterator")),
              "has iterator()");
        // nested Filter type
        Class<?>[] nested = c.getDeclaredClasses();
        boolean hasFilter = false;
        for (Class<?> n : nested) if (n.getSimpleName().equals("Filter")) hasFilter = true;
        check(hasFilter, "declares nested Filter interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testDirectoryStream() throws Exception {
        Path dir = Files.createTempDirectory("jck-ds");
        Files.write(dir.resolve("a.txt"), new byte[] {1});
        Files.write(dir.resolve("b.txt"), new byte[] {2});
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            int count = 0;
            for (Path p : ds) count++;
            check(count == 2, "two entries");
        }
        Files.delete(dir.resolve("a.txt"));
        Files.delete(dir.resolve("b.txt"));
        Files.delete(dir);
    }

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    static void testDeepened() throws Exception {
        Path dir = Files.createTempDirectory("jck-ds2");
        try {
            Files.write(dir.resolve("a.txt"), new byte[] {1});
            Files.write(dir.resolve("b.bin"), new byte[] {2});
            Files.write(dir.resolve("c.TXT"), new byte[] {3});
            Files.createDirectory(dir.resolve("sub"));

            // full listing: every entry exactly once, never . or ..
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.size() == 4, "all four entries");
                check(!names.contains(".") && !names.contains(".."), "no dot entries");
                check(names.contains("a.txt") && names.contains("sub"), "names present");
                check(names.contains("c.TXT"), "case preserved");
            }
            // entries are resolved against the streamed directory
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                boolean allParented = true;
                for (Path p : ds)
                    if (!p.getParent().equals(dir)) allParented = false;
                check(allParented, "each entry's parent is the streamed dir");
            }
            // glob narrowing is exact and case-sensitive
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
                List<String> names = new ArrayList<>();
                for (Path p : ds) names.add(p.getFileName().toString());
                check(names.size() == 1 && names.get(0).equals("a.txt"), "glob *.txt exact");
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.{txt,bin}")) {
                int c = 0;
                for (Path p : ds) c++;
                check(c == 2, "glob brace alternation");
            }
            // explicit Filter narrowing
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) { return p.toString().endsWith(".bin"); }
                    })) {
                int c = 0;
                for (Path p : ds) c++;
                check(c == 1, "Filter accept=false excludes entries");
            }
            // Filter throwing IOException is wrapped in DirectoryIteratorException (same cause)
            final IOException sentinel = new IOException("from-filter");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir,
                    new DirectoryStream.Filter<Path>() {
                        public boolean accept(Path p) throws IOException { throw sentinel; }
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
            // one iterator per stream
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                ds.iterator();
                checkThrows(IllegalStateException.class, () -> ds.iterator(),
                      "second iterator() -> IllegalStateException");
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
            // for-each over the Iterable view
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                int c = 0;
                for (Path p : ds) { c++; if (c > 10) break; }
                check(c == 4, "for-each iterates all entries");
            }
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
            try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList()))
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testDirectoryStream();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
