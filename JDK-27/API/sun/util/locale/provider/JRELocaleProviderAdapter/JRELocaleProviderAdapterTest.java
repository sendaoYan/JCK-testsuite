/*
 * @test
 * @summary sun.util.locale.provider.JRELocaleProviderAdapter conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main JRELocaleProviderAdapterTest
 */
public class JRELocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.JRELocaleProviderAdapter", false, JRELocaleProviderAdapterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.LocaleProviderAdapter"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.util.locale.provider.ResourceBundleBasedAdapter"), "interface sun.util.locale.provider.ResourceBundleBasedAdapter");
        check(hasMethod(c, "createLanguageTagSet"), "method createLanguageTagSet");
        check(hasMethod(c, "getAdapterType"), "method getAdapterType");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "getBreakIteratorProvider"), "method getBreakIteratorProvider");
        check(hasMethod(c, "getCalendarDataProvider"), "method getCalendarDataProvider");
        check(hasMethod(c, "getCalendarNameProvider"), "method getCalendarNameProvider");
        check(hasMethod(c, "getCalendarProvider"), "method getCalendarProvider");
        check(hasMethod(c, "getCandidateLocales"), "method getCandidateLocales");
        check(hasMethod(c, "getCollatorProvider"), "method getCollatorProvider");
        check(hasMethod(c, "getCurrencyNameProvider"), "method getCurrencyNameProvider");
        check(hasMethod(c, "getDateFormatProvider"), "method getDateFormatProvider");
        check(hasMethod(c, "getDateFormatSymbolsProvider"), "method getDateFormatSymbolsProvider");
        check(hasMethod(c, "getDateTimeFormatterPatternProvider"), "method getDateTimeFormatterPatternProvider");
        check(hasMethod(c, "getDecimalFormatSymbolsProvider"), "method getDecimalFormatSymbolsProvider");
        check(hasMethod(c, "getLanguageTagSet"), "method getLanguageTagSet");
        check(hasMethod(c, "getLocaleData"), "method getLocaleData");
        check(hasMethod(c, "getLocaleNameProvider"), "method getLocaleNameProvider");
        check(hasMethod(c, "getLocaleResources"), "method getLocaleResources");
        check(hasMethod(c, "getLocaleServiceProvider"), "method getLocaleServiceProvider");
        check(hasMethod(c, "getNumberFormatProvider"), "method getNumberFormatProvider");
        check(hasMethod(c, "getTimeZoneNameProvider"), "method getTimeZoneNameProvider");
        check(hasMethod(c, "isSupportedProviderLocale"), "method isSupportedProviderLocale");
        check(hasField(c, "breakIteratorProvider"), "field breakIteratorProvider");
        check(hasField(c, "calendarDataProvider"), "field calendarDataProvider");
        check(hasField(c, "calendarNameProvider"), "field calendarNameProvider");
        check(hasField(c, "calendarProvider"), "field calendarProvider");
        check(hasField(c, "collatorProvider"), "field collatorProvider");
        check(hasField(c, "currencyNameProvider"), "field currencyNameProvider");
        check(hasField(c, "dateFormatProvider"), "field dateFormatProvider");
        check(hasField(c, "dateFormatSymbolsProvider"), "field dateFormatSymbolsProvider");
        check(hasField(c, "dateTimeFormatterPatternProvider"), "field dateTimeFormatterPatternProvider");
        check(hasField(c, "decimalFormatSymbolsProvider"), "field decimalFormatSymbolsProvider");
        check(hasField(c, "langtagSets"), "field langtagSets");
        check(hasField(c, "localeData"), "field localeData");
        check(hasField(c, "localeNameProvider"), "field localeNameProvider");
        check(hasField(c, "localeResourcesMap"), "field localeResourcesMap");
        check(hasField(c, "numberFormatProvider"), "field numberFormatProvider");
        check(hasField(c, "timeZoneNameProvider"), "field timeZoneNameProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
