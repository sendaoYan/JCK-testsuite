/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.cgroupv1.CgroupV1SubsystemController conformance: type shape via reflection (headless-safe).
 * @run main CgroupV1SubsystemControllerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupV1SubsystemControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.cgroupv1.CgroupV1SubsystemController", false, CgroupV1SubsystemControllerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "jdk.internal.platform.CgroupSubsystemController"), "implements jdk.internal.platform.CgroupSubsystemController");
        check(hasMethod(c, "convertHierachicalLimitLine"), "declares convertHierachicalLimitLine");
        check(hasMethod(c, "convertStringToLong"), "declares convertStringToLong");
        check(hasMethod(c, "getDoubleValue"), "declares getDoubleValue");
        check(hasMethod(c, "getLongEntry"), "declares getLongEntry");
        check(hasMethod(c, "getLongValueMatchingLine"), "declares getLongValueMatchingLine");
        check(hasMethod(c, "longValOrUnlimited"), "declares longValOrUnlimited");
        check(hasMethod(c, "path"), "declares path");
        check(hasMethod(c, "setPath"), "declares setPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
