/*
 * @test
 * @summary sun.util.locale.provider.LocaleServiceProviderPool conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main LocaleServiceProviderPoolTest
 */
public class LocaleServiceProviderPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleServiceProviderPool", false, LocaleServiceProviderPoolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getAllAvailableLocales"), "method getAllAvailableLocales");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "getLocalizedObject"), "method getLocalizedObject");
        check(hasMethod(c, "getPool"), "method getPool");
        check(hasMethod(c, "streamAllAvailableLocales"), "method streamAllAvailableLocales");
        check(hasField(c, "NULL_LIST"), "field NULL_LIST");
        check(hasField(c, "availableLocales"), "field availableLocales");
        check(hasField(c, "poolOfPools"), "field poolOfPools");
        check(hasField(c, "providerClass"), "field providerClass");
        check(hasField(c, "providersCache"), "field providersCache");
        check(hasField(c, "spiClasses"), "field spiClasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
