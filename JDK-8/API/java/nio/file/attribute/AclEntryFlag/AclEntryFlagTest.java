/*
 * @test
 * @summary java.nio.file.attribute.AclEntryFlag conformance: enum constants.
 * @run main AclEntryFlagTest
 */

import java.nio.file.attribute.AclEntryFlag;

public class AclEntryFlagTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclEntryFlag() {
        check(AclEntryFlag.valueOf("FILE_INHERIT") == AclEntryFlag.FILE_INHERIT, "valueOf FILE_INHERIT");
        check(AclEntryFlag.valueOf("DIRECTORY_INHERIT") == AclEntryFlag.DIRECTORY_INHERIT, "valueOf DIRECTORY_INHERIT");
        check(AclEntryFlag.valueOf("NO_PROPAGATE_INHERIT") == AclEntryFlag.NO_PROPAGATE_INHERIT, "valueOf NO_PROPAGATE_INHERIT");
        check(AclEntryFlag.valueOf("INHERIT_ONLY") == AclEntryFlag.INHERIT_ONLY, "valueOf INHERIT_ONLY");
        check(AclEntryFlag.values().length >= 4, "at least 4 values");
        check(AclEntryFlag.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testAclEntryFlag();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
