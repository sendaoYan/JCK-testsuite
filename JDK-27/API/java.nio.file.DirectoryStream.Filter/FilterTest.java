/*
 * @test
 * @summary java.nio.file.DirectoryStream.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
