/*
 * @test
 * @requires os.family == "linux"
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

    // ---- appended: full enum contract matrix ----
    static void testEnumContract() {
        AccessMode[] vals = AccessMode.values();
        check(vals.length == 3, "exactly 3 constants");
        check(vals[0] == AccessMode.READ, "values[0] == READ");
        check(vals[1] == AccessMode.WRITE, "values[1] == WRITE");
        check(vals[2] == AccessMode.EXECUTE, "values[2] == EXECUTE");
        check("READ".equals(AccessMode.READ.name()), "READ name");
        check("WRITE".equals(AccessMode.WRITE.name()), "WRITE name");
        check("EXECUTE".equals(AccessMode.EXECUTE.name()), "EXECUTE name");
        check(AccessMode.READ.ordinal() == 0, "READ ordinal 0");
        check(AccessMode.WRITE.ordinal() == 1, "WRITE ordinal 1");
        check(AccessMode.EXECUTE.ordinal() == 2, "EXECUTE ordinal 2");
        check("READ".equals(AccessMode.READ.toString()), "toString == name");
        check(AccessMode.READ.compareTo(AccessMode.WRITE) < 0, "READ < WRITE");
        check(AccessMode.WRITE.compareTo(AccessMode.EXECUTE) < 0, "WRITE < EXECUTE");
        check(AccessMode.EXECUTE.compareTo(AccessMode.READ) > 0, "EXECUTE > READ");
        check(AccessMode.READ.compareTo(AccessMode.READ) == 0, "compareTo self == 0");
        check(AccessMode.READ.getDeclaringClass() == AccessMode.class, "declaring class");
        check(AccessMode.READ.hashCode() == AccessMode.valueOf("READ").hashCode(),
              "hashCode stable across valueOf");
        java.util.EnumSet<AccessMode> all = java.util.EnumSet.allOf(AccessMode.class);
        check(all.size() == 3 && all.contains(AccessMode.EXECUTE), "EnumSet allOf");
        check(java.util.EnumSet.of(AccessMode.READ, AccessMode.WRITE).size() == 2,
              "EnumSet of two");
        boolean iae = false;
        try { AccessMode.valueOf("read"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf lowercase -> IllegalArgumentException");
        boolean npe = false;
        try { AccessMode.valueOf(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "valueOf(null) -> NullPointerException");
    }

    // ---- appended: provider.checkAccess consumes the modes ----
    static void testCheckAccessBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        var prov = dir.getFileSystem().provider();
        try {
            Path f = dir.resolve("am.sh");
            Files.writeString(f, "#!/bin/sh\n");
            Files.setPosixFilePermissions(f,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-x---"));
            prov.checkAccess(f, AccessMode.READ);
            check(true, "rwx READ passes");
            prov.checkAccess(f, AccessMode.WRITE);
            check(true, "rwx WRITE passes");
            prov.checkAccess(f, AccessMode.EXECUTE);
            check(true, "rwx EXECUTE passes");
            prov.checkAccess(f, AccessMode.READ, AccessMode.WRITE, AccessMode.EXECUTE);
            check(true, "all three at once passes");
            prov.checkAccess(dir, AccessMode.WRITE);
            check(true, "temp dir writable (WRITE)");

            Path nx = dir.resolve("nx.txt");
            Files.writeString(nx, "x");
            Files.setPosixFilePermissions(nx,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
            boolean denied = false;
            try { prov.checkAccess(nx, AccessMode.EXECUTE); }
            catch (java.nio.file.AccessDeniedException e) { denied = true; }
            check(denied, "EXECUTE on non-executable -> AccessDeniedException");
            prov.checkAccess(nx, AccessMode.READ, AccessMode.WRITE);
            check(true, "same file READ+WRITE passes");

            boolean nsfe = false;
            try { prov.checkAccess(dir.resolve("ghost"), AccessMode.READ); }
            catch (java.nio.file.NoSuchFileException e) { nsfe = true; }
            check(nsfe, "missing file READ -> NoSuchFileException");
            boolean npe = false;
            try { prov.checkAccess(null, AccessMode.READ); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "checkAccess(null path) -> NullPointerException");
        } finally {
            try { Files.deleteIfExists(dir.resolve("am.sh")); } catch (Exception ignore) { }
            try { Files.deleteIfExists(dir.resolve("nx.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumContract();
        testCheckAccessBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
