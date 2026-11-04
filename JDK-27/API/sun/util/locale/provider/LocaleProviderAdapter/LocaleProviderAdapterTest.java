/*
 * @test
 * @summary sun.util.locale.provider.LocaleProviderAdapter conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main LocaleProviderAdapterTest
 */
public class LocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleProviderAdapter", false, LocaleProviderAdapterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "forJRE"), "method forJRE");
        check(hasMethod(c, "forType"), "method forType");
        check(hasMethod(c, "getAdapter"), "method getAdapter");
        check(hasMethod(c, "getAdapterPreference"), "method getAdapterPreference");
        check(hasMethod(c, "getAdapterType"), "method getAdapterType");
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
        check(hasMethod(c, "getResourceBundleBased"), "method getResourceBundleBased");
        check(hasMethod(c, "getTimeZoneNameProvider"), "method getTimeZoneNameProvider");
        check(hasMethod(c, "isSupportedProviderLocale"), "method isSupportedProviderLocale");
        check(hasMethod(c, "toLocaleArray"), "method toLocaleArray");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "adapterCache"), "field adapterCache");
        check(hasField(c, "adapterInstances"), "field adapterInstances");
        check(hasField(c, "adapterPreference"), "field adapterPreference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
