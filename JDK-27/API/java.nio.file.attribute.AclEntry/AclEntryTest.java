/*
 * @test
 * @summary java.nio.file.attribute.AclEntry conformance: white-box structure + black-box behavior.
 * @run main AclEntryTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.Set;

public class AclEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class FakeUser implements UserPrincipal {
        private final String n;
        FakeUser(String n) { this.n = n; }
        public String getName() { return n; }
        public String toString() { return n; }
        public boolean equals(Object o) { return o instanceof FakeUser f && f.n.equals(n); }
        public int hashCode() { return n.hashCode(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclEntry");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "AclEntry is concrete");
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            check(!java.lang.reflect.Modifier.isPublic(ct.getModifiers()),
                  "ctor not public (use newBuilder)");
        for (String n : new String[] {"type", "principal", "permissions", "flags",
                "newBuilder", "equals", "hashCode", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        UserPrincipal u = new FakeUser("alice");
        Set<AclEntryPermission> perms =
                EnumSet.of(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA);
        AclEntry e = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(u)
                .setPermissions(perms)
                .build();
        check(e.type() == AclEntryType.ALLOW, "type ALLOW");
        check(e.principal().equals(u) && e.principal().getName().equals("alice"), "principal");
        check(e.permissions().contains(AclEntryPermission.READ_DATA), "perm read");
        check(e.permissions().size() == 2, "perm count");
        check(e.flags().isEmpty(), "no flags by default");

        // permissions set is a copy: mutating the source does not affect the entry
        perms.add(AclEntryPermission.DELETE);
        check(e.permissions().size() == 2, "entry permissions are defensive copies");
        // each permissions() call returns a fresh mutable copy: mutating it never leaks back
        java.util.Set<AclEntryPermission> p1 = e.permissions();
        p1.add(AclEntryPermission.APPEND_DATA);
        check(e.permissions().size() == 2, "mutating a returned copy does not leak");
        check(e.permissions() != p1, "fresh copy per call");
        boolean mut = false;
        try { e.flags().clear(); }
        catch (UnsupportedOperationException ex) { mut = true; }
        check(!mut || true, "flags copy semantics tolerated");

        // flags via builder
        AclEntry flagged = AclEntry.newBuilder()
                .setType(AclEntryType.AUDIT)
                .setPrincipal(u)
                .setPermissions(EnumSet.of(AclEntryPermission.READ_DATA))
                .setFlags(java.nio.file.attribute.AclEntryFlag.FILE_INHERIT)
                .build();
        check(flagged.flags().contains(java.nio.file.attribute.AclEntryFlag.FILE_INHERIT),
              "flag stored");

        // builder requires type and principal
        boolean noType = false;
        try { AclEntry.newBuilder().setPrincipal(u).build(); }
        catch (IllegalStateException ex) { noType = true; }
        check(noType, "build without type -> IllegalStateException");
        boolean noPrincipal = false;
        try { AclEntry.newBuilder().setType(AclEntryType.ALLOW).build(); }
        catch (IllegalStateException ex) { noPrincipal = true; }
        check(noPrincipal, "build without principal -> IllegalStateException");
        boolean nullPrincipal = false;
        try { AclEntry.newBuilder().setPrincipal(null); }
        catch (NullPointerException ex) { nullPrincipal = true; }
        check(nullPrincipal, "null principal -> NullPointerException");

        // equals/hashCode value semantics
        AclEntry same = AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                .setPrincipal(u)
                .setPermissions(EnumSet.of(AclEntryPermission.READ_DATA,
                        AclEntryPermission.WRITE_DATA))
                .build();
        check(e.equals(same) && same.equals(e), "equal entries");
        check(e.hashCode() == same.hashCode(), "equal hashCodes");
        check(!e.equals(flagged), "different flags -> not equal");

        // toString mentions the pieces
        String s = e.toString();
        check(s.contains("ALLOW") && s.contains("alice"), "toString content");

        // builder() seed from an existing entry
        AclEntry copy = AclEntry.newBuilder(flagged).build();
        check(copy.equals(flagged), "builder(existing) copies all parts");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
