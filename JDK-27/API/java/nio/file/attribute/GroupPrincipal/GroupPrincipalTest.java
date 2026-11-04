/*
 * @test
 * @summary java.nio.file.attribute.GroupPrincipal conformance: white-box structure + black-box behavior.
 * @run main GroupPrincipalTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class GroupPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.GroupPrincipal");
        check(c.isInterface(), "GroupPrincipal is an interface");
        check(UserPrincipal.class.isAssignableFrom(c), "extends UserPrincipal");
        check(c.getDeclaredMethods().length == 0, "adds no methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        if (!java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                .contains("posix")) {
            System.out.println("NOTE: posix unsupported; behavioral part skipped");
            return;
        }
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("g.txt");
            Files.writeString(file, "g");

            // a real group principal from the OS
            GroupPrincipal group = Files.readAttributes(file,
                    java.nio.file.attribute.PosixFileAttributes.class).group();
            check(group != null && group.getName() != null && !group.getName().isEmpty(),
                  "group readable from file");
            check(group instanceof UserPrincipal, "usable as a UserPrincipal");

            // lookup by name returns an equal principal
            UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();
            GroupPrincipal found = lookup.lookupPrincipalByGroupName(group.getName());
            check(found != null && found.equals(group), "group lookup round trip");

            // setting the same group is a successful no-op
            java.nio.file.attribute.PosixFileAttributeView view =
                    Files.getFileAttributeView(file, java.nio.file.attribute.PosixFileAttributeView.class);
            view.setGroup(group);
            check(view.readAttributes().group().equals(group), "setGroup round trip");

            // a group principal is not necessarily a user principal by lookup
            String primary = System.getProperty("user.name");
            UserPrincipal me = lookup.lookupPrincipalByName(primary);
            check(me != null, "current user lookup works alongside");
        } finally {
            Files.deleteIfExists(dir.resolve("g.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
