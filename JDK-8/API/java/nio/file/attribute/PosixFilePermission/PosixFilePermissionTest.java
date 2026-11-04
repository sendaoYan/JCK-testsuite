/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermission conformance: enum constants.
 * @run main PosixFilePermissionTest
 */

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

public class PosixFilePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFilePermission() {
        check(PosixFilePermission.valueOf("OWNER_READ") == PosixFilePermission.OWNER_READ, "valueOf OWNER_READ");
        check(PosixFilePermission.valueOf("OWNER_WRITE") == PosixFilePermission.OWNER_WRITE, "valueOf OWNER_WRITE");
        check(PosixFilePermission.valueOf("OWNER_EXECUTE") == PosixFilePermission.OWNER_EXECUTE, "valueOf OWNER_EXECUTE");
        check(PosixFilePermission.valueOf("GROUP_READ") == PosixFilePermission.GROUP_READ, "valueOf GROUP_READ");
        check(PosixFilePermission.valueOf("GROUP_WRITE") == PosixFilePermission.GROUP_WRITE, "valueOf GROUP_WRITE");
        check(PosixFilePermission.valueOf("GROUP_EXECUTE") == PosixFilePermission.GROUP_EXECUTE, "valueOf GROUP_EXECUTE");
        check(PosixFilePermission.valueOf("OTHERS_READ") == PosixFilePermission.OTHERS_READ, "valueOf OTHERS_READ");
        check(PosixFilePermission.valueOf("OTHERS_WRITE") == PosixFilePermission.OTHERS_WRITE, "valueOf OTHERS_WRITE");
        check(PosixFilePermission.valueOf("OTHERS_EXECUTE") == PosixFilePermission.OTHERS_EXECUTE, "valueOf OTHERS_EXECUTE");
        check(PosixFilePermission.values().length >= 9, "at least 9 values");
        check(PosixFilePermission.class.isEnum(), "is enum");
    }

    public static void main(String[] args) throws Exception {
        testPosixFilePermission();
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
