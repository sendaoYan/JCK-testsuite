/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.analysis.BasicValue conformance: type shape via reflection (headless-safe).
 * @run main BasicValueTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BasicValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.analysis.BasicValue", false, BasicValueTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "jdk.internal.org.objectweb.asm.tree.analysis.Value"), "implements jdk.internal.org.objectweb.asm.tree.analysis.Value");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getSize"), "declares getSize");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "isReference"), "declares isReference");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasField(c, "DOUBLE_VALUE"), "declares field DOUBLE_VALUE");
        check(hasField(c, "FLOAT_VALUE"), "declares field FLOAT_VALUE");
        check(hasField(c, "INT_VALUE"), "declares field INT_VALUE");
        check(hasField(c, "LONG_VALUE"), "declares field LONG_VALUE");
        check(hasField(c, "REFERENCE_VALUE"), "declares field REFERENCE_VALUE");
        check(hasField(c, "RETURNADDRESS_VALUE"), "declares field RETURNADDRESS_VALUE");
        check(hasField(c, "UNINITIALIZED_VALUE"), "declares field UNINITIALIZED_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
