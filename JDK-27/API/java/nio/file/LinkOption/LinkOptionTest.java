/*
 * @test
 * @summary java.nio.file.LinkOption conformance: white-box structure + black-box behavior.
 * @run main LinkOptionTest
 */

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LinkOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.LinkOption");
        check(c.isEnum(), "LinkOption is an enum");
        check(c.getEnumConstants().length == 1, "exactly 1 constant");
        check(((Enum<?>) c.getEnumConstants()[0]).name().equals("NOFOLLOW_LINKS"), "constant NOFOLLOW_LINKS");
        check(java.nio.file.OpenOption.class.isAssignableFrom(c), "implements OpenOption");
        check(java.nio.file.CopyOption.class.isAssignableFrom(c), "implements CopyOption");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(LinkOption.values().length == 1, "single option");
        check(LinkOption.valueOf("NOFOLLOW_LINKS") == LinkOption.NOFOLLOW_LINKS, "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path target = dir.resolve("target.txt");
            Files.writeString(target, "t");
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, target);

            // default existence checks follow the link
            check(Files.exists(link), "link exists by default");
            check(Files.exists(link, LinkOption.NOFOLLOW_LINKS), "link exists unfollowed");
            check(Files.isSymbolicLink(link), "identified as symlink");

            // dangling link: followed -> absent, unfollowed -> present
            Path broken = dir.resolve("broken");
            Files.createSymbolicLink(broken, dir.resolve("nowhere"));
            check(!Files.exists(broken), "broken link absent when followed");
            check(Files.exists(broken, LinkOption.NOFOLLOW_LINKS), "broken link present unfollowed");

            // readAttributes honors the option
            var followed = Files.readAttributes(link,
                    java.nio.file.attribute.BasicFileAttributes.class);
            check(followed.isRegularFile(), "default attrs follow to the file");
            var unfollowed = Files.readAttributes(link,
                    java.nio.file.attribute.BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            check(unfollowed.isSymbolicLink(), "NOFOLLOW attrs expose the link itself");

            // copy of a link with NOFOLLOW copies the link itself
            Path copyOfLink = dir.resolve("link-copy");
            Files.copy(link, copyOfLink, LinkOption.NOFOLLOW_LINKS);
            check(Files.isSymbolicLink(copyOfLink), "NOFOLLOW copy preserves the link");
        } finally {
            for (String n : new String[] {"link", "broken", "link-copy", "target.txt"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
