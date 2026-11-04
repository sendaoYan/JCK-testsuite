/*
 * @test
 * @summary java.awt.FileDialog conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FileDialogTest
 */

import java.awt.*;
import java.io.FilenameFilter;
import java.lang.reflect.*;

public class FileDialogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected " + ex.getSimpleName() + ": " + msg);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), msg + " -> " + t.getClass().getSimpleName());
        }
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.FileDialog", false, FileDialogTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.FileDialog");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "plain class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass().getName().equals("java.awt.Dialog"), "extends Dialog");
        check(c.getConstructors().length == 6, "6 public constructors");
        for (String n : new String[]{"getMode", "setMode", "getFile", "setFile", "getFiles",
                "getDirectory", "setDirectory", "getFilenameFilter", "setFilenameFilter",
                "isMultipleMode", "setMultipleMode"})
            check(hasMethod(c, n), "declares " + n);
        try {
            Field load = c.getField("LOAD");
            Field save = c.getField("SAVE");
            check(Modifier.isStatic(load.getModifiers()) && load.getType() == int.class,
                  "LOAD static int");
            check(Modifier.isStatic(save.getModifiers()) && save.getType() == int.class,
                  "SAVE static int");
        } catch (NoSuchFieldException e) {
            check(false, "LOAD/SAVE constants present");
        }
    }

    static void testBlackBox() throws Exception {
        // constants
        check(FileDialog.LOAD == 0, "LOAD == 0");
        check(FileDialog.SAVE == 1, "SAVE == 1");

        Frame parent = new Frame();

        // all six constructors
        FileDialog c1 = new FileDialog(parent);
        check(c1.getMode() == FileDialog.LOAD, "FileDialog(Frame) default LOAD");
        check(c1.getOwner() == parent, "owner is parent frame");
        c1.dispose();
        FileDialog c2 = new FileDialog(parent, "t2");
        check("t2".equals(c2.getTitle()) && c2.getMode() == FileDialog.LOAD, "FileDialog(Frame,String)");
        c2.dispose();
        FileDialog c3 = new FileDialog(parent, "t3", FileDialog.SAVE);
        check(c3.getMode() == FileDialog.SAVE, "FileDialog(Frame,String,SAVE)");
        c3.dispose();
        Dialog dOwner = new Dialog(parent);
        FileDialog c4 = new FileDialog(dOwner);
        check(c4.getOwner() == dOwner, "FileDialog(Dialog) owner");
        c4.dispose();
        FileDialog c5 = new FileDialog(dOwner, "d2");
        check("d2".equals(c5.getTitle()), "FileDialog(Dialog,String)");
        c5.dispose();
        FileDialog c6 = new FileDialog(dOwner, "d3", FileDialog.SAVE);
        check(c6.getMode() == FileDialog.SAVE, "FileDialog(Dialog,String,SAVE)");
        c6.dispose();

        // invalid mode in constructor
        checkThrows(IllegalArgumentException.class, () -> new FileDialog(parent, "bad", 99),
                    "FileDialog(...,99)");
        checkThrows(IllegalArgumentException.class, () -> new FileDialog(parent, "bad", -1),
                    "FileDialog(...,-1)");

        // null parents permitted; getOwner() reports null (no owner stored)
        FileDialog fnull1 = new FileDialog((Frame) null, "nf");
        check(fnull1.getOwner() == null, "FileDialog((Frame)null) ownerless");
        fnull1.dispose();
        FileDialog fnull2 = new FileDialog((Dialog) null);
        check(fnull2.getOwner() == null, "FileDialog((Dialog)null) ownerless");
        fnull2.dispose();

        FileDialog fd = new FileDialog(parent, "Choose");
        try {
            check(fd.getMode() == FileDialog.LOAD, "default mode LOAD");
            // mode roundtrip + invalid mode rejection
            fd.setMode(FileDialog.SAVE);
            check(fd.getMode() == FileDialog.SAVE, "setMode SAVE");
            fd.setMode(FileDialog.LOAD);
            check(fd.getMode() == FileDialog.LOAD, "setMode LOAD");
            checkThrows(IllegalArgumentException.class, () -> fd.setMode(99), "setMode(99)");
            checkThrows(IllegalArgumentException.class, () -> fd.setMode(-1), "setMode(-1)");
            check(fd.getMode() == FileDialog.LOAD, "mode unchanged after invalid set");

            // directory/file roundtrips; null accepted, defaults null
            check(fd.getDirectory() == null, "default directory null");
            check(fd.getFile() == null, "default file null");
            fd.setDirectory("/tmp");
            check("/tmp".equals(fd.getDirectory()), "setDirectory/getDirectory");
            fd.setDirectory(null);
            check(fd.getDirectory() == null, "setDirectory(null)");
            fd.setFile("out.txt");
            check("out.txt".equals(fd.getFile()), "setFile/getFile");
            fd.setFile(null);
            check(fd.getFile() == null, "setFile(null)");

            // getFiles: before showing, no files selected -> empty array
            check(fd.getFiles() != null, "getFiles non-null");
            check(fd.getFiles().length == 0, "getFiles empty before show");

            // title (inherited)
            fd.setTitle("Save As");
            check("Save As".equals(fd.getTitle()), "setTitle/getTitle");

            // multiple mode roundtrip
            check(!fd.isMultipleMode(), "default multiple mode false");
            fd.setMultipleMode(true);
            check(fd.isMultipleMode(), "setMultipleMode(true)");
            fd.setMultipleMode(false);
            check(!fd.isMultipleMode(), "setMultipleMode(false)");

            // filename filter identity roundtrip; default null
            check(fd.getFilenameFilter() == null, "default filter null");
            FilenameFilter filter = (dir, name) -> name.endsWith(".txt");
            fd.setFilenameFilter(filter);
            check(fd.getFilenameFilter() == filter, "setFilenameFilter/getFilenameFilter identity");
            // Note: on X11 the native peer ignores the filter (documented in the
            // class javadoc) but the getter always returns the last value set.

            // modality: FileDialog constructors are modal (APPLICATION_MODAL)
            check(fd.isModal(), "FileDialog modal by construction");
            check(fd.getModalityType() == Dialog.ModalityType.APPLICATION_MODAL,
                  "FileDialog APPLICATION_MODAL");

            // toString of the never-shown dialog carries the title
            check(fd.toString().contains("Save As"), "toString contains title");
        } finally {
            fd.dispose();
            dOwner.dispose();
            parent.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
