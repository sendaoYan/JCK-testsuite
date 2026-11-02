/*
 * @test
 * @summary sun.util.resources.cldr.provider.CLDRLocaleDataMetaInfo conformance: internal package reflection.
 * @modules jdk.localedata/sun.util.resources.cldr.provider:+open
 * @run main CLDRLocaleDataMetaInfoTest
 */
public class CLDRLocaleDataMetaInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static boolean hasMethod(Class<?> c, String name, boolean stat) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals(name) && java.lang.reflect.Modifier.isStatic(m.getModifiers()) == stat) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.cldr.provider.CLDRLocaleDataMetaInfo", false, CLDRLocaleDataMetaInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "non-abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
        check(hasInterface(c, "sun.util.locale.provider.LocaleDataMetaInfo"), "implements sun.util.locale.provider.LocaleDataMetaInfo");
        check(hasMethod(c, "availableLanguageTags", false), "method availableLanguageTags");
        check(hasMethod(c, "getType", false), "method getType");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(java.lang.reflect.Modifier.isPublic(ctor.getModifiers()), "public no-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
