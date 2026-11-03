/*
 * @test
 * @summary java.nio.file.attribute.AclEntryFlag conformance: white-box structure + black-box behavior.
 * @run main AclEntryFlagTest
 */

import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntry;

public class AclEntryFlagTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclEntryFlag");
        check(c.isEnum(), "AclEntryFlag is an enum");
        check(c.getEnumConstants().length == 4, "exactly 4 constants");
        for (String n : new String[] {"FILE_INHERIT", "DIRECTORY_INHERIT",
                "NO_PROPAGATE_INHERIT", "INHERIT_ONLY"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // exact constant set
        check(AclEntryFlag.values().length == 4, "values length");
        check(AclEntryFlag.valueOf("FILE_INHERIT") == AclEntryFlag.FILE_INHERIT, "valueOf");
        check(AclEntryFlag.valueOf("INHERIT_ONLY") == AclEntryFlag.INHERIT_ONLY, "valueOf 2");
        check(AclEntryFlag.values()[0] == AclEntryFlag.FILE_INHERIT, "declaration order");
        check(AclEntryFlag.values()[3] == AclEntryFlag.INHERIT_ONLY, "last constant");

        // enum machinery: valueOf unknown constant throws
        boolean threw = false;
        try { AclEntryFlag.valueOf("BOGUS"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf unknown -> IllegalArgumentException");

        // usage: flags flow into an AclEntry unchanged
        java.util.EnumSet<AclEntryFlag> flags = java.util.EnumSet.of(
                AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.NO_PROPAGATE_INHERIT);
        AclEntry entry = AclEntry.newBuilder()
                .setType(java.nio.file.attribute.AclEntryType.ALLOW)
                .setPrincipal(new java.nio.file.attribute.UserPrincipal() {
                    public String getName() { return "carol"; } })
                .setPermissions(java.nio.file.attribute.AclEntryPermission.READ_DATA)
                .setFlags(flags)
                .build();
        check(entry.flags().equals(flags), "flags round trip through AclEntry");

        // ordinal distinctness
        for (int i = 0; i < AclEntryFlag.values().length; i++)
            check(AclEntryFlag.values()[i].ordinal() == i, "ordinal " + i);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
