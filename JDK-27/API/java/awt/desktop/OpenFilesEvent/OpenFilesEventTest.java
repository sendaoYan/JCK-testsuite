/*
 * @test
 * @summary java.awt.desktop.OpenFilesEvent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main OpenFilesEventTest
 */

import java.lang.reflect.*;

public class OpenFilesEventTest {
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
        Class<?> c = Class.forName("java.awt.desktop.OpenFilesEvent", false, OpenFilesEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.desktop.OpenFilesEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().endsWith("AppEvent") || c.getSuperclass().getName().equals("java.awt.desktop.FilesEvent"), "extends AppEvent/FilesEvent");
    }

    static void testBlackBox() throws Exception {
        java.io.File f = new java.io.File("/tmp/a.txt");
        java.awt.desktop.OpenFilesEvent e = new java.awt.desktop.OpenFilesEvent(java.util.Arrays.asList(f), "searchTerm");

        check(e.getFiles() != null && e.getFiles().size() == 1, "getFiles size 1");
        check("/tmp/a.txt".equals(e.getFiles().get(0).getPath()) || e.getFiles().get(0).getName().equals("a.txt"), "file");
        check("searchTerm".equals(e.getSearchTerm()), "getSearchTerm");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
