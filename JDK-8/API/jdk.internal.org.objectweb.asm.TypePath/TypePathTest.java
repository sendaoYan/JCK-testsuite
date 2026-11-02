/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.TypePath conformance: type shape via reflection (headless-safe).
 * @run main TypePathTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TypePathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.TypePath", false, TypePathTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "fromString"), "declares fromString");
        check(hasMethod(c, "getLength"), "declares getLength");
        check(hasMethod(c, "getStep"), "declares getStep");
        check(hasMethod(c, "getStepArgument"), "declares getStepArgument");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasField(c, "ARRAY_ELEMENT"), "declares field ARRAY_ELEMENT");
        check(hasField(c, "INNER_TYPE"), "declares field INNER_TYPE");
        check(hasField(c, "TYPE_ARGUMENT"), "declares field TYPE_ARGUMENT");
        check(hasField(c, "WILDCARD_BOUND"), "declares field WILDCARD_BOUND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
