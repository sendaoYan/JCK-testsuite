/*
 * @test
 * @summary jdk.internal.access.JavaLangModuleAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaLangModuleAccessTest
 */
public class JavaLangModuleAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaLangModuleAccess", false, JavaLangModuleAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "newConfiguration"), "method newConfiguration");
        check(hasMethod(c, "newExports"), "method newExports");
        check(hasMethod(c, "newModuleBuilder"), "method newModuleBuilder");
        check(hasMethod(c, "newModuleDescriptor"), "method newModuleDescriptor");
        check(hasMethod(c, "newOpens"), "method newOpens");
        check(hasMethod(c, "newProvides"), "method newProvides");
        check(hasMethod(c, "newRequires"), "method newRequires");
        check(hasMethod(c, "packages"), "method packages");
        check(hasMethod(c, "requires"), "method requires");
        check(hasMethod(c, "resolveAndBind"), "method resolveAndBind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
