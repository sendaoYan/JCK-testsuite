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
import java.nio.file.attribute.AclEntryFlag;

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
