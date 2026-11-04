/*
 * @test
 * @summary java.nio.file.attribute.FileTime conformance: white-box structure + black-box behavior.
 * @run main FileTimeTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class FileTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileTime");
        check(Modifier.isFinal(c.getModifiers()), "FileTime is final");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(c.getDeclaredConstructors().length == 0
              || java.util.Arrays.stream(c.getDeclaredConstructors())
                   .allMatch(k -> Modifier.isPrivate(k.getModifiers())),
              "no public constructors (factory class)");
        for (String n : new String[] {"from", "fromMillis", "to", "toMillis", "toInstant",
                "equals", "hashCode", "compareTo", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fromMillis round trip
        FileTime a = FileTime.fromMillis(123456789L);
        check(a.toMillis() == 123456789L, "fromMillis/toMillis");
        check(a.to(TimeUnit.SECONDS) == 123456L, "to(SECONDS) truncates");
        check(a.to(TimeUnit.MILLISECONDS) == 123456789L, "to(MILLIS) identity");
        check(a.to(TimeUnit.NANOSECONDS) == 123456789_000_000L, "to(NANOS) scales");

        // from(long, unit)
        FileTime b = FileTime.from(3, TimeUnit.SECONDS);
        check(b.toMillis() == 3000L, "from seconds to millis");
        check(b.to(TimeUnit.SECONDS) == 3, "from seconds to seconds");
        FileTime n = FileTime.from(1500, TimeUnit.NANOSECONDS);
        check(n.to(TimeUnit.NANOSECONDS) == 1500, "nanosecond precision preserved");

        // from(Instant) round trip with fixed instants (no wall clock)
        Instant i1 = Instant.parse("2023-01-02T03:04:05.123456789Z");
        FileTime ft = FileTime.from(i1);
        check(ft.toInstant().equals(i1), "Instant round trip");
        check(ft.toMillis() == 1672628645123L, "toMillis drops sub-milli digits");
        Instant i2 = Instant.parse("1970-01-01T00:00:00Z");
        check(FileTime.from(i2).toMillis() == 0, "epoch maps to 0");

        // negative time (before epoch)
        FileTime neg = FileTime.fromMillis(-1000);
        check(neg.toMillis() == -1000, "negative millis round trip");
        check(neg.toInstant().equals(Instant.ofEpochMilli(-1000)), "negative instant");

        // equals/hashCode/compareTo consistent
        check(FileTime.fromMillis(500).equals(FileTime.fromMillis(500)), "equals");
        check(!FileTime.fromMillis(500).equals(FileTime.fromMillis(501)), "not equals");
        check(FileTime.fromMillis(500).hashCode() == FileTime.fromMillis(500).hashCode(),
              "hashCode equal for equal");
        check(FileTime.fromMillis(1).compareTo(FileTime.fromMillis(2)) < 0, "compareTo less");
        check(FileTime.fromMillis(2).compareTo(FileTime.fromMillis(1)) > 0, "compareTo greater");
        check(FileTime.fromMillis(7).compareTo(FileTime.fromMillis(7)) == 0, "compareTo equal");

        // toString: ISO-8601 in UTC
        check(FileTime.from(Instant.parse("2023-01-02T03:04:05Z")).toString()
              .equals("2023-01-02T03:04:05Z"), "toString ISO seconds");
        check(FileTime.from(Instant.parse("2023-01-02T03:04:05.25Z")).toString()
              .equals("2023-01-02T03:04:05.25Z"), "toString fractional seconds");
        check(FileTime.fromMillis(0).toString().equals("1970-01-01T00:00:00Z"), "epoch toString");

        // Files integration: set then read a fixed mtime
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("ft.txt");
            java.nio.file.Files.writeString(f, "x");
            FileTime set = FileTime.from(Instant.parse("2020-06-01T12:00:00Z"));
            java.nio.file.Files.setLastModifiedTime(f, set);
            FileTime got = java.nio.file.Files.getLastModifiedTime(f);
            check(got.toMillis() == set.toMillis(), "mtime round trip through Files");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("ft.txt"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
