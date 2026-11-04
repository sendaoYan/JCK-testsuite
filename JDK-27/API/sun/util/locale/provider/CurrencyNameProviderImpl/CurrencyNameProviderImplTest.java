/*
 * @test
 * @summary sun.util.locale.provider.CurrencyNameProviderImpl conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main CurrencyNameProviderImplTest
 */
public class CurrencyNameProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.CurrencyNameProviderImpl", false, CurrencyNameProviderImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.spi.CurrencyNameProvider"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.util.locale.provider.AvailableLanguageTags"), "interface sun.util.locale.provider.AvailableLanguageTags");
        check(hasMethod(c, "getAvailableLanguageTags"), "method getAvailableLanguageTags");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "getDisplayName"), "method getDisplayName");
        check(hasMethod(c, "getSymbol"), "method getSymbol");
        check(hasField(c, "langtags"), "field langtags");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
