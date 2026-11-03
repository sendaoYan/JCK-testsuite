/*
 * @test
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
