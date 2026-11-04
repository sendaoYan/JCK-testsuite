/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.TypeReference conformance: type shape via reflection (headless-safe).
 * @run main TypeReferenceTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TypeReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.TypeReference", false, TypeReferenceTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getExceptionIndex"), "declares getExceptionIndex");
        check(hasMethod(c, "getFormalParameterIndex"), "declares getFormalParameterIndex");
        check(hasMethod(c, "getSort"), "declares getSort");
        check(hasMethod(c, "getSuperTypeIndex"), "declares getSuperTypeIndex");
        check(hasMethod(c, "getTryCatchBlockIndex"), "declares getTryCatchBlockIndex");
        check(hasMethod(c, "getTypeArgumentIndex"), "declares getTypeArgumentIndex");
        check(hasMethod(c, "getTypeParameterBoundIndex"), "declares getTypeParameterBoundIndex");
        check(hasMethod(c, "getTypeParameterIndex"), "declares getTypeParameterIndex");
        check(hasField(c, "CAST"), "declares field CAST");
        check(hasField(c, "CLASS_EXTENDS"), "declares field CLASS_EXTENDS");
        check(hasField(c, "CLASS_TYPE_PARAMETER"), "declares field CLASS_TYPE_PARAMETER");
        check(hasField(c, "CLASS_TYPE_PARAMETER_BOUND"), "declares field CLASS_TYPE_PARAMETER_BOUND");
        check(hasField(c, "CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT"), "declares field CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT");
        check(hasField(c, "CONSTRUCTOR_REFERENCE"), "declares field CONSTRUCTOR_REFERENCE");
        check(hasField(c, "CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT"), "declares field CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT");
        check(hasField(c, "EXCEPTION_PARAMETER"), "declares field EXCEPTION_PARAMETER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
