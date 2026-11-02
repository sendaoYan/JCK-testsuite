/*
 * @test
 * @summary jdk.jfr.internal.LogTag conformance: type shape via reflection (headless-safe).
 * @run main LogTagTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LogTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.LogTag", false, LogTagTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        check(hasMethod(c, "valueOf"), "declares valueOf");
        check(hasMethod(c, "values"), "declares values");
        check(hasField(c, "JFR"), "declares field JFR");
        check(hasField(c, "JFR_DCMD"), "declares field JFR_DCMD");
        check(hasField(c, "JFR_EVENT"), "declares field JFR_EVENT");
        check(hasField(c, "JFR_METADATA"), "declares field JFR_METADATA");
        check(hasField(c, "JFR_SETTING"), "declares field JFR_SETTING");
        check(hasField(c, "JFR_SYSTEM"), "declares field JFR_SYSTEM");
        check(hasField(c, "JFR_SYSTEM_BYTECODE"), "declares field JFR_SYSTEM_BYTECODE");
        check(hasField(c, "JFR_SYSTEM_EVENT"), "declares field JFR_SYSTEM_EVENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
