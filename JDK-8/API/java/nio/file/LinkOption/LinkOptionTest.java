/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.LinkOption conformance: white-box structure + black-box behavior (JDK-8).
 * @run main LinkOptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class LinkOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.LinkOption");
        check(c.isEnum(), "LinkOption is an enum");
        check(c.getEnumConstants().length == 1, "exactly 1 constant");
        check(((Enum<?>) c.getEnumConstants()[0]).name().equals("NOFOLLOW_LINKS"), "constant NOFOLLOW_LINKS");
        check(java.nio.file.OpenOption.class.isAssignableFrom(c), "implements OpenOption");
        check(java.nio.file.CopyOption.class.isAssignableFrom(c), "implements CopyOption");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        check(LinkOption.values().length == 1, "single option");
        check(LinkOption.valueOf("NOFOLLOW_LINKS") == LinkOption.NOFOLLOW_LINKS, "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path target = dir.resolve("target.txt");
            Files.write(target, "t".getBytes(StandardCharsets.UTF_8));
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, target);

            check(Files.exists(link), "link exists by default");
            check(Files.exists(link, LinkOption.NOFOLLOW_LINKS), "link exists unfollowed");
            check(Files.isSymbolicLink(link), "identified as symlink");

            Path broken = dir.resolve("broken");
            Files.createSymbolicLink(broken, dir.resolve("nowhere"));
            check(!Files.exists(broken), "broken link absent when followed");
            check(Files.exists(broken, LinkOption.NOFOLLOW_LINKS), "broken link present unfollowed");

            BasicFileAttributes followed = Files.readAttributes(link,
                    java.nio.file.attribute.BasicFileAttributes.class);
            check(followed.isRegularFile(), "default attrs follow to the file");
            BasicFileAttributes unfollowed = Files.readAttributes(link,
                    java.nio.file.attribute.BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            check(unfollowed.isSymbolicLink(), "NOFOLLOW attrs expose the link itself");

            Path copyOfLink = dir.resolve("link-copy");
            Files.copy(link, copyOfLink, LinkOption.NOFOLLOW_LINKS);
            check(Files.isSymbolicLink(copyOfLink), "NOFOLLOW copy preserves the link");
        } finally {
            for (String n : new String[] {"link", "broken", "link-copy", "target.txt"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: enum matrix ----
    static void testEnumMatrix() {
        LinkOption[] vals = LinkOption.values();
        check(vals.length == 1, "single constant");
        check(vals[0] == LinkOption.NOFOLLOW_LINKS, "values[0] NOFOLLOW_LINKS");
        check("NOFOLLOW_LINKS".equals(LinkOption.NOFOLLOW_LINKS.name()), "name");
        check(LinkOption.NOFOLLOW_LINKS.ordinal() == 0, "ordinal 0");
        check("NOFOLLOW_LINKS".equals(LinkOption.NOFOLLOW_LINKS.toString()),
              "toString == name");
        check(LinkOption.NOFOLLOW_LINKS.compareTo(LinkOption.NOFOLLOW_LINKS) == 0,
              "compareTo self == 0");
        check(LinkOption.NOFOLLOW_LINKS.getDeclaringClass() == LinkOption.class,
              "declaring class");
        check(java.util.EnumSet.allOf(LinkOption.class).size() == 1, "EnumSet allOf size 1");
        check(java.util.EnumSet.noneOf(LinkOption.class).isEmpty(), "EnumSet noneOf empty");
        check(LinkOption.valueOf("NOFOLLOW_LINKS") == LinkOption.NOFOLLOW_LINKS,
              "valueOf round-trip");
        check(LinkOption.NOFOLLOW_LINKS.hashCode()
              == LinkOption.valueOf("NOFOLLOW_LINKS").hashCode(), "hashCode stable");
        boolean iae = false;
        try { LinkOption.valueOf("nofollow_links"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf wrong case -> IllegalArgumentException");
    }

    // ---- appended: follow/unfollow semantics across operations ----
    static void testFollowSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path target = dir.resolve("tgt.txt");
            Files.write(target, "0123456789".getBytes(StandardCharsets.UTF_8));
            Path link = dir.resolve("lnk");
            Files.createSymbolicLink(link, target);

            // an empty option array is equivalent to "follow links"
            check(Files.exists(link, new LinkOption[0]) == Files.exists(link),
                  "empty LinkOption[] == default (follow)");
            check(Files.notExists(link, new LinkOption[0]) == Files.notExists(link),
                  "empty LinkOption[] notExists matches default");

            // type queries follow the link by default
            check(Files.isRegularFile(link), "isRegularFile follows the link");
            check(!Files.isDirectory(link), "file link is not a directory");
            check(Files.size(link) == 10, "size follows to the target");

            // deleting the link never deletes the target
            Files.delete(link);
            check(!Files.exists(link, LinkOption.NOFOLLOW_LINKS), "link gone after delete");
            check(Files.exists(target), "target survives link deletion");

            // a followed copy duplicates the target; a NOFOLLOW copy duplicates the link
            Files.createSymbolicLink(link, target);
            Path followedCopy = dir.resolve("followed.txt");
            Files.copy(link, followedCopy);
            check(Files.isRegularFile(followedCopy) && !Files.isSymbolicLink(followedCopy),
                  "default copy duplicates the target content");
            check(Files.size(followedCopy) == 10, "followed copy size matches");

            // getAttribute honors the option set
            Object symFollowed = Files.getAttribute(link, "basic:isSymbolicLink");
            Object symUnfollowed = Files.getAttribute(link, "basic:isSymbolicLink",
                    LinkOption.NOFOLLOW_LINKS);
            check(Boolean.FALSE.equals(symFollowed), "followed getAttribute hides the link");
            check(Boolean.TRUE.equals(symUnfollowed),
                  "NOFOLLOW getAttribute exposes the link");
        } finally {
            for (String n : new String[] {"tgt.txt", "lnk", "followed.txt"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumMatrix();
        testFollowSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
