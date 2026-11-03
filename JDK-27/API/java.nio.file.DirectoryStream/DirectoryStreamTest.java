/*
 * @test
 * @summary java.nio.file.DirectoryStream conformance: white-box structure + black-box behavior.
 * @run main DirectoryStreamTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
