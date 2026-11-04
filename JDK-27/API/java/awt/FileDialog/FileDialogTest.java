/*
 * @test
 * @summary java.awt.FileDialog conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FileDialogTest
 */

import java.lang.reflect.*;

public class FileDialogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.FileDialog", false, FileDialogTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.FileDialog");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.awt.Dialog"), "extends Dialog");
        check(hasMethod(c, "getMode"), "declares getMode");
        check(hasMethod(c, "setFile"), "declares setFile");
        check(hasMethod(c, "setDirectory"), "declares setDirectory");
    }

    static void testBlackBox() throws Exception {
        java.awt.Frame parent = new java.awt.Frame();
        java.awt.FileDialog fd = new java.awt.FileDialog(parent, "Choose");
        check(fd.getMode() == java.awt.FileDialog.LOAD, "default mode LOAD");
        fd.setMode(java.awt.FileDialog.SAVE);
        check(fd.getMode() == java.awt.FileDialog.SAVE, "setMode SAVE");
        fd.setFile("out.txt");
        check("out.txt".equals(fd.getFile()), "setFile/getFile");
        fd.setDirectory("/tmp");
        check("/tmp".equals(fd.getDirectory()), "setDirectory/getDirectory");
        fd.setTitle("Save As");
        check("Save As".equals(fd.getTitle()), "setTitle/getTitle");
        fd.setMultipleMode(true);
        check(fd.isMultipleMode() == true, "setMultipleMode true");
        fd.setMultipleMode(false);
        check(fd.isMultipleMode() == false, "setMultipleMode false");
        java.io.FilenameFilter filter = (dir, name) -> name.endsWith(".txt");
        fd.setFilenameFilter(filter);
        check(fd.getFilenameFilter() == filter, "setFilenameFilter/getFilenameFilter");
        fd.dispose();
        parent.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
