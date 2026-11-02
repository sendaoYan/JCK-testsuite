/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.commons.GeneratorAdapter conformance: type shape via reflection (headless-safe).
 * @run main GeneratorAdapterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GeneratorAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.commons.GeneratorAdapter", false, GeneratorAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.commons.LocalVariablesSorter"), "superclass jdk.internal.org.objectweb.asm.commons.LocalVariablesSorter");
        check(hasMethod(c, "arrayLength"), "declares arrayLength");
        check(hasMethod(c, "arrayLoad"), "declares arrayLoad");
        check(hasMethod(c, "arrayStore"), "declares arrayStore");
        check(hasMethod(c, "box"), "declares box");
        check(hasMethod(c, "cast"), "declares cast");
        check(hasMethod(c, "catchException"), "declares catchException");
        check(hasMethod(c, "checkCast"), "declares checkCast");
        check(hasMethod(c, "dup"), "declares dup");
        check(hasField(c, "ADD"), "declares field ADD");
        check(hasField(c, "AND"), "declares field AND");
        check(hasField(c, "DIV"), "declares field DIV");
        check(hasField(c, "EQ"), "declares field EQ");
        check(hasField(c, "GE"), "declares field GE");
        check(hasField(c, "GT"), "declares field GT");
        check(hasField(c, "LE"), "declares field LE");
        check(hasField(c, "LT"), "declares field LT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
