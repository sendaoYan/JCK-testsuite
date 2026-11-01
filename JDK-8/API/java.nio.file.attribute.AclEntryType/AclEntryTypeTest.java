/*
 * @test
 * @summary java.nio.file.attribute.AclEntryType conformance: enum constants.
 * @run main AclEntryTypeTest
 */

import java.nio.file.attribute.AclEntryType;

public class AclEntryTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclEntryType() {
        check(AclEntryType.valueOf("ALLOW") == AclEntryType.ALLOW, "valueOf ALLOW");
        check(AclEntryType.valueOf("DENY") == AclEntryType.DENY, "valueOf DENY");
        check(AclEntryType.valueOf("AUDIT") == AclEntryType.AUDIT, "valueOf AUDIT");
        check(AclEntryType.valueOf("ALARM") == AclEntryType.ALARM, "valueOf ALARM");
        check(AclEntryType.values().length >= 4, "at least 4 values");
        check(AclEntryType.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testAclEntryType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
