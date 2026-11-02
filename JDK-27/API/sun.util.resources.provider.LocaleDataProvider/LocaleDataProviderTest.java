/*
 * @test
 * @summary sun.util.resources.provider.LocaleDataProvider conformance: internal package reflection.
 * @modules jdk.localedata/sun.util.resources.provider:+open
 * @run main LocaleDataProviderTest
 */
public class LocaleDataProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name, boolean stat) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals(name) && java.lang.reflect.Modifier.isStatic(m.getModifiers()) == stat) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.provider.LocaleDataProvider", false, LocaleDataProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "non-abstract");
        check("sun.util.resources.LocaleData$LocaleDataResourceBundleProvider".equals(c.getSuperclass().getName()), "superclass sun.util.resources.LocaleData$LocaleDataResourceBundleProvider");
        check(hasMethod(c, "getBundle", false), "method getBundle");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(java.lang.reflect.Modifier.isPublic(ctor.getModifiers()), "public no-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
