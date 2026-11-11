/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.CopyOption conformance: hierarchy and methods.
 * @run main CopyOptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCopyOption() {
        check(CopyOption.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : CopyOption.class.getMethods()) names.add(m.getName());
        String[] required = {  };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    // ---- marker-interface contract + implementation classification ----
    static void testMarkerAndImplementors() throws Exception {
        Class<?> c = Class.forName("java.nio.file.CopyOption");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "zero declared methods (marker)");
        int fromObject = 0;
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) fromObject++;
        check(fromObject == 0, "no methods beyond Object");

        // implementation classification
        boolean scoImplements = false;
        for (Class<?> i : StandardCopyOption.class.getInterfaces())
            if (i == c) scoImplements = true;
        check(scoImplements, "StandardCopyOption implements CopyOption");
        boolean loImplements = false;
        for (Class<?> i : LinkOption.class.getInterfaces())
            if (i == c) loImplements = true;
        check(loImplements, "LinkOption implements CopyOption");
        for (StandardCopyOption o : StandardCopyOption.values())
            check(o instanceof CopyOption, "constant " + o + " is a CopyOption");
        check(LinkOption.NOFOLLOW_LINKS instanceof CopyOption,
              "NOFOLLOW_LINKS constant is a CopyOption");
        CopyOption custom = new CopyOption() { };
        check(custom instanceof CopyOption, "anonymous implementation allowed (marker)");
        check(!custom.equals(null), "marker instance not equal to null");
    }

    // ---- black-box: the option slot drives copy behavior ----
    static void testOptionSlotBehavior() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path src = dir.resolve("src.txt");
            Files.write(src, "data".getBytes(StandardCharsets.UTF_8));

            // empty options array == no options: refuses an existing target
            Path dst = dir.resolve("dst.txt");
            Files.write(dst, "old".getBytes(StandardCharsets.UTF_8));
            boolean refused = false;
            try { Files.copy(src, dst, new CopyOption[0]); }
            catch (java.nio.file.FileAlreadyExistsException e) { refused = true; }
            check(refused, "empty CopyOption[] behaves like no options");

            // standard options flow through the same slot
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            check(new String(Files.readAllBytes(dst), StandardCharsets.UTF_8).equals("data"),
                  "REPLACE_EXISTING accepted through the slot");

            // LinkOption in the CopyOption slot copies the link itself
            Path target2 = dir.resolve("target2.txt");
            Files.write(target2, "t2".getBytes(StandardCharsets.UTF_8));
            Path link = dir.resolve("link");
            Files.createSymbolicLink(link, target2);
            Path linkCopy = dir.resolve("link-copy");
            Files.copy(link, linkCopy, LinkOption.NOFOLLOW_LINKS);
            check(Files.isSymbolicLink(linkCopy),
                  "LinkOption.NOFOLLOW_LINKS honored in the CopyOption slot");

            // unknown option must be rejected with the exact exception
            boolean uoe = false;
            try { Files.copy(src, dir.resolve("no.txt"), new CopyOption() { }); }
            catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "unknown CopyOption -> UnsupportedOperationException");

            // null option element -> NullPointerException
            boolean npe = false;
            try { Files.copy(src, dir.resolve("no.txt"), (CopyOption) null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "null CopyOption element -> NullPointerException");
        } finally {
            for (String n : new String[] {"src.txt", "dst.txt", "target2.txt", "link",
                    "link-copy", "no.txt"})
                try { Files.deleteIfExists(dir.resolve(n)); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testCopyOption();
        testMarkerAndImplementors();
        testOptionSlotBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
