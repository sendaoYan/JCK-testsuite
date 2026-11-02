/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.Opcodes conformance: type shape via reflection (headless-safe).
 * @run main OpcodesTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class OpcodesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.Opcodes", false, OpcodesTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasField(c, "AALOAD"), "declares field AALOAD");
        check(hasField(c, "AASTORE"), "declares field AASTORE");
        check(hasField(c, "ACC_ABSTRACT"), "declares field ACC_ABSTRACT");
        check(hasField(c, "ACC_ANNOTATION"), "declares field ACC_ANNOTATION");
        check(hasField(c, "ACC_BRIDGE"), "declares field ACC_BRIDGE");
        check(hasField(c, "ACC_DEPRECATED"), "declares field ACC_DEPRECATED");
        check(hasField(c, "ACC_ENUM"), "declares field ACC_ENUM");
        check(hasField(c, "ACC_FINAL"), "declares field ACC_FINAL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
