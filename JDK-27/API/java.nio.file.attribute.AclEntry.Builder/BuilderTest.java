/*
 * @test
 * @summary java.nio.file.attribute.AclEntry.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class FakeUser implements UserPrincipal {
        public String getName() { return "bob"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclEntry$Builder");
        check(c.getDeclaredConstructors().length > 0, "Builder has constructors");
        check(java.util.Arrays.stream(c.getConstructors()).count() == 0,
              "Builder constructors are not public (obtained via newBuilder)");
        for (String n : new String[] {"setType", "setPrincipal", "setPermissions",
                "setPermissions", "setFlags", "setFlags", "build"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        UserPrincipal u = new FakeUser();
        AclEntry.Builder b = AclEntry.newBuilder();
        check(b != null, "newBuilder returns builder");

        // fluent chaining: each setter returns the same builder
        check(b.setType(AclEntryType.DENY) == b, "setType returns this");
        check(b.setPrincipal(u) == b, "setPrincipal returns this");
        check(b.setPermissions(EnumSet.of(AclEntryPermission.EXECUTE)) == b,
              "setPermissions returns this");
        check(b.setFlags(EnumSet.of(AclEntryFlag.DIRECTORY_INHERIT)) == b,
              "setFlags returns this");
        AclEntry e = b.build();
        check(e.type() == AclEntryType.DENY, "built type");
        check(e.principal() == u, "built principal");
        check(e.permissions().equals(EnumSet.of(AclEntryPermission.EXECUTE)), "built perms");
        check(e.flags().equals(EnumSet.of(AclEntryFlag.DIRECTORY_INHERIT)), "built flags");

        // varargs overloads
        AclEntry e2 = AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                .setPrincipal(u)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA)
                .setFlags(AclEntryFlag.FILE_INHERIT)
                .build();
        check(e2.permissions().size() == 2, "varargs permissions");
        check(e2.flags().size() == 1, "varargs flags");

        // builder can produce multiple independent equal entries
        AclEntry.Builder again = AclEntry.newBuilder().setType(AclEntryType.DENY)
                .setPrincipal(u).setPermissions(AclEntryPermission.EXECUTE);
        AclEntry first = again.build();
        AclEntry second = again.build();
        check(first.equals(second) && first != second, "repeated build() yields equal entries");
        // a builder with unset parts still refuses to build
        boolean bare = false;
        try { AclEntry.newBuilder().build(); }
        catch (IllegalStateException ex) { bare = true; }
        check(bare, "empty builder build() -> IllegalStateException");

        // null arguments rejected per spec
        boolean np = false;
        try { AclEntry.newBuilder().setPermissions((java.util.Set<AclEntryPermission>) null); }
        catch (NullPointerException ex) { np = true; }
        check(np, "null permission set -> NullPointerException");
        boolean nf = false;
        try { AclEntry.newBuilder().setFlags((java.util.Set<AclEntryFlag>) null); }
        catch (NullPointerException ex) { nf = true; }
        check(nf, "null flag set -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
