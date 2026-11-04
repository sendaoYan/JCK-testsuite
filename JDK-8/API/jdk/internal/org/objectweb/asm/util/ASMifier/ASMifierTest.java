/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.util.ASMifier conformance: type shape via reflection (headless-safe).
 * @run main ASMifierTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ASMifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.util.ASMifier", false, ASMifierTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.util.Printer"), "superclass jdk.internal.org.objectweb.asm.util.Printer");
        check(hasMethod(c, "main"), "declares main");
        check(hasMethod(c, "visit"), "declares visit");
        check(hasMethod(c, "visitAnnotation"), "declares visitAnnotation");
        check(hasMethod(c, "visitAnnotationDefault"), "declares visitAnnotationDefault");
        check(hasMethod(c, "visitAnnotationEnd"), "declares visitAnnotationEnd");
        check(hasMethod(c, "visitArray"), "declares visitArray");
        check(hasMethod(c, "visitAttribute"), "declares visitAttribute");
        check(hasMethod(c, "visitClassAnnotation"), "declares visitClassAnnotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
