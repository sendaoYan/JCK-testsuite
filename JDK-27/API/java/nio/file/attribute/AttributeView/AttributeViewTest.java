/*
 * @test
 * @summary java.nio.file.attribute.AttributeView conformance: white-box structure + black-box behavior.
 * @run main AttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AttributeView;
import java.nio.file.attribute.BasicFileAttributeView;

public class AttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AttributeView");
        check(c.isInterface(), "AttributeView is an interface");
        check(c.getMethods().length == 1 && c.getMethods()[0].getName().equals("name"),
              "single method name()");
        check(c.getDeclaredMethods()[0].getReturnType() == String.class, "name() returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("av.txt");
            Files.writeString(file, "x");

            // every concrete view names its scheme through the common interface
            AttributeView basic = Files.getFileAttributeView(file, BasicFileAttributeView.class);
            check(basic != null, "basic view present");
            check(basic.name().equals("basic"), "basic name");
            check(((BasicFileAttributeView) basic).readAttributes().size() == 1,
                  "usable through the concrete type");

            // owner/posix views (posix guarded) report their scheme names
            AttributeView owner = Files.getFileAttributeView(file,
                    java.nio.file.attribute.FileOwnerAttributeView.class);
            check(owner != null && owner.name().equals("owner"), "owner name");
            if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                    .contains("posix")) {
                AttributeView posix = Files.getFileAttributeView(file,
                        java.nio.file.attribute.PosixFileAttributeView.class);
                check(posix != null && posix.name().equals("posix"), "posix name");
            }
            if (Files.getFileStore(file).supportsFileAttributeView(
                    java.nio.file.attribute.DosFileAttributeView.class)) {
                AttributeView dos = Files.getFileAttributeView(file,
                        java.nio.file.attribute.DosFileAttributeView.class);
                check(dos != null && dos.name().equals("dos"), "dos name");
            }

            // user view on Linux
            if (Files.getFileStore(file).supportsFileAttributeView(
                    java.nio.file.attribute.UserDefinedFileAttributeView.class)) {
                AttributeView user = Files.getFileAttributeView(file,
                        java.nio.file.attribute.UserDefinedFileAttributeView.class);
                check(user != null && user.name().equals("user"), "user name");
            }
        } finally {
            Files.deleteIfExists(dir.resolve("av.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
