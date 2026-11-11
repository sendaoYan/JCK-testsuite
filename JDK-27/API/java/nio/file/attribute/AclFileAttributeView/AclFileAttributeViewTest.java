/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.attribute.AclFileAttributeView conformance: white-box structure + black-box behavior.
 * @run main AclFileAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.Collections;
import java.util.List;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.AttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.FileOwnerAttributeView;

public class AclFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AclFileAttributeView");
        check(c.isInterface(), "AclFileAttributeView is an interface");
        check(java.nio.file.attribute.FileOwnerAttributeView.class.isAssignableFrom(c),
              "extends FileOwnerAttributeView");
        for (String n : new String[] {"name", "getAcl", "setAcl"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("acl.txt");
            Files.writeString(file, "acl");
            AclFileAttributeView view = Files.getFileAttributeView(file,
                    AclFileAttributeView.class);
            if (view == null) {
                System.out.println("NOTE: acl view unsupported here; behavioral part skipped");
                return;
            }
            check(view.name().equals("acl"), "view name is acl");

            List<AclEntry> original = view.getAcl();
            check(original != null && !original.isEmpty(), "existing ACL non-empty");

            // round trip: setAcl with the same list changes nothing observable
            view.setAcl(original);
            check(view.getAcl().equals(original), "setAcl/getAcl round trip");

            // an ALLOW entry for the current owner is honored and readable back
            AclEntry mine = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(view.getOwner())
                    .setPermissions(Collections.singleton(AclEntryPermission.READ_DATA))
                    .build();
            view.setAcl(List.of(mine));
            List<AclEntry> now = view.getAcl();
            check(now.size() == 1 && now.get(0).type() == AclEntryType.ALLOW
                  && now.get(0).principal().equals(view.getOwner()),
                  "single ALLOW entry visible");

            // FileOwnerAttributeView face works through the same object
            check(view.getOwner() != null, "getOwner non-null");
            view.setOwner(view.getOwner());
            check(true, "setOwner with same owner succeeds");

            // restore the original ACL, then string-form read works too
            view.setAcl(original);
            Object aclAttr = Files.getAttribute(file, "acl:acl");
            check(aclAttr instanceof List && !((List<?>) aclAttr).isEmpty(),
                  "string-form acl readable");

            // invalid: null / empty semantics
            boolean npe = false;
            try { view.setAcl(null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "setAcl(null) -> NullPointerException");

            // a missing file's view operations fail with NoSuchFileException
            AclFileAttributeView missing = Files.getFileAttributeView(
                    dir.resolve("nope"), AclFileAttributeView.class);
            boolean nsf = false;
            try { if (missing != null) missing.getAcl(); }
            catch (java.nio.file.NoSuchFileException e) { nsf = true; }
            check(nsf, "getAcl on missing file -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("acl.txt"));
            Files.deleteIfExists(dir);
        }
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

    static void testContract() throws Exception {
        // interface hierarchy chain: Acl -> Owner -> FileAttributeView -> AttributeView
        check(FileOwnerAttributeView.class.isAssignableFrom(AclFileAttributeView.class),
              "extends FileOwnerAttributeView");
        check(FileAttributeView.class.isAssignableFrom(AclFileAttributeView.class),
              "extends FileAttributeView (via owner)");
        check(AttributeView.class.isAssignableFrom(AclFileAttributeView.class),
              "extends AttributeView (transitively)");
        check(AclFileAttributeView.class.isInterface(), "is an interface");
        // declared/visible method surface
        java.util.Set<String> methodNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : AclFileAttributeView.class.getMethods()) methodNames.add(m.getName());
        for (String n : new String[] { "getAcl", "setAcl", "name", "getOwner", "setOwner" }) {
            check(methodNames.contains(n), "method " + n + " visible");
        }
        for (java.lang.reflect.Method m : AclFileAttributeView.class.getMethods()) {
            if (m.getName().equals("setAcl")) {
                check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == java.util.List.class,
                      "setAcl takes List");
            }
            if (m.getName().equals("getAcl")) {
                check(m.getReturnType() == java.util.List.class, "getAcl returns List");
            }
        }
        // the interface is implementable by user code (contract surface usable)
        AclFileAttributeView custom = new AclFileAttributeView() {
            public String name() { return "acl"; }
            public java.util.List<AclEntry> getAcl() { return java.util.Collections.<AclEntry>emptyList(); }
            public void setAcl(java.util.List<AclEntry> list) { }
            public UserPrincipal getOwner() { return null; }
            public void setOwner(UserPrincipal owner) { }
        };
        check("acl".equals(custom.name()), "custom implementation name contract");
        check(custom.getAcl().isEmpty(), "custom implementation getAcl");
        // runtime: Linux ext4 has no ACL file attribute view (probed on this machine)
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-acl");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            AclFileAttributeView v = java.nio.file.Files.getFileAttributeView(f, AclFileAttributeView.class);
            if (v == null) {
                check(true, "acl view unavailable on ext4 (Linux): getFileAttributeView returns null");
                checkThrowsIO(java.lang.UnsupportedOperationException.class,
                        () -> java.nio.file.Files.getAttribute(f, "acl:acl"),
                        "getAttribute acl: unsupported view UOE");
                checkThrowsIO(java.lang.UnsupportedOperationException.class,
                        () -> java.nio.file.Files.setAttribute(f, "acl:acl", java.util.Collections.emptyList()),
                        "setAttribute acl: unsupported view UOE");
            } else {
                // defensive branch for filesystems that do support ACLs (not this machine)
                java.util.List<AclEntry> before = v.getAcl();
                v.setAcl(before);
                check(v.getAcl().equals(before), "acl round-trip on supporting fs");
                check("acl".equals(v.name()), "acl view name on supporting fs");
            }
            java.nio.file.FileStore st = java.nio.file.Files.getFileStore(f);
            check(!st.supportsFileAttributeView("acl"), "FileStore does not support acl view");
            check(!java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("acl"),
                  "acl not in supportedFileAttributeViews");
            check(st.supportsFileAttributeView("posix"), "FileStore does support posix view (contrast)");
        } finally {
            cleanup(dir);
        }
    }

}
