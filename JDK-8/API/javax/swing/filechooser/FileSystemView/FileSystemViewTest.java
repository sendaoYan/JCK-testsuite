/*
 * @test
 * @summary javax.swing.filechooser.FileSystemView conformance: type shape via reflection (headless-safe).
 * @run main FileSystemViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileSystemViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.filechooser.FileSystemView", false, FileSystemViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getFileSystemView"), "method getFileSystemView");
        check(names.contains("isRoot"), "method isRoot");
        check(names.contains("isTraversable"), "method isTraversable");
        check(names.contains("getSystemDisplayName"), "method getSystemDisplayName");
        check(names.contains("getSystemTypeDescription"), "method getSystemTypeDescription");
        check(names.contains("getSystemIcon"), "method getSystemIcon");
        check(names.contains("isParent"), "method isParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
