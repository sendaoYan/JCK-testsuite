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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
