/*
 * @test
 * @summary javax.swing.JFileChooser conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JFileChooserTest
 */

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class JFileChooserTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // 校验“用给定目录构造 chooser 后当前目录被保留”的可移植契约：
    // 目录可遍历时必须原样保留（用 Files.isSameFile 按文件系统标识比较，
    // 容忍 Windows 上短名/长名等不同表示）；不可遍历时按 setCurrentDirectory
    // 文档规定的行为沿父目录链回退，此时只要求结果是真实目录（或 null）。
    static void checkPreservedDirectory(JFileChooser fc, File dir, java.nio.file.Path dirPath, String what)
            throws java.io.IOException {
        File cur = fc.getCurrentDirectory();
        if (fc.isTraversable(dir)) {
            check(cur != null && java.nio.file.Files.isSameFile(cur.toPath(), dirPath), what);
        } else {
            // 不可遍历时实现按文档回退（父目录链，或换成默认目录），
            // 这里只断言结果仍是可用的目录，不假定具体回退目标。
            check(cur == null || cur.isDirectory(),
                  what + " (non-traversable dir falls back to a usable directory)");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JFileChooser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFileChooser fc = new JFileChooser();
        check(fc.getCurrentDirectory() != null, "current directory non-null");
        check(fc.getDialogType() == JFileChooser.OPEN_DIALOG, "default dialog type");
        check(fc.getFileSelectionMode() == JFileChooser.FILES_ONLY, "default selection mode");
        check(!fc.isMultiSelectionEnabled(), "multi selection default false");
        check(fc.isFileHidingEnabled(), "file hiding default true");
        check(fc.isAcceptAllFileFilterUsed(), "accept all default true");
        check(fc.getAcceptAllFileFilter() != null, "accept all filter");
        check(fc.getFileSystemView() != null, "file system view");

        fc.setCurrentDirectory(new File("."));
        check(fc.getCurrentDirectory().getName().length() > 0, "setCurrentDirectory");
        fc.setSelectedFile(new File("test.txt"));
        check("test.txt".equals(fc.getSelectedFile().getName()), "setSelectedFile");
        fc.setMultiSelectionEnabled(true);
        check(fc.isMultiSelectionEnabled(), "setMultiSelectionEnabled");
        fc.setSelectedFiles(new File[] { new File("a"), new File("b") });
        check(fc.getSelectedFiles().length == 2, "setSelectedFiles");

        fc.setFileHidingEnabled(false);
        check(!fc.isFileHidingEnabled(), "setFileHidingEnabled");
        fc.setAcceptAllFileFilterUsed(false);
        check(!fc.isAcceptAllFileFilterUsed(), "setAcceptAllFileFilterUsed");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        check(fc.getDialogType() == JFileChooser.SAVE_DIALOG, "setDialogType");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        check(fc.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY, "setFileSelectionMode");

        fc.setDialogTitle("title");
        check("title".equals(fc.getDialogTitle()), "setDialogTitle");
        fc.setApproveButtonText("OK");
        check("OK".equals(fc.getApproveButtonText()), "setApproveButtonText");
        fc.setApproveButtonMnemonic('O');
        check(fc.getApproveButtonMnemonic() == 'O', "setApproveButtonMnemonic");
        fc.setControlButtonsAreShown(true);
        check(fc.getControlButtonsAreShown(), "setControlButtonsAreShown");

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) { return true; }
            public String getDescription() { return "all"; }
        };
        fc.setFileFilter(filter);
        check(fc.getFileFilter() == filter, "setFileFilter");
        fc.addChoosableFileFilter(filter);
        check(fc.getChoosableFileFilters().length >= 1, "addChoosableFileFilter");
        check(fc.removeChoosableFileFilter(filter), "removeChoosableFileFilter");
        fc.setAccessory(new JPanel());
        check(fc.getAccessory() != null, "setAccessory");
        fc.setDragEnabled(true);
        check(fc.getDragEnabled(), "setDragEnabled");

        check(JFileChooser.OPEN_DIALOG == 0 && JFileChooser.SAVE_DIALOG == 1 && JFileChooser.CUSTOM_DIALOG == 2, "dialog constants");
        check(JFileChooser.APPROVE_OPTION == 0 && JFileChooser.CANCEL_OPTION == 1 && JFileChooser.ERROR_OPTION == -1, "option constants");
        check(JFileChooser.FILES_ONLY == 0 && JFileChooser.DIRECTORIES_ONLY == 1 && JFileChooser.FILES_AND_DIRECTORIES == 2, "selection constants");

        JFrame frame = new JFrame();
        frame.add(fc);
        frame.setSize(120, 120);
        frame.setVisible(true);
        frame.setVisible(false);
        frame.dispose();
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JFileChooserTest::testWhiteBox);
        runOnEDT(JFileChooserTest::testBlackBox);
        runOnEDT(JFileChooserTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
    }
    // Appended contract section: IAE matrix with unchanged state, null
    // semantics, ctor directory preservation, choosable list lifecycle.
    static void testContract() throws Exception {
        // invalid mode/type: IllegalArgumentException, state unchanged
        JFileChooser bad = new JFileChooser();
        try { bad.setFileSelectionMode(99); throw new AssertionError("expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { passed++; }
        check(bad.getFileSelectionMode() == JFileChooser.FILES_ONLY, "mode unchanged after IAE");
        try { bad.setDialogType(99); throw new AssertionError("expected IllegalArgumentException"); }
        catch (IllegalArgumentException expected) { passed++; }
        check(bad.getDialogType() == JFileChooser.OPEN_DIALOG, "type unchanged after IAE");

        File tmp = new File(System.getProperty("java.io.tmpdir"));
        JFileChooser d = new JFileChooser();
        File initial = d.getCurrentDirectory();
        d.setCurrentDirectory(null);
        check(d.getCurrentDirectory().equals(initial), "setCurrentDirectory(null) falls back to home");
        // 用自建临时目录探测构造器是否保留给定目录（不依赖 java.io.tmpdir 的
        // 具体形式）。可遍历目录必须原样保留；Windows 上路径可能带 8.3 短名
        // （如 YANSEN~1）等不同表示，因此按“是否同一个目录”判定
        // （Files.isSameFile 走文件系统标识）。不可遍历时按文档允许回退。
        java.nio.file.Path ownedPath = java.nio.file.Files.createTempDirectory("jck-jfc");
        File owned = ownedPath.toFile();
        JFileChooser d2 = new JFileChooser(owned);
        try {
            checkPreservedDirectory(d2, owned, ownedPath, "ctor(File) preserves directory");
            javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView.getFileSystemView();
            JFileChooser d3 = new JFileChooser(owned, fsv);
            checkPreservedDirectory(d3, owned, ownedPath, "ctor(File,FSV) preserves directory");
            check(d3.getFileSystemView() == fsv, "ctor(File,FSV) preserves FSV");
            // 探测用目录稍后会被删除，先把 d2 指回仍然存在的临时目录，
            // 避免后续 updateUI 去列一个已删除的目录。
            d2.setCurrentDirectory(tmp);
        } finally {
            try { java.nio.file.Files.deleteIfExists(ownedPath); }
            catch (java.io.IOException ignore) { }
        }

        d2.setSelectedFile(new File(tmp, "s.tmp"));
        d2.setSelectedFile(null);
        check(d2.getSelectedFile() == null, "setSelectedFile(null)");
        d2.setSelectedFiles(new File[]{new File(tmp, "m1.tmp"), new File(tmp, "m2.tmp")});
        check(d2.getSelectedFiles().length == 2, "setSelectedFiles roundtrip");
        d2.setSelectedFiles(new File[0]);
        check(d2.getSelectedFiles().length == 0, "setSelectedFiles(empty)");
        d2.setSelectedFiles(null);
        check(d2.getSelectedFiles().length == 0, "setSelectedFiles(null) -> empty");

        javax.swing.filechooser.FileFilter custom =
                new javax.swing.filechooser.FileNameExtensionFilter("tmp files", "tmp");
        d2.setFileFilter(custom);
        check(d2.getFileFilter() == custom, "setFileFilter roundtrip");
        d2.setFileFilter(null);
        check(d2.getFileFilter() == null, "setFileFilter(null) -> null");
        d2.addChoosableFileFilter(custom);
        check(d2.removeChoosableFileFilter(custom), "removeChoosableFileFilter true");
        check(d2.removeChoosableFileFilter(custom) == false, "second remove false");
        d2.resetChoosableFileFilters();
        check(d2.getChoosableFileFilters().length == 1, "reset restores default choosable list");

        d2.setApproveButtonToolTipText("tip");
        check("tip".equals(d2.getApproveButtonToolTipText()), "setApproveButtonToolTipText roundtrip");
        check(d2.getFileView() == null, "default fileView null");
        javax.swing.filechooser.FileView fv = new javax.swing.filechooser.FileView() {};
        d2.setFileView(fv);
        check(d2.getFileView() == fv, "setFileView roundtrip");

        check("FileChooserUI".equals(d2.getUIClassID()), "getUIClassID");
        d2.updateUI();
        check(d2.getUI() != null, "updateUI no-throw");
        check(d2.getAccessibleContext() != null, "accessible context non-null");
    }
}