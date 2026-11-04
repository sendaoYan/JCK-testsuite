/*
 * @test
 * @summary jdk.internal.module.Checks conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ChecksTest
 */
public class ChecksTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.Checks", false, ChecksTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isClassName"), "method isClassName");
        check(hasMethod(c, "isJavaIdentifier"), "method isJavaIdentifier");
        check(hasMethod(c, "isPackageName"), "method isPackageName");
        check(hasMethod(c, "requireModuleName"), "method requireModuleName");
        check(hasMethod(c, "requirePackageName"), "method requirePackageName");
        check(hasMethod(c, "requireQualifiedClassName"), "method requireQualifiedClassName");
        check(hasMethod(c, "requireServiceProviderName"), "method requireServiceProviderName");
        check(hasMethod(c, "requireServiceTypeName"), "method requireServiceTypeName");
        check(hasField(c, "RESERVED"), "field RESERVED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
