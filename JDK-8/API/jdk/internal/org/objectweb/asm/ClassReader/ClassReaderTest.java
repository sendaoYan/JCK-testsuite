/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.ClassReader conformance: type shape via reflection (headless-safe).
 * @run main ClassReaderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.ClassReader", false, ClassReaderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "accept"), "declares accept");
        check(hasMethod(c, "getAccess"), "declares getAccess");
        check(hasMethod(c, "getClassName"), "declares getClassName");
        check(hasMethod(c, "getInterfaces"), "declares getInterfaces");
        check(hasMethod(c, "getItem"), "declares getItem");
        check(hasMethod(c, "getItemCount"), "declares getItemCount");
        check(hasMethod(c, "getMaxStringLength"), "declares getMaxStringLength");
        check(hasMethod(c, "getSuperName"), "declares getSuperName");
        check(hasField(c, "EXPAND_FRAMES"), "declares field EXPAND_FRAMES");
        check(hasField(c, "SKIP_CODE"), "declares field SKIP_CODE");
        check(hasField(c, "SKIP_DEBUG"), "declares field SKIP_DEBUG");
        check(hasField(c, "SKIP_FRAMES"), "declares field SKIP_FRAMES");
        check(hasField(c, "b"), "declares field b");
        check(hasField(c, "header"), "declares field header");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
