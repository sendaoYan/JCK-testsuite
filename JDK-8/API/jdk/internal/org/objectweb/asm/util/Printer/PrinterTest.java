/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.util.Printer conformance: type shape via reflection (headless-safe).
 * @run main PrinterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PrinterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.util.Printer", false, PrinterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "appendString"), "declares appendString");
        check(hasMethod(c, "getText"), "declares getText");
        check(hasMethod(c, "print"), "declares print");
        check(hasMethod(c, "visit"), "declares visit");
        check(hasMethod(c, "visitAnnotation"), "declares visitAnnotation");
        check(hasMethod(c, "visitAnnotationDefault"), "declares visitAnnotationDefault");
        check(hasMethod(c, "visitAnnotationEnd"), "declares visitAnnotationEnd");
        check(hasMethod(c, "visitArray"), "declares visitArray");
        check(hasField(c, "HANDLE_TAG"), "declares field HANDLE_TAG");
        check(hasField(c, "OPCODES"), "declares field OPCODES");
        check(hasField(c, "TYPES"), "declares field TYPES");
        check(hasField(c, "text"), "declares field text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
