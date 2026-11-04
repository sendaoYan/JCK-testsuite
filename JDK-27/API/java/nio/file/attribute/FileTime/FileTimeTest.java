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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===== A12 contract deepening (appended) =====
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    interface IORunnable { void run() throws java.io.IOException; }

    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void cleanup(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                         java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) cleanup(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    static void testContract() throws Exception {
        // fromMillis/toMillis round-trip matrix (0, negative, extremes)
        check(FileTime.fromMillis(0L).toMillis() == 0L, "epoch round-trip");
        check(FileTime.fromMillis(-1L).toMillis() == -1L, "negative round-trip");
        check(FileTime.fromMillis(-1234567890L).toMillis() == -1234567890L, "negative ms round-trip");
        check(FileTime.fromMillis(Long.MAX_VALUE).toMillis() == Long.MAX_VALUE, "Long.MAX_VALUE round-trip");
        check(FileTime.fromMillis(Long.MIN_VALUE).toMillis() == Long.MIN_VALUE, "Long.MIN_VALUE round-trip");
        // to(TimeUnit) unit-conversion matrix (fixed input 123456789 ms, no wall clock)
        FileTime t = FileTime.fromMillis(123456789L);
        check(t.to(TimeUnit.MILLISECONDS) == 123456789L, "to MILLIS identity");
        check(t.to(TimeUnit.SECONDS) == 123456789L / 1000, "to SECONDS truncation");
        check(t.to(TimeUnit.MINUTES) == 2057L, "to MINUTES");
        check(t.to(TimeUnit.HOURS) == 34L, "to HOURS");
        check(t.to(TimeUnit.DAYS) == 1L, "to DAYS");
        check(t.to(TimeUnit.MICROSECONDS) == 123456789000L, "to MICROS");
        check(t.to(TimeUnit.NANOSECONDS) == 123456789000000L, "to NANOS");
        check(FileTime.fromMillis(-123456789L).to(TimeUnit.NANOSECONDS) == -123456789000000L, "negative to NANOS");
        // negative conversion truncates toward zero (not floor)
        check(FileTime.fromMillis(-1500L).to(TimeUnit.SECONDS) == -1L, "negative to SECONDS toward zero");
        check(FileTime.fromMillis(-1L).to(TimeUnit.SECONDS) == 0L, "-1ms to SECONDS is 0");
        // saturating conversion at Long.MAX_VALUE millis (millis->nanos is lossy)
        FileTime max = FileTime.fromMillis(Long.MAX_VALUE);
        check(max.to(TimeUnit.MICROSECONDS) == Long.MAX_VALUE, "MAX millis to MICROS saturates");
        check(max.to(TimeUnit.NANOSECONDS) == Long.MAX_VALUE, "MAX millis to NANOS saturates");
        check(max.to(TimeUnit.SECONDS) == Long.MAX_VALUE / 1000, "MAX millis to SECONDS saturating divide");
        check(FileTime.from(max.to(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS).toMillis() == 9223372036854L,
              "nanosecond saturation loses precision (lossy round-trip)");
        // from(long, unit) matrix
        check(FileTime.from(2, TimeUnit.SECONDS).toMillis() == 2000L, "from seconds");
        check(FileTime.from(1500, TimeUnit.NANOSECONDS).to(TimeUnit.NANOSECONDS) == 1500L, "from nanos precision");
        check(FileTime.from(1234, TimeUnit.MICROSECONDS).toMillis() == 1L, "micros to millis truncation");
        check(FileTime.from(0, TimeUnit.DAYS).toMillis() == 0L, "from zero days");
        check(FileTime.from(-3, TimeUnit.SECONDS).toMillis() == -3000L, "from negative seconds");
        check(FileTime.from(0, TimeUnit.NANOSECONDS).toMillis() == 0L, "from zero nanos");
        // cross-representation equals: value normalized to the instant
        check(FileTime.fromMillis(1000L).equals(FileTime.from(1, TimeUnit.SECONDS)), "cross-unit equals");
        check(FileTime.from(1000000000L, TimeUnit.NANOSECONDS).equals(FileTime.from(1, TimeUnit.SECONDS)),
              "nanos equals seconds");
        // sub-millisecond comparisons need real precision, not millis
        check(FileTime.from(1500, TimeUnit.NANOSECONDS).compareTo(FileTime.fromMillis(1L)) < 0, "1500ns < 1ms");
        check(FileTime.from(1000000, TimeUnit.NANOSECONDS).compareTo(FileTime.fromMillis(1L)) == 0,
              "1000000ns == 1ms");
        check(FileTime.from(999, TimeUnit.NANOSECONDS).compareTo(FileTime.fromMillis(0L)) > 0, "999ns > 0");
        check(FileTime.from(999, TimeUnit.NANOSECONDS).compareTo(FileTime.fromMillis(1L)) < 0, "999ns < 1ms");
        // equals/hashCode contract
        check(FileTime.fromMillis(500L).equals(FileTime.fromMillis(500L)), "equals same value");
        check(!FileTime.fromMillis(500L).equals(FileTime.fromMillis(501L)), "not equals different value");
        check(FileTime.fromMillis(500L).hashCode() == FileTime.fromMillis(500L).hashCode(), "hashCode consistent");
        check(!FileTime.fromMillis(0L).equals(null), "equals null is false");
        check(!FileTime.fromMillis(0L).equals("x"), "equals foreign type is false");
        // compareTo total order
        check(FileTime.fromMillis(1L).compareTo(FileTime.fromMillis(2L)) < 0, "compareTo less");
        check(FileTime.fromMillis(2L).compareTo(FileTime.fromMillis(1L)) > 0, "compareTo greater");
        check(FileTime.fromMillis(7L).compareTo(FileTime.fromMillis(7L)) == 0, "compareTo equal");
        check(FileTime.fromMillis(-2L).compareTo(FileTime.fromMillis(-1L)) < 0, "compareTo negative order");
        // Instant interop (fixed instants)
        java.time.Instant i = java.time.Instant.parse("2023-01-02T03:04:05.123456789Z");
        check(FileTime.from(i).toInstant().equals(i), "from/to Instant round-trip");
        check(FileTime.from(i).toMillis() == 1672628645123L, "Instant toMillis drops sub-milli digits");
        check(FileTime.fromMillis(-1000L).toInstant().equals(java.time.Instant.ofEpochMilli(-1000L)),
              "negative toInstant");
        check(FileTime.fromMillis(0L).toInstant().equals(java.time.Instant.EPOCH), "epoch toInstant");
        // toString exact formats (ISO-8601 in UTC, fixed expected strings)
        check(FileTime.fromMillis(0L).toString().equals("1970-01-01T00:00:00Z"), "epoch toString");
        check(FileTime.fromMillis(-1000L).toString().equals("1969-12-31T23:59:59Z"), "pre-epoch toString");
        check(FileTime.fromMillis(-1L).toString().equals("1969-12-31T23:59:59.999Z"), "pre-epoch fractional toString");
        check(FileTime.fromMillis(1000000000000L).toString().equals("2001-09-09T01:46:40Z"), "2001 toString");
        check(FileTime.from(1, TimeUnit.MILLISECONDS).toString().equals("1970-01-01T00:00:00.001Z"),
              "millis fraction toString");
        check(FileTime.from(123456789L, TimeUnit.NANOSECONDS).toString().equals("1970-01-01T00:00:00.123456789Z"),
              "nano fraction toString");
        check(FileTime.from(1234L, TimeUnit.MICROSECONDS).toString().equals("1970-01-01T00:00:00.001234Z"),
              "micro fraction toString");
        check(FileTime.from(-123456789L, TimeUnit.NANOSECONDS).toString().equals("1969-12-31T23:59:59.876543211Z"),
              "negative nano toString");
        check(FileTime.fromMillis(-62135596800000L).toString().equals("0001-01-01T00:00:00Z"), "year 0001 toString");
        check(FileTime.fromMillis(253402300799999L).toString().equals("9999-12-31T23:59:59.999Z"), "year 9999 toString");
        check(FileTime.fromMillis(Long.MAX_VALUE).toString().equals("292278994-08-17T07:12:55.807Z"),
              "far future toString");
        check(FileTime.fromMillis(Long.MIN_VALUE).toString().equals("-292275056-05-16T16:47:04.192Z"),
              "far past toString");
        // value type: FileTime is NOT Serializable (javadoc: no Serializable in signature)
        check(!java.io.Serializable.class.isAssignableFrom(FileTime.class), "FileTime not Serializable");
        checkThrowsIO(java.io.NotSerializableException.class, () -> {
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream());
            oos.writeObject(FileTime.fromMillis(1L));
        }, "ObjectOutputStream rejects FileTime");
        // Files integration: fixed timestamp round trip through the default provider
        java.nio.file.Path tdir = java.nio.file.Files.createTempDirectory("a12-ft");
        try {
            java.nio.file.Path tf = tdir.resolve("t.txt");
            java.nio.file.Files.write(tf, "x".getBytes("UTF-8"));
            java.nio.file.Files.setLastModifiedTime(tf, FileTime.fromMillis(1234567890123L));
            check(java.nio.file.Files.getLastModifiedTime(tf).toMillis() == 1234567890123L,
                  "Files mtime round trip");
            java.nio.file.Files.setLastModifiedTime(tf, FileTime.fromMillis(-1000000000L));
            check(java.nio.file.Files.getLastModifiedTime(tf).toMillis() == -1000000000L,
                  "Files pre-epoch mtime round trip");
        } finally {
            java.nio.file.Files.deleteIfExists(tdir.resolve("t.txt"));
            java.nio.file.Files.deleteIfExists(tdir);
        }
    }

}
