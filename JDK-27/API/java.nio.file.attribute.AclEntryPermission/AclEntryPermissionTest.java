/*
 * @test
 * @summary java.nio.file.attribute.AclEntryPermission conformance: white-box structure + black-box behavior.
 * @run main AclEntryPermissionTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;

public class AclEntryPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclEntryPermission");
        check(c.isEnum(), "AclEntryPermission is an enum");
        check(c.getEnumConstants().length == 14, "exactly 14 constants (JDK-8334333)");
        for (String n : new String[] {"READ_DATA", "WRITE_DATA", "APPEND_DATA", "READ_NAMED_ATTRS",
                "WRITE_NAMED_ATTRS", "EXECUTE", "DELETE_CHILD", "READ_ATTRIBUTES",
                "WRITE_ATTRIBUTES", "DELETE", "READ_ACL", "WRITE_ACL", "WRITE_OWNER",
                "SYNCHRONIZE"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(AclEntryPermission.values().length == 14, "values length");
        // JDK-8334333 aliases: static fields, not enum constants
        check(AclEntryPermission.LIST_DIRECTORY == AclEntryPermission.READ_DATA,
              "LIST_DIRECTORY aliases READ_DATA");
        check(AclEntryPermission.ADD_FILE == AclEntryPermission.WRITE_DATA,
              "ADD_FILE aliases WRITE_DATA");
        check(AclEntryPermission.ADD_SUBDIRECTORY == AclEntryPermission.APPEND_DATA,
              "ADD_SUBDIRECTORY aliases APPEND_DATA");
        check(AclEntryPermission.valueOf("DELETE_CHILD") == AclEntryPermission.DELETE_CHILD,
              "valueOf");
        check(AclEntryPermission.values()[0] == AclEntryPermission.READ_DATA, "first constant");
        check(AclEntryPermission.values()[13] == AclEntryPermission.SYNCHRONIZE,
              "last constant");

        boolean threw = false;
        try { AclEntryPermission.valueOf("NOPE"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf unknown -> IllegalArgumentException");

        // permissions integrate with AclEntry
        java.util.EnumSet<AclEntryPermission> perms = java.util.EnumSet.of(
                AclEntryPermission.READ_DATA, AclEntryPermission.EXECUTE,
                AclEntryPermission.SYNCHRONIZE);
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(new java.nio.file.attribute.UserPrincipal() {
                    public String getName() { return "dave"; } })
                .setPermissions(perms)
                .build();
        check(entry.permissions().equals(perms), "permissions round trip");
        check(!entry.permissions().contains(AclEntryPermission.DELETE), "absent perm absent");

        // EnumSet.of over this type keeps the enum's natural ordering
        check(perms.iterator().next() == AclEntryPermission.READ_DATA, "natural ordering");

        for (int i = 0; i < AclEntryPermission.values().length; i++)
            check(AclEntryPermission.values()[i].ordinal() == i, "ordinal " + i);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
