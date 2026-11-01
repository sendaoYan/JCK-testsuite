/*
 * @test
 * @summary java.nio.file.attribute.AclEntryPermission conformance: enum constants.
 * @run main AclEntryPermissionTest
 */

import java.nio.file.attribute.AclEntryPermission;

public class AclEntryPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclEntryPermission() {
        check(AclEntryPermission.valueOf("READ_DATA") == AclEntryPermission.READ_DATA, "valueOf READ_DATA");
        check(AclEntryPermission.valueOf("WRITE_DATA") == AclEntryPermission.WRITE_DATA, "valueOf WRITE_DATA");
        check(AclEntryPermission.valueOf("APPEND_DATA") == AclEntryPermission.APPEND_DATA, "valueOf APPEND_DATA");
        check(AclEntryPermission.valueOf("READ_NAMED_ATTRS") == AclEntryPermission.READ_NAMED_ATTRS, "valueOf READ_NAMED_ATTRS");
        check(AclEntryPermission.valueOf("WRITE_NAMED_ATTRS") == AclEntryPermission.WRITE_NAMED_ATTRS, "valueOf WRITE_NAMED_ATTRS");
        check(AclEntryPermission.valueOf("EXECUTE") == AclEntryPermission.EXECUTE, "valueOf EXECUTE");
        check(AclEntryPermission.valueOf("DELETE_CHILD") == AclEntryPermission.DELETE_CHILD, "valueOf DELETE_CHILD");
        check(AclEntryPermission.valueOf("READ_ATTRIBUTES") == AclEntryPermission.READ_ATTRIBUTES, "valueOf READ_ATTRIBUTES");
        check(AclEntryPermission.valueOf("WRITE_ATTRIBUTES") == AclEntryPermission.WRITE_ATTRIBUTES, "valueOf WRITE_ATTRIBUTES");
        check(AclEntryPermission.valueOf("DELETE") == AclEntryPermission.DELETE, "valueOf DELETE");
        check(AclEntryPermission.valueOf("READ_ACL") == AclEntryPermission.READ_ACL, "valueOf READ_ACL");
        check(AclEntryPermission.valueOf("WRITE_ACL") == AclEntryPermission.WRITE_ACL, "valueOf WRITE_ACL");
        check(AclEntryPermission.valueOf("WRITE_OWNER") == AclEntryPermission.WRITE_OWNER, "valueOf WRITE_OWNER");
        check(AclEntryPermission.valueOf("SYNCHRONIZE") == AclEntryPermission.SYNCHRONIZE, "valueOf SYNCHRONIZE");
        check(AclEntryPermission.values().length >= 14, "at least 14 values");
        check(AclEntryPermission.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testAclEntryPermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
