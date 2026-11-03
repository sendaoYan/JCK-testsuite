/*
 * @test
 * @summary javax.swing.filechooser.FileNameExtensionFilter conformance: white-box structure + black-box behavior.
 * @run main FileNameExtensionFilterTest
 */

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileNameExtensionFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileNameExtensionFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.filechooser.FileFilter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(Modifier.isFinal(mods) == true, "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "png");
        check("Images".equals(filter.getDescription()), "getDescription returns description");

        // getExtensions returns the supplied extensions (in order) as a defensive copy.
        String[] exts = filter.getExtensions();
        check(exts.length == 3, "three extensions");
        check("jpg".equals(exts[0]) && "jpeg".equals(exts[1]) && "png".equals(exts[2]), "extensions in order");
        exts[0] = "hacked";
        check("jpg".equals(filter.getExtensions()[0]), "getExtensions returns a copy");

        // accept: matching extension (case-insensitive), directories, null, no extension, dotfiles.
        check(filter.accept(new File("photo.jpg")), "accept .jpg");
        check(filter.accept(new File("photo.JPG")), "accept .JPG case-insensitive");
        check(filter.accept(new File("photo.jpeg")), "accept .jpeg");
        check(filter.accept(new File("photo.PNG")), "accept .PNG");
        check(!filter.accept(new File("photo.gif")), "reject other extension");
        check(!filter.accept(new File("photo")), "reject file without extension");
        check(!filter.accept(new File(".jpg")), "reject dotfile named .jpg");
        check(!filter.accept(null), "reject null");

        // accept a real directory (directory check requires existence).
        Path tmp = Files.createTempDirectory("jckfnef");
        try {
            File dir = tmp.toFile();
            check(filter.accept(dir), "accept directory");
            // A file inside the directory with a matching name still matches by name.
            check(filter.accept(new File(dir, "nested.jpg")), "accept nested .jpg path by name");
        } finally {
            Files.deleteIfExists(tmp);
        }

        // Single-extension filter.
        FileNameExtensionFilter single = new FileNameExtensionFilter("Java", "java");
        check(single.accept(new File("Hello.java")), "single extension accepts match");
        check(!single.accept(new File("Hello.class")), "single extension rejects non-match");

        // toString includes the description.
        check(filter.toString().contains("Images"), "toString includes description");

        // Illegal arguments: null / empty extension arrays, null / empty elements.
        try {
            new FileNameExtensionFilter("bad", (String[]) null);
            check(false, "null extensions should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "null extensions throw IllegalArgumentException");
        }
        try {
            new FileNameExtensionFilter("bad", new String[0]);
            check(false, "empty extensions should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "empty extensions throw IllegalArgumentException");
        }
        try {
            new FileNameExtensionFilter("bad", "jpg", null);
            check(false, "null element should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "null extension element throws IllegalArgumentException");
        }
        try {
            new FileNameExtensionFilter("bad", "jpg", "");
            check(false, "empty element should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "empty extension element throws IllegalArgumentException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
