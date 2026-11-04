/*
 * @test
 * @summary javax.swing.JFileChooser conformance: type shape via reflection (headless-safe)
 *          plus behavioral contract (Metal LaF, headless; IAE matrix uses fresh instances).
 * @run main JFileChooserTest
 */

import java.awt.HeadlessException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

public class JFileChooserTest {
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
        Class<?> c = Class.forName("javax.swing.JFileChooser", false, JFileChooserTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSelectedFile"), "method getSelectedFile");
        check(names.contains("setFileFilter"), "method setFileFilter");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        // constants
        check(JFileChooser.OPEN_DIALOG == 0, "OPEN_DIALOG 0");
        check(JFileChooser.SAVE_DIALOG == 1, "SAVE_DIALOG 1");
        check(JFileChooser.CUSTOM_DIALOG == 2, "CUSTOM_DIALOG 2");
        check(JFileChooser.FILES_ONLY == 0, "FILES_ONLY 0");
        check(JFileChooser.DIRECTORIES_ONLY == 1, "DIRECTORIES_ONLY 1");
        check(JFileChooser.FILES_AND_DIRECTORIES == 2, "FILES_AND_DIRECTORIES 2");
        check("ApproveSelection".equals(JFileChooser.APPROVE_SELECTION), "APPROVE_SELECTION");
        check("CancelSelection".equals(JFileChooser.CANCEL_SELECTION), "CANCEL_SELECTION");

        // defaults
        JFileChooser fc = new JFileChooser();
        check(fc.getDialogType() == JFileChooser.OPEN_DIALOG, "default dialogType OPEN");
        check(fc.getFileSelectionMode() == JFileChooser.FILES_ONLY, "default fileSelectionMode FILES_ONLY");
        check(!fc.isMultiSelectionEnabled(), "default multiSelection false");
        check(fc.getSelectedFile() == null, "default selectedFile null");
        check(fc.getSelectedFiles().length == 0, "default selectedFiles empty");
        check(fc.getDialogTitle() == null, "default dialogTitle null");
        check(fc.getApproveButtonText() == null, "default approveButtonText null");
        check(fc.getApproveButtonToolTipText() == null, "default approveButtonToolTipText null");
        check(fc.getApproveButtonMnemonic() == 0, "default approveButtonMnemonic 0");
        check(fc.isFileHidingEnabled(), "default fileHiding true");
        check(!fc.getDragEnabled(), "default dragEnabled false");
        check(fc.getControlButtonsAreShown(), "default controlButtonsAreShown true");
        check(fc.getFileFilter() != null, "default fileFilter non-null (accept-all)");
        check(fc.getFileSystemView() != null, "default fileSystemView non-null");
        File home = fc.getCurrentDirectory();
        check(home != null, "default currentDirectory non-null (home)");

        // IAE matrix: invalid mode/type rejected, state unchanged, and a fresh
        // instance is used afterwards (calling setCurrentDirectory on the same
        // instance after the failed UI install path throws IndexOutOfBoundsException)
        final JFileChooser bad = new JFileChooser();
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { bad.setFileSelectionMode(99); }});
        check(bad.getFileSelectionMode() == JFileChooser.FILES_ONLY, "mode unchanged after IAE");
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { bad.setDialogType(99); }});
        check(bad.getDialogType() == JFileChooser.OPEN_DIALOG, "type unchanged after IAE");

        // valid mode/type roundtrips (fresh instance)
        JFileChooser ok = new JFileChooser();
        ok.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        check(ok.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY, "setFileSelectionMode DIRECTORIES");
        ok.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        check(ok.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES, "setFileSelectionMode FILES_AND_DIRS");
        ok.setDialogType(JFileChooser.SAVE_DIALOG);
        check(ok.getDialogType() == JFileChooser.SAVE_DIALOG, "setDialogType SAVE");
        ok.setDialogType(JFileChooser.CUSTOM_DIALOG);
        check(ok.getDialogType() == JFileChooser.CUSTOM_DIALOG, "setDialogType CUSTOM");

        // current directory
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        JFileChooser d = new JFileChooser();
        File initial = d.getCurrentDirectory();
        d.setCurrentDirectory(tmp);
        check(d.getCurrentDirectory().equals(tmp), "setCurrentDirectory roundtrip");
        d.setCurrentDirectory(null);
        check(d.getCurrentDirectory().equals(initial), "setCurrentDirectory(null) falls back to home");
        JFileChooser d2 = new JFileChooser(tmp);
        check(d2.getCurrentDirectory().equals(tmp), "ctor(File) preserves directory");
        FileSystemView fsv = FileSystemView.getFileSystemView();
        JFileChooser d3 = new JFileChooser(tmp, fsv);
        check(d3.getCurrentDirectory().equals(tmp), "ctor(File,FSV) preserves directory");
        check(d3.getFileSystemView() == fsv, "ctor(File,FSV) preserves FSV");

        // selection
        File sel = new File(tmp, "jfcsel.tmp");
        d2.setSelectedFile(sel);
        check(d2.getSelectedFile().equals(sel), "setSelectedFile roundtrip");
        d2.setSelectedFile(null);
        check(d2.getSelectedFile() == null, "setSelectedFile(null)");
        File[] multi = {new File(tmp, "m1.tmp"), new File(tmp, "m2.tmp")};
        d2.setSelectedFiles(multi);
        check(d2.getSelectedFiles().length == 2, "setSelectedFiles roundtrip");
        d2.setSelectedFiles(new File[0]);
        check(d2.getSelectedFiles().length == 0, "setSelectedFiles(empty)");
        d2.setSelectedFiles(null);
        check(d2.getSelectedFiles().length == 0, "setSelectedFiles(null) -> empty");

        // choosable filters
        check(d2.getChoosableFileFilters().length >= 1, "choosable filters contain default accept-all");
        FileFilter custom = new FileNameExtensionFilter("tmp files", "tmp");
        d2.addChoosableFileFilter(custom);
        check(d2.getChoosableFileFilters().length >= 2, "addChoosableFileFilter");
        boolean found = false;
        for (FileFilter ff : d2.getChoosableFileFilters()) if (ff.equals(custom)) found = true;
        check(found, "new filter present in choosable list");
        d2.setFileFilter(custom);
        check(d2.getFileFilter() == custom, "setFileFilter roundtrip");
        d2.setFileFilter(null);
        check(d2.getFileFilter() == null, "setFileFilter(null) -> null");
        check(d2.removeChoosableFileFilter(custom), "removeChoosableFileFilter returns true");
        check(!d2.removeChoosableFileFilter(custom), "second remove returns false");
        d2.resetChoosableFileFilters();
        check(d2.getChoosableFileFilters().length == 1, "reset restores default choosable list");
        d2.setAcceptAllFileFilterUsed(false);
        check(!d2.isAcceptAllFileFilterUsed(), "setAcceptAllFileFilterUsed(false)");
        d2.setAcceptAllFileFilterUsed(true);
        check(d2.isAcceptAllFileFilterUsed(), "setAcceptAllFileFilterUsed(true)");

        // misc toggles and text
        d2.setDialogTitle("T");
        check("T".equals(d2.getDialogTitle()), "setDialogTitle roundtrip");
        d2.setApproveButtonText("Go");
        check("Go".equals(d2.getApproveButtonText()), "setApproveButtonText roundtrip");
        d2.setApproveButtonToolTipText("tip");
        check("tip".equals(d2.getApproveButtonToolTipText()), "setApproveButtonToolTipText roundtrip");
        d2.setApproveButtonMnemonic('G');
        check(d2.getApproveButtonMnemonic() == 'G', "setApproveButtonMnemonic roundtrip");
        d2.setFileHidingEnabled(false);
        check(!d2.isFileHidingEnabled(), "setFileHidingEnabled(false)");
        // headless contract: enabling DnD needs a toolkit -> HeadlessException
        checkThrows(HeadlessException.class, new Runnable() {
            public void run() { d2.setDragEnabled(true); }});
        check(!d2.getDragEnabled(), "dragEnabled unchanged after HeadlessException");
        d2.setMultiSelectionEnabled(true);
        check(d2.isMultiSelectionEnabled(), "setMultiSelectionEnabled(true)");
        d2.setControlButtonsAreShown(false);
        check(!d2.getControlButtonsAreShown(), "setControlButtonsAreShown(false)");
        FileView fv = new FileView() {};
        check(d2.getFileView() == null, "default fileView null");
        d2.setFileView(fv);
        check(d2.getFileView() == fv, "setFileView roundtrip");

        // headless: dialog creation throws
        checkThrows(HeadlessException.class, new Runnable() {
            public void run() { new JFileChooser().showOpenDialog(null); }});
        checkThrows(HeadlessException.class, new Runnable() {
            public void run() { new JFileChooser().showSaveDialog(null); }});
        checkThrows(HeadlessException.class, new Runnable() {
            public void run() { new JFileChooser().showDialog(null, "Go"); }});

        // UI plumbing
        check(fc.getUIClassID().equals("FileChooserUI"), "getUIClassID");
        check(fc.getUI() instanceof javax.swing.plaf.FileChooserUI, "getUI is FileChooserUI");
        fc.updateUI();
        check(fc.getUI() != null, "updateUI no-throw");
        check(fc.getAccessibleContext() != null, "accessible context non-null");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
