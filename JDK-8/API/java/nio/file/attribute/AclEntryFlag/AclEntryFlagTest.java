/*
 * @test
 * @summary java.nio.file.attribute.AclEntryFlag conformance: enum constants.
 * @run main AclEntryFlagTest
 */

import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;

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

    public static void main(String[] args) throws Exception {
        testAclEntryFlag();
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
        AclEntryFlag[] order = AclEntryFlag.values();
        String[] names = { "FILE_INHERIT", "DIRECTORY_INHERIT", "NO_PROPAGATE_INHERIT", "INHERIT_ONLY" };
        check(order.length == 4, "exactly 4 flags (spec: NFSv4 inheritance model)");
        for (int i = 0; i < 4; i++) {
            check(order[i].name().equals(names[i]), "values()[" + i + "] is " + names[i]);
            check(order[i].ordinal() == i, "ordinal " + i);
            check(AclEntryFlag.valueOf(names[i]) == order[i], "valueOf round-trip " + names[i]);
            check(order[i].toString().equals(names[i]), "toString equals name " + names[i]);
            check(order[i].getDeclaringClass() == AclEntryFlag.class, "declaring class " + names[i]);
        }
        for (int i = 1; i < 4; i++) {
            check(order[i - 1].compareTo(order[i]) < 0, "ordinal order " + names[i - 1] + " < " + names[i]);
        }
        check(java.util.EnumSet.allOf(AclEntryFlag.class).size() == 4, "EnumSet allOf size 4");
        check(java.util.EnumSet.noneOf(AclEntryFlag.class).isEmpty(), "EnumSet noneOf empty");
        checkThrows(IllegalArgumentException.class, () -> AclEntryFlag.valueOf("FILE_INHERIT_X"),
                "valueOf unknown constant IAE");
        checkThrows(IllegalArgumentException.class, () -> AclEntryFlag.valueOf("file_inherit"),
                "valueOf case-sensitive");
        checkThrows(NullPointerException.class, () -> AclEntryFlag.valueOf(null), "valueOf(null) NPE");
        // integration: an entry can carry all 4 flags at once
        UserPrincipal p = new UserPrincipal() {
            public String getName() { return "u"; }
            public String toString() { return "u"; }
        };
        AclEntry e = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(p)
                .setFlags(java.util.EnumSet.allOf(AclEntryFlag.class)).build();
        check(e.flags().size() == 4, "entry carries all 4 flags");
        check(e.flags().containsAll(java.util.EnumSet.allOf(AclEntryFlag.class)), "entry flags members");
    }

}
