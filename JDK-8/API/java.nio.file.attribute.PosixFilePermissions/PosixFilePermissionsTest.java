/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermissions conformance: parse/format.
 * @run main PosixFilePermissionsTest
 */

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class PosixFilePermissionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFilePermissions() {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-r--r--");
        check(perms.size() == 4, "fromString has 4 perms");
        check(perms.contains(PosixFilePermission.OWNER_READ), "owner read");
        check(perms.contains(PosixFilePermission.OWNER_WRITE), "owner write");
        check(perms.contains(PosixFilePermission.OTHERS_READ), "others read");
        check(!perms.contains(PosixFilePermission.OWNER_EXECUTE), "no owner execute");
        check("rw-r--r--".equals(PosixFilePermissions.toString(perms)), "toString");
        PosixFilePermissions.asFileAttribute(perms);
        check(true, "asFileAttribute callable");
    }

    public static void main(String[] args) {
        testPosixFilePermissions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
