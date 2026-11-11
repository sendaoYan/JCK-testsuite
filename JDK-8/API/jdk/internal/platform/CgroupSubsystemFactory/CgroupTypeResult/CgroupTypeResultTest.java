/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupSubsystemFactory.CgroupTypeResult conformance: type shape via reflection (headless-safe).
 * @run main CgroupTypeResultTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupTypeResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystemFactory$CgroupTypeResult", false, CgroupTypeResultTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getInfos"), "declares getInfos");
        check(hasMethod(c, "isAnyCgroupV1Controllers"), "declares isAnyCgroupV1Controllers");
        check(hasMethod(c, "isAnyCgroupV2Controllers"), "declares isAnyCgroupV2Controllers");
        check(hasMethod(c, "isAnyControllersEnabled"), "declares isAnyControllersEnabled");
        check(hasMethod(c, "isCgroupV2"), "declares isCgroupV2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
