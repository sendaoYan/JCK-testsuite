/*
 * @test
 * @summary jdk.internal.access.JavaIOFileDescriptorAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaIOFileDescriptorAccessTest
 */
public class JavaIOFileDescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaIOFileDescriptorAccess", false, JavaIOFileDescriptorAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getAppend"), "method getAppend");
        check(hasMethod(c, "getHandle"), "method getHandle");
        check(hasMethod(c, "registerCleanup"), "method registerCleanup");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "setAppend"), "method setAppend");
        check(hasMethod(c, "setHandle"), "method setHandle");
        check(hasMethod(c, "unregisterCleanup"), "method unregisterCleanup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
