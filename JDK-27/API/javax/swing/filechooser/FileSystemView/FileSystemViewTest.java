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

        // Roots: getRoots 在 Windows 上返回“桌面”这个 shell 虚拟节点而不是盘符
        // （WindowsFileSystemView.getRoots -> {Desktop}），isFileSystemRoot(桌面)
        // 在 Windows 上为 false，但它仍应被 isRoot 认作根。
        File[] roots = fsv.getRoots();
        check(roots.length >= 1, "at least one root partition");
        boolean anyRoot = false;
        boolean anyFileSystemRoot = false;
        for (File r : roots) {
            check(r != null, "root element non-null");
            if (fsv.isFileSystemRoot(r)) anyFileSystemRoot = true;
            if (fsv.isRoot(r)) anyRoot = true;
        }
        check(anyFileSystemRoot || anyRoot, "at least one reported root is recognised (isFileSystemRoot or isRoot)");

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

            // Windows 的 display name 来自 shell（SHGDN_NORMAL）：资源管理器默认
            // 隐藏已知扩展名，alpha.txt 可能显示成 "alpha"。因此只要求返回
            // “非空且是文件名本身或去掉最后一个扩展名的形式”，X11 上仍是全名。
            String f1Display = fsv.getSystemDisplayName(f1);
            String f1NoExt = f1.getName();
            int dot = f1NoExt.lastIndexOf('.');
            if (dot > 0) f1NoExt = f1NoExt.substring(0, dot);
            check(f1Display != null
                  && (f1Display.equals(f1.getName()) || f1Display.equals(f1NoExt)),
                  "getSystemDisplayName is the file name (optionally without known extension) [" + f1Display + "]");
            check(dirF.getName().equals(fsv.getSystemDisplayName(dirF)), "getSystemDisplayName returns dir name");

            check(Boolean.TRUE.equals(fsv.isTraversable(dirF)), "isTraversable(directory) true");
            check(Boolean.FALSE.equals(fsv.isTraversable(f1)), "isTraversable(file) false");

            // Windows 上 shell 返回的父目录可能用长路径（C:\Users\yansendao\...），
            // 而 java.io.tmpdir 可能来自 8.3 短名（YANSEN~1），字符串比较会不等。
            // 契约是“父目录就是所在目录”，按文件系统标识比较。
            File parent = fsv.getParentDirectory(f1);
            check(parent != null && Files.isSameFile(parent.toPath(), dirF.toPath()),
                  "getParentDirectory(file) is the containing directory");

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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Appended contract section: shared-view identity, icon absence semantics,
    // platform flags, and JDK-27-only link / shortcut / sized-icon APIs.
    static void testContract() throws java.io.FileNotFoundException {
        FileSystemView v = FileSystemView.getFileSystemView();
        check(FileSystemView.getFileSystemView() == v, "getFileSystemView returns shared instance");

        File home = v.getHomeDirectory();
        File missing = new File(home, "definitely-missing-file.txt");
        check(v.getSystemIcon(home) != null, "getSystemIcon(directory) non-null");
        check(v.getSystemIcon(missing) == null, "getSystemIcon(nonexistent) null");
        check(!v.isComputerNode(home), "isComputerNode false for home");
        check(!v.isDrive(home), "isDrive false for home");
        check(!v.isFloppyDrive(home), "isFloppyDrive false for home");
        check(v.isFileSystem(home), "home is a plain filesystem dir");

        // ---- JDK-27-only APIs (absent from JDK-8) ----
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        check(!v.isLink(tmp), "isLink(non-link) false");
        try { v.isLink(null); throw new AssertionError("expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
        check(v.getLinkLocation(tmp) == null, "getLinkLocation(non-link) null");
        check(v.getChooserComboBoxFiles() != null, "getChooserComboBoxFiles non-null array");
        check(v.getChooserShortcutPanelFiles().length >= 1, "getChooserShortcutPanelFiles non-empty");
        check(v.getSystemIcon(tmp, 16, 16) != null, "getSystemIcon(f,16,16) non-null");
        try { v.getSystemIcon(tmp, 0, 0); throw new AssertionError("expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { passed++; }
        try { v.getSystemIcon(tmp, -1, 16); throw new AssertionError("expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { passed++; }
    }
}
