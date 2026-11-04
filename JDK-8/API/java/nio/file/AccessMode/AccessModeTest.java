/*
 * @test
 * @summary java.nio.file.AccessMode conformance: enum constants.
 * @run main AccessModeTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;

public class AccessModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccessMode() {
        check(AccessMode.valueOf("READ") == AccessMode.READ, "valueOf READ");
        check(AccessMode.valueOf("WRITE") == AccessMode.WRITE, "valueOf WRITE");
        check(AccessMode.valueOf("EXECUTE") == AccessMode.EXECUTE, "valueOf EXECUTE");
        check(AccessMode.values().length >= 3, "at least 3 values");
        check(AccessMode.class.isEnum(), "is enum");
    }

    // ---- enum contract matrix ----
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

    // ---- black-box: provider.checkAccess consumes the modes ----
    static void testCheckAccessBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        FileSystemProvider prov = dir.getFileSystem().provider();
        try {
            Path f = dir.resolve("am.sh");
            Files.write(f, "#!/bin/sh\n".getBytes(StandardCharsets.UTF_8));
            Files.setPosixFilePermissions(f, PosixFilePermissions.fromString("rwxr-x---"));
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
            Files.write(nx, "x".getBytes(StandardCharsets.UTF_8));
            Files.setPosixFilePermissions(nx, PosixFilePermissions.fromString("rw-r--r--"));
            boolean denied = false;
            try { prov.checkAccess(nx, AccessMode.EXECUTE); }
            catch (AccessDeniedException e) { denied = true; }
            check(denied, "EXECUTE on non-executable -> AccessDeniedException");
            prov.checkAccess(nx, AccessMode.READ, AccessMode.WRITE);
            check(true, "same file READ+WRITE passes");

            boolean nsfe = false;
            try { prov.checkAccess(dir.resolve("ghost"), AccessMode.READ); }
            catch (NoSuchFileException e) { nsfe = true; }
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
        testAccessMode();
        testEnumContract();
        testCheckAccessBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
