/*
 * @test
 * @summary java.nio.file.attribute.FileOwnerAttributeView conformance: white-box structure + black-box behavior.
 * @run main FileOwnerAttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;

public class FileOwnerAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.FileOwnerAttributeView");
        check(c.isInterface(), "FileOwnerAttributeView is an interface");
        check(java.nio.file.attribute.FileAttributeView.class.isAssignableFrom(c),
              "extends FileAttributeView");
        for (String n : new String[] {"name", "getOwner", "setOwner"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("own.txt");
            Files.writeString(file, "o");

            FileOwnerAttributeView view = Files.getFileAttributeView(file,
                    FileOwnerAttributeView.class);
            check(view != null, "owner view available");
            check(view.name().equals("owner"), "view name is owner");

            UserPrincipal me = view.getOwner();
            check(me != null && me.getName() != null && !me.getName().isEmpty(), "owner readable");

            // getOwner agrees with Files.getOwner and string-form read
            check(me.equals(Files.getOwner(file)), "consistent with Files.getOwner");
            Object attr = Files.getAttribute(file, "owner:owner");
            check(me.equals(attr), "string-form owner read");

            // setOwner with the same identity is a no-op success
            view.setOwner(me);
            check(view.getOwner().equals(me), "setOwner round trip");

            // lookup service integration: principal from lookup is settable
            java.nio.file.attribute.UserPrincipalLookupService lookup =
                    dir.getFileSystem().getUserPrincipalLookupService();
            UserPrincipal current = lookup.lookupPrincipalByName(me.getName());
            view.setOwner(current);
            check(view.getOwner().equals(me), "principal from lookup accepted");

            // setOwner(null) throws NullPointerException
            boolean npe = false;
            try { view.setOwner(null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "setOwner(null) -> NullPointerException");

            // view of a missing file: getOwner fails with NoSuchFileException
            FileOwnerAttributeView missing = Files.getFileAttributeView(
                    dir.resolve("nope"), FileOwnerAttributeView.class);
            boolean nsf = false;
            try { if (missing != null) missing.getOwner(); }
            catch (java.nio.file.NoSuchFileException e) { nsf = true; }
            check(nsf, "getOwner on missing file -> NoSuchFileException");
        } finally {
            Files.deleteIfExists(dir.resolve("own.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
