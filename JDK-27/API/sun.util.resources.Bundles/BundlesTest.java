/*
 * @test
 * @summary sun.util.resources.Bundles conformance: internal package reflection.
 * @modules java.base/sun.util.resources:+open
 * @run main BundlesTest
 */
public class BundlesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.Bundles", false, BundlesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "of"), "method of");
        check(hasMethod(c, "toOtherBundleName"), "method toOtherBundleName");
        check(hasField(c, "INITIAL_CACHE_SIZE"), "field INITIAL_CACHE_SIZE");
        check(hasField(c, "NONEXISTENT_BUNDLE"), "field NONEXISTENT_BUNDLE");
        check(hasField(c, "bundleAccess"), "field bundleAccess");
        check(hasField(c, "cacheList"), "field cacheList");
        check(hasField(c, "referenceQueue"), "field referenceQueue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
