/*
 * @test
 * @summary jdk.internal.access.JavaLangReflectAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaLangReflectAccessTest
 */
public class JavaLangReflectAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaLangReflectAccess", false, JavaLangReflectAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAllowedToUnreflectFinalSetter"), "method checkAllowedToUnreflectFinalSetter");
        check(hasMethod(c, "copyConstructor"), "method copyConstructor");
        check(hasMethod(c, "copyField"), "method copyField");
        check(hasMethod(c, "copyMethod"), "method copyMethod");
        check(hasMethod(c, "getExecutableSharedExceptionTypes"), "method getExecutableSharedExceptionTypes");
        check(hasMethod(c, "getExecutableSharedParameterTypes"), "method getExecutableSharedParameterTypes");
        check(hasMethod(c, "getExecutableTypeAnnotationBytes"), "method getExecutableTypeAnnotationBytes");
        check(hasMethod(c, "getRoot"), "method getRoot");
        check(hasMethod(c, "isTrustedFinalField"), "method isTrustedFinalField");
        check(hasMethod(c, "newConstructorWithAccessor"), "method newConstructorWithAccessor");
        check(hasMethod(c, "newInstance"), "method newInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
