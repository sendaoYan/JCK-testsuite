/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.commons.RemappingMethodAdapter conformance: type shape via reflection (headless-safe).
 * @run main RemappingMethodAdapterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RemappingMethodAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.commons.RemappingMethodAdapter", false, RemappingMethodAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.commons.LocalVariablesSorter"), "superclass jdk.internal.org.objectweb.asm.commons.LocalVariablesSorter");
        check(hasMethod(c, "visitAnnotation"), "declares visitAnnotation");
        check(hasMethod(c, "visitAnnotationDefault"), "declares visitAnnotationDefault");
        check(hasMethod(c, "visitFieldInsn"), "declares visitFieldInsn");
        check(hasMethod(c, "visitFrame"), "declares visitFrame");
        check(hasMethod(c, "visitInsnAnnotation"), "declares visitInsnAnnotation");
        check(hasMethod(c, "visitInvokeDynamicInsn"), "declares visitInvokeDynamicInsn");
        check(hasMethod(c, "visitLdcInsn"), "declares visitLdcInsn");
        check(hasMethod(c, "visitLocalVariable"), "declares visitLocalVariable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
