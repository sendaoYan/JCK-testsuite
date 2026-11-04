/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.ClassWriter conformance: type shape via reflection (headless-safe).
 * @run main ClassWriterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.ClassWriter", false, ClassWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.ClassVisitor"), "superclass jdk.internal.org.objectweb.asm.ClassVisitor");
        check(hasMethod(c, "newClass"), "declares newClass");
        check(hasMethod(c, "newConst"), "declares newConst");
        check(hasMethod(c, "newField"), "declares newField");
        check(hasMethod(c, "newHandle"), "declares newHandle");
        check(hasMethod(c, "newInvokeDynamic"), "declares newInvokeDynamic");
        check(hasMethod(c, "newMethod"), "declares newMethod");
        check(hasMethod(c, "newMethodType"), "declares newMethodType");
        check(hasMethod(c, "newNameType"), "declares newNameType");
        check(hasField(c, "COMPUTE_FRAMES"), "declares field COMPUTE_FRAMES");
        check(hasField(c, "COMPUTE_MAXS"), "declares field COMPUTE_MAXS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
