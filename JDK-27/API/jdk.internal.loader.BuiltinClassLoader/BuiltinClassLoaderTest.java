/*
 * @test
 * @summary jdk.internal.loader.BuiltinClassLoader conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main BuiltinClassLoaderTest
 */
public class BuiltinClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.BuiltinClassLoader", false, BuiltinClassLoaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.SecureClassLoader"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "defineOrCheckPackage"), "method defineOrCheckPackage");
        check(hasMethod(c, "findClass"), "method findClass");
        check(hasMethod(c, "findModule"), "method findModule");
        check(hasMethod(c, "findResource"), "method findResource");
        check(hasMethod(c, "findResourceAsStream"), "method findResourceAsStream");
        check(hasMethod(c, "findResources"), "method findResources");
        check(hasMethod(c, "loadClass"), "method loadClass");
        check(hasMethod(c, "loadClassOrNull"), "method loadClassOrNull");
        check(hasMethod(c, "loadModule"), "method loadModule");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "moduleToReader"), "field moduleToReader");
        check(hasField(c, "nameToModule"), "field nameToModule");
        check(hasField(c, "packageToModule"), "field packageToModule");
        check(hasField(c, "parent"), "field parent");
        check(hasField(c, "resourceCache"), "field resourceCache");
        check(hasField(c, "ucp"), "field ucp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
