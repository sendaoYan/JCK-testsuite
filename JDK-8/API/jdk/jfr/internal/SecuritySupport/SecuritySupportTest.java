/*
 * @test
 * @summary jdk.jfr.internal.SecuritySupport conformance: type shape via reflection (headless-safe).
 * @run main SecuritySupportTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SecuritySupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.SecuritySupport", false, SecuritySupportTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "exists"), "declares exists");
        check(hasMethod(c, "getAbsolutePath"), "declares getAbsolutePath");
        check(hasMethod(c, "getFileSize"), "declares getFileSize");
        check(hasMethod(c, "getPredefinedJFCFiles"), "declares getPredefinedJFCFiles");
        check(hasMethod(c, "getResourceAsStream"), "declares getResourceAsStream");
        check(hasMethod(c, "isDirectory"), "declares isDirectory");
        check(hasMethod(c, "newFileInputStream"), "declares newFileInputStream");
        check(hasMethod(c, "newFileReader"), "declares newFileReader");
        check(hasField(c, "JFC_DIRECTORY"), "declares field JFC_DIRECTORY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
