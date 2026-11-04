/*
 * @test
 * @summary java.nio.file.attribute.AclEntryType conformance: white-box structure + black-box behavior.
 * @run main AclEntryTypeTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;

public class AclEntryTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclEntryType");
        check(c.isEnum(), "AclEntryType is an enum");
        check(c.getEnumConstants().length == 4, "exactly 4 constants");
        for (String n : new String[] {"ALARM", "ALLOW", "AUDIT", "DENY"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(AclEntryType.values().length == 4, "values length");
        check(AclEntryType.valueOf("ALLOW") == AclEntryType.ALLOW, "valueOf ALLOW");
        check(AclEntryType.valueOf("DENY") == AclEntryType.DENY, "valueOf DENY");
        check(AclEntryType.values()[0] == AclEntryType.ALLOW, "declaration order ALLOW first");
        check(AclEntryType.values()[3] == AclEntryType.ALARM, "ALARM last");

        boolean threw = false;
        try { AclEntryType.valueOf("allow"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "valueOf is case sensitive");

        // each type is accepted by AclEntry.Builder
        for (AclEntryType t : AclEntryType.values()) {
            AclEntry e = AclEntry.newBuilder()
                    .setType(t)
                    .setPrincipal(new java.nio.file.attribute.UserPrincipal() {
                        public String getName() { return "u"; } })
                    .setPermissions(AclEntryPermission.READ_DATA)
                    .build();
            check(e.type() == t, "entry stores type " + t);
            check(e.toString().contains(t.name()), "toString exposes type " + t);
        }
        for (int i = 0; i < AclEntryType.values().length; i++)
            check(AclEntryType.values()[i].ordinal() == i, "ordinal " + i);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
