/*
 * @test
 * @summary java.nio.file.attribute.AclEntryType conformance: enum constants.
 * @run main AclEntryTypeTest
 */

import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.UserPrincipal;

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

    public static void main(String[] args) throws Exception {
        testAclEntryType();
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
        AclEntryType[] order = AclEntryType.values();
        String[] names = { "ALLOW", "DENY", "AUDIT", "ALARM" };
        check(order.length == 4, "exactly 4 types");
        for (int i = 0; i < 4; i++) {
            check(order[i].name().equals(names[i]), "values()[" + i + "] is " + names[i]);
            check(order[i].ordinal() == i, "ordinal " + i);
            check(AclEntryType.valueOf(names[i]) == order[i], "valueOf round-trip " + names[i]);
            check(order[i].toString().equals(names[i]), "toString equals name " + names[i]);
        }
        for (int i = 1; i < 4; i++) {
            check(order[i - 1].compareTo(order[i]) < 0, "ordinal order " + names[i - 1] + " < " + names[i]);
        }
        check(java.util.EnumSet.allOf(AclEntryType.class).size() == 4, "EnumSet allOf size 4");
        checkThrows(IllegalArgumentException.class, () -> AclEntryType.valueOf("allow"),
                "valueOf case-sensitive");
        checkThrows(IllegalArgumentException.class, () -> AclEntryType.valueOf("GRANT"),
                "valueOf unknown constant IAE");
        checkThrows(NullPointerException.class, () -> AclEntryType.valueOf(null), "valueOf(null) NPE");
        // integration: each type is accepted by the entry builder
        UserPrincipal p = new UserPrincipal() {
            public String getName() { return "u"; }
            public String toString() { return "u"; }
        };
        for (AclEntryType t : order) {
            check(AclEntry.newBuilder().setType(t).setPrincipal(p).build().type() == t,
                    "builder accepts type " + t);
        }
    }

}
