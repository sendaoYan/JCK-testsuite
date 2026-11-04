/*
 * @test
 * @summary java.nio.file.attribute.AclEntry conformance: type/principal/permissions.
 * @run main AclEntryTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.FileSystems;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.nio.file.attribute.AclEntryFlag;

public class AclEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclEntry() throws Exception {
        UserPrincipal owner = FileSystems.getDefault().getUserPrincipalLookupService()
                .lookupPrincipalByName(System.getProperty("user.name"));
        Set<AclEntryPermission> perms = new HashSet<AclEntryPermission>();
        perms.add(AclEntryPermission.READ_DATA);
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(perms)
                .build();
        check(entry.type() == AclEntryType.ALLOW, "type");
        check(entry.principal().equals(owner), "principal");
        check(entry.permissions().contains(AclEntryPermission.READ_DATA), "permissions");
        check(entry.flags().isEmpty(), "flags empty");
        check(entry.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testAclEntry();
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
            public String getName() { return "alice"; }
            public String toString() { return "alice"; }
        };
        final UserPrincipal v = new UserPrincipal() {
            public String getName() { return "bob"; }
            public String toString() { return "bob"; }
        };
        java.util.Set<AclEntryPermission> rd = java.util.EnumSet.of(AclEntryPermission.READ_DATA);
        java.util.Set<AclEntryPermission> wd = java.util.EnumSet.of(AclEntryPermission.WRITE_DATA);
        java.util.Set<AclEntryFlag> fi = java.util.EnumSet.of(AclEntryFlag.FILE_INHERIT);
        AclEntry base = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(rd).setFlags(fi).build();
        // getters return exactly what was configured
        check(base.type() == AclEntryType.ALLOW, "type getter");
        check(base.principal().equals(u), "principal getter");
        check(base.permissions().equals(rd), "permissions getter");
        check(base.flags().equals(fi), "flags getter");
        // permissions()/flags() hand out fresh copies each call
        java.util.Set<AclEntryPermission> pc1 = base.permissions();
        pc1.add(AclEntryPermission.DELETE);
        check(base.permissions().size() == 1, "permissions() is a per-call copy");
        java.util.Set<AclEntryFlag> fc1 = base.flags();
        fc1.add(AclEntryFlag.DIRECTORY_INHERIT);
        check(base.flags().size() == 1, "flags() is a per-call copy");
        check(base.permissions() != base.permissions(), "permissions() new object per call");
        // snapshot isolation: mutating the source set after build does not affect the entry
        java.util.Set<AclEntryPermission> mutable = new java.util.HashSet<AclEntryPermission>(rd);
        AclEntry isolated = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(mutable).build();
        mutable.add(AclEntryPermission.EXECUTE);
        check(isolated.permissions().size() == 1, "entry permissions isolated from source set");
        // equals/hashCode value semantics over all four components
        AclEntry same = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(java.util.EnumSet.of(AclEntryPermission.READ_DATA))
                .setFlags(java.util.EnumSet.of(AclEntryFlag.FILE_INHERIT)).build();
        check(base.equals(same), "equal entries");
        check(base.hashCode() == same.hashCode(), "hashCode consistent with equals");
        check(!base.equals(AclEntry.newBuilder(base).setType(AclEntryType.DENY).build()),
              "different type not equal");
        check(!base.equals(AclEntry.newBuilder(base).setPrincipal(v).build()), "different principal not equal");
        check(!base.equals(AclEntry.newBuilder(base).setPermissions(wd).build()), "different permissions not equal");
        check(!base.equals(AclEntry.newBuilder(base).setFlags(java.util.EnumSet.noneOf(AclEntryFlag.class)).build()),
              "different flags not equal");
        check(!base.equals(null), "equals null is false");
        check(!base.equals("entry"), "equals foreign type is false");
        // varargs and Set overloads produce equal entries
        check(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(AclEntryPermission.READ_DATA).build()
                .equals(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                        .setPermissions(rd).build()), "varargs equals Set overload");
        // defaults: permissions/flags default to empty sets
        AclEntry bare = AclEntry.newBuilder().setType(AclEntryType.AUDIT).setPrincipal(u).build();
        check(bare.permissions().isEmpty(), "default permissions empty");
        check(bare.flags().isEmpty(), "default flags empty");
        // toString exact formats (principal[:perms][/...][:flags][/...]:type, empty sections omitted)
        check(bare.toString().equals("alice:AUDIT"), "toString bare format");
        check(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(rd).build().toString().equals("alice:READ_DATA:ALLOW"),
              "toString perms-only format");
        check(AclEntry.newBuilder().setType(AclEntryType.AUDIT).setPrincipal(u)
                .setFlags(fi).build().toString().equals("alice:FILE_INHERIT:AUDIT"),
              "toString flags-only format");
        check(base.toString().equals("alice:READ_DATA:FILE_INHERIT:ALLOW"), "toString full format");
        check(AclEntry.newBuilder().setType(AclEntryType.DENY).setPrincipal(u)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA).build()
                .toString().equals("alice:READ_DATA/WRITE_DATA:DENY"), "toString multi-perm format");
        check(AclEntry.newBuilder().setType(AclEntryType.ALARM).setPrincipal(u).build().toString()
                .equals("alice:ALARM"), "toString ALARM bare");
        // full-width entries carry all 14 permissions and all 4 flags
        AclEntry wide = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(u)
                .setPermissions(java.util.EnumSet.allOf(AclEntryPermission.class))
                .setFlags(java.util.EnumSet.allOf(AclEntryFlag.class)).build();
        check(wide.permissions().size() == 14, "all 14 permissions");
        check(wide.flags().size() == 4, "all 4 flags");
        check(wide.toString().startsWith("alice:"), "toString starts with principal");
        check(wide.toString().endsWith(":ALLOW"), "toString ends with type");
        // newBuilder(entry) copies: modifying the derived builder leaves the original untouched
        AclEntry copy = AclEntry.newBuilder(base).build();
        check(copy.equals(base) && copy != base, "newBuilder(entry) equal copy");
        AclEntry mutated = AclEntry.newBuilder(base).setType(AclEntryType.ALARM).build();
        check(mutated.type() == AclEntryType.ALARM, "derived builder type override");
        check(base.type() == AclEntryType.ALLOW, "original entry immutable after derived build");
        // structural: final value class, no public constructors, no clone/Serializable
        check(java.lang.reflect.Modifier.isFinal(AclEntry.class.getModifiers()), "AclEntry is final");
        check(AclEntry.class.getConstructors().length == 0, "no public constructors (factory only)");
        boolean hasClone = false;
        try { AclEntry.class.getMethod("clone"); hasClone = true; } catch (NoSuchMethodException ignore) { }
        check(!hasClone, "no clone method (immutable value object)");
        check(!java.io.Serializable.class.isAssignableFrom(AclEntry.class), "not Serializable");
    }

}
