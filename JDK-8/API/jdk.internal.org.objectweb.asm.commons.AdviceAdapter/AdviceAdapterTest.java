/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.commons.AdviceAdapter conformance: type shape via reflection (headless-safe).
 * @run main AdviceAdapterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AdviceAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.commons.AdviceAdapter", false, AdviceAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.commons.GeneratorAdapter"), "superclass jdk.internal.org.objectweb.asm.commons.GeneratorAdapter");
        check(hasInterface(c, "jdk.internal.org.objectweb.asm.Opcodes"), "implements jdk.internal.org.objectweb.asm.Opcodes");
        check(hasMethod(c, "visitCode"), "declares visitCode");
        check(hasMethod(c, "visitFieldInsn"), "declares visitFieldInsn");
        check(hasMethod(c, "visitInsn"), "declares visitInsn");
        check(hasMethod(c, "visitIntInsn"), "declares visitIntInsn");
        check(hasMethod(c, "visitInvokeDynamicInsn"), "declares visitInvokeDynamicInsn");
        check(hasMethod(c, "visitJumpInsn"), "declares visitJumpInsn");
        check(hasMethod(c, "visitLabel"), "declares visitLabel");
        check(hasMethod(c, "visitLdcInsn"), "declares visitLdcInsn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
