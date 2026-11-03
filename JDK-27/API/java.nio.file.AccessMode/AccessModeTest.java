/*
 * @test
 * @summary java.nio.file.AccessMode conformance: white-box structure + black-box behavior.
 * @run main AccessModeTest
 */

import java.nio.file.AccessMode;
import java.nio.file.Files;
import java.nio.file.Path;

public class AccessModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.AccessMode");
        check(c.isEnum(), "AccessMode is an enum");
        check(c.getEnumConstants().length == 3, "exactly 3 constants");
        for (String n : new String[] {"READ", "WRITE", "EXECUTE"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(AccessMode.values().length == 3, "values length");
        check(AccessMode.values()[0] == AccessMode.READ, "order READ first");
        check(AccessMode.values()[2] == AccessMode.EXECUTE, "EXECUTE last");
        check(AccessMode.valueOf("WRITE") == AccessMode.WRITE, "valueOf");
        boolean threw = false;
        try { AccessMode.valueOf("read"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf case sensitive");

        // the modes map onto real file accessibility via the provider
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("am.sh");
            Files.writeString(f, "#!/bin/sh\n");
            Files.setPosixFilePermissions(f,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-x---"));
            var provider = dir.getFileSystem().provider();
            provider.checkAccess(f, AccessMode.READ, AccessMode.WRITE, AccessMode.EXECUTE);
            check(true, "rwx file passes all three checks");
            boolean denied = false;
            try { provider.checkAccess(dir.resolve("ghost"), AccessMode.READ); }
            catch (java.nio.file.NoSuchFileException e) { denied = true; }
            check(denied, "missing file -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("am.sh"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
