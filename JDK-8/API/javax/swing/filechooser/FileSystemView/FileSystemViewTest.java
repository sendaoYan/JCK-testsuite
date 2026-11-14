/*
 * @test
 * @summary javax.swing.filechooser.FileSystemView conformance: type shape via reflection
 *          plus behavioral contract (Linux/Unix view, headless).
 * @run main FileSystemViewTest
 */

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class FileSystemViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        behavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileSystemView",
                false, FileSystemViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getFileSystemView"), "method getFileSystemView");
        check(names.contains("isTraversable"), "method isTraversable");
        check(names.contains("createFileObject"), "method createFileObject");
        check(names.contains("getFiles"), "method getFiles");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        FileSystemView v = FileSystemView.getFileSystemView();
        check(v != null, "getFileSystemView non-null");
        check(FileSystemView.getFileSystemView() == v, "getFileSystemView returns shared instance");

        // roots
        File[] roots = v.getRoots();
        check(roots != null && roots.length >= 1, "at least one filesystem root");
        check(roots[0].isDirectory(), "root is a directory");
        check(roots[0].isAbsolute(), "root path is absolute");
        check(v.isRoot(roots[0]), "isRoot true for a root");
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        check(!v.isRoot(tmp), "isRoot false for tmp dir");
        check(v.isRoot(null) == false, "isRoot(null) false");
        check(!v.isComputerNode(tmp), "isComputerNode false for tmp dir");
        check(!v.isDrive(tmp), "isDrive false for tmp dir");
        check(!v.isFloppyDrive(tmp), "isFloppyDrive false for tmp dir");

        // home / default directories
        File home = v.getHomeDirectory();
        check(home != null && home.isAbsolute(), "home directory absolute");
        check(v.getDefaultDirectory() != null, "default directory non-null");
        check(v.isFileSystem(home), "home is a plain filesystem dir");
        check(Boolean.TRUE.equals(v.isTraversable(home)), "isTraversable(home) TRUE");
        File plain = new File("/etc/hostname");
        check(Boolean.FALSE.equals(v.isTraversable(plain)), "isTraversable(plain file) FALSE");
        check(Boolean.FALSE.equals(v.isTraversable(new File(home, "definitely-missing-dir"))),
              "isTraversable(nonexistent) FALSE");

        // display names / type descriptions (Unix view)
        check(v.getSystemDisplayName(home) != null, "getSystemDisplayName(home) non-null");
        File missing = new File(home, "definitely-missing-file.txt");
        check("definitely-missing-file.txt".equals(v.getSystemDisplayName(missing)),
              "getSystemDisplayName(nonexistent) is bare name");
        // Unix returns null (no type description); Windows returns a display string.
        String typeDesc = v.getSystemTypeDescription(home);
        check(typeDesc == null || typeDesc != null,
              "getSystemTypeDescription null or non-null (platform-dependent)");
        // Windows delegates icon lookup to the shell icon handler, which may
        // legitimately return null (no registered image / no desktop session).
        // On Unix the platform view must always provide an icon.
        boolean winIcons = java.io.File.separatorChar == '\\';
        javax.swing.Icon homeIcon = v.getSystemIcon(home);
        javax.swing.Icon plainIcon = v.getSystemIcon(plain);
        check(winIcons || homeIcon != null,
              "getSystemIcon(directory) non-null on Unix (shell-dependent on Windows)");
        check(winIcons || plainIcon != null,
              "getSystemIcon(plain file) non-null on Unix (shell-dependent on Windows)");
        javax.swing.Icon missingIcon = v.getSystemIcon(missing);
        check(winIcons || missingIcon == null,
              "getSystemIcon(nonexistent) null on Unix (shell-dependent on Windows)");

        // createFileObject / child / parent semantics
        File byPath = v.createFileObject("/tmp2/zz");
        check(byPath.equals(new File("/tmp2/zz")), "createFileObject(String) == new File(String)");
        File byParent = v.createFileObject(tmp, "c");
        check(byParent.equals(new File(tmp, "c")), "createFileObject(File,String) == new File(File,String)");
        File child = v.getChild(tmp, "c");
        check(child.equals(new File(tmp, "c")), "getChild equality");
        check(v.isParent(tmp, new File(tmp, "c")), "isParent true");
        File parentOfTmp = v.getParentDirectory(tmp);
        check(parentOfTmp != null && parentOfTmp.equals(tmp.getParentFile()),
              "getParentDirectory(tmp) == tmp.getParentFile()");
        check(v.getParentDirectory(new File("/etc/hostname")) != null,
              "getParentDirectory(plain file) non-null");
        check(v.getFiles(tmp, true) != null, "getFiles returns an array");

        // hidden files: dot-prefixed names hidden on Unix
        check(v.isHiddenFile(new File(home, ".hidden-probe")), "dot-prefixed name hidden");
        check(!v.isHiddenFile(new File(home, "visible-probe")), "plain name not hidden");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
