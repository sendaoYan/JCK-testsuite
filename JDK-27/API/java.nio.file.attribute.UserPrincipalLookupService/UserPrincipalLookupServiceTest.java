/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalLookupService conformance: white-box structure + black-box behavior.
 * @run main UserPrincipalLookupServiceTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class UserPrincipalLookupServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.UserPrincipalLookupService");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "UserPrincipalLookupService is abstract");
        check(c.getDeclaredConstructors().length == 0
              || java.lang.reflect.Modifier.isProtected(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "no public constructors");
        for (String n : new String[] {"lookupPrincipalByName", "lookupPrincipalByGroupName"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();
            check(lookup != null, "default fs provides the service");

            // user lookup: current user, deterministic identity
            String me = System.getProperty("user.name");
            var u1 = lookup.lookupPrincipalByName(me);
            var u2 = lookup.lookupPrincipalByName(me);
            check(u1 != null && u1.equals(u2), "user lookup deterministic");
            check(u1.getName().equals(me), "name preserved");

            // group lookup: primary group of this user is discoverable via file metadata
            Path probe = dir.resolve("probe");
            Files.writeString(probe, "p");
            GroupPrincipal group = Files.readAttributes(probe,
                    java.nio.file.attribute.PosixFileAttributes.class).group();
            GroupPrincipal found = lookup.lookupPrincipalByGroupName(group.getName());
            check(found != null && found.equals(group), "group lookup round trip");

            // error cases
            boolean upnfe = false;
            try { lookup.lookupPrincipalByName("jck-ghost-user"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) { upnfe = true; }
            check(upnfe, "unknown user -> UserPrincipalNotFoundException");
            boolean gnf = false;
            try { lookup.lookupPrincipalByGroupName("jck-ghost-group"); }
            catch (java.nio.file.attribute.UserPrincipalNotFoundException e) { gnf = true; }
            check(gnf, "unknown group -> UserPrincipalNotFoundException");

            // null names are rejected outright
            boolean npe = false;
            try { lookup.lookupPrincipalByName(null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null user name -> NullPointerException");
            boolean npe2 = false;
            try { lookup.lookupPrincipalByGroupName(null); }
            catch (NullPointerException e) { npe2 = true; }
            check(npe2, "null group name -> NullPointerException");
        } finally {
            Files.deleteIfExists(dir.resolve("probe"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
