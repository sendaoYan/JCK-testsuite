/*
 * @test
 * @summary java.nio.file.attribute.AclEntryPermission conformance: enum constants.
 * @run main AclEntryPermissionTest
 */

import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;

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

    public static void main(String[] args) throws Exception {
        testAclEntryPermission();
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
        AclEntryPermission[] order = AclEntryPermission.values();
        String[] names = { "READ_DATA", "WRITE_DATA", "APPEND_DATA", "READ_NAMED_ATTRS",
                "WRITE_NAMED_ATTRS", "EXECUTE", "DELETE_CHILD", "READ_ATTRIBUTES",
                "WRITE_ATTRIBUTES", "DELETE", "READ_ACL", "WRITE_ACL", "WRITE_OWNER", "SYNCHRONIZE" };
        check(order.length == 14, "exactly 14 permissions (spec: NFSv4 ACL)");
        for (int i = 0; i < 14; i++) {
            check(order[i].name().equals(names[i]), "values()[" + i + "] is " + names[i]);
            check(order[i].ordinal() == i, "ordinal " + i + " for " + names[i]);
            check(AclEntryPermission.valueOf(names[i]) == order[i], "valueOf round-trip " + names[i]);
        }
        check(java.util.EnumSet.allOf(AclEntryPermission.class).size() == 14, "EnumSet allOf size 14");
        check(java.util.EnumSet.noneOf(AclEntryPermission.class).isEmpty(), "EnumSet noneOf empty");
        checkThrows(IllegalArgumentException.class, () -> AclEntryPermission.valueOf("READ"),
                "valueOf unknown constant IAE");
        checkThrows(IllegalArgumentException.class, () -> AclEntryPermission.valueOf("read_data"),
                "valueOf case-sensitive");
        checkThrows(NullPointerException.class, () -> AclEntryPermission.valueOf(null), "valueOf(null) NPE");
        // integration: an entry can carry all 14 permissions at once
        UserPrincipal p = new UserPrincipal() {
            public String getName() { return "u"; }
            public String toString() { return "u"; }
        };
        AclEntry e = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(p)
                .setPermissions(java.util.EnumSet.allOf(AclEntryPermission.class)).build();
        check(e.permissions().size() == 14, "entry carries all 14 permissions");
        check(e.permissions().contains(AclEntryPermission.READ_DATA)
              && e.permissions().contains(AclEntryPermission.SYNCHRONIZE), "entry permission members");
    }

}
