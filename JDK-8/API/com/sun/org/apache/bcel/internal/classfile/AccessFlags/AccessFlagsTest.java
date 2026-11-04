/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.AccessFlags conformance: reflection.
 * @run main AccessFlagsTest
 */

public class AccessFlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.AccessFlags", false, AccessFlagsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AccessFlags"), "simple name AccessFlags");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "getAccessFlags"), "method getAccessFlags");
        check(has(c, "getModifiers"), "method getModifiers");
        check(has(c, "setAccessFlags"), "method setAccessFlags");
        check(has(c, "setModifiers"), "method setModifiers");
        check(has(c, "isPublic"), "method isPublic");
        check(has(c, "isPrivate"), "method isPrivate");
        check(has(c, "isProtected"), "method isProtected");
        check(has(c, "isStatic"), "method isStatic");
        check(has(c, "isFinal"), "method isFinal");
        check(has(c, "isSynchronized"), "method isSynchronized");
        check(has(c, "isVolatile"), "method isVolatile");
        check(has(c, "isTransient"), "method isTransient");
        check(has(c, "isNative"), "method isNative");
        check(has(c, "isInterface"), "method isInterface");
        check(has(c, "isAbstract"), "method isAbstract");
        check(has(c, "isStrictfp"), "method isStrictfp");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
