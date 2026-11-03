/*
 * @test
 * @summary javax.swing.filechooser.FileSystemView conformance: white-box structure + black-box behavior.
 * @run main FileSystemViewTest
 */

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.swing.filechooser.FileSystemView;

public class FileSystemViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileSystemView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getMethod("getFileSystemView") != null, "static getFileSystemView factory present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        check(fsv != null, "getFileSystemView returns non-null");

        // Roots: at least one file-system root, and it is reported as such.
        File[] roots = fsv.getRoots();
        check(roots.length >= 1, "at least one root partition");
        boolean anyRoot = false;
        for (File r : roots) {
            check(r != null, "root element non-null");
            if (fsv.isFileSystemRoot(r)) anyRoot = true;
        }
        check(anyRoot, "isFileSystemRoot true for at least one root");

        // isRoot: null and relative paths are not roots.
        check(!fsv.isRoot(null), "isRoot(null) false");
        check(!fsv.isRoot(new File("relative")), "isRoot(relative) false");

        // Home and default directory are non-null and absolute.
        File home = fsv.getHomeDirectory();
        check(home != null && home.isAbsolute(), "getHomeDirectory non-null absolute");
        File def = fsv.getDefaultDirectory();
        check(def != null, "getDefaultDirectory non-null");

        // createFileObject builds File objects from a path or parent+name.
        check(fsv.createFileObject("a").equals(new File("a")), "createFileObject(path) == new File(path)");
        check(fsv.createFileObject(new File("dir"), "name").equals(new File("dir", "name")),
                "createFileObject(dir,name) == new File(dir,name)");

        // Temporary directory with two files: display name, traversable, parent, files list.
        Path tmp = Files.createTempDirectory("jckfsv");
        File dirF = tmp.toFile();
        File f1 = new File(dirF, "alpha.txt");
        File f2 = new File(dirF, "beta.txt");
        try {
            Files.write(f1.toPath(), "a".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Files.write(f2.toPath(), "b".getBytes(java.nio.charset.StandardCharsets.UTF_8));

            check(f1.getName().equals(fsv.getSystemDisplayName(f1)), "getSystemDisplayName returns file name");
            check(dirF.getName().equals(fsv.getSystemDisplayName(dirF)), "getSystemDisplayName returns dir name");

            check(Boolean.TRUE.equals(fsv.isTraversable(dirF)), "isTraversable(directory) true");
            check(Boolean.FALSE.equals(fsv.isTraversable(f1)), "isTraversable(file) false");

            File parent = fsv.getParentDirectory(f1);
            check(parent != null && parent.equals(dirF), "getParentDirectory(file) == parent dir");

            File[] files = fsv.getFiles(dirF, false);
            String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) names[i] = files[i].getName();
            Arrays.sort(names);
            String[] expected = { "alpha.txt", "beta.txt" };
            Arrays.sort(expected);
            check(Arrays.equals(expected, names), "getFiles lists directory entries");

            // isHiddenFile mirrors File.isHidden for a normal visible file.
            check(fsv.isHiddenFile(f1) == f1.isHidden(), "isHiddenFile == File.isHidden");
        } finally {
            Files.deleteIfExists(f1.toPath());
            Files.deleteIfExists(f2.toPath());
            Files.deleteIfExists(tmp);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
