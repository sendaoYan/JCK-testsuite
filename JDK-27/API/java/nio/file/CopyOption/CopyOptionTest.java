/*
 * @test
 * @summary java.nio.file.CopyOption conformance: white-box structure + black-box behavior.
 * @run main CopyOptionTest
 */

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.CopyOption");
        check(c.isInterface(), "CopyOption is a marker interface");
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(StandardCopyOption.class.getInterfaces().length > 0, "implemented by enum");
        check(java.util.Arrays.asList(StandardCopyOption.class.getInterfaces())
              .contains(c) || java.util.Arrays.asList(StandardCopyOption.class.getInterfaces())
              .toString().contains("CopyOption"), "StandardCopyOption implements it");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // custom implementations are accepted wherever varargs CopyOption... appear
        CopyOption custom = new CopyOption() { public String toString() { return "jck"; } };
        Path dir = Files.createTempDirectory("jck");
        try {
            Path src = dir.resolve("c.txt");
            Files.writeString(src, "data");
            // unknown options must be rejected by copy/move
            boolean uoe = false;
            try {
                Files.copy(src, dir.resolve("out.txt"), custom);
            } catch (UnsupportedOperationException e) {
                uoe = true;
            }
            check(uoe, "unknown CopyOption -> UnsupportedOperationException");

            // the standard options flow through the same parameter slot
            Files.copy(src, dir.resolve("std.txt"), StandardCopyOption.REPLACE_EXISTING);
            check(Files.readString(dir.resolve("std.txt")).equals("data"),
                  "standard options accepted");
        } finally {
            for (String n : new String[] {"c.txt", "out.txt", "std.txt"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: marker classification + option-slot edge cases ----
    static void testMarkerAndImplementors() throws Exception {
        Class<?> c = Class.forName("java.nio.file.CopyOption");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "zero declared methods (marker)");
        int fromObject = 0;
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) fromObject++;
        check(fromObject == 0, "no methods beyond Object");

        boolean scoImplements = false;
        for (Class<?> i : StandardCopyOption.class.getInterfaces())
            if (i == c) scoImplements = true;
        check(scoImplements, "StandardCopyOption implements CopyOption");
        boolean loImplements = false;
        for (Class<?> i : java.nio.file.LinkOption.class.getInterfaces())
            if (i == c) loImplements = true;
        check(loImplements, "LinkOption implements CopyOption");
        for (StandardCopyOption o : StandardCopyOption.values())
            check(o instanceof CopyOption, "constant " + o + " is a CopyOption");
        check(java.nio.file.LinkOption.NOFOLLOW_LINKS instanceof CopyOption,
              "NOFOLLOW_LINKS constant is a CopyOption");
        CopyOption anon = new CopyOption() { };
        check(anon instanceof CopyOption, "anonymous implementation allowed (marker)");
        check(!anon.equals(null), "marker instance not equal to null");
    }

    // ---- appended: option-slot edge cases beyond the base tests ----
    static void testOptionSlotEdges() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path src = dir.resolve("cs.txt");
            Files.writeString(src, "data");
            Path dst = dir.resolve("cd.txt");
            Files.writeString(dst, "old");
            boolean refused = false;
            try { Files.copy(src, dst, new CopyOption[0]); }
            catch (java.nio.file.FileAlreadyExistsException e) { refused = true; }
            check(refused, "empty CopyOption[] behaves like no options");

            // LinkOption in the CopyOption slot copies the link itself
            Path target2 = dir.resolve("target2.txt");
            Files.writeString(target2, "t2");
            Path link = dir.resolve("clink");
            Files.createSymbolicLink(link, target2);
            Path linkCopy = dir.resolve("clink-copy");
            Files.copy(link, linkCopy, java.nio.file.LinkOption.NOFOLLOW_LINKS);
            check(Files.isSymbolicLink(linkCopy),
                  "LinkOption.NOFOLLOW_LINKS honored in the CopyOption slot");

            // null option element -> NullPointerException (measured both JDKs)
            boolean npe = false;
            try { Files.copy(src, dir.resolve("cn.txt"), (CopyOption) null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null CopyOption element -> NullPointerException");
        } finally {
            for (String n : new String[] {"cs.txt", "cd.txt", "target2.txt", "clink",
                    "clink-copy", "cn.txt"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerAndImplementors();
        testOptionSlotEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
