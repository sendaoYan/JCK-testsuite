/*
 * @test
 * @summary jdk.internal.access.JavaLangInvokeAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaLangInvokeAccessTest
 */
public class JavaLangInvokeAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaLangInvokeAccess", false, JavaLangInvokeAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "collectCoordinates"), "method collectCoordinates");
        check(hasMethod(c, "dropCoordinates"), "method dropCoordinates");
        check(hasMethod(c, "exceptionTypes"), "method exceptionTypes");
        check(hasMethod(c, "filterCoordinates"), "method filterCoordinates");
        check(hasMethod(c, "filterValue"), "method filterValue");
        check(hasMethod(c, "findStatic"), "method findStatic");
        check(hasMethod(c, "findVirtual"), "method findVirtual");
        check(hasMethod(c, "generateHolderClasses"), "method generateHolderClasses");
        check(hasMethod(c, "getDeclaringClass"), "method getDeclaringClass");
        check(hasMethod(c, "getMethodType"), "method getMethodType");
        check(hasMethod(c, "insertCoordinates"), "method insertCoordinates");
        check(hasMethod(c, "isCallerSensitive"), "method isCallerSensitive");
        check(hasMethod(c, "isHiddenMember"), "method isHiddenMember");
        check(hasMethod(c, "memorySegmentViewHandle"), "method memorySegmentViewHandle");
        check(hasMethod(c, "nativeMethodHandle"), "method nativeMethodHandle");
        check(hasMethod(c, "permuteCoordinates"), "method permuteCoordinates");
        check(hasMethod(c, "reflectiveInvoker"), "method reflectiveInvoker");
        check(hasMethod(c, "serializableConstructor"), "method serializableConstructor");
        check(hasMethod(c, "unreflectConstructor"), "method unreflectConstructor");
        check(hasMethod(c, "unreflectField"), "method unreflectField");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
