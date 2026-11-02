/*
 * @test
 * @summary jdk.internal.platform.CgroupInfo conformance: type shape via reflection (headless-safe).
 * @run main CgroupInfoTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupInfo", false, CgroupInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getCgroupPath"), "declares getCgroupPath");
        check(hasMethod(c, "getHierarchyId"), "declares getHierarchyId");
        check(hasMethod(c, "getMountPoint"), "declares getMountPoint");
        check(hasMethod(c, "getMountRoot"), "declares getMountRoot");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isEnabled"), "declares isEnabled");
        check(hasMethod(c, "setCgroupPath"), "declares setCgroupPath");
        check(hasMethod(c, "setMountPoint"), "declares setMountPoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
