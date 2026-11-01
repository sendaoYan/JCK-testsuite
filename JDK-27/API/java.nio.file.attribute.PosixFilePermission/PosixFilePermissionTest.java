/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermission conformance: enum constants.
 * @run main PosixFilePermissionTest
 */

import java.nio.file.attribute.PosixFilePermission;

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

    public static void main(String[] args) {
        testPosixFilePermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
