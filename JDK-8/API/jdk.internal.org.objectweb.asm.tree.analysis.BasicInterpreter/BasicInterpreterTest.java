/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.analysis.BasicInterpreter conformance: type shape via reflection (headless-safe).
 * @run main BasicInterpreterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BasicInterpreterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.analysis.BasicInterpreter", false, BasicInterpreterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.tree.analysis.Interpreter"), "superclass jdk.internal.org.objectweb.asm.tree.analysis.Interpreter");
        check(hasInterface(c, "jdk.internal.org.objectweb.asm.Opcodes"), "implements jdk.internal.org.objectweb.asm.Opcodes");
        check(hasMethod(c, "binaryOperation"), "declares binaryOperation");
        check(hasMethod(c, "copyOperation"), "declares copyOperation");
        check(hasMethod(c, "merge"), "declares merge");
        check(hasMethod(c, "naryOperation"), "declares naryOperation");
        check(hasMethod(c, "newOperation"), "declares newOperation");
        check(hasMethod(c, "newValue"), "declares newValue");
        check(hasMethod(c, "returnOperation"), "declares returnOperation");
        check(hasMethod(c, "ternaryOperation"), "declares ternaryOperation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
