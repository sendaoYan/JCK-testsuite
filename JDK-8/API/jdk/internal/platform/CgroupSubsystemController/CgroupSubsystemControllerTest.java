/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupSubsystemController conformance: type shape via reflection (headless-safe).
 * @run main CgroupSubsystemControllerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CgroupSubsystemControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystemController", false, CgroupSubsystemControllerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "convertStringToLong"), "declares convertStringToLong");
        check(hasMethod(c, "getDoubleValue"), "declares getDoubleValue");
        check(hasMethod(c, "getLongEntry"), "declares getLongEntry");
        check(hasMethod(c, "getLongValue"), "declares getLongValue");
        check(hasMethod(c, "getLongValueMatchingLine"), "declares getLongValueMatchingLine");
        check(hasMethod(c, "getStringValue"), "declares getStringValue");
        check(hasMethod(c, "path"), "declares path");
        check(hasMethod(c, "stringRangeToIntArray"), "declares stringRangeToIntArray");
        check(hasField(c, "EMPTY_STR"), "declares field EMPTY_STR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
