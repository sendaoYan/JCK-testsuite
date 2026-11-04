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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
