/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.MethodVisitor conformance: type shape via reflection (headless-safe).
 * @run main MethodVisitorTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.MethodVisitor", false, MethodVisitorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "visitAnnotation"), "declares visitAnnotation");
        check(hasMethod(c, "visitAnnotationDefault"), "declares visitAnnotationDefault");
        check(hasMethod(c, "visitAttribute"), "declares visitAttribute");
        check(hasMethod(c, "visitCode"), "declares visitCode");
        check(hasMethod(c, "visitEnd"), "declares visitEnd");
        check(hasMethod(c, "visitFieldInsn"), "declares visitFieldInsn");
        check(hasMethod(c, "visitFrame"), "declares visitFrame");
        check(hasMethod(c, "visitIincInsn"), "declares visitIincInsn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
