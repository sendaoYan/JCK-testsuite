/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermission conformance: white-box structure + black-box behavior.
 * @run main PosixFilePermissionTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.nio.file.attribute.PosixFilePermissions;

public class PosixFilePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.PosixFilePermission");
        check(c.isEnum(), "PosixFilePermission is an enum");
        check(c.getEnumConstants().length == 9, "exactly 9 constants");
        for (String n : new String[] {"OWNER_READ", "OWNER_WRITE", "OWNER_EXECUTE",
                "GROUP_READ", "GROUP_WRITE", "GROUP_EXECUTE",
                "OTHERS_READ", "OTHERS_WRITE", "OTHERS_EXECUTE"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PosixFilePermission.values().length == 9, "values length");
        check(PosixFilePermission.values()[0] == PosixFilePermission.OWNER_READ, "first");
        check(PosixFilePermission.values()[8] == PosixFilePermission.OTHERS_EXECUTE, "last");
        check(PosixFilePermission.valueOf("GROUP_WRITE") == PosixFilePermission.GROUP_WRITE,
              "valueOf");

        boolean threw = false;
        try { PosixFilePermission.valueOf("owner_read"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf case sensitive");

        if (!java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                .contains("posix")) {
            System.out.println("NOTE: posix unsupported; fs part skipped");
            return;
        }
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("perm.txt");
            Files.writeString(file, "p");

            // each permission bit is observable on a real file
            EnumSet<PosixFilePermission> rw =
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file, rw);
            check(Files.getPosixFilePermissions(file).equals(rw), "rw round trip");

            // all-bits and single-bit extremes
            Files.setPosixFilePermissions(file, EnumSet.allOf(PosixFilePermission.class));
            check(Files.getPosixFilePermissions(file).size() == 9, "all 9 bits settable");
            EnumSet<PosixFilePermission> onlyX =
                    EnumSet.of(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(file, onlyX);
            check(Files.getPosixFilePermissions(file).equals(onlyX), "single bit round trip");

            // PosixFilePermissions.fromString interop with these constants
            check(java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-x---")
                    .equals(EnumSet.of(PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE)),
                  "rwxr-x--- maps to constants");
        } finally {
            Files.deleteIfExists(dir.resolve("perm.txt"));
            Files.deleteIfExists(dir);
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

    static void testContract() {
        PosixFilePermission[] order = PosixFilePermission.values();
        String[] names = { "OWNER_READ", "OWNER_WRITE", "OWNER_EXECUTE",
                           "GROUP_READ", "GROUP_WRITE", "GROUP_EXECUTE",
                           "OTHERS_READ", "OTHERS_WRITE", "OTHERS_EXECUTE" };
        check(order.length == 9, "exactly 9 constants (spec fixes 3x3 matrix)");
        for (int i = 0; i < 9; i++) {
            check(order[i].name().equals(names[i]), "values()[" + i + "] name is " + names[i]);
            check(order[i].ordinal() == i, "values()[" + i + "] ordinal " + i);
            check(PosixFilePermission.valueOf(names[i]) == order[i], "valueOf round-trip " + names[i]);
            check(order[i].toString().equals(names[i]), "toString equals name " + names[i]);
            check(order[i].getDeclaringClass() == PosixFilePermission.class, "declaring class " + names[i]);
            check(order[i].compareTo(order[i]) == 0, "compareTo self " + names[i]);
        }
        // natural order follows declaration order
        for (int i = 1; i < 9; i++) {
            check(order[i - 1].compareTo(order[i]) < 0, "ordinal order " + names[i - 1] + " < " + names[i]);
        }
        // enum semantics
        check(PosixFilePermission.class.isEnum(), "is enum");
        check(java.util.Arrays.equals(PosixFilePermission.class.getEnumConstants(), order),
              "getEnumConstants same constants");
        checkThrows(IllegalArgumentException.class, () -> PosixFilePermission.valueOf("owner_read"),
                "valueOf case-sensitive");
        checkThrows(IllegalArgumentException.class, () -> PosixFilePermission.valueOf("NO_SUCH"),
                "valueOf unknown constant IAE");
        checkThrows(NullPointerException.class, () -> PosixFilePermission.valueOf(null), "valueOf(null) NPE");
        // integration: every constant round-trips through the permission-string codec
        java.util.Set<PosixFilePermission> all = PosixFilePermissions.fromString("rwxrwxrwx");
        for (PosixFilePermission p : order) {
            check(all.contains(p), "fromString rwxrwxrwx contains " + p);
        }
        check(java.util.EnumSet.allOf(PosixFilePermission.class).size() == 9, "EnumSet allOf size 9");
        check(java.util.EnumSet.noneOf(PosixFilePermission.class).isEmpty(), "EnumSet noneOf empty");
    }

}
