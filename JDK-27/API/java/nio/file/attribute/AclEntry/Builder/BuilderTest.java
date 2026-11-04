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
        final UserPrincipal u = new UserPrincipal() {
            public String getName() { return "carol"; }
            public String toString() { return "carol"; }
        };
        // build() requires type first, then principal (exact ISEs, message probed)
        checkThrows(java.lang.IllegalStateException.class, () -> AclEntry.newBuilder().build(),
                "build without type/principal");
        checkThrows(java.lang.IllegalStateException.class,
                () -> AclEntry.newBuilder().setPrincipal(u).build(), "build without type");
        checkThrows(java.lang.IllegalStateException.class,
                () -> AclEntry.newBuilder().setType(AclEntryType.ALLOW).build(), "build without principal");
        try {
            AclEntry.newBuilder().build();
            check(false, "must throw");
        } catch (IllegalStateException e) {
            check("Missing type component".equals(e.getMessage()), "missing-type ISE message");
        }
        try {
            AclEntry.newBuilder().setType(AclEntryType.ALLOW).build();
            check(false, "must throw");
        } catch (IllegalStateException e) {
            check("Missing who component".equals(e.getMessage()), "missing-who ISE message");
        }
        // setter null matrix -> NPE
        checkThrows(NullPointerException.class, () -> AclEntry.newBuilder().setType(null), "setType(null)");
        checkThrows(NullPointerException.class, () -> AclEntry.newBuilder().setPrincipal(null),
                "setPrincipal(null)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setPermissions((java.util.Set<AclEntryPermission>) null),
                "setPermissions((Set)null)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setPermissions((AclEntryPermission[]) null),
                "setPermissions((varargs)null)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setPermissions(AclEntryPermission.READ_DATA, null),
                "setPermissions(varargs with null element)");
        java.util.Set<AclEntryPermission> permWithNull = new java.util.HashSet<AclEntryPermission>();
        permWithNull.add(null);
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setPermissions(permWithNull), "setPermissions(Set with null element)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setFlags((java.util.Set<AclEntryFlag>) null), "setFlags((Set)null)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setFlags((AclEntryFlag[]) null), "setFlags((varargs)null)");
        checkThrows(NullPointerException.class,
                () -> AclEntry.newBuilder().setFlags(AclEntryFlag.FILE_INHERIT, null),
                "setFlags(varargs with null element)");
        java.util.Set<AclEntryFlag> flagWithNull = new java.util.HashSet<AclEntryFlag>();
        flagWithNull.add(null);
        checkThrows(NullPointerException.class, () -> AclEntry.newBuilder().setFlags(flagWithNull),
                "setFlags(Set with null element)");
        // fluent chaining: every setter returns the same builder instance
        AclEntry.Builder b = AclEntry.newBuilder();
        check(b.setType(AclEntryType.ALLOW) == b, "setType returns this");
        check(b.setPrincipal(u) == b, "setPrincipal returns this");
        check(b.setPermissions(AclEntryPermission.READ_DATA) == b, "setPermissions(varargs) returns this");
        check(b.setPermissions(java.util.EnumSet.of(AclEntryPermission.READ_DATA)) == b,
                "setPermissions(Set) returns this");
        check(b.setFlags(AclEntryFlag.FILE_INHERIT) == b, "setFlags(varargs) returns this");
        check(b.setFlags(java.util.EnumSet.of(AclEntryFlag.FILE_INHERIT)) == b, "setFlags(Set) returns this");
        // builder is reusable: repeated build() yields equal (distinct) entries
        AclEntry e1 = b.build();
        check(b.build().equals(e1), "second build equal");
        check(b.build() != e1, "each build a new object");
        // late setters override earlier ones
        check(b.setType(AclEntryType.DENY).build().type() == AclEntryType.DENY, "setType overrides");
        check(b.setPrincipal(new UserPrincipal() {
            public String getName() { return "dave"; }
            public String toString() { return "dave"; }
        }).build().principal().getName().equals("dave"), "setPrincipal overrides");
        check(b.setPermissions(AclEntryPermission.DELETE).build().permissions()
                .equals(java.util.EnumSet.of(AclEntryPermission.DELETE)), "setPermissions overrides");
        check(b.setFlags(AclEntryFlag.INHERIT_ONLY).build().flags()
                .equals(java.util.EnumSet.of(AclEntryFlag.INHERIT_ONLY)), "setFlags overrides");
        // permissions snapshot is taken when setPermissions runs
        java.util.Set<AclEntryPermission> src = new java.util.HashSet<AclEntryPermission>(
                java.util.EnumSet.of(AclEntryPermission.READ_DATA));
        AclEntry snap = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(src).build();
        src.add(AclEntryPermission.WRITE_DATA);
        check(snap.permissions().size() == 1, "permissions snapshotted at set time");
        // empty varargs produce empty sets
        AclEntry empty = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(new AclEntryPermission[0]).setFlags(new AclEntryFlag[0]).build();
        check(empty.permissions().isEmpty() && empty.flags().isEmpty(), "empty varargs -> empty sets");
        // newBuilder(entry) prefills all four components
        AclEntry orig = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(java.util.EnumSet.of(AclEntryPermission.READ_DATA))
                .setFlags(java.util.EnumSet.of(AclEntryFlag.FILE_INHERIT)).build();
        AclEntry pref = AclEntry.newBuilder(orig).build();
        check(pref.type() == orig.type() && pref.principal().equals(orig.principal())
              && pref.permissions().equals(orig.permissions()) && pref.flags().equals(orig.flags()),
              "newBuilder(entry) prefills all components");
        checkThrows(java.lang.NullPointerException.class, () -> AclEntry.newBuilder(null),
                "newBuilder(null) NPE");
        // structural: builder is a final class with package-private constructor
        check(java.lang.reflect.Modifier.isFinal(AclEntry.Builder.class.getModifiers()), "Builder is final");
        check(AclEntry.Builder.class.getConstructors().length == 0, "Builder has no public constructor");
    }

}
