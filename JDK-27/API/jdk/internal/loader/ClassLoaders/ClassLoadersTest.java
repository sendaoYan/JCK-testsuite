/*
 * @test
 * @summary jdk.internal.loader.ClassLoaders conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main ClassLoadersTest
 */
public class ClassLoadersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.ClassLoaders", false, ClassLoadersTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "appClassLoader"), "method appClassLoader");
        check(hasMethod(c, "platformClassLoader"), "method platformClassLoader");
        check(hasField(c, "APP_LOADER"), "field APP_LOADER");
        check(hasField(c, "BOOT_LOADER"), "field BOOT_LOADER");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "PLATFORM_LOADER"), "field PLATFORM_LOADER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
