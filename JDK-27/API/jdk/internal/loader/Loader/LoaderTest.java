/*
 * @test
 * @summary jdk.internal.loader.Loader conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main LoaderTest
 */
public class LoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.Loader", false, LoaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.SecureClassLoader"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "findClass"), "method findClass");
        check(hasMethod(c, "findResource"), "method findResource");
        check(hasMethod(c, "findResources"), "method findResources");
        check(hasMethod(c, "getResource"), "method getResource");
        check(hasMethod(c, "getResources"), "method getResources");
        check(hasMethod(c, "initRemotePackageMap"), "method initRemotePackageMap");
        check(hasMethod(c, "loadClass"), "method loadClass");
        check(hasMethod(c, "pool"), "method pool");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "localPackageToModule"), "field localPackageToModule");
        check(hasField(c, "moduleToReader"), "field moduleToReader");
        check(hasField(c, "nameToModule"), "field nameToModule");
        check(hasField(c, "parent"), "field parent");
        check(hasField(c, "pool"), "field pool");
        check(hasField(c, "remotePackageToLoader"), "field remotePackageToLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
