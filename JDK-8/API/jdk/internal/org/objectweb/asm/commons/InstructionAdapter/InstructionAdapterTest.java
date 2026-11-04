/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.commons.InstructionAdapter conformance: type shape via reflection (headless-safe).
 * @run main InstructionAdapterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InstructionAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.commons.InstructionAdapter", false, InstructionAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.MethodVisitor"), "superclass jdk.internal.org.objectweb.asm.MethodVisitor");
        check(hasMethod(c, "aconst"), "declares aconst");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "aload"), "declares aload");
        check(hasMethod(c, "and"), "declares and");
        check(hasMethod(c, "anew"), "declares anew");
        check(hasMethod(c, "areturn"), "declares areturn");
        check(hasMethod(c, "arraylength"), "declares arraylength");
        check(hasMethod(c, "astore"), "declares astore");
        check(hasField(c, "OBJECT_TYPE"), "declares field OBJECT_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
