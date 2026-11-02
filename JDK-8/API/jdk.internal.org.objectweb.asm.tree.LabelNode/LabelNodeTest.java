/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.LabelNode conformance: type shape via reflection (headless-safe).
 * @run main LabelNodeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LabelNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.LabelNode", false, LabelNodeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.tree.AbstractInsnNode"), "superclass jdk.internal.org.objectweb.asm.tree.AbstractInsnNode");
        check(hasMethod(c, "accept"), "declares accept");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "resetLabel"), "declares resetLabel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
