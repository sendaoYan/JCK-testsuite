/*
 * @test
 * @summary jdk.internal.loader.BootLoader conformance: internal package reflection.
 * @modules java.base/jdk.internal.loader:+open
 * @run main BootLoaderTest
 */
public class BootLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.loader.BootLoader", false, BootLoaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "definePackage"), "method definePackage");
        check(hasMethod(c, "findResource"), "method findResource");
        check(hasMethod(c, "findResourceAsStream"), "method findResourceAsStream");
        check(hasMethod(c, "findResources"), "method findResources");
        check(hasMethod(c, "getClassLoaderValueMap"), "method getClassLoaderValueMap");
        check(hasMethod(c, "getDefinedPackage"), "method getDefinedPackage");
        check(hasMethod(c, "getNativeLibraries"), "method getNativeLibraries");
        check(hasMethod(c, "getServicesCatalog"), "method getServicesCatalog");
        check(hasMethod(c, "getUnnamedModule"), "method getUnnamedModule");
        check(hasMethod(c, "hasClassPath"), "method hasClassPath");
        check(hasMethod(c, "loadClass"), "method loadClass");
        check(hasMethod(c, "loadClassOrNull"), "method loadClassOrNull");
        check(hasMethod(c, "loadLibrary"), "method loadLibrary");
        check(hasMethod(c, "loadModule"), "method loadModule");
        check(hasMethod(c, "packages"), "method packages");
        check(hasField(c, "CLASS_LOADER_VALUE_MAP"), "field CLASS_LOADER_VALUE_MAP");
        check(hasField(c, "JAVA_HOME"), "field JAVA_HOME");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "UNNAMED_MODULE"), "field UNNAMED_MODULE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
