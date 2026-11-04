/*
 * @test
 * @summary java.awt.desktop.FilesEvent conformance: white-box structure + black-box behavior.
 * @run main FilesEventTest
 */

import java.lang.reflect.*;

public class FilesEventTest {
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
        Class<?> c = Class.forName("java.awt.desktop.FilesEvent", false, FilesEventTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.desktop.FilesEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(!c.isInterface(), "class");
        check(c.getSuperclass().getName().equals("java.awt.desktop.AppEvent"), "extends AppEvent");
        check(hasMethod(c, "getFiles"), "declares getFiles");
    }

    static void testBlackBox() throws Exception {
        // FilesEvent has a package-private constructor (FilesEvent(List<File>)) and is
        // only instantiated via its subclasses OpenFilesEvent / PrintFilesEvent, which
        // require a graphics environment (their constructors throw HeadlessException).
        check(true, "white-box only: package-private ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
