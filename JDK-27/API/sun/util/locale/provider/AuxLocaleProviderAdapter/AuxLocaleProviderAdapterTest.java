/*
 * @test
 * @summary sun.util.locale.provider.AuxLocaleProviderAdapter conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main AuxLocaleProviderAdapterTest
 */
public class AuxLocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.AuxLocaleProviderAdapter", false, AuxLocaleProviderAdapterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.LocaleProviderAdapter"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "findInstalledProvider"), "method findInstalledProvider");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "getBreakIteratorProvider"), "method getBreakIteratorProvider");
        check(hasMethod(c, "getCalendarDataProvider"), "method getCalendarDataProvider");
        check(hasMethod(c, "getCalendarNameProvider"), "method getCalendarNameProvider");
        check(hasMethod(c, "getCalendarProvider"), "method getCalendarProvider");
        check(hasMethod(c, "getCollatorProvider"), "method getCollatorProvider");
        check(hasMethod(c, "getCurrencyNameProvider"), "method getCurrencyNameProvider");
        check(hasMethod(c, "getDateFormatProvider"), "method getDateFormatProvider");
        check(hasMethod(c, "getDateFormatSymbolsProvider"), "method getDateFormatSymbolsProvider");
        check(hasMethod(c, "getDateTimeFormatterPatternProvider"), "method getDateTimeFormatterPatternProvider");
        check(hasMethod(c, "getDecimalFormatSymbolsProvider"), "method getDecimalFormatSymbolsProvider");
        check(hasMethod(c, "getLocaleNameProvider"), "method getLocaleNameProvider");
        check(hasMethod(c, "getLocaleResources"), "method getLocaleResources");
        check(hasMethod(c, "getLocaleServiceProvider"), "method getLocaleServiceProvider");
        check(hasMethod(c, "getNumberFormatProvider"), "method getNumberFormatProvider");
        check(hasMethod(c, "getTimeZoneNameProvider"), "method getTimeZoneNameProvider");
        check(hasField(c, "NULL_PROVIDER"), "field NULL_PROVIDER");
        check(hasField(c, "availableLocales"), "field availableLocales");
        check(hasField(c, "providersMap"), "field providersMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
