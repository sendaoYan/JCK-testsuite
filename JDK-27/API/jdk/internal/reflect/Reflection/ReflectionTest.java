/*
 * @test
 * @summary jdk.internal.reflect.Reflection conformance: internal package reflection.
 * @modules java.base/jdk.internal.reflect:+open
 * @run main ReflectionTest
 */
public class ReflectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.reflect.Reflection", false, ReflectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "accessControlStatus"), "method accessControlStatus");
        check(hasMethod(c, "appendAccessControlModifiers"), "method appendAccessControlModifiers");
        check(hasMethod(c, "areNestMates"), "method areNestMates");
        check(hasMethod(c, "ensureMemberAccess"), "method ensureMemberAccess");
        check(hasMethod(c, "ensureNativeAccess"), "method ensureNativeAccess");
        check(hasMethod(c, "filterFields"), "method filterFields");
        check(hasMethod(c, "filterMethods"), "method filterMethods");
        check(hasMethod(c, "getCallerClass"), "method getCallerClass");
        check(hasMethod(c, "getClassAccessFlags"), "method getClassAccessFlags");
        check(hasMethod(c, "isCallerSensitive"), "method isCallerSensitive");
        check(hasMethod(c, "isTrustedFinalField"), "method isTrustedFinalField");
        check(hasMethod(c, "newIllegalAccessException"), "method newIllegalAccessException");
        check(hasMethod(c, "registerFieldsToFilter"), "method registerFieldsToFilter");
        check(hasMethod(c, "registerMethodsToFilter"), "method registerMethodsToFilter");
        check(hasMethod(c, "verifyMemberAccess"), "method verifyMemberAccess");
        check(hasMethod(c, "verifyModuleAccess"), "method verifyModuleAccess");
        check(hasMethod(c, "verifyPublicMemberAccess"), "method verifyPublicMemberAccess");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
