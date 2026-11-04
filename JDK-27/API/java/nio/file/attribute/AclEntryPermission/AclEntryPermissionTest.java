/*
 * @test
 * @summary java.nio.file.attribute.AclEntryPermission conformance: white-box structure + black-box behavior.
 * @run main AclEntryPermissionTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;

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
