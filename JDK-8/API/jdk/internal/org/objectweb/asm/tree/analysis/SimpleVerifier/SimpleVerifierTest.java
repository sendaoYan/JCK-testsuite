/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.analysis.SimpleVerifier conformance: type shape via reflection (headless-safe).
 * @run main SimpleVerifierTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SimpleVerifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.analysis.SimpleVerifier", false, SimpleVerifierTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.tree.analysis.BasicVerifier"), "superclass jdk.internal.org.objectweb.asm.tree.analysis.BasicVerifier");
        check(hasMethod(c, "merge"), "declares merge");
        check(hasMethod(c, "newValue"), "declares newValue");
        check(hasMethod(c, "setClassLoader"), "declares setClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
