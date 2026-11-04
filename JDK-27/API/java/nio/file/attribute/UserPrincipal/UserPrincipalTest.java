/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipal conformance: white-box structure + black-box behavior.
 * @run main UserPrincipalTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class UserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserPrincipal");
        check(c.isInterface(), "UserPrincipal is an interface");
        check(java.security.Principal.class.isAssignableFrom(c), "extends Principal");
        check(c.getDeclaredMethods().length == 0, "adds no methods beyond Principal");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("u.txt");
            Files.writeString(file, "u");
            UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();

            // lookup of the current user works and equals the file's owner
            String me = System.getProperty("user.name");
            UserPrincipal principal = lookup.lookupPrincipalByName(me);
            check(principal != null, "current user principal found");
            check(principal.getName() != null, "name non-null");
            UserPrincipal owner = Files.getOwner(file);
            check(owner.equals(principal), "file owner equals current user");

            // Principal face: toString/implies available
            check(principal.toString() != null, "toString non-null");
            check(!principal.implies(new javax.security.auth.Subject())
                  || true, "implies(Subject) callable");

            // principals are equal for equal names
            UserPrincipal again = lookup.lookupPrincipalByName(me);
            check(principal.equals(again) && principal.hashCode() == again.hashCode(),
                  "lookup deterministic");

            // a bogus name yields UserPrincipalNotFoundException
            boolean upnfe = false;
            try { lookup.lookupPrincipalByName("jck-no-such-user-xyz"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) {
                upnfe = true;
            }
            check(upnfe, "unknown user -> UserPrincipalNotFoundException");
        } finally {
            Files.deleteIfExists(dir.resolve("u.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
